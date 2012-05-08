/**
 * 
 */
package eu.emi.client;

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

import eu.emi.client.util.Log;
import eu.eu_emi.emiregistry.QueryResult;

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
			// setting keystore
			KeyStore ks = KeyStore.getInstance(sProps.getKeystoreType());
			FileInputStream fis = new FileInputStream(new File(
					sProps.getKeystore()));
			ks.load(fis, sProps.getKeystorePassword().toCharArray());
			fis.close();

			KeyManagerFactory kmf = KeyManagerFactory
					.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(ks, sProps.getKeystorePassword().toCharArray());

			// setting truststore
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

			config.getProperties().put(
					HTTPSProperties.PROPERTY_HTTPS_PROPERTIES,
					new HTTPSProperties(hv, ctx));

			cr = Client.create(config);
		} catch (NoSuchAlgorithmException e) {
			Log.logException("", e);
		} catch (KeyStoreException e) {
			Log.logException("", e);
		} catch (FileNotFoundException e) {
			Log.logException("", e);
		} catch (KeyManagementException e) {
			Log.logException("", e);
		} catch (CertificateException e) {
			Log.logException("", e);
		} catch (IOException e) {
			Log.logException("", e);
		} catch (UnrecoverableKeyException e) {
			Log.logException("", e);
		}

	}

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
		return getClientResource().path("services").accept(MediaType.APPLICATION_JSON_TYPE).post(
				JSONArray.class, queryDocument);
	}

	public QueryResult queryXML(MultivaluedMap<String, String> attrMap,
			Integer skip, Integer limit) {
		QueryResult ja = getClientResource().path("services")
				.queryParams(attrMap).accept(MediaType.APPLICATION_XML_TYPE)
				.get(QueryResult.class);
		return ja;
	}
}
