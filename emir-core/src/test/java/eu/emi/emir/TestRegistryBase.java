package eu.emi.emir;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;


/**
 * 
 */

/**
 * @author a.memon
 * 
 */
public abstract class TestRegistryBase {
	static EMIRServer server = null;
	protected static String BaseURI;
	private static Process p = null;
	private static String mongodPath = "/usr/sbin/mongod";
	private static Logger logger = Logger.getLogger(TestRegistryBase.class);
	protected static Properties props;
	
	@BeforeClass
	public static void startServer() throws InterruptedException, IOException {
		startMongoDB();
		props = new Properties();
		props.put(ServerProperties.PREFIX+ServerProperties.PROP_ADDRESS, "http://localhost:0");
		
		server = new EMIRServer();
		server.run(props);
		
		server.getJettyServer();
		
		BaseURI = "http://localhost:"
				+ server.getJettyServer().getConnectors()[0].getLocalPort();
		
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
		server.stop();
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
