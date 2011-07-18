package eu.emi.dsr;
import java.util.Enumeration;
import java.util.Iterator;
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
	protected static String BaseURI;
	@BeforeClass
	public static void startServer() throws InterruptedException {
		Properties p = new Properties();
		p.put(ServerConstants.REGISTRY_HOSTNAME, "localhost");
		p.put(ServerConstants.REGISTRY_PORT, "0");
		p.put(ServerConstants.REGISTRY_SCHEME, "http");
		p.put(ServerConstants.JETTY_LOW_RESOURCE_MAXIDLETIME, "10000");
		p.put(ServerConstants.JETTY_LOWTHREADS, "50");
		p.put(ServerConstants.JETTY_MAXIDLETIME, "30000");
		p.put(ServerConstants.JETTY_MAXTHREADS, "255");
		p.put(ServerConstants.LOGGER_CONF_PATH, "src/main/resources/log4j.properties");
		p.put(ServerConstants.MONGODB_HOSTNAME, "localhost");
		p.put(ServerConstants.MONGODB_PORT, "27017");
		p.put(ServerConstants.MONGODB_COLLECTION_NAME, "services-test");
		p.put(ServerConstants.MONGODB_DB_NAME, "emiregistry");
		p.put(ServerConstants.MONGODB_COL_CREATE, "true");
     	Configuration conf = new Configuration(p);
		server = new DSRServer(conf);
		server.startJetty();
		System.out.println("server started");
		BaseURI = "http://localhost:"+server.getServer().getConnectors()[0].getLocalPort();	
		
	}

	@AfterClass
	public static void stopServer() {
		if (server.isStarted()) {
			server.stopJetty();
		}
	}
}
