/**
 * 
 */
package eu.emi.dsr.performance;

import java.util.Properties;

import eu.emi.dsr.DSRServer;
import eu.emi.dsr.core.Configuration;
import eu.emi.dsr.core.ServerConstants;

/**
 * @author a.memon
 *
 */
public class ServerStart {
	public static void main(String[] args) {
		Properties p = new Properties();
		p.put(ServerConstants.REGISTRY_HOSTNAME, "localhost");
		p.put(ServerConstants.REGISTRY_PORT, "54321");
		p.put(ServerConstants.REGISTRY_SCHEME, "http");
		p.put(ServerConstants.JETTY_LOW_RESOURCE_MAXIDLETIME, "10000");
		p.put(ServerConstants.JETTY_LOWTHREADS, "50");
		p.put(ServerConstants.JETTY_MAXIDLETIME, "30000");
		p.put(ServerConstants.JETTY_MAXTHREADS, "255");
//		p.put(ServerConstants.LOGGER_CONF_PATH,
//				"src/test/resources/conf/log4j.properties");
		p.put(ServerConstants.MONGODB_HOSTNAME, "localhost");
		p.put(ServerConstants.MONGODB_PORT, "27017");
		p.put(ServerConstants.MONGODB_COLLECTION_NAME, "services-loadcol");
		p.put(ServerConstants.MONGODB_DB_NAME, "emiregistry-loaddb");
		p.put(ServerConstants.MONGODB_COL_CREATE, "true");		
		Configuration conf = new Configuration(p);
		DSRServer server = new DSRServer(conf);
		server.startJetty();
	}
}
