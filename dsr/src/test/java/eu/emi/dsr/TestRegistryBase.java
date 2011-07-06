package eu.emi.dsr;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import eu.emi.dsr.DSRServer;
import eu.emi.dsr.core.Configuration;
import eu.emi.dsr.core.ServerConstants;

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
		p.put(ServerConstants.REGISTRY_HOSTNAME, "localhost");
		p.put(ServerConstants.REGISTRY_PORT, "54321");
		p.put(ServerConstants.REGISTRY_SCHEME, "http");
		p.put(ServerConstants.JETTY_LOW_RESOURCE_MAXIDLETIME, "10000");
		p.put(ServerConstants.JETTY_LOWTHREADS, "50");
		p.put(ServerConstants.JETTY_MAXIDLETIME, "30000");
		p.put(ServerConstants.JETTY_MAXTHREADS, "255");
		p.put(ServerConstants.LOGGER_CONF_PATH, "src/main/resources/log4j.properties");
		p.put(ServerConstants.MONGODB_HOSTNAME, "localhost");
		p.put(ServerConstants.MONGODB_PORT, "27017");
		p.put(ServerConstants.MONGODB_COLLECTION_NAME, "services");
		p.put(ServerConstants.MONGODB_DB_NAME, "emiregistry");
     	Configuration conf = new Configuration(p);
		server = new DSRServer(conf);
		server.startJetty();
		System.out.println("server started");
	}

	@AfterClass
	public static void stopServer() {
		if (server.isStarted()) {
			server.stopJetty();
		}
	}
}
