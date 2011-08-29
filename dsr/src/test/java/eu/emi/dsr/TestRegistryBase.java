package eu.emi.dsr;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;
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
	private static Process p = null;
	private static String mongodPath = "/usr/sbin/mongod";
	private static Logger logger = Logger.getLogger(TestRegistryBase.class);

	@BeforeClass
	public static void startServer() throws InterruptedException, IOException {
		startMongoDB();

		Properties p = new Properties();
		p.put(ServerConstants.REGISTRY_HOSTNAME, "localhost");
		p.put(ServerConstants.REGISTRY_PORT, "0");
		p.put(ServerConstants.REGISTRY_SCHEME, "http");
		p.put(ServerConstants.JETTY_LOW_RESOURCE_MAXIDLETIME, "10000");
		p.put(ServerConstants.JETTY_LOWTHREADS, "50");
		p.put(ServerConstants.JETTY_MAXIDLETIME, "30000");
		p.put(ServerConstants.JETTY_MAXTHREADS, "255");
		p.put(ServerConstants.LOGGER_CONF_PATH,
				"src/test/resources/conf/log4j.properties");
		p.put(ServerConstants.MONGODB_HOSTNAME, "localhost");
		p.put(ServerConstants.MONGODB_PORT, "27017");
		p.put(ServerConstants.MONGODB_COLLECTION_NAME, "services-test");
		p.put(ServerConstants.MONGODB_DB_NAME, "emiregistry");
		p.put(ServerConstants.MONGODB_COL_CREATE, "true");		
		Configuration conf = new Configuration(p);
		server = new DSRServer(conf);
		server.startJetty();
		System.out.println("server started");
		BaseURI = "http://localhost:"
				+ server.getServer().getConnectors()[0].getLocalPort();
	}

	/**
	 * @throws IOException
	 * 
	 */
	private static void startMongoDB() throws IOException {
		File daemonFile = new File(mongodPath);
		if (!daemonFile.exists()) {
			mongodPath = "/usr/bin/mongod";
		}

		File f = new File("./mongodata");
		if (!f.exists()) {
			f.mkdir();
		}
		String[] command = new String[] { mongodPath, "--dbpath",
				f.getAbsolutePath() };

		ProcessBuilder pb = new ProcessBuilder(command);

		p = pb.start();

		logger.debug("Process started with pid: " + p);

	}

	@AfterClass
	public static void stopServer() throws InterruptedException {
		stopMongoDB();
		if (server.isStarted()) {
			server.stopJetty();
		}
	}

	/**
	 * @throws InterruptedException
	 * 
	 */
	private static void stopMongoDB() throws InterruptedException {
		// Stop mongod process
		boolean processClosed = false;

		Thread.sleep(500);
		if (p != null) {
			while (!processClosed) {

				try {
					p.destroy();
					processClosed = true;
					Thread.sleep(500);
					logger.info(" Process destroyed: " + p.exitValue());
				} catch (IllegalThreadStateException itse) {
					logger.warn(itse);
					processClosed = false;
				}
			}
		}

	}
}