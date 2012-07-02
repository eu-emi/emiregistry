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

import eu.emi.emir.EMIRServer;
import eu.emi.emir.ServerProperties;
import eu.emi.emir.TestRegistryBase;
import eu.emi.emir.client.EMIRClient;
import eu.emi.emir.client.ServiceBasicAttributeNames;
import eu.emi.emir.db.mongodb.MongoDBServiceDatabase;
import eu.emi.emir.p2p.NeighborsManager;
import eu.emi.emir.util.DateUtil;
import eu.emi.emir.util.ServiceUtil;

public class TestNeighborsManager {
	EMIRServer s = null;
	private static String URL = "http://localhost:54321";
	private static String dbHostname = "localhost";
	private static int dbPort = 27017;
	private static String dbName = "emiregistry";
	private static String dbCollectionName = "services-test";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		final MongoDBServiceDatabase childDB = new MongoDBServiceDatabase(
				dbHostname, dbPort, dbName, dbCollectionName);
		childDB.deleteAll();
	}

	@Before
	public void setup() throws Exception {
		setUpBeforeClass();

		TestRegistryBase.startMongoDB();
		Properties serverProps = new Properties();
		serverProps.put(ServerProperties.PROP_GLOBAL_ENABLE, "true");
		serverProps.put(ServerProperties.PROP_ADDRESS, URL);

		serverProps.put(ServerProperties.PROP_MONGODB_HOSTNAME, dbHostname);
		serverProps.put(ServerProperties.PROP_MONGODB_PORT, dbPort);
		serverProps.put(ServerProperties.PROP_MONGODB_DB_NAME, dbName);
		serverProps.put(ServerProperties.PROP_MONGODB_COLLECTION_NAME,
				dbCollectionName);
		
		serverProps.put(ServerProperties.PROP_RECORD_BLOCKLIST_INCOMING,
				"src/test/resources/conf/inputfilters_for_test");
		serverProps.put(ServerProperties.PROP_RECORD_BLOCKLIST_OUTGOING,
				"src/test/resources/conf/outputfilters_for_test");
		
		s = new EMIRServer();
		s.run(serverProps);
		// Clear the hash table
		NeighborsManager.getInstance().hashClear();
		Thread.sleep(1000);
	}

	@Test
	public void noNeighborsTest() throws JSONException, InterruptedException {
		// Neighbors check
		EMIRClient client = new EMIRClient(URL + "/neighbors");
		ClientResponse res = client.getClientResource()
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.get(ClientResponse.class);

		assertTrue(res.getStatus() == Status.OK.getStatusCode());
		JSONArray jos = res.getEntity(JSONArray.class);

		assertNotNull(jos);
		System.out.println(jos.toString());
		assertTrue(jos.length() == 1);
		assertTrue(jos.get(0).toString().equals(URL));
	}

	@Test
	public void oneNeighborsTest() throws JSONException {
		EMIRClient sc = new EMIRClient(URL + "/serviceadmin");
		WebResource selfRegisterClient = sc.getClientResource();

		for (int i = 0; i < 1; i++) {
			// Fill the infos
			JSONObject myInfos = new JSONObject();
			try {
				myInfos.put("Service_Endpoint_URL", "http://gsr" + i + ".hu");
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
			@SuppressWarnings("unused")
			ClientResponse res = selfRegisterClient.accept(
					MediaType.APPLICATION_JSON_TYPE).post(ClientResponse.class,
					message);
		}

		// Neighbors check
		EMIRClient client = new EMIRClient(URL + "/neighbors");
		ClientResponse res = client.getClientResource()
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.get(ClientResponse.class);

		assertTrue(res.getStatus() == Status.OK.getStatusCode());
		JSONArray jos = res.getEntity(JSONArray.class);

		assertNotNull(jos);
		System.out.println(jos.toString());
		assertTrue(jos.length() == 1);
		assertTrue(jos.get(0).toString().equals("http://gsr0.hu"));
	}

	@Test
	public void twoNeighborsTest() throws JSONException {
		EMIRClient sc = new EMIRClient(URL + "/serviceadmin");
		WebResource selfRegisterClient = sc.getClientResource();

		for (int i = 0; i < 2; i++) {
			// Fill the infos
			JSONObject myInfos = new JSONObject();
			try {
				myInfos.put("Service_Endpoint_URL", "http://gsr" + i + ".hu");
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
			@SuppressWarnings("unused")
			ClientResponse res = selfRegisterClient.accept(
					MediaType.APPLICATION_JSON_TYPE).post(ClientResponse.class,
					message);
		}

		// Neighbors check
		EMIRClient client = new EMIRClient(URL + "/neighbors");
		ClientResponse res = client.getClientResource()
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.get(ClientResponse.class);

		assertTrue(res.getStatus() == Status.OK.getStatusCode());
		JSONArray jos = res.getEntity(JSONArray.class);

		assertNotNull(jos);
		System.out.println(jos.toString());
		assertTrue(jos.length() == 2);
		assertTrue(jos.get(0).toString().equals("http://gsr1.hu"));
		assertTrue(jos.get(1).toString().equals("http://gsr0.hu"));
	}

	@Test
	public void threeNeighborsTest() throws JSONException {
		EMIRClient sc = new EMIRClient(URL + "/serviceadmin");
		WebResource selfRegisterClient = sc.getClientResource();

		for (int i = 0; i < 4; i++) {
			// Fill the infos
			JSONObject myInfos = new JSONObject();
			try {
				myInfos.put("Service_Endpoint_URL", "http://gsr" + i + ".hu");
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
			@SuppressWarnings("unused")
			ClientResponse res = selfRegisterClient.accept(
					MediaType.APPLICATION_JSON_TYPE).post(ClientResponse.class,
					message);
		}

		// Neighbors check
		EMIRClient client = new EMIRClient(URL + "/neighbors");
		ClientResponse res = client.getClientResource()
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.get(ClientResponse.class);

		assertTrue(res.getStatus() == Status.OK.getStatusCode());
		JSONArray jos = res.getEntity(JSONArray.class);

		assertNotNull(jos);
		System.out.println(jos.toString());
		assertTrue(jos.length() == 3);
		assertTrue(jos.get(0).toString().equals("http://gsr2.hu"));
		assertTrue(jos.get(1).toString().equals("http://gsr3.hu"));
		assertTrue(jos.get(2).toString().equals("http://gsr0.hu"));
	}

	@Test
	public void fourNeighborsTest() throws JSONException {
		EMIRClient sc = new EMIRClient(URL + "/serviceadmin");
		WebResource selfRegisterClient = sc.getClientResource();

		for (int i = 0; i < 8; i++) {
			// Fill the infos
			JSONObject myInfos = new JSONObject();
			try {
				myInfos.put("Service_Endpoint_URL", "http://gsr" + i + ".hu");
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
			@SuppressWarnings("unused")
			ClientResponse res = selfRegisterClient.accept(
					MediaType.APPLICATION_JSON_TYPE).post(ClientResponse.class,
					message);
		}

		// Neighbors check
		EMIRClient client = new EMIRClient(URL + "/neighbors");
		ClientResponse res = client.getClientResource()
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.get(ClientResponse.class);

		assertTrue(res.getStatus() == Status.OK.getStatusCode());
		JSONArray jos = res.getEntity(JSONArray.class);

		assertNotNull(jos);
		System.out.println(jos.toString());
		assertTrue(jos.length() == 4);
		assertTrue(jos.get(0).toString().equals("http://gsr2.hu"));
		assertTrue(jos.get(1).toString().equals("http://gsr6.hu"));
		assertTrue(jos.get(2).toString().equals("http://gsr5.hu"));
		assertTrue(jos.get(3).toString().equals("http://gsr7.hu"));
	}

	@After
	public void cleanUp() throws InterruptedException {
		s.stop();
		TestRegistryBase.stopMongoDB();
	}
}
