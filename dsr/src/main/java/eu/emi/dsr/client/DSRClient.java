/**
 * 
 */
package eu.emi.dsr.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManagerFactory;

import org.apache.log4j.Logger;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.client.urlconnection.HTTPSProperties;

import eu.emi.dsr.util.Log;

/**
 * Helper class to create the client resource instance
 * 
 * @author a.memon
 * 
 */
public class DSRClient {
	/**
	 * 
	 */
	
	private String url;
	
	private static Logger logger = Log
			.getLogger(Log.DSRCLIENT, DSRClient.class);
	Client cr = null;
	ClientSecurityProperties sProps = null;

	public DSRClient(String url, ClientSecurityProperties sp) {
		logger.debug("creating ssl client");
		sProps = sp;
		this.url = url;
		initSec();
	}

	public DSRClient(String url) {
		logger.debug("creating default client");
		this.url = url;
		cr = Client.create();
		
	}

	private void initSec() {
		ClientConfig config = new DefaultClientConfig();

		SSLContext ctx;
		try {
			ctx = SSLContext.getInstance("SSL");
			//setting keystore
			KeyStore ks = KeyStore.getInstance(sProps.getKeystoreType());
			FileInputStream fis = new FileInputStream(new File(
					sProps.getKeystore()));
			ks.load(fis, sProps.getKeystorePassword().toCharArray());
			fis.close();

			KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory
					.getDefaultAlgorithm());
			kmf.init(ks, sProps.getKeystorePassword().toCharArray());
			
			
			//setting truststore
			KeyStore ts = KeyStore.getInstance(sProps.getTruststoreType());
			FileInputStream fis1 = new FileInputStream(new File(
					sProps.getTruststore()));
			ts.load(fis1, sProps.getTruststorePassword().toCharArray());
			fis1.close();
			TrustManagerFactory tmf = TrustManagerFactory
					.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			tmf.init(ts);
			
			SecureRandom se = new SecureRandom();
			ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), se);

			HostnameVerifier hv = new HostnameVerifier() {

				public boolean verify(String arg0, SSLSession arg1) {
					return true;
				}
			};

			config.getProperties().put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES,
					new HTTPSProperties(hv, ctx));

			cr = Client.create(config);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnrecoverableKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public WebResource getClientResource() {
		return cr.resource(url);
	}
	
	public Client getClient(){
		return cr;
	}
}
