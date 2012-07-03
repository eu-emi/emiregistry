/**
 * 
 */
package eu.emi.emir.infrastructure;

import java.util.Properties;

import eu.emi.emir.ServerProperties;
import eu.emi.emir.security.ServerSecurityProperties;
import eu.unicore.security.canl.CredentialProperties;
import eu.unicore.security.canl.TruststoreProperties;

/**
 * @author a.memon
 * @author g.szigeti
 *
 */
public abstract class AbstractServer {
	public abstract void start();
	 
	/**
	 * @param jettyHostname
	 * @param jettyPort
	 * @param mongodbHostName
	 * @param mongodbPort
	 * @param mongodbName
	 * @param use Secure mode
	 * @param parentURL
	 * @param h2dbPath
	 * @return
	 */
	protected Properties getConfiguration(String jettyHostname,
			int jettyPort, String mongodbHostName, int mongodbPort,
			String mongodbName, boolean secure, String parenturl,
			String h2path) {
		Properties p = new Properties();

		if (!secure) {
			p.put(ServerProperties.PREFIX+ServerProperties.PROP_ADDRESS, "http://"+jettyHostname+":"+jettyPort);
		} else {
			// set secure properties
			p.put(ServerProperties.PREFIX+ServerProperties.PROP_ADDRESS, "https://"+jettyHostname+":"+jettyPort);
			p.put(ServerSecurityProperties.PREFIX+ServerSecurityProperties.PROP_CHECKACCESS_ACL, "src/test/resources/conf/emir.acl");

			// Credential options
			p.put(ServerSecurityProperties.PREFIX
					+ CredentialProperties.DEFAULT_PREFIX
					+ CredentialProperties.PROP_LOCATION, "src/test/resources/certs/demo-server.p12");
			p.put(ServerSecurityProperties.PREFIX
					+ CredentialProperties.DEFAULT_PREFIX
					+ CredentialProperties.PROP_FORMAT,
					CredentialProperties.CredentialFormat.pkcs12.toString());
			p.put(ServerSecurityProperties.PREFIX
					+ CredentialProperties.DEFAULT_PREFIX
					+ CredentialProperties.PROP_PASSWORD, "emi");

			// Trust store options
			p.put(ServerSecurityProperties.PREFIX
					+ TruststoreProperties.DEFAULT_PREFIX
					+ TruststoreProperties.PROP_TYPE, "keystore");
			p.put(ServerSecurityProperties.PREFIX
					+ TruststoreProperties.DEFAULT_PREFIX
					+ TruststoreProperties.PROP_KS_PATH, "src/test/resources/certs/demo-server.jks");
			p.put(ServerSecurityProperties.PREFIX
					+ TruststoreProperties.DEFAULT_PREFIX
					+ TruststoreProperties.PROP_KS_PASSWORD, "emi");
			p.put(ServerSecurityProperties.PREFIX
					+ TruststoreProperties.DEFAULT_PREFIX
					+ TruststoreProperties.PROP_KS_TYPE, "JKS");
		}

		// When you would like to use GSR
		//p.put(ServerProperties.PREFIX+ServerProperties.PROP_GLOBAL_ENABLE, "true");
		//p.put(ServerProperties.PREFIX+ServerProperties.PROP_GLOBAL_SOFTSTATE_DELAY, "-1");
		//p.put(ServerProperties.PREFIX+ServerProperties.PROP_GLOBAL_PROVIDERLIST, "http://valami.hu/lista, http://emi.eu/EMIR.txt");
		//End of the GSR section
		
		p.put(ServerProperties.PREFIX+ServerProperties.PROP_LOGGER_CONFIGPATH,
				"src/test/resources/conf/log4j.properties");
		p.put(ServerProperties.PREFIX+ServerProperties.PROP_MONGODB_HOSTNAME, mongodbHostName);
		p.put(ServerProperties.PREFIX+ServerProperties.PROP_MONGODB_PORT, ""+mongodbPort);
		p.put(ServerProperties.PREFIX+ServerProperties.PROP_MONGODB_COLLECTION_NAME, "services-test");
		p.put(ServerProperties.PREFIX+ServerProperties.PROP_MONGODB_DB_NAME, mongodbName);
		p.put(ServerProperties.PREFIX+ServerProperties.PROP_RECORD_EXPIRY_MAXIMUM, "1000");
		p.put(ServerProperties.PREFIX+ServerProperties.PROP_RECORD_EXPIRY_DEFAULT, "1");
		if (parenturl != null) {
			p.put(ServerProperties.PREFIX+ServerProperties.PROP_PARENT_ADDRESS, parenturl);	
		}
		
		p.put(ServerProperties.PREFIX+ServerProperties.PROP_H2_DBFILE_PATH, h2path);
		p.put(ServerProperties.PREFIX+ServerProperties.PROP_REQUEST_INTERCEPTORS, "eu.emi.emir.infrastructure.InputFilter");
		p.put(ServerProperties.PREFIX+ServerProperties.PROP_RECORD_BLOCKLIST_INCOMING, "src/test/resources/conf/inputfilters");
		p.put(ServerProperties.PREFIX+ServerProperties.PROP_RECORD_BLOCKLIST_OUTGOING, "src/test/resources/conf/outputfilters");

		return p;
	}
	
}
