/**
 * 
 */
package eu.emi.emir.resource;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Properties;

import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;

import eu.emi.emir.EMIRServer;
import eu.emi.emir.ServerProperties;
import eu.emi.emir.aip.FileAttributeSource;
import eu.emi.emir.client.EMIRClient;
import eu.emi.emir.pdp.local.LocalHerasafPDP;
import eu.emi.emir.security.ClientSecurityProperties;
import eu.emi.emir.security.ServerSecurityProperties;
import eu.unicore.security.canl.AuthnAndTrustProperties;
import eu.unicore.security.canl.CredentialProperties;
import eu.unicore.security.canl.CredentialProperties.CredentialFormat;
import eu.unicore.security.canl.TruststoreProperties;

/**
 * @author a.memon
 *
 */
public class TestPingResourceWithSecurity {
	protected static Properties props;
	static EMIRServer server = null;
	protected static String BaseURI;
	private static Logger logger = Logger.getLogger(TestPingResourceWithSecurity
			.class);
	
	@BeforeClass
	public static void startServer() {
		// System.setProperty("javax.net.debug", "all");
		Properties p = new Properties();
		setSecuritySettingsWithDir(p);
		props = p;
		server = new EMIRServer();
		server.run(props);
		
		server.getJettyServer();
		
		BaseURI = "https://localhost:"
				+ server.getJettyServer().getConnectors()[0].getLocalPort();
	}
	private static void setSecuritySettingsWithDir(Properties p) {
		p.setProperty(ServerSecurityProperties.PREFIX
				+ CredentialProperties.DEFAULT_PREFIX
				+ CredentialProperties.PROP_FORMAT, CredentialFormat.pem.toString());
		p.setProperty(ServerSecurityProperties.PREFIX
				+ CredentialProperties.DEFAULT_PREFIX
				+ CredentialProperties.PROP_LOCATION, "src/test/resources/certs/pem/server/cert.pem");
		p.setProperty(ServerSecurityProperties.PREFIX
				+ CredentialProperties.DEFAULT_PREFIX
				+ CredentialProperties.PROP_PASSWORD, "emi");		
		p.setProperty(ServerSecurityProperties.PREFIX
				+ CredentialProperties.DEFAULT_PREFIX
				+ CredentialProperties.PROP_KEY_LOCATION, "src/test/resources/certs/pem/server/key.pem");
		
		
		p.setProperty(ServerSecurityProperties.PREFIX
				+ TruststoreProperties.DEFAULT_PREFIX
				+ TruststoreProperties.PROP_TYPE,
				TruststoreProperties.TruststoreType.directory.toString());
		p.setProperty(ServerSecurityProperties.PREFIX
				+ TruststoreProperties.DEFAULT_PREFIX
				+ TruststoreProperties.PROP_DIRECTORY_LOCATIONS,
				"src/test/resources/certs/cadir/*.pem");
		p.setProperty(ServerSecurityProperties.PREFIX
				+ TruststoreProperties.DEFAULT_PREFIX
				+ TruststoreProperties.PROP_DIRECTORY_ENCODING, "PEM");
		
		
	    p.put(ServerSecurityProperties.PREFIX+ServerSecurityProperties.PROP_AIP_ORDER,"FILE");
	    p.put(ServerSecurityProperties.PREFIX+ServerSecurityProperties.PROP_AIP_PREFIX+".FILE.class",FileAttributeSource.class.getName());
	    p.put(ServerSecurityProperties.PREFIX+ServerSecurityProperties.PROP_AIP_PREFIX+".FILE.file","src/test/resources/conf/users/testUdb-strict.xml");
	    p.put(ServerSecurityProperties.PREFIX+ServerSecurityProperties.PROP_CHECKACCESS_PDPCONFIG, "src/test/resources/conf/xacml2.config");
	    p.put(ServerSecurityProperties.PREFIX+ServerSecurityProperties.PROP_CHECKACCESS_PDP, LocalHerasafPDP.class.getName());

	    p.put(ServerProperties.PREFIX+ServerProperties.PROP_ADDRESS, "https://localhost:0");	
//	    p.put(ServerSecurityProperties.PREFIX+ServerSecurityProperties.PROP_CHECKACCESS_ACL, "src/test/resources/conf/emir.acl");    
	    
	    //p.put(ISecurityProperties.REGISTRY_CHECKACCESS_PDPCONFIG, "src/test/resources/conf/xacml2.config");
//		p.put(ISecurityProperties.REGISTRY_CHECKACCESS_PDP, LocalHerasafPDP.class.getName());
//      p.put(ISecurityProperties.REGISTRY_CHECKACCESS_PDP, FlatFilePDP.class.getName());
//		p.put(ISecurityProperties.REGISTRY_ACL_FILE, "src/test/resources/conf/emir.acl");
	}
	
	public static ClientSecurityProperties getSecurityProperties(){
		Properties p = new Properties();
		p.setProperty(ClientSecurityProperties.PREFIX
				+ CredentialProperties.DEFAULT_PREFIX
				+ CredentialProperties.PROP_FORMAT, CredentialFormat.pem.toString());
		p.setProperty(ClientSecurityProperties.PREFIX
				+ CredentialProperties.DEFAULT_PREFIX
				+ CredentialProperties.PROP_LOCATION, "src/test/resources/certs/pem/client/client-cert.pem");
		p.setProperty(ClientSecurityProperties.PREFIX
				+ CredentialProperties.DEFAULT_PREFIX
				+ CredentialProperties.PROP_PASSWORD, "emi");		
		p.setProperty(ClientSecurityProperties.PREFIX
				+ CredentialProperties.DEFAULT_PREFIX
				+ CredentialProperties.PROP_KEY_LOCATION, "src/test/resources/certs/pem/client/client-key.pem");
		
		
		p.setProperty(ClientSecurityProperties.PREFIX
				+ TruststoreProperties.DEFAULT_PREFIX
				+ TruststoreProperties.PROP_TYPE,
				TruststoreProperties.TruststoreType.directory.toString());
		p.setProperty(ClientSecurityProperties.PREFIX
				+ TruststoreProperties.DEFAULT_PREFIX
				+ TruststoreProperties.PROP_DIRECTORY_LOCATIONS,
				"src/test/resources/certs/cadir/*.pem");
		p.setProperty(ClientSecurityProperties.PREFIX
				+ TruststoreProperties.DEFAULT_PREFIX
				+ TruststoreProperties.PROP_DIRECTORY_ENCODING, "PEM");
		
		AuthnAndTrustProperties authn = new AuthnAndTrustProperties(p, ClientSecurityProperties.PREFIX
				+ TruststoreProperties.DEFAULT_PREFIX, ClientSecurityProperties.PREFIX
				+ CredentialProperties.DEFAULT_PREFIX);
		
		ClientSecurityProperties csp = new ClientSecurityProperties(p, authn); 
		
		return csp;
	}
	
	@Test
	public void test() throws Exception {
		EMIRClient cr1 = new EMIRClient(BaseURI + "/ping",getSecurityProperties());
		assertTrue(cr1.getClientResource()
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.get(ClientResponse.class).getStatus() == Status.OK
				.getStatusCode());
		
		JSONObject jo = cr1.getClientResource()
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.get(ClientResponse.class).getEntity(JSONObject.class);
		
		System.out.println(jo);
		
		assertNotNull(jo);
	}
}
