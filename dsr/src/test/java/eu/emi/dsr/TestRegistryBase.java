package eu.emi.dsr;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import eu.emi.dsr.DSRServer;
import eu.emi.dsr.core.Configuration;
import eu.emi.dsr.core.ServerProperties;

/**
 * 
 */

/**
 * @author a.memon
 * 
 */
public class TestRegistryBase {
	static DSRServer server = null;
	public static String BaseURI = "http://localhost:54321";
	@BeforeClass
	public static void startServer() {
		Properties p = new Properties();
		p.put(ServerProperties.REGISTRY_HOSTNAME, "localhost");
		p.put(ServerProperties.REGISTRY_PORT, "54321");
		p.put(ServerProperties.REGISTRY_SCHEME, "http");
		p.put(ServerProperties.JETTY_LOW_RESOURCE_MAXIDLETIME, "10000");
		p.put(ServerProperties.JETTY_LOWTHREADS, "50");
		p.put(ServerProperties.JETTY_MAXIDLETIME, "30000");
		p.put(ServerProperties.JETTY_MAXTHREADS, "255");
		p.put(ServerProperties.LOGGER_CONF_PATH, "src/main/resources/log4j.properties");
		p.put(ServerProperties.REGISTRY_PORT, "54321");
     	Configuration conf = new Configuration(p);
		server = new DSRServer(conf);
		server.startJetty();
	}

	@AfterClass
	public static void stopServer() {
		if (server.isStarted()) {
			server.stopJetty();
		}
	}
}
