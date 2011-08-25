/**
 * 
 */
package eu.emi.dsr.client;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.net.ssl.SSLContext;

import org.apache.log4j.Logger;

import eu.emi.dsr.security.client.IClientProperties;
import eu.emi.dsr.security.util.KeystoreChecker;
import eu.emi.dsr.util.Log;

/**
 * @author a.memon
 * 
 */
public class ClientSecurityProperties implements IClientProperties {
	private static final Logger logger=Log.getLogger(Log.SECURITY,ClientSecurityProperties.class);
	/**
	 * property defining whether SSL is enabled
	 */
	public static final String REGISTRY_SSL_ENABLED = "registry.ssl.enabled";

	/**
	 * property defining whether SSL is client authenticated
	 */
	public static final String REGISTRY_SSL_CLIENTAUTH = "registry.ssl.clientauth";

	/**
	 * property defining the SSL keystore
	 */
	public static final String REGISTRY_SSL_KEYSTORE = "registry.ssl.keystore";

	/**
	 * property defining the SSL keystore password
	 */
	public static final String REGISTRY_SSL_KEYPASS = "registry.ssl.keypass";

	/**
	 * property defining the SSL keystore type (e.g. JKS)
	 */
	public static final String REGISTRY_SSL_KEYTYPE = "registry.ssl.keytype";

	/**
	 * property defining the alias of the key to use
	 */
	public static final String REGISTRY_SSL_KEYALIAS = "registry.ssl.keyalias";

	/**
	 * property defining the SSL trust store
	 */
	public static final String REGISTRY_SSL_TRUSTSTORE = "registry.ssl.truststore";

	/**
	 * property defining the SSL trust tore password
	 */
	public static final String REGISTRY_SSL_TRUSTPASS = "registry.ssl.truststorepass";

	/**
	 * property defining the SSL trust store type (e.g. JKS)
	 */
	public static final String REGISTRY_SSL_TRUSTTYPE = "registry.ssl.truststoretype";

	// /**
	// * set to "true" to enable proxy certificate support, i.e. proper handling
	// of the proxy DNs
	// * (does NOT mean that proxies can be used for the SSL connection to the
	// UNICORE/X container)
	// */
	// public static final String
	// UAS_AUTHORISER_PROXY_SUPPORT="uas.security.authoriser.proxysupport";

	/**
	 * do we check access on the WSRF level?
	 */
	public static final String REGISTRY_CHECKACCESS = "registry.security.accesscontrol";

	/**
	 * access control PDP class name (implementing
	 * <code>de.fzj.uas.security.XacmlPDP</code>)
	 */
	public static final String REGISTRY_CHECKACCESS_PDP = "registry.security.accesscontrol.pdp";

	/**
	 * configuration file for the PDP
	 */
	public static final String REGISTRY_CHECKACCESS_PDPCONFIG = "registry.security.accesscontrol.pdp.config";
	
	private Properties properties;

	private X509Certificate[] certChain;

	private volatile PrivateKey privateKey;

	private boolean signMessage = true;

	private Map<String, Object> extraSecurityTokens;

	private volatile X509Certificate gatewayCertificate;

	private ClassLoader classLoader;

	private Properties parent;

	public ClientSecurityProperties(Properties parent, Properties local)
			throws UnrecoverableKeyException, KeyStoreException,
			NoSuchAlgorithmException, CertificateException, IOException {
		properties = local;
		this.parent = parent;
		if (isSslEnabled())
			readKeystore();
	}

	public ClientSecurityProperties(Properties parent)
			throws UnrecoverableKeyException, KeyStoreException,
			NoSuchAlgorithmException, CertificateException, IOException {
		this(parent, new Properties());
	}

	public ClientSecurityProperties(Properties parent, InputStream is)
			throws UnrecoverableKeyException, KeyStoreException,
			NoSuchAlgorithmException, CertificateException, IOException {
		try {
			properties = new Properties();
			properties.load(is);
		} catch (Exception e) {
			logger.fatal("Could not read properties.", e);
		}
		this.parent = parent;
		if (isSslEnabled())
			readKeystore();
	}

	public ClientSecurityProperties(Properties parent, String propertiesFileName)
			throws IOException, UnrecoverableKeyException, KeyStoreException,
			NoSuchAlgorithmException, CertificateException {
		this(parent, new FileInputStream(propertiesFileName));
	}

	@Override
	public ClientSecurityProperties clone() {
		if (logger.isTraceEnabled())
			logger.trace("Copying security props: " + toString());
		ClientSecurityProperties clone;
		try {
			clone = (ClientSecurityProperties) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(
					"Clone not supported n Cloneable class?!", e);
		}
		clone.properties = (Properties) this.properties.clone();
		// Note: parentProperties reference is copied intentionally, it is not
		// cloned.
		// as we need a live instance.
		// clone.etdSettings = etdSettings.clone();
		if (extraSecurityTokens != null) {
			clone.extraSecurityTokens = new HashMap<String, Object>();
			clone.extraSecurityTokens.putAll(extraSecurityTokens);
		}
		return clone;
	}

	// @Override
	// public String getInHandlerClassNames() {
	// return doGetProperty(ISecurityProperties.UAS_INHANDLER_NAME);
	// }
	//
	// @Override
	// public String getOutHandlerClassNames() {
	// return doGetProperty(ISecurityProperties.UAS_OUTHANDLER_NAME);
	// }

	/**
	 * return named property. Lookup order: - own properties -
	 * Kernel.getKernel().getProperty()
	 */
	protected String doGetProperty(String key) {
		String p = (String) properties.get(key);
		if (p == null) {
			p = parent.getProperty(key);
		}
		return p;
	}

	

	/**
	 * returns true if SSL mode is enabled.<br/>
	 * SSL can be enabled by setting a property "unicore.wsrflite.ssl" to "true"
	 * in the wsrflite.xml file or by defining a system property
	 * 
	 * @return
	 */
	@Override
	public boolean isSslEnabled() {
		return "true".equalsIgnoreCase(doGetProperty(REGISTRY_SSL_ENABLED));
	}

	@Override
	public boolean doSSLAuthn() {
		return isSslEnabled();
	}

	

	@Override
	public synchronized X509Certificate[] getCertificateChain() {
		return certChain;
	}

	@Override
	public X509Certificate getPublicKey() {
		return certChain != null ? getCertificateChain()[0] : null;
	}

	/**
	 * read the keys from the keystore
	 * 
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws UnrecoverableKeyException
	 * @throws CertificateException
	 */
	private void readKeystore() throws IOException, KeyStoreException,
			UnrecoverableKeyException, NoSuchAlgorithmException,
			CertificateException {
		String keystoreName = getKeystore();
		if (keystoreName == null)
			throw new IOException(
					"Keystore path is not defined in the security properties");
		String keystoreType = getKeystoreType();
		if (keystoreType == null)
			keystoreType = "jks";
		String keystorePassword = getKeystorePassword();
		String keyPassword = getKeystoreKeyPassword();
		String keystoreAlias = getKeystoreAlias();

		KeyStore keyStore;
		logger.debug("Reading from keystore: " + keystoreName);
		keyStore = KeyStore.getInstance(keystoreType);
		FileInputStream f = new FileInputStream(keystoreName);
		try {
			keyStore.load(f, keystorePassword.toCharArray());
		} finally {
			f.close();
		}
		logger.debug("Keystore: " + keystoreName + " successfully loaded");
		if (keystoreAlias == null) {
			keystoreAlias = KeystoreChecker.findAlias(keyStore);
			if (keystoreAlias == null) {
				throw new KeyStoreException("Keystore " + keystoreName
						+ " does not contain any key entries!");
			}
			logger.debug("No alias supplied, loading  <" + keystoreAlias + ">");
		} else {
			logger.debug("Loading  <" + keystoreAlias + ">");
		}
		Certificate[] path = keyStore.getCertificateChain(keystoreAlias);
		if (path == null)
			throw new KeyStoreException(
					"Alias <"
							+ keystoreAlias
							+ "> cannot be found in keystore. Please check your configuration.");
		privateKey = (PrivateKey) keyStore.getKey(keystoreAlias,
				keyPassword.toCharArray());
		if (privateKey == null)
			throw new KeyStoreException(
					"Alias <"
							+ keystoreAlias
							+ "> does not denote a key entry. Please check your configuration.");

		certChain = new X509Certificate[path.length];
		for (int i = 0; i < path.length; i++) {
			certChain[i] = (X509Certificate) path[i];
		}
		// etdSettings.setIssuerCertificateChain(certChain);
		// load gateway cert if configured
		// String gwAlias=getProperty(UAS_GATEWAY_ALIAS);
		// if(gwAlias!=null){
		// loadGWCert(gwAlias);
		// }
	}

	@SuppressWarnings("unused")
	private void loadGWCert(String alias) {
		String truststoreName = getTruststore();
		String truststoreType = getTruststoreType();
		if (truststoreType == null)
			truststoreType = "jks";
		try {
			KeyStore trustStore;
			trustStore = KeyStore.getInstance(truststoreType);
			FileInputStream f = new FileInputStream(truststoreName);
			try {
				trustStore.load(f, getTruststorePassword().toCharArray());
			} finally {
				f.close();
			}
			logger.debug("Truststore: " + truststoreName
					+ " successfully loaded");
			gatewayCertificate = (X509Certificate) trustStore
					.getCertificate(alias);
			if (gatewayCertificate == null)
				throw new IllegalArgumentException(
						"Alias <"
								+ alias
								+ "> cannot be found in truststore. Please check your configuration.");
			logger.debug("Using gateway certificate <"
					+ gatewayCertificate.getSubjectX500Principal().getName()
					+ ">");
		} catch (Exception e) {
			String msg = "Gateway alias is defined, but I could not load a gateway certificate from the truststore at "
					+ truststoreName;
			throw new IllegalArgumentException(msg, e);
		}
	}

	/**
	 * returns the private key entry identified by getKeystoreAlias() from the
	 * keystore
	 */
	@Override
	public synchronized PrivateKey getPrivateKey() {
		return privateKey;
	}

	public String toString() {
		StringBuffer sb=new StringBuffer();
		sb.append(this.getClass().getName()+"\n");
		sb.append("Keystore <"+doGetProperty(REGISTRY_SSL_KEYSTORE)+">\n");
		sb.append("Truststore <"+doGetProperty(REGISTRY_SSL_TRUSTSTORE)+">\n");
		try{
			sb.append("Identity: "+getCertificateChain()[0].getSubjectX500Principal().getName()+"\n");
		}catch(Exception e){
			sb.append("User not set.\n");
		}
		sb.append("Signing messages: "+doSignMessage()+"\n");
		return sb.toString();
	}

	@Override
	public boolean doSignMessage() {
		return signMessage;
	}

	

	@Override
	public boolean doHttpAuthn() {
		return false;
	}

	@Override
	public String getHttpUser() {
		return null;
	}

	@Override
	public String getHttpPassword() {
		return null;
	}

	@Override
	public SSLContext getSSLContext() {
		return null;
	}

	@Override
	public String getKeystorePassword() {
		return doGetProperty(REGISTRY_SSL_KEYPASS);
	}

	@Override
	public String getKeystoreKeyPassword() {
		return doGetProperty(REGISTRY_SSL_KEYPASS);
	}

	@Override
	public String getKeystore() {
		return doGetProperty(REGISTRY_SSL_KEYSTORE);
	}

	@Override
	public String getKeystoreType() {
		return doGetProperty(REGISTRY_SSL_KEYTYPE);
	}

	@Override
	public String getKeystoreAlias() {
		return doGetProperty(REGISTRY_SSL_KEYALIAS);
	}

	@Override
	public String getTruststore() {
		return doGetProperty(REGISTRY_SSL_TRUSTSTORE);
	}

	@Override
	public String getTruststoreType() {
		return doGetProperty(REGISTRY_SSL_TRUSTTYPE);
	}

	@Override
	public String getTruststorePassword() {
		return doGetProperty(REGISTRY_SSL_TRUSTPASS);
	}

	@Override
	public Properties getExtraSettings() {
		return properties;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eu.emi.dsr.security.client.IClientProperties#getOutHandlerClassNames()
	 */
	@Override
	public String getOutHandlerClassNames() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eu.emi.dsr.security.client.IClientProperties#getInHandlerClassNames()
	 */
	@Override
	public String getInHandlerClassNames() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see eu.emi.dsr.security.client.IClientProperties#getClassLoader()
	 */
	@Override
	public ClassLoader getClassLoader() {
		return classLoader;
	}

	/* (non-Javadoc)
	 * @see eu.emi.dsr.security.client.IClientProperties#getExtraSecurityTokens()
	 */
	@Override
	public Map<String, Object> getExtraSecurityTokens() {
		// TODO Auto-generated method stub
		return null;
	}

	// @Override
	// public synchronized ETDClientSettings getETDSettings() {
	// return etdSettings;
	// }
}
