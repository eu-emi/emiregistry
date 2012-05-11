package eu.emi.emir.client.security;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.net.SocketFactory;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory;
import org.apache.log4j.Logger;

import eu.emi.emir.client.util.Log;




/**
 * Code from Commons HTTPClient "contrib" section <br/>
 * 
 * 
 * <p>
 * AuthSSLProtocolSocketFactory can be used to validate the identity of the
 * HTTPS server against a list of trusted certificates and to authenticate to
 * the HTTPS server using a private key.
 * </p>
 * 
 * <p>
 * AuthSSLProtocolSocketFactory will enable server authentication when supplied
 * with a {@link KeyStore truststore} file containing one or several trusted
 * certificates. The client secure socket will reject the connection during the
 * SSL session handshake if the target HTTPS server attempts to authenticate
 * itself with a non-trusted certificate.
 * </p>
 * 
 * @author <a href="mailto:oleg -at- ural.ru">Oleg Kalnichevski</a>
 * @author K. Benedyczak
 * 
 * <p>
 * DISCLAIMER: HttpClient developers DO NOT actively support this component. The
 * component is provided as a reference material, which may be inappropriate for
 * use without additional customization.
 * </p>
 */

public class AuthSSLProtocolSocketFactory implements SecureProtocolSocketFactory
{
	private static final Logger LOG = Log.getLogger(Log.EMIR_SECURITY,
			AuthSSLProtocolSocketFactory.class);

	private SSLContext sslcontext = null;

	private IAuthenticationConfiguration sec;

	public AuthSSLProtocolSocketFactory(IAuthenticationConfiguration sec)
	{
		this.sec = sec;
	}

	static KeyStore createKeyStore(String name, String passwd,
			String type, String alias, boolean isTruststore) throws KeyStoreException,
			NoSuchAlgorithmException, CertificateException,
			IOException
	{
		if (name == null)
			throw new IllegalArgumentException(
					"Keystore/Truststore name may not be null");

		String storeType = type != null ? type : "jks";
		KeyStore keystore = KeyStore.getInstance(storeType);
		InputStream is = null;
		try
		{
			is = new FileInputStream(name);
			keystore.load(is, passwd != null ? passwd.toCharArray()
					: null);
		} finally
		{
			if (is != null)
				is.close();
		}
		
		if (isTruststore)
			return keystore;
		
		//if there is only one alias let's use it even if there is no user-defined one
		//if there are more than one protest
		if (alias == null || alias.trim().length() == 0)
		{
			boolean keyFound = false;
			Enumeration<String> en = keystore.aliases();
			while (en.hasMoreElements())
			{
				String a = en.nextElement();
				if (keystore.isKeyEntry(a))
				{
					if (keyFound)
						throw new IllegalArgumentException(
							"Keystore key alias must be specified when " +
							"keystore contains multiple keys.");
					keyFound = true;
					alias = a;
				}
			}
		}
		
		
		// we must have alias defined here. However it may be wrong.
		if (!keystore.containsAlias(alias))
			throw new IllegalArgumentException("Alias " + alias + " does not exist" +
				" in the keystore " + name);
		if (!keystore.isKeyEntry(alias))
			throw new IllegalArgumentException("Alias " + alias + " does not " +
				" refer to a key entry in the keystore " + name);
		
		LOG.debug("Using alias <" + alias + ">");
		Enumeration<String> en = keystore.aliases();
		List<String> delete = new ArrayList<String>();
		while (en.hasMoreElements())
		{
			String a = en.nextElement();
			if (!alias.equalsIgnoreCase(a) && keystore.isKeyEntry(a))
			{
				LOG.debug("Deleting non-needed key " + a);
				delete.add(a);
			}
		}
		for (String del : delete)
			keystore.deleteEntry(del);

		return keystore;
	}

	private static KeyManager[] createKeyManagers(final KeyStore keystore,
			final String password) throws KeyStoreException,
			NoSuchAlgorithmException, UnrecoverableKeyException
	{
		if (keystore == null)
		{
			throw new IllegalArgumentException(
					"Keystore may not be null");
		}
		KeyManagerFactory kmfactory = KeyManagerFactory
				.getInstance(KeyManagerFactory
						.getDefaultAlgorithm());
		kmfactory.init(keystore, password != null ? password
				.toCharArray() : null);
		return kmfactory.getKeyManagers();
	}

	private static TrustManager[] createTrustManagers(
			final KeyStore keystore) throws KeyStoreException,
			NoSuchAlgorithmException
	{
		if (keystore == null)
		{
			return new TrustManager[] {new DummyTrustManager()};
		}
		TrustManagerFactory tmfactory = TrustManagerFactory
				.getInstance(TrustManagerFactory
						.getDefaultAlgorithm());
		tmfactory.init(keystore);
		TrustManager[] trustmanagers = tmfactory.getTrustManagers();
		return trustmanagers;
	}

	private synchronized SSLContext createSSLContext()
	{
		if (sec.getSSLContext() != null)
			return sec.getSSLContext();
		try
		{
			KeyManager[] keymanagers = null;
			TrustManager[] trustmanagers = null;
			if (sec.doSSLAuthn())
			{
				KeyStore keystore = createKeyStore(sec.getKeystore(), 
						sec.getKeystorePassword(), 
						sec.getKeystoreType(), 
						sec.getKeystoreAlias(), false);
				if (LOG.isDebugEnabled())
					debugKS(keystore);
				keymanagers = createKeyManagers(keystore, 
						sec.getKeystoreKeyPassword());
			}
			if (sec.getTruststore() != null)
			{
				KeyStore truststore = createKeyStore(sec
						.getTruststore(), sec
						.getTruststorePassword(), sec
						.getTruststoreType(), null, true);
				if (LOG.isDebugEnabled())
					debugTS(truststore);
				trustmanagers = createTrustManagers(truststore);
			} else
				trustmanagers = createTrustManagers(null);
			SSLContext sslcontext = SSLContext.getInstance("SSL");
			sslcontext.init(keymanagers, trustmanagers, null);
			return sslcontext;
		} catch (Exception e)
		{
			LOG.fatal(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	private void debugTS(KeyStore ks) throws KeyStoreException
	{
		Enumeration<String> aliases = ks.aliases();
		while (aliases.hasMoreElements())
		{
			String alias = aliases.nextElement();
			LOG.debug("Trusted certificate '" + alias + "':");
			Certificate trustedcert = ks.getCertificate(alias);
			if (trustedcert != null	&& trustedcert instanceof X509Certificate)
			{
				X509Certificate cert = (X509Certificate) trustedcert;
				LOG.debug("  Subject DN: " + cert.getSubjectDN());
				LOG.debug("  Signature Algorithm: " + cert.getSigAlgName());
				LOG.debug("  Valid from: " + cert.getNotBefore());
				LOG.debug("  Valid until: " + cert.getNotAfter());
				LOG.debug("  Issuer: " + cert.getIssuerDN());
			}
		}
		
	}
	
	private void debugKS(KeyStore keystore) throws KeyStoreException
	{
		Enumeration<String> aliases = keystore.aliases();
		while (aliases.hasMoreElements())
		{
			String alias = aliases.nextElement();
			Certificate[] certs = keystore.getCertificateChain(alias);
			if (certs != null)
			{
				LOG.debug("Certificate chain '"	+ alias	+ "':");
				for (int c = 0; c < certs.length; c++)
				{
					if (certs[c] instanceof X509Certificate)
					{
						X509Certificate cert = (X509Certificate) certs[c];
						LOG.debug(" Certificate " + (c + 1) + ":");
						LOG.debug("  Subject DN: " + cert.getSubjectDN());
						LOG.debug("  Signature Algorithm: " + cert.getSigAlgName());
						LOG.debug("  Valid from: " + cert.getNotBefore());
						LOG.debug("  Valid until: " + cert.getNotAfter());
						LOG.debug("  Issuer: " + cert.getIssuerDN());
					}
				}
			}
		}
	}	
	
	private SSLContext getSSLContext()
	{
		if (this.sslcontext == null)
		{
			this.sslcontext = createSSLContext();
		}
		return this.sslcontext;
	}

	/**
	 * Attempts to get a new socket connection to the given host within the
	 * given time limit.
	 * <p>
	 * To circumvent the limitations of older JREs that do not support
	 * connect timeout a controller thread is executed. The controller
	 * thread attempts to create a new socket within the given limit of
	 * time. If socket constructor does not return until the timeout
	 * expires, the controller terminates and throws an
	 * {@link ConnectTimeoutException}
	 * </p>
	 * 
	 * @param host
	 *                the host name/IP
	 * @param port
	 *                the port on the host
	 * @param localAddress
	 *                the local host name/IP to bind the socket to
	 * @param localPort
	 *                the port on the local machine
	 * @param params
	 *                {@link HttpConnectionParams Http connection parameters}
	 * 
	 * @return Socket a new socket
	 * 
	 * @throws IOException
	 *                 if an I/O error occurs while creating the socket
	 * @throws UnknownHostException
	 *                 if the IP address of the host cannot be determined
	 */
	public Socket createSocket(final String host, final int port,
			final InetAddress localAddress, final int localPort,
			final HttpConnectionParams params) throws IOException,
			UnknownHostException, ConnectTimeoutException
	{
		if (params == null)
		{
			throw new IllegalArgumentException(
					"Parameters may not be null");
		}
		int timeout = params.getConnectionTimeout();
		SocketFactory socketfactory = getSSLContext()
				.getSocketFactory();
		if (timeout == 0)
		{
			return socketfactory.createSocket(host, port,
					localAddress, localPort);
		} else
		{
			Socket socket = socketfactory.createSocket();
			SocketAddress localaddr = new InetSocketAddress(
					localAddress, localPort);
			SocketAddress remoteaddr = new InetSocketAddress(host,
					port);
			socket.bind(localaddr);
			socket.connect(remoteaddr, timeout);
			return socket;
		}
	}

	/**
	 * @see SecureProtocolSocketFactory#createSocket(java.lang.String,int,java.net.InetAddress,int)
	 */
	public Socket createSocket(String host, int port,
			InetAddress clientHost, int clientPort)
			throws IOException, UnknownHostException
	{
		return getSSLContext().getSocketFactory().createSocket(host,
				port, clientHost, clientPort);
	}

	/**
	 * @see SecureProtocolSocketFactory#createSocket(java.lang.String,int)
	 */
	public Socket createSocket(String host, int port) throws IOException,
			UnknownHostException
	{
		return getSSLContext().getSocketFactory().createSocket(host,
				port);
	}

	/**
	 * @see SecureProtocolSocketFactory#createSocket(java.net.Socket,java.lang.String,int,boolean)
	 */
	public Socket createSocket(Socket socket, String host, int port,
			boolean autoClose) throws IOException,
			UnknownHostException
	{
		return getSSLContext().getSocketFactory().createSocket(socket,
				host, port, autoClose);
	}
	
	public static class DummyTrustManager implements X509TrustManager
	{

		public void checkClientTrusted(X509Certificate[] chain, String authType) 
			throws CertificateException
		{
		}

		public void checkServerTrusted(X509Certificate[] chain, String authType) 
			throws CertificateException
		{
		}

		public X509Certificate[] getAcceptedIssuers()
		{
			return null;
		}
	}
}
