/**
 * 
 */
package eu.emi.emir.db.mongodb;

import java.io.File;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * Base class for the db related tests
 * @author a.memon
 *
 */
public class MongoDBTestBase {
	private static final Logger logger = Logger
			.getLogger(MongoDBTestBase.class);
	static Process p = null;
	
	@AfterClass
	public static void afterClass() throws Exception {
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
	
	@BeforeClass
	public static void beforeClass() throws Exception {
		String mongodPath = "/usr/sbin/mongod";
		File daemonFile = new File("/usr/sbin/mongod");
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
		//give sometime to start the db
		Thread.sleep(500);
		logger.debug("Process started with pid: " + p);

	}
}
