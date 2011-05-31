/**
 * 
 */
package eu.emi.dsr;

import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import eu.emi.dsr.core.Configuration;
import eu.emi.dsr.core.ServerProperties;

/**
 * @author a.memon
 * 
 */
public class TestRegistryBaseWithSecurity {
	static DSRServer server = null;
	public static String BaseURI = "https://localhost:9443";

	@BeforeClass
	public static void startServer() {
		System.setProperty("javax.net.debug", "all");
		Properties p = new Properties();
		setAuthenticationSettings(p);
		setGeneralSettings(p);
		
		
     	Configuration conf = new Configuration(p);
		server = new DSRServer(conf);
		server.startJetty();
	}

	private static void setAuthenticationSettings(Properties p) {
		p.put(ServerProperties.CLIENT_AUTHN, "true");
		p.put(ServerProperties.KEYSTORE_PASSWORD, "emi");
		p.put(ServerProperties.KEYSTORE_TYPE, "pkcs12");
		p.put(ServerProperties.KEYSTORE_PATH, "src/main/certs/demo-server.p12");		
		p.put(ServerProperties.TRUSTSTORE_PASSWORD, "emi");
		p.put(ServerProperties.TRUSTSTORE_PATH, "src/main/certs/demo-server.jks");
		p.put(ServerProperties.TRUSTSTORE_TYPE, "jks");
	}

	private static void setGeneralSettings(Properties p) {
		p.put(ServerProperties.REGISTRY_HOSTNAME, "localhost");
		p.put(ServerProperties.REGISTRY_PORT, "9443");
		p.put(ServerProperties.REGISTRY_SCHEME, "https");
		p.put(ServerProperties.JETTY_LOW_RESOURCE_MAXIDLETIME, "10000");
		p.put(ServerProperties.JETTY_LOWTHREADS, "50");
		p.put(ServerProperties.JETTY_MAXIDLETIME, "30000");
		p.put(ServerProperties.JETTY_MAXTHREADS, "255");
		p.put(ServerProperties.LOGGER_CONF_PATH, "src/main/resources/log4j.properties");
	}

	@AfterClass
	public static void stopServer() {
		if (server.isStarted()) {
			server.stopJetty();
		}
	}
}
