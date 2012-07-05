/**
 * 
 */
package eu.emi.emir.jetty;

import java.net.URL;
import java.util.Properties;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.emi.emir.EMIRServer;
import eu.emi.emir.ServerProperties;
import eu.emi.emir.TestRegistryBaseWithSecurity;
import eu.emi.emir.client.EMIRClient;
import eu.emi.emir.db.mongodb.MongoDBTestBase;
import eu.emi.emir.security.ServerSecurityProperties;
import eu.unicore.security.canl.CredentialProperties;
import eu.unicore.security.canl.TruststoreProperties;

/**
 * @author g.szigeti
 *
 */
public class TestHttpServerWithSecureServer extends MongoDBTestBase {
	HttpServer h = null;
	EMIRServer server = null;
	Properties p = null;
	String serverAddress = "https://localhost:54321";
	String anonymousPort = "8080";
	@Before
	public void setup() throws Exception{
		p = new Properties();
		server = new EMIRServer();
		p.put(ServerProperties.PREFIX+ServerProperties.PROP_ADDRESS, serverAddress);
		p.put(ServerProperties.PREFIX+ServerProperties.PROP_ANONYMOUS_PORT, anonymousPort);
		
		p.put(ServerSecurityProperties.PREFIX+ServerSecurityProperties.PROP_CHECKACCESS_ACL, "src/test/resources/conf/emir.acl");

		// Credential options
		p.put(ServerSecurityProperties.PREFIX
				+ CredentialProperties.DEFAULT_PREFIX
				+ CredentialProperties.PROP_LOCATION, "src/test/resources/certs/demo-server.p12");
		p.put(ServerSecurityProperties.PREFIX
				+ CredentialProperties.DEFAULT_PREFIX
				+ CredentialProperties.PROP_FORMAT,
				CredentialProperties.CredentialFormat.pkcs12.toString());
		p.put(ServerSecurityProperties.PREFIX
				+ CredentialProperties.DEFAULT_PREFIX
				+ CredentialProperties.PROP_PASSWORD, "emi");

		// Trust store options
		p.put(ServerSecurityProperties.PREFIX
				+ TruststoreProperties.DEFAULT_PREFIX
				+ TruststoreProperties.PROP_TYPE, "keystore");
		p.put(ServerSecurityProperties.PREFIX
				+ TruststoreProperties.DEFAULT_PREFIX
				+ TruststoreProperties.PROP_KS_PATH, "src/test/resources/certs/demo-server.jks");
		p.put(ServerSecurityProperties.PREFIX
				+ TruststoreProperties.DEFAULT_PREFIX
				+ TruststoreProperties.PROP_KS_PASSWORD, "emi");
		p.put(ServerSecurityProperties.PREFIX
				+ TruststoreProperties.DEFAULT_PREFIX
				+ TruststoreProperties.PROP_KS_TYPE, "JKS");

		h = new HttpServer(p);
	}
	
	@Test
	public void testRun() throws Exception{
		server.run(p);
		System.out.println("Anonymous resources TEST:");
		URL address = new URL(serverAddress);
		EMIRClient cr = new EMIRClient("http://"+address.getHost()+":"+anonymousPort);
		System.out.println(cr.getClientResource().path("/services").get(JSONArray.class).toString());
		System.out.println(cr.getClientResource().path("/ping").get(JSONObject.class).toString());

		System.out.println("Secure resources TEST:");
		EMIRClient crs = new EMIRClient(serverAddress, TestRegistryBaseWithSecurity.getSecurityProperties_2());
		System.out.println(crs.getClientResource().path("/services").get(JSONArray.class).toString());
		System.out.println(crs.getClientResource().path("/ping").get(JSONObject.class).toString());
	}
	
	@After
	public void tearDown() throws Exception{
		server.stop();
	}
}
