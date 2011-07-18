/**
 * 
 */
package eu.emi.dsr;

import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import eu.emi.dsr.client.ClientSecurityProperties;
import eu.emi.dsr.core.Configuration;
import eu.emi.dsr.core.ServerConstants;

/**
 * @author a.memon
 * 
 */
public class TestRegistryBaseWithSecurity {
	static DSRServer server = null;
	public static String BaseURI;

	@BeforeClass
	public static void startServer() {
//		System.setProperty("javax.net.debug", "all");
		Properties p = new Properties();
		setSecuritySettings(p);
		setGeneralSettings(p);
		setDatabaseProperties(p);
		
     	Configuration conf = new Configuration(p);
		server = new DSRServer(conf);
		server.startJetty();
		BaseURI = "https://localhost:"+server.getServer().getConnectors()[0].getLocalPort();	
	}

	
	
	private static void setSecuritySettings(Properties p) {
		p.put(ServerConstants.CLIENT_AUTHN, "true");
		p.put(ServerConstants.KEYSTORE_PASSWORD, "emi");
		p.put(ServerConstants.KEYSTORE_TYPE, "pkcs12");
		p.put(ServerConstants.KEYSTORE_PATH, "src/main/certs/demo-server.p12");		
		p.put(ServerConstants.TRUSTSTORE_PASSWORD, "emi");
		p.put(ServerConstants.TRUSTSTORE_PATH, "src/main/certs/demo-server.jks");
		p.put(ServerConstants.TRUSTSTORE_TYPE, "jks");
		p.put(ServerConstants.REGISTRY_ACCESSCONTROL, "true");
	}

	
	
	private static void setGeneralSettings(Properties p) {
		p.put(ServerConstants.REGISTRY_HOSTNAME, "localhost");
		p.put(ServerConstants.REGISTRY_PORT, "0");
		p.put(ServerConstants.REGISTRY_SCHEME, "https");
		p.put(ServerConstants.JETTY_LOW_RESOURCE_MAXIDLETIME, "10000");
		p.put(ServerConstants.JETTY_LOWTHREADS, "50");
		p.put(ServerConstants.JETTY_MAXIDLETIME, "30000");
		p.put(ServerConstants.JETTY_MAXTHREADS, "255");
		p.put(ServerConstants.LOGGER_CONF_PATH, "src/main/resources/log4j.properties");
	}
	
	private static void setDatabaseProperties(Properties p){
		p.put(ServerConstants.MONGODB_HOSTNAME, "localhost");
		p.put(ServerConstants.MONGODB_PORT, "27017");
		p.put(ServerConstants.MONGODB_COLLECTION_NAME, "services-test");
		p.put(ServerConstants.MONGODB_DB_NAME, "emiregistry");
		p.put(ServerConstants.MONGODB_COL_CREATE, "true");
	}

	public ClientSecurityProperties getSecurityProperties(){
		ClientSecurityProperties csp = new ClientSecurityProperties();
		csp.setKeystorePassword("emi");
		csp.setKeystorePath("src/main/certs/demo-user.p12");
		csp.setKeystoreType("pkcs12");
		csp.setTruststoreType("jks");
		csp.setTruststorePassword("emi");
		csp.setTruststorePath("src/main/certs/demo-user.jks");
		return csp;
	}
	
	
	
	
	@AfterClass
	public static void stopServer() {
		if (server.isStarted()) {
			server.stopJetty();
		}
	}
}
