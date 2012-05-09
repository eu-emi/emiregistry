package eu.emi.emir;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import eu.emi.client.security.ISecurityProperties;
import eu.emi.emir.DSRServer;
import eu.emi.emir.core.Configuration;
import eu.emi.emir.core.ServerConstants;

/**
 * 
 */

/**
 * @author a.memon
 * 
 */
public abstract class TestRegistryBase {
	static DSRServer server = null;
	protected static String BaseURI;
	private static Process p = null;
	private static String mongodPath = "/usr/sbin/mongod";
	private static Logger logger = Logger.getLogger(TestRegistryBase.class);
	protected static Properties serverProps;
	
	@BeforeClass
	public static void startServer() throws InterruptedException, IOException {
		startMongoDB();
		if (serverProps == null) {
			serverProps = new Properties();
		}
		serverProps.put(ServerConstants.REGISTRY_HOSTNAME, "localhost");
		serverProps.put(ServerConstants.REGISTRY_PORT, "0");
		serverProps.put(ServerConstants.REGISTRY_SCHEME, "http");
		serverProps.put(ServerConstants.JETTY_LOW_RESOURCE_MAXIDLETIME, "10000");
		serverProps.put(ServerConstants.JETTY_LOWTHREADS, "50");
		serverProps.put(ServerConstants.JETTY_MAXIDLETIME, "30000");
		serverProps.put(ServerConstants.JETTY_MAXTHREADS, "255");
		serverProps.put(ServerConstants.LOGGER_CONF_PATH,
				"src/test/resources/conf/log4j.properties");
		serverProps.put(ServerConstants.MONGODB_HOSTNAME, "localhost");
		serverProps.put(ServerConstants.MONGODB_PORT, "27017");
		serverProps.put(ServerConstants.MONGODB_COLLECTION_NAME, "services-test");
		serverProps.put(ServerConstants.MONGODB_DB_NAME, "emiregistry");
		serverProps.put(ServerConstants.MONGODB_COL_CREATE, "true");
		serverProps.put(ISecurityProperties.REGISTRY_ACL_FILE, "src/test/resources/conf/emir.acl");
		Configuration conf = new Configuration(serverProps);
		server = new DSRServer(conf);
		server.startJetty();
		System.out.println("server started");
		BaseURI = "http://localhost:"
				+ server.getServer().getConnectors()[0].getLocalPort();
	}

	

	/**
	 * @throws IOException
	 * @throws InterruptedException 
	 * 
	 */
	public static void startMongoDB() throws IOException, InterruptedException {
		File daemonFile = new File(mongodPath);
		if (!daemonFile.exists()) {
			mongodPath = "/usr/bin/mongod";
		}

		File f = new File("mongodata");
		if (!f.exists()) {
			f.mkdir();
		}
		String[] command = new String[] { mongodPath, "--dbpath",
				f.getAbsolutePath() };

		ProcessBuilder pb = new ProcessBuilder(command);

		p = pb.start();
		
		Thread.sleep(500);
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
	public static void stopMongoDB() throws InterruptedException {
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
