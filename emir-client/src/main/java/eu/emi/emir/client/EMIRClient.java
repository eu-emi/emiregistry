/**
 * 
 */
package eu.emi.emir.client;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.client.urlconnection.HTTPSProperties;

import eu.emi.emir.client.util.Log;
import eu.emi.security.authn.x509.X509CertChainValidator;
import eu.emi.security.authn.x509.X509Credential;
import eu.emi.security.authn.x509.impl.SocketFactoryCreator;
import eu.eu_emi.emiregistry.QueryResult;
import eu.unicore.security.canl.LoggingX509TrustManager;
import eu.unicore.util.httpclient.IClientConfiguration;

/**
 * Helper class to create the client resource instance
 * 
 * @author a.memon
 * 
 */
public class EMIRClient {
	/**
	 * 
	 */

	private String url;

	private static Logger logger = Log.getLogger(Log.EMIR_CLIENT,
			EMIRClient.class);
	Client cr = null;

	IClientConfiguration clientConfig = null;

	public EMIRClient(String url, IClientConfiguration clientConfig) {
		logger.debug("creating ssl client");
		this.clientConfig = clientConfig;
		this.url = url;
		initSec();
	}

	/**
	 * 
	 */
	private void initSec() {
		ClientConfig config = new DefaultClientConfig();
		SSLContext ctx = null;
		try {
			ctx = createSSLContext(clientConfig.getValidator(), clientConfig.getCredential(), "SSL", null, new SecureRandom().getAlgorithm());
		} catch (Exception e) {
			Log.logException("Error initializing the Security - check security configuration", e, logger);
		} 
		
		HostnameVerifier hv = new HostnameVerifier() {
			public boolean verify(String arg0, SSLSession arg1) {
				return true;
			}
		};

		config.getProperties().put(
				HTTPSProperties.PROPERTY_HTTPS_PROPERTIES,
				new HTTPSProperties(hv, ctx));

		cr = Client.create(config);

	}

	private SSLContext createSSLContext(X509CertChainValidator validator,
			X509Credential credential, String protocol, String provider,
			String secRandomAlg) throws NoSuchAlgorithmException,
			NoSuchProviderException, KeyManagementException {

		try {
			if (logger.isTraceEnabled()) {
				logger.trace(credential.getKeyStore().getCertificate("NOT_SET"));	
			}			
		} catch (KeyStoreException e) {
			Log.logException("Error creating the SSL context", e, logger);
		}

		KeyManager[] keyManagers = new KeyManager[] { credential
				.getKeyManager() };

		X509TrustManager trustManager = SocketFactoryCreator
				.getSSLTrustManager(validator);
		X509TrustManager decoratedTrustManager = new LoggingX509TrustManager(
				trustManager);
		TrustManager[] trustManagers = new X509TrustManager[] { decoratedTrustManager };
		
		SecureRandom secureRandom = (secRandomAlg == null) ? null
				: SecureRandom.getInstance(secRandomAlg);
		
		
		
		SSLContext context = (provider == null) ? SSLContext
				.getInstance(protocol) : SSLContext.getInstance(protocol,
				provider);

		context.init(keyManagers, trustManagers, secureRandom);

		return context;
	}

	public EMIRClient(String url) {
		logger.debug("creating default client");
		this.url = url;
		cr = Client.create();

	}

//	private void _initSec() {
//		ClientConfig config = new DefaultClientConfig();
//
//		SSLContext ctx;
//		try {
//			ctx = SSLContext.getInstance("SSL");
//
//			// setting keystore
//			KeyStore ks = KeyStore.getInstance(sProps.getKeystoreType());
//			FileInputStream fis = new FileInputStream(new File(
//					sProps.getKeystore()));
//			ks.load(fis, sProps.getKeystorePassword().toCharArray());
//			fis.close();
//
//			KeyManagerFactory kmf = KeyManagerFactory
//					.getInstance(KeyManagerFactory.getDefaultAlgorithm());
//			kmf.init(ks, sProps.getKeystorePassword().toCharArray());
//
//			// setting truststore
//			KeyStore ts = KeyStore.getInstance(sProps.getTruststoreType());
//			FileInputStream fis1 = new FileInputStream(new File(
//					sProps.getTruststore()));
//			ts.load(fis1, sProps.getTruststorePassword().toCharArray());
//			fis1.close();
//			TrustManagerFactory tmf = TrustManagerFactory
//					.getInstance(TrustManagerFactory.getDefaultAlgorithm());
//			tmf.init(ts);
//
//			SecureRandom se = new SecureRandom();
//			ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), se);
//
//			HostnameVerifier hv = new HostnameVerifier() {
//
//				public boolean verify(String arg0, SSLSession arg1) {
//					return true;
//				}
//			};
//
//			config.getProperties().put(
//					HTTPSProperties.PROPERTY_HTTPS_PROPERTIES,
//					new HTTPSProperties(hv, ctx));
//
//			cr = Client.create(config);
//		} catch (NoSuchAlgorithmException e) {
//			Log.logException("", e);
//		} catch (KeyStoreException e) {
//			Log.logException("", e);
//		} catch (FileNotFoundException e) {
//			Log.logException("", e);
//		} catch (KeyManagementException e) {
//			Log.logException("", e);
//		} catch (CertificateException e) {
//			Log.logException("", e);
//		} catch (IOException e) {
//			Log.logException("", e);
//		} catch (UnrecoverableKeyException e) {
//			Log.logException("", e);
//		}
//
//	}

	public WebResource getClientResource() {
		return cr.resource(url);
	}

	public Client getClient() {
		return cr;
	}

	public JSONArray register(JSONArray ja) {
		ClientResponse res = getClientResource().path("serviceadmin")
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.post(ClientResponse.class, ja);
		return res.getEntity(JSONArray.class);
	}

	public JSONArray update(JSONArray ja) {
		ClientResponse res = getClientResource().path("serviceadmin")
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.put(ClientResponse.class, ja);
		return res.getEntity(JSONArray.class);
	}

	public ClientResponse delete(String url) {
		ClientResponse res = getClientResource()
				.path("serviceadmin")
				.queryParam(
						ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
								.getAttributeName(),
						url).delete(ClientResponse.class);
		return res;
	}

	public JSONArray query(MultivaluedMap<String, String> attrMap) {
		JSONArray ja = null;
		if (attrMap != null) {
			ja = getClientResource().path("services").queryParams(attrMap)
					.accept(MediaType.APPLICATION_JSON_TYPE)
					.get(JSONArray.class);
		} else {
			ja = getClientResource().path("services")
					.accept(MediaType.APPLICATION_JSON_TYPE)
					.get(JSONArray.class);
		}
		return ja;
	}

	public JSONArray queryJSON(JSONObject queryDocument) {
		return getClientResource().path("services")
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.post(JSONArray.class, queryDocument);
	}

	public QueryResult queryXML(MultivaluedMap<String, String> attrMap,
			Integer skip, Integer limit) {
		QueryResult ja = getClientResource().path("services")
				.queryParams(attrMap).accept(MediaType.APPLICATION_XML_TYPE)
				.get(QueryResult.class);
		return ja;
	}
}
