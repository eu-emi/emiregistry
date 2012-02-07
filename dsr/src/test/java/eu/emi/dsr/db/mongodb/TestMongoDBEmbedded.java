/**
 * 
 */
package eu.emi.dsr.db.mongodb;

import java.io.File;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import eu.emi.dsr.db.ExistingResourceException;
import eu.emi.dsr.db.PersistentStoreFailureException;

/**
 * Running mongodb from test case
 * 
 * @author a.memon
 * 
 */
public class TestMongoDBEmbedded extends Assert{
	private static final Logger logger = Logger
			.getLogger(TestMongoDBEmbedded.class);
	static Process p = null;
	
	@Before
	public void setUp() throws Exception {
		// @Todo Run mongo with a test specific .js file to produce initial data
		// state
	}

	@After
	public void tearDown() throws Exception {
		// @Todo Drop database
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
		String[] command = new String[] {
				mongodPath,"--dbpath",f.getAbsolutePath()};

		ProcessBuilder pb = new ProcessBuilder(command);

		p = pb.start();
		
		//wait for 1 sec to start the db
		Thread.sleep(500);
		
		logger.debug("Process started with pid: " + p);

	}
	
	@Test
	public void testDb() throws JSONException, ExistingResourceException, PersistentStoreFailureException{
		
		MongoDBServiceDatabase m = new MongoDBServiceDatabase("localhost",27017,"testdb","testcol");
		
		BasicDBObject bdb = new BasicDBObject();
		
		bdb.put("testname", "testvalue");
		
		Date now = new Date();
		
		bdb.put("now", now);
		
		m.insert(bdb);
		
		List<ServiceObject> lst = m.findAll();
		
		DBObject db = lst.get(0).toDBObject();
		
		Date d = (Date) db.get("now");
		
		assertEquals(now, d);
		
		assertEquals("testvalue", lst.get(0).toDBObject().get("testname"));
		
		m.deleteAll();
	}

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
}
