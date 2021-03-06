/**
 * 
 */
package eu.emi.emir;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

import eu.emi.emir.aip.FileAttributeSource;
import eu.emi.emir.db.ServiceDatabase;
import eu.emi.emir.db.mongodb.MongoDBServiceDatabase;
import eu.emi.emir.pdp.local.LocalHerasafPDP;
import eu.emi.emir.security.ClientSecurityProperties;
import eu.emi.emir.security.ServerSecurityProperties;
import eu.unicore.security.canl.AuthnAndTrustProperties;
import eu.unicore.security.canl.CredentialProperties;
import eu.unicore.security.canl.TruststoreProperties;
//import eu.unicore.uas.pdp.local.LocalHerasafPDP;

/**
 * @author a.memon
 * 
 */
public class TestRegistryBaseWithSecurity {
//	static DSRServer server = null;
	static EMIRServer server = null;
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
		props = p;
//		Configuration conf = new Configuration(p);
//		server = new DSRServer(conf);
		
		server = new EMIRServer();
		server.run(props);
		
		Server s = server.getJettyServer();
		
		BaseURI = "https://localhost:"
				+s.getConnectors()[0].getLocalPort();
	}

	private static void setSecuritySettings(Properties p) {
		p.setProperty(ServerSecurityProperties.PREFIX
				+ CredentialProperties.DEFAULT_PREFIX
				+ CredentialProperties.PROP_PASSWORD, "emi");
		p.setProperty(ServerSecurityProperties.PREFIX
				+ CredentialProperties.DEFAULT_PREFIX
				+ CredentialProperties.PROP_FORMAT,
				CredentialProperties.CredentialFormat.pkcs12.toString());
		p.setProperty(ServerSecurityProperties.PREFIX
				+ CredentialProperties.DEFAULT_PREFIX
				+ CredentialProperties.PROP_LOCATION,
				"src/test/resources/certs/demo-server.p12");

		p.setProperty(ServerSecurityProperties.PREFIX
				+ TruststoreProperties.DEFAULT_PREFIX
				+ TruststoreProperties.PROP_TYPE,
				TruststoreProperties.TruststoreType.keystore.toString());
		p.setProperty(ServerSecurityProperties.PREFIX
				+ TruststoreProperties.DEFAULT_PREFIX
				+ TruststoreProperties.PROP_KS_PATH,
				"src/test/resources/certs/demo-server.jks");
		p.setProperty(ServerSecurityProperties.PREFIX
				+ TruststoreProperties.DEFAULT_PREFIX
				+ TruststoreProperties.PROP_KS_TYPE, "JKS");
		p.setProperty(ServerSecurityProperties.PREFIX
				+ TruststoreProperties.DEFAULT_PREFIX
				+ TruststoreProperties.PROP_KS_PASSWORD, "emi");
		
		//using file based mechanism
	    p.put(ServerSecurityProperties.PREFIX+ServerSecurityProperties.PROP_AIP_ORDER,"FILE");
	    p.put(ServerSecurityProperties.PREFIX+ServerSecurityProperties.PROP_AIP_PREFIX+".FILE.class",FileAttributeSource.class.getName());
	    
	    p.put(ServerSecurityProperties.PREFIX+ServerSecurityProperties.PROP_AIP_PREFIX+".FILE.file","src/test/resources/conf/users/testUdb-strict.xml");
	    p.put(ServerSecurityProperties.PREFIX+ServerSecurityProperties.PROP_AIP_PREFIX+".FILE.matching","strict");
	    
//	    p.put(ServerSecurityProperties.PREFIX+ServerSecurityProperties.PROP_AIP_PREFIX+".FILE.file","src/test/resources/conf/users/testUdb-regexp.xml");
//	    p.put(ServerSecurityProperties.PREFIX+ServerSecurityProperties.PROP_AIP_PREFIX+".FILE.matching","regexp");
	    
	    p.put(ServerSecurityProperties.PREFIX+ServerSecurityProperties.PROP_CHECKACCESS_PDPCONFIG, "src/test/resources/conf/xacml2.config");
	    p.put(ServerSecurityProperties.PREFIX+ServerSecurityProperties.PROP_CHECKACCESS_PDP, LocalHerasafPDP.class.getName());

	    // using simplified acl mechanism
//	    p.put(ServerSecurityProperties.PREFIX+ServerSecurityProperties.PROP_CHECKACCESS_ACL, "src/test/resources/conf/emir.acl");    
	}
	
	
	public static ClientSecurityProperties getSecurityProperties_2(){
		Properties p = new Properties();
		// keystore setting
		p.setProperty(ClientSecurityProperties.PREFIX
				+ CredentialProperties.DEFAULT_PREFIX
				+ CredentialProperties.PROP_PASSWORD, "emi");
		p.setProperty(ClientSecurityProperties.PREFIX
				+ CredentialProperties.DEFAULT_PREFIX
				+ CredentialProperties.PROP_FORMAT,
				CredentialProperties.CredentialFormat.pkcs12.toString());
		p.setProperty(ClientSecurityProperties.PREFIX
				+ CredentialProperties.DEFAULT_PREFIX
				+ CredentialProperties.PROP_LOCATION,
				"src/test/resources/certs/demo-user.p12");

		p.setProperty(ClientSecurityProperties.PREFIX
				+ TruststoreProperties.DEFAULT_PREFIX
				+ TruststoreProperties.PROP_TYPE,
				TruststoreProperties.TruststoreType.keystore.toString());
		p.setProperty(ClientSecurityProperties.PREFIX
				+ TruststoreProperties.DEFAULT_PREFIX
				+ TruststoreProperties.PROP_KS_PATH,
				"src/test/resources/certs/demo-server.jks");
		p.setProperty(ClientSecurityProperties.PREFIX
				+ TruststoreProperties.DEFAULT_PREFIX
				+ TruststoreProperties.PROP_KS_TYPE, "JKS");
		p.setProperty(ClientSecurityProperties.PREFIX
				+ TruststoreProperties.DEFAULT_PREFIX
				+ TruststoreProperties.PROP_KS_PASSWORD, "emi");
		
		AuthnAndTrustProperties authn = new AuthnAndTrustProperties(p, ClientSecurityProperties.PREFIX
				+ TruststoreProperties.DEFAULT_PREFIX, ClientSecurityProperties.PREFIX
				+ CredentialProperties.DEFAULT_PREFIX);
		
		ClientSecurityProperties csp = new ClientSecurityProperties(p, authn); 
		
		return csp;
	}
	
	public static ClientSecurityProperties getSecurityProperties_3(){
		Properties p = new Properties();
		// keystore setting
		p.setProperty(ClientSecurityProperties.PREFIX
				+ CredentialProperties.DEFAULT_PREFIX
				+ CredentialProperties.PROP_PASSWORD, "emi");
		p.setProperty(ClientSecurityProperties.PREFIX
				+ CredentialProperties.DEFAULT_PREFIX
				+ CredentialProperties.PROP_FORMAT,
				CredentialProperties.CredentialFormat.pkcs12.toString());
		p.setProperty(ClientSecurityProperties.PREFIX
				+ CredentialProperties.DEFAULT_PREFIX
				+ CredentialProperties.PROP_LOCATION,
				"src/test/resources/certs/demo-user-2.p12");

		p.setProperty(ClientSecurityProperties.PREFIX
				+ TruststoreProperties.DEFAULT_PREFIX
				+ TruststoreProperties.PROP_TYPE,
				TruststoreProperties.TruststoreType.keystore.toString());
		p.setProperty(ClientSecurityProperties.PREFIX
				+ TruststoreProperties.DEFAULT_PREFIX
				+ TruststoreProperties.PROP_KS_PATH,
				"src/test/resources/certs/demo-server.jks");
		p.setProperty(ClientSecurityProperties.PREFIX
				+ TruststoreProperties.DEFAULT_PREFIX
				+ TruststoreProperties.PROP_KS_TYPE, "JKS");
		p.setProperty(ClientSecurityProperties.PREFIX
				+ TruststoreProperties.DEFAULT_PREFIX
				+ TruststoreProperties.PROP_KS_PASSWORD, "emi");
		
		AuthnAndTrustProperties authn = new AuthnAndTrustProperties(p, ClientSecurityProperties.PREFIX
				+ TruststoreProperties.DEFAULT_PREFIX, ClientSecurityProperties.PREFIX
				+ CredentialProperties.DEFAULT_PREFIX);
		
		ClientSecurityProperties csp = new ClientSecurityProperties(p, authn); 
		
		return csp;
	}
	

	private static void setGeneralSettings(Properties p) {
		p.put(ServerProperties.PREFIX+ServerProperties.PROP_ADDRESS, "https://localhost:0");		
		
	}

	@AfterClass
	public static void stopServer() {
		try {
			stopMongoDB();
		} catch (InterruptedException e) {
			e.printStackTrace();
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

}
