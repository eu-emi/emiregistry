package eu.emi.dsr;
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
		Configuration conf = new Configuration();
		Configuration.setProperty(ServerProperties.REGISTRY_PORT, "54321");
		server = new DSRServer(conf);
		server.start();
	}

	@AfterClass
	public static void stopServer() {
		if (server.isStarted()) {
			server.shutdown();
		}
	}
}
