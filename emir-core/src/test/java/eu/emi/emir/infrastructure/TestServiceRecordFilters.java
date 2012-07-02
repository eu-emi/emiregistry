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

import eu.emi.emir.EMIRServer;
import eu.emi.emir.ServerProperties;
import eu.emi.emir.TestRegistryBase;
import eu.emi.emir.client.EMIRClient;
import eu.emi.emir.client.ServiceBasicAttributeNames;
import eu.emi.emir.db.mongodb.MongoDBServiceDatabase;
import eu.emi.emir.util.DateUtil;
import eu.unicore.bugsreporter.annotation.FunctionalTest;

/**
 * @author a.memon
 * @author g.szigeti
 *
 */
public class TestServiceRecordFilters {
	static EMIRServer server = null;
	private static String URL = "http://localhost:54321";
	private static String dbHostname = "localhost";
	private static int    dbPort = 27017;
	private static String dbName = "emiregistry";
	private static String dbCollectionName = "services-test";
	
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    	//should start the db before making any operations 
    	TestRegistryBase.startMongoDB();
    	
 		final MongoDBServiceDatabase parentDB = new MongoDBServiceDatabase(
 				dbHostname, dbPort, dbName, dbCollectionName);
		parentDB.deleteAll();
		
	}
    
	@Before
	public void setup() throws IOException, InterruptedException{
		Properties props = new Properties();
		props = new Properties();
		props.put(ServerProperties.PREFIX+ServerProperties.PROP_ADDRESS, URL);
		props.put(ServerProperties.PREFIX+ServerProperties.PROP_RECORD_BLOCKLIST_INCOMING, "src/test/resources/conf/inputfilters_for_test");
		props.put(ServerProperties.PREFIX+ServerProperties.PROP_RECORD_BLOCKLIST_OUTGOING, "src/test/resources/conf/outputfilters_for_test");
		
		server = new EMIRServer();
		server.run(props);
	}
	
	
	@Test
	public void testInputFilterSimpleValue() throws JSONException, IOException{
		EMIRClient c = new EMIRClient(URL+"/serviceadmin");
		
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
		EMIRClient c = new EMIRClient(URL+"/serviceadmin");
		
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
		EMIRClient c = new EMIRClient(URL+"/serviceadmin");
		
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
		EMIRClient c = new EMIRClient(URL+"/ping");
		System.out.println(c.getClientResource().get(JSONObject.class));
	}
	
	@After
	public void cleanUp() throws InterruptedException{
		server.stop();
		TestRegistryBase.stopMongoDB();
	}
	
}
