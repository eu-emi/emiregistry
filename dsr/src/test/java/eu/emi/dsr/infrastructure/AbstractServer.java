/**
 * 
 */
package eu.emi.dsr.infrastructure;

import java.util.Properties;

import eu.emi.dsr.core.Configuration;
import eu.emi.dsr.core.ServerConstants;

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
		}

		p.put(ServerConstants.JETTY_LOW_RESOURCE_MAXIDLETIME, "10000");
		p.put(ServerConstants.JETTY_LOWTHREADS, "50");
		p.put(ServerConstants.JETTY_MAXIDLETIME, "30000");
		p.put(ServerConstants.JETTY_MAXTHREADS, "255");
		p.put(ServerConstants.LOGGER_CONF_PATH,
				"src/main/resources/log4j.properties");
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
		p.put(ServerConstants.REGISTRY_FILTERS_INPUTFILEPATH, "src/main/resources/conf/inputfilters");
		p.put(ServerConstants.REGISTRY_FILTERS_OUTPUTFILEPATH, "src/main/resources/conf/outputfilters");

		Configuration c = new Configuration(p);
		return c;
	}
	
	public String getUrl(){
		return url;
	}
}
