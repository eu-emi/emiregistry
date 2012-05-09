/**
 * 
 */
package eu.emi.emir.infrastructure;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import javax.ws.rs.core.MediaType;


import org.apache.commons.io.FileUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.ClientResponse.Status;

import eu.emi.client.DSRClient;
import eu.emi.client.ServiceBasicAttributeNames;
import eu.emi.emir.DSRServer;
import eu.emi.emir.TestRegistryBase;
import eu.emi.emir.core.Configuration;
import eu.emi.emir.core.ServerConstants;
import eu.emi.emir.db.mongodb.MongoDBServiceDatabase;
import eu.emi.emir.infrastructure.InputFilter;
import eu.emi.emir.util.DateUtil;
import eu.emi.emir.util.ServiceUtil;
import eu.unicore.bugsreporter.annotation.FunctionalTest;

/**
 * @author a.memon
 * @author g.szigeti
 *
 */
public class TestServiceRecordFilters {
	DSRServer s = null;
	private static String URL = "http://localhost:54321";
	private static String dbHostname = "localhost";
	private static int    dbPort = 27017;
	private static String dbName = "emiregistry";
	private static String dbCollectionName = "services-test";
	
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    	//should start the db before making any operations 
    	TestRegistryBase.startMongoDB();
    	ServiceUtil.initLogger("src/test/resources/conf/log4j.properties");
 		final MongoDBServiceDatabase parentDB = new MongoDBServiceDatabase(
 				dbHostname, dbPort, dbName, dbCollectionName);
		parentDB.deleteAll();
	}
    
	@Before
	public void setup() throws IOException, InterruptedException{
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
		serverProps.put(ServerConstants.MONGODB_HOSTNAME, dbHostname);
		serverProps.put(ServerConstants.MONGODB_PORT, dbPort);
		serverProps.put(ServerConstants.MONGODB_COLLECTION_NAME, dbCollectionName);
		serverProps.put(ServerConstants.MONGODB_DB_NAME, dbName);
		serverProps.put(ServerConstants.MONGODB_COL_CREATE, "true");
		serverProps.put(ServerConstants.REGISTRY_FILTERS_REQUEST, InputFilter.class.getName());
		serverProps.put(ServerConstants.REGISTRY_FILTERS_INPUTFILEPATH, "src/test/resources/conf/inputfilters_for_test");
		serverProps.put(ServerConstants.REGISTRY_FILTERS_OUTPUTFILEPATH, "src/test/resources/conf/outputfilters_for_test");
		Configuration c = new Configuration(serverProps);
		s = new DSRServer(c);
		s.startJetty();
	}
	
	
	@Test
	public void testInputFilterSimpleValue() throws JSONException, IOException{
		DSRClient c = new DSRClient(URL+"/serviceadmin");
		
		// simple value filter test
		JSONObject jo = new JSONObject(FileUtils.readFileToString(new File("src/test/resources/json/serviceinfo.json")));
		jo = DateUtil.setExpiryTime(jo, 12);
		jo.remove("Service_Endpoint_URL");
		jo.put("Service_Endpoint_URL", "http://url_from_black_list.no");
		JSONArray jos = new JSONArray();
		jos.put(jo);
		
		ClientResponse res = c.getClientResource().accept(MediaType.APPLICATION_JSON_TYPE)
				.post(ClientResponse.class, jos);
		assertTrue(res.getStatus() == Status.OK.getStatusCode());
		
		//Registration check
		try {
			@SuppressWarnings("unused")
			JSONObject responseJO = c.getClientResource().queryParam(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL.getAttributeName(), "http://url_from_black_list.no")
				.accept(MediaType.APPLICATION_JSON_TYPE).get(JSONObject.class);
			fail("Filter is not working because the server returned with a valid entry.");
		} catch (UniformInterfaceException e) {
			assertTrue("Filter is working.",true);
		}
	}

	@Test
	@FunctionalTest(id="RunInputFilterTest", description="Test Filtering of incoming service records")
	public void testInputFilterJSONArrayValue() throws JSONException, IOException{
		DSRClient c = new DSRClient(URL+"/serviceadmin");
		
		// simple JSONArray value filter test
		JSONObject jo = new JSONObject(FileUtils.readFileToString(new File("src/test/resources/json/serviceinfo.json")));
		jo = DateUtil.setExpiryTime(jo, 12);
		JSONArray jos = new JSONArray();
		jos.put(jo);
		
		ClientResponse res = c.getClientResource().accept(MediaType.APPLICATION_JSON_TYPE)
				.post(ClientResponse.class, jos);
		assertTrue(res.getStatus() == Status.OK.getStatusCode());
		
		//Registration check
		try {
			@SuppressWarnings("unused")
			JSONObject responseJO = c.getClientResource().queryParam(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL.getAttributeName(), "http://1")
				.accept(MediaType.APPLICATION_JSON_TYPE).get(JSONObject.class);
			fail("Filter is not working because the server returned with a valid entry.");
		} catch (UniformInterfaceException e) {
			assertTrue("Filter is working.",true);
		}
	}

	@Test
	public void testInputFilterArrayValue() throws JSONException, IOException{
		DSRClient c = new DSRClient(URL+"/serviceadmin");
		
		// simple JSONArray value filter test
		JSONObject jo = new JSONObject(FileUtils.readFileToString(new File("src/test/resources/json/serviceinfo2.json")));
		jo = DateUtil.setExpiryTime(jo, 12);
		JSONArray jos = new JSONArray();
		jos.put(jo);
		
		ClientResponse res = c.getClientResource().accept(MediaType.APPLICATION_JSON_TYPE)
				.post(ClientResponse.class, jos);
		assertTrue(res.getStatus() == Status.OK.getStatusCode());
		
		//Registration check
		try {
			@SuppressWarnings("unused")
			JSONObject responseJO = c.getClientResource().queryParam(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL.getAttributeName(), "http://2")
				.accept(MediaType.APPLICATION_JSON_TYPE).get(JSONObject.class);
			fail("Filter is not working because the server returned with a valid entry.");
		} catch (UniformInterfaceException e) {
			assertTrue("Filter is working.",true);
		}
	}

	@Test
	@FunctionalTest(id="RunOutputFilterTest", description="Test Filtering of outgoing service records")
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
