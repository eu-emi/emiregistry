package eu.emi.emir.p2p;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.Properties;

import javax.ws.rs.core.MediaType;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.ClientResponse.Status;

import eu.emi.client.DSRClient;
import eu.emi.client.ServiceBasicAttributeNames;
import eu.emi.emir.DSRServer;
import eu.emi.emir.TestRegistryBase;
import eu.emi.emir.core.Configuration;
import eu.emi.emir.core.ServerConstants;
import eu.emi.emir.db.mongodb.MongoDBServiceDatabase;
import eu.emi.emir.infrastructure.InputFilter;
import eu.emi.emir.p2p.NeighborsManager;
import eu.emi.emir.util.DateUtil;
import eu.emi.emir.util.ServiceUtil;

public class TestNeighborsManager {
	DSRServer s = null;
	private static String URL = "http://localhost:54321";
	private static String dbHostname = "localhost";
	private static int    dbPort = 27017;
	private static String dbName = "emiregistry";
	private static String dbCollectionName = "services-test";
	
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    	ServiceUtil.initLogger("src/test/resources/conf/log4j.properties");
		final MongoDBServiceDatabase childDB = new MongoDBServiceDatabase(
 				dbHostname, dbPort, dbName, dbCollectionName);
		childDB.deleteAll();
	}
    
	@Before
	public void setup() throws Exception{
		setUpBeforeClass();
		
		TestRegistryBase.startMongoDB();
		Properties serverProps = new Properties();
		serverProps.put(ServerConstants.REGISTRY_GLOBAL_ENABLE, "true");
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
		// Clear the hash table
		NeighborsManager.getInstance().hashClear();
		Thread.sleep(1000);
	}
	
	@Test
	public void noNeighborsTest() throws JSONException, InterruptedException {
		// Neighbors check
		DSRClient client = new DSRClient(URL + "/neighbors");
		ClientResponse res = client.getClientResource()
				.accept(MediaType.APPLICATION_JSON_TYPE)
					.get(ClientResponse.class);

		assertTrue(res.getStatus() == Status.OK.getStatusCode());		
		JSONArray jos = res.getEntity(JSONArray.class);
		
		assertNotNull(jos);
		System.out.println(jos.toString());
		assertTrue(jos.length()==1);
		assertTrue(jos.get(0).toString().equals(URL));
	}

	@Test
	public void oneNeighborsTest() throws JSONException {
		DSRClient sc = new DSRClient(URL+"/serviceadmin");
		WebResource selfRegisterClient = sc.getClientResource();
		
		for (int i=0; i<1; i++){
			// Fill the infos
			JSONObject myInfos = new JSONObject();
			try {
				myInfos.put("Service_Endpoint_URL", "http://gsr"+i+".hu");
				myInfos.put("Service_Name", "IndexService");
				myInfos.put("Service_Type", "GSR");
				// current time and last update should be same in the beginning
				JSONObject date = new JSONObject();
				date.put("$date", ServiceUtil.toUTCFormat(new Date()));
				myInfos.put(ServiceBasicAttributeNames.SERVICE_CREATED_ON
						.getAttributeName(), date);
				myInfos.put(ServiceBasicAttributeNames.SERVICE_EXPIRE_ON
						.getAttributeName(), date);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	
			DateUtil.setExpiryTime(myInfos, 3);
			JSONArray message = new JSONArray();
			message.put(myInfos);
			ClientResponse res = selfRegisterClient.accept(MediaType.APPLICATION_JSON_TYPE)
					.post(ClientResponse.class, message);
		}
		
		// Neighbors check
		DSRClient client = new DSRClient(URL + "/neighbors");
		ClientResponse res = client.getClientResource()
				.accept(MediaType.APPLICATION_JSON_TYPE)
					.get(ClientResponse.class);

		assertTrue(res.getStatus() == Status.OK.getStatusCode());		
		JSONArray jos = res.getEntity(JSONArray.class);
		
		assertNotNull(jos);
		System.out.println(jos.toString());
		assertTrue(jos.length()==1);
		assertTrue(jos.get(0).toString().equals("http://gsr0.hu"));
	}

	@Test
	public void twoNeighborsTest() throws JSONException {
		DSRClient sc = new DSRClient(URL+"/serviceadmin");
		WebResource selfRegisterClient = sc.getClientResource();
		
		for (int i=0; i<2; i++){
			// Fill the infos
			JSONObject myInfos = new JSONObject();
			try {
				myInfos.put("Service_Endpoint_URL", "http://gsr"+i+".hu");
				myInfos.put("Service_Name", "IndexService");
				myInfos.put("Service_Type", "GSR");
				// current time and last update should be same in the beginning
				JSONObject date = new JSONObject();
				date.put("$date", ServiceUtil.toUTCFormat(new Date()));
				myInfos.put(ServiceBasicAttributeNames.SERVICE_CREATED_ON
						.getAttributeName(), date);
				myInfos.put(ServiceBasicAttributeNames.SERVICE_EXPIRE_ON
						.getAttributeName(), date);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	
			DateUtil.setExpiryTime(myInfos, 3);
			JSONArray message = new JSONArray();
			message.put(myInfos);
			ClientResponse res = selfRegisterClient.accept(MediaType.APPLICATION_JSON_TYPE)
					.post(ClientResponse.class, message);
		}
		
		// Neighbors check
		DSRClient client = new DSRClient(URL + "/neighbors");
		ClientResponse res = client.getClientResource()
				.accept(MediaType.APPLICATION_JSON_TYPE)
					.get(ClientResponse.class);

		assertTrue(res.getStatus() == Status.OK.getStatusCode());		
		JSONArray jos = res.getEntity(JSONArray.class);
		
		assertNotNull(jos);
		System.out.println(jos.toString());
		assertTrue(jos.length()==2);
		assertTrue(jos.get(0).toString().equals("http://gsr1.hu"));
		assertTrue(jos.get(1).toString().equals("http://gsr0.hu"));
	}

	@Test
	public void threeNeighborsTest() throws JSONException {
		DSRClient sc = new DSRClient(URL+"/serviceadmin");
		WebResource selfRegisterClient = sc.getClientResource();
		
		for (int i=0; i<4; i++){
			// Fill the infos
			JSONObject myInfos = new JSONObject();
			try {
				myInfos.put("Service_Endpoint_URL", "http://gsr"+i+".hu");
				myInfos.put("Service_Name", "IndexService");
				myInfos.put("Service_Type", "GSR");
				// current time and last update should be same in the beginning
				JSONObject date = new JSONObject();
				date.put("$date", ServiceUtil.toUTCFormat(new Date()));
				myInfos.put(ServiceBasicAttributeNames.SERVICE_CREATED_ON
						.getAttributeName(), date);
				myInfos.put(ServiceBasicAttributeNames.SERVICE_EXPIRE_ON
						.getAttributeName(), date);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	
			DateUtil.setExpiryTime(myInfos, 3);
			JSONArray message = new JSONArray();
			message.put(myInfos);
			ClientResponse res = selfRegisterClient.accept(MediaType.APPLICATION_JSON_TYPE)
					.post(ClientResponse.class, message);
		}
		
		// Neighbors check
		DSRClient client = new DSRClient(URL + "/neighbors");
		ClientResponse res = client.getClientResource()
				.accept(MediaType.APPLICATION_JSON_TYPE)
					.get(ClientResponse.class);

		assertTrue(res.getStatus() == Status.OK.getStatusCode());		
		JSONArray jos = res.getEntity(JSONArray.class);
		
		assertNotNull(jos);
		System.out.println(jos.toString());
		assertTrue(jos.length()==3);
		assertTrue(jos.get(0).toString().equals("http://gsr2.hu"));
		assertTrue(jos.get(1).toString().equals("http://gsr3.hu"));
		assertTrue(jos.get(2).toString().equals("http://gsr0.hu"));
	}

	@Test
	public void fourNeighborsTest() throws JSONException {
		DSRClient sc = new DSRClient(URL+"/serviceadmin");
		WebResource selfRegisterClient = sc.getClientResource();
		
		for (int i=0; i<8; i++){
			// Fill the infos
			JSONObject myInfos = new JSONObject();
			try {
				myInfos.put("Service_Endpoint_URL", "http://gsr"+i+".hu");
				myInfos.put("Service_Name", "IndexService");
				myInfos.put("Service_Type", "GSR");
				// current time and last update should be same in the beginning
				JSONObject date = new JSONObject();
				date.put("$date", ServiceUtil.toUTCFormat(new Date()));
				myInfos.put(ServiceBasicAttributeNames.SERVICE_CREATED_ON
						.getAttributeName(), date);
				myInfos.put(ServiceBasicAttributeNames.SERVICE_EXPIRE_ON
						.getAttributeName(), date);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	
			DateUtil.setExpiryTime(myInfos, 3);
			JSONArray message = new JSONArray();
			message.put(myInfos);
			ClientResponse res = selfRegisterClient.accept(MediaType.APPLICATION_JSON_TYPE)
					.post(ClientResponse.class, message);
		}
		
		// Neighbors check
		DSRClient client = new DSRClient(URL + "/neighbors");
		ClientResponse res = client.getClientResource()
				.accept(MediaType.APPLICATION_JSON_TYPE)
					.get(ClientResponse.class);

		assertTrue(res.getStatus() == Status.OK.getStatusCode());		
		JSONArray jos = res.getEntity(JSONArray.class);
		
		assertNotNull(jos);
		System.out.println(jos.toString());
		assertTrue(jos.length()==4);
		assertTrue(jos.get(0).toString().equals("http://gsr2.hu"));
		assertTrue(jos.get(1).toString().equals("http://gsr6.hu"));
		assertTrue(jos.get(2).toString().equals("http://gsr5.hu"));
		assertTrue(jos.get(3).toString().equals("http://gsr7.hu"));
	}

	@After
	public void cleanUp() throws InterruptedException{
		s.stopJetty();
		TestRegistryBase.stopMongoDB();
	}
}
