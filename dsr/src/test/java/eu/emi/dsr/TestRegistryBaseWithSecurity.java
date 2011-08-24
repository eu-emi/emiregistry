/**
 * 
 */
package eu.emi.dsr;

import java.io.File;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

import eu.emi.dsr.aip.FileAttributeSource;
import eu.emi.dsr.client.ClientSecurityProperties;
import eu.emi.dsr.core.Configuration;
import eu.emi.dsr.core.ServerConstants;
import eu.emi.dsr.db.ServiceDatabase;
import eu.emi.dsr.db.mongodb.MongoDBServiceDatabase;
import eu.emi.dsr.pdp.local.LocalHerasafPDP;
import eu.emi.dsr.security.AccessControlFilter;
import eu.emi.dsr.security.ISecurityProperties;
//import eu.unicore.uas.pdp.local.LocalHerasafPDP;

/**
 * @author a.memon
 * 
 */
public class TestRegistryBaseWithSecurity {
	static DSRServer server = null;
	public static String BaseURI;
	private static Properties props = null;
	private static Process p = null;
	private static String mongodPath = "/usr/sbin/mongod";
	private static Logger logger = Logger.getLogger(TestRegistryBaseWithSecurity
			.class);
	
	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	@BeforeClass
	public static void startServer() {
		try {
			startMongoDB();
		} catch (IOException e) {			
			e.printStackTrace();
		}
		// System.setProperty("javax.net.debug", "all");
		Properties p = new Properties();
		setSecuritySettings(p);
		setGeneralSettings(p);
		setDatabaseProperties(p);
		props = p;
		Configuration conf = new Configuration(p);
		server = new DSRServer(conf);
		server.startJetty();
		BaseURI = "https://localhost:"
				+ server.getServer().getConnectors()[0].getLocalPort();
	}

	private static void setSecuritySettings(Properties p) {
		p.put(ISecurityProperties.REGISTRY_SSL_ENABLED, "true");
		p.put(ISecurityProperties.REGISTRY_SSL_CLIENTAUTH, "true");
		p.put(ISecurityProperties.REGISTRY_SSL_KEYPASS, "emi");
		p.put(ISecurityProperties.REGISTRY_SSL_KEYTYPE, "pkcs12");
		p.put(ISecurityProperties.REGISTRY_SSL_KEYSTORE,
				"src/test/resources/certs/demo-server.p12");
		p.put(ISecurityProperties.REGISTRY_SSL_TRUSTPASS, "emi");
		p.put(ISecurityProperties.REGISTRY_SSL_TRUSTSTORE,
				"src/test/resources/certs/demo-server.jks");
		p.put(ISecurityProperties.REGISTRY_SSL_TRUSTTYPE, "jks");
		p.put(ISecurityProperties.REGISTRY_CHECKACCESS, "true");
		p.put("registry.security.attributes.order", "FILE");
		p.put("registry.security.attributes.FILE.class",FileAttributeSource.class.getName());
		p.put("registry.security.attributes.FILE.file", "src/test/resources/conf/users/testUudb-strict.xml");
		p.put(ISecurityProperties.REGISTRY_CHECKACCESS_PDPCONFIG, "src/test/resources/conf/xacml2.config");
		p.put(ISecurityProperties.REGISTRY_CHECKACCESS_PDP, LocalHerasafPDP.class.getName());
	}

	private static void setGeneralSettings(Properties p) {
		p.put(ServerConstants.REGISTRY_HOSTNAME, "localhost");
		p.put(ServerConstants.REGISTRY_PORT, "0");
		p.put(ServerConstants.REGISTRY_SCHEME, "https");
		p.put(ServerConstants.JETTY_LOW_RESOURCE_MAXIDLETIME, "10000");
		p.put(ServerConstants.JETTY_LOWTHREADS, "50");
		p.put(ServerConstants.JETTY_MAXIDLETIME, "30000");
		p.put(ServerConstants.JETTY_MAXTHREADS, "255");
		p.put(ServerConstants.LOGGER_CONF_PATH,
				"src/test/resources/conf/log4j.properties");	
		
	}

	private static void setDatabaseProperties(Properties p) {
		p.put(ServerConstants.MONGODB_HOSTNAME, "localhost");
		p.put(ServerConstants.MONGODB_PORT, "27017");
		p.put(ServerConstants.MONGODB_COLLECTION_NAME, "services-test");
		p.put(ServerConstants.MONGODB_DB_NAME, "emiregistry");
		p.put(ServerConstants.MONGODB_COL_CREATE, "true");
	}

	public ClientSecurityProperties getSecurityProperties_1()
			throws UnrecoverableKeyException, KeyStoreException,
			NoSuchAlgorithmException, CertificateException, IOException {
		Properties p = new Properties();

		p.put(ISecurityProperties.REGISTRY_SSL_CLIENTAUTH, "true");
		p.put(ISecurityProperties.REGISTRY_SSL_KEYPASS, "emi");
		p.put(ISecurityProperties.REGISTRY_SSL_KEYTYPE, "pkcs12");
		p.put(ISecurityProperties.REGISTRY_SSL_KEYSTORE,
				"src/test/resources/certs/demo-user.p12");
		p.put(ISecurityProperties.REGISTRY_SSL_TRUSTPASS, "emi");
		p.put(ISecurityProperties.REGISTRY_SSL_TRUSTSTORE,
				"src/test/resources/certs/demo-server.jks");
		p.put(ISecurityProperties.REGISTRY_SSL_TRUSTTYPE, "jks");
		ClientSecurityProperties csp = new ClientSecurityProperties(p);
		return csp;
	}
	
	public ClientSecurityProperties getSecurityProperties_2()
			throws UnrecoverableKeyException, KeyStoreException,
			NoSuchAlgorithmException, CertificateException, IOException {
		Properties p = new Properties();

		p.put(ISecurityProperties.REGISTRY_SSL_CLIENTAUTH, "true");
		p.put(ISecurityProperties.REGISTRY_SSL_KEYPASS, "emi");
		p.put(ISecurityProperties.REGISTRY_SSL_KEYTYPE, "pkcs12");
		p.put(ISecurityProperties.REGISTRY_SSL_KEYSTORE,
				"src/test/resources/certs/demo-user-2.p12");
		p.put(ISecurityProperties.REGISTRY_SSL_TRUSTPASS, "emi");
		p.put(ISecurityProperties.REGISTRY_SSL_TRUSTSTORE,
				"src/test/resources/certs/demo-server.jks");
		p.put(ISecurityProperties.REGISTRY_SSL_TRUSTTYPE, "jks");
		ClientSecurityProperties csp = new ClientSecurityProperties(p);
		return csp;
	}
	

	@AfterClass
	public static void stopServer() {
		try {
			stopMongoDB();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (server.isStarted()) {
			server.stopJetty();
		}
	}

	@After
	public void cleanup() {
		ServiceDatabase sd = new MongoDBServiceDatabase();
		sd.deleteAll();
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
	/**
	 * @throws IOException
	 * 
	 */
	private static void startMongoDB() throws IOException {
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

}
