/**
 * 
 */
package eu.emi.emir.infrastructure;

import java.util.Properties;

import eu.emi.emir.client.security.ISecurityProperties;
import eu.emi.emir.core.Configuration;
import eu.emi.emir.core.ServerConstants;

/**
 * @author a.memon
 *
 */
public abstract class AbstractServer {
	protected String url;
	public abstract void start();
	 
	/**
	 * @param b
	 * @param mongodbName
	 * @param mongodbPort
	 * @param mongodbHostName
	 * @param jettyPort
	 * @param jettyHostname
	 * @return
	 */
	protected Configuration getConfiguration(String jettyHostname,
			int jettyPort, String mongodbHostName, int mongodbPort,
			String mongodbName, boolean secure, String parenturl,
			String h2path) {
		Properties p = new Properties();
		p.put(ServerConstants.REGISTRY_HOSTNAME, jettyHostname);
		p.put(ServerConstants.REGISTRY_PORT, ""+jettyPort);

		if (!secure) {
			p.put(ServerConstants.REGISTRY_SCHEME, "http");
			// set certificate properties
		} else {
			p.put(ServerConstants.REGISTRY_SCHEME, "https");
			p.put(ISecurityProperties.REGISTRY_ACL_FILE, "src/test/resources/conf/emir.acl");
			
			p.put(ISecurityProperties.REGISTRY_SSL_KEYSTORE, "src/test/resources/certs/demo-server.p12");
			p.put(ISecurityProperties.REGISTRY_SSL_KEYTYPE, "pkcs12");
			p.put(ISecurityProperties.REGISTRY_SSL_KEYPASS, "emi");

			p.put(ISecurityProperties.REGISTRY_SSL_TRUSTSTORE, "src/test/resources/certs/demo-server.jks");
			p.put(ISecurityProperties.REGISTRY_SSL_TRUSTPASS, "emi");
			p.put(ISecurityProperties.REGISTRY_SSL_TRUSTTYPE, "jks");

			//		registry.ssl.clientauthentication=true
			p.put(ISecurityProperties.REGISTRY_SSL_CLIENTAUTH, "true");


		}
		// When you would like to use GSR
		p.put(ServerConstants.REGISTRY_GLOBAL_ENABLE, "true");
		//p.put(ServerConstants.REGISTRY_GLOBAL_SOFTSTATE_DELAY, "-1");
		p.put(ServerConstants.REGISTRY_GLOBAL_PROVIDERLIST, "http://valami.hu/lista, http://emi.eu/EMIR.txt");
		//End of the GSR section
		
		p.put(ServerConstants.JETTY_LOW_RESOURCE_MAXIDLETIME, "10000");
		p.put(ServerConstants.JETTY_LOWTHREADS, "50");
		p.put(ServerConstants.JETTY_MAXIDLETIME, "30000");
		p.put(ServerConstants.JETTY_MAXTHREADS, "255");
		p.put(ServerConstants.LOGGER_CONF_PATH,
				"src/test/resources/conf/log4j.properties");
		p.put(ServerConstants.MONGODB_HOSTNAME, mongodbHostName);
		p.put(ServerConstants.MONGODB_PORT, ""+mongodbPort);
		p.put(ServerConstants.MONGODB_COLLECTION_NAME, "services-test");
		p.put(ServerConstants.MONGODB_DB_NAME, mongodbName);
		p.put(ServerConstants.REGISTRY_EXPIRY_MAXIMUM, "1000");
		p.put(ServerConstants.REGISTRY_EXPIRY_DEFAULT, "5");
		if (parenturl != null) {
			p.put(ServerConstants.REGISTRY_PARENT_URL, parenturl);	
		}
		
		p.put(ServerConstants.H2_DBFILE_PATH, h2path);
		p.put(ServerConstants.REGISTRY_FILTERS_REQUEST, "eu.emi.dsr.infrastructure.InputFilter");
		p.put(ServerConstants.REGISTRY_FILTERS_INPUTFILEPATH, "src/test/resources/conf/inputfilters");
		p.put(ServerConstants.REGISTRY_FILTERS_OUTPUTFILEPATH, "src/test/resources/conf/outputfilters");

		Configuration c = new Configuration(p);
		return c;
	}
	
	public String getUrl(){
		return url;
	}
}
