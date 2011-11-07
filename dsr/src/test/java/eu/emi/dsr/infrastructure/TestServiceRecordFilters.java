/**
 * 
 */
package eu.emi.dsr.infrastructure;

import java.io.IOException;
import java.util.Properties;


import org.codehaus.jettison.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.emi.client.DSRClient;
import eu.emi.dsr.DSRServer;
import eu.emi.dsr.TestRegistryBase;
import eu.emi.dsr.core.Configuration;
import eu.emi.dsr.core.ServerConstants;

/**
 * @author a.memon
 *
 */
public class TestServiceRecordFilters {
	DSRServer s = null;
	private static String URL = "http://localhost:54321";
	@Before
	public void setup() throws IOException{
		TestRegistryBase.startMongoDB();
		Properties serverProps = new Properties();
		serverProps.put(ServerConstants.REGISTRY_HOSTNAME, "localhost");
		serverProps.put(ServerConstants.REGISTRY_PORT, "54321");
		serverProps.put(ServerConstants.REGISTRY_SCHEME, "http");
		serverProps.put(ServerConstants.JETTY_LOW_RESOURCE_MAXIDLETIME, "10000");
		serverProps.put(ServerConstants.JETTY_LOWTHREADS, "50");
		serverProps.put(ServerConstants.JETTY_MAXIDLETIME, "30000");
		serverProps.put(ServerConstants.JETTY_MAXTHREADS, "1000");
		serverProps.put(ServerConstants.LOGGER_CONF_PATH,
				"src/test/resources/conf/log4j.properties");
		serverProps.put(ServerConstants.MONGODB_HOSTNAME, "localhost");
		serverProps.put(ServerConstants.MONGODB_PORT, "27017");
		serverProps.put(ServerConstants.MONGODB_COLLECTION_NAME, "services-test");
		serverProps.put(ServerConstants.MONGODB_DB_NAME, "emiregistry");
		serverProps.put(ServerConstants.MONGODB_COL_CREATE, "true");
		serverProps.put(ServerConstants.REGISTRY_FILTERS_REQUEST, InputFilter.class.getName());
		serverProps.put(ServerConstants.REGISTRY_FILTERS_INPUTFILEPATH, "src/main/resources/conf/inputfilters");
		serverProps.put(ServerConstants.REGISTRY_FILTERS_RESPONSE, OutputFilter.class.getName());
		serverProps.put(ServerConstants.REGISTRY_FILTERS_OUTPUTFILEPATH, "src/main/resources/conf/outputfilters");
		Configuration c = new Configuration(serverProps);
		s = new DSRServer(c);
		s.startJetty();
	}
	
	
	@Test
	public void testInputFilter(){
		//TODO add more functional code here
		DSRClient c = new DSRClient(URL+"/ping");
		System.out.println(c.getClientResource().get(JSONObject.class));
	}
	
	@Test
	public void testOutputFilter(){
		//TODO add more functional code here
		DSRClient c = new DSRClient(URL+"/ping");
		System.out.println(c.getClientResource().get(JSONObject.class));
	}
	
	@After
	public void cleanUp() throws InterruptedException{
		s.stopJetty();
		TestRegistryBase.stopMongoDB();
	}
	
	
	
	
	
}
