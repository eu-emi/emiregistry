/**
 * 
 */
package eu.emi.dsr.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import javax.ws.rs.core.MediaType;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.emi.dsr.TestRegistryBase;
import eu.emi.dsr.client.DSRClient;
import eu.emi.dsr.core.ServiceBasicAttributeNames;
import eu.emi.dsr.db.ExistingResourceException;
import eu.emi.dsr.db.PersistentStoreFailureException;
import eu.emi.dsr.db.mongodb.MongoDBServiceDatabase;
import eu.emi.dsr.db.mongodb.ServiceObject;

/**
 * Integration test
 * 
 * @author a.memon
 * 
 */
public class TestServiceCollectionResource extends TestRegistryBase {

	public static MongoDBServiceDatabase db;

	@BeforeClass
	public static void setUp() throws JSONException, ExistingResourceException,
			PersistentStoreFailureException {
		db = new MongoDBServiceDatabase("localhost", 27017, "emiregistry",
				"services-test");
		db.deleteAll();
		for (int i = 0; i < 50; i++) {
			JSONObject entry1 = new JSONObject();
			entry1.put(
					ServiceBasicAttributeNames.SERVICE_URL.getAttributeName(),
					"http://" + UUID.randomUUID());
			entry1.put(
					ServiceBasicAttributeNames.SERVICE_TYPE.getAttributeName(),
					"jms");
			ServiceObject so = new ServiceObject(entry1);
			db.insert(so);
		}
		for (int i = 0; i < 50; i++) {
			JSONObject entry1 = new JSONObject();
			entry1.put(
					ServiceBasicAttributeNames.SERVICE_URL.getAttributeName(),
					"http://" + UUID.randomUUID());
			entry1.put(
					ServiceBasicAttributeNames.SERVICE_TYPE.getAttributeName(),
					"sms");
			ServiceObject so = new ServiceObject(entry1);
			db.insert(so);
		}
	}

	@AfterClass
	public static void cleanUp() throws JSONException {
		db.deleteAll();
		assertTrue(db.findAll().size() == 0);
	}

		

	@Test
	public void testFindByType() {
		try {
			DSRClient cr = new DSRClient(BaseURI
					+ "/services/query?serviceType=jms");
			// JSONObject o =
			// cr.getClientResource().accept(MediaType.APPLICATION_JSON_TYPE).get(JSONObject.class);
			JSONArray o = cr.getClientResource()
					.accept(MediaType.APPLICATION_JSON_TYPE)
					.get(JSONArray.class);

			assertTrue(o.length() == 50);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	@Test
	public void testFindNone() {
		try {
			DSRClient cr = new DSRClient(BaseURI
					+ "/services/query?serviceType=blah");
			// JSONObject o =
			// cr.getClientResource().accept(MediaType.APPLICATION_JSON_TYPE).get(JSONObject.class);
			JSONArray o = cr.getClientResource()
					.accept(MediaType.APPLICATION_JSON_TYPE)
					.get(JSONArray.class);

			assertTrue(o.length() == 0);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	@Test
	public void testLimit20() {
		try {
			DSRClient cr = new DSRClient(BaseURI
					+ "/services/query?serviceType=jms&limit=20");
			// JSONObject o =
			// cr.getClientResource().accept(MediaType.APPLICATION_JSON_TYPE).get(JSONObject.class);
			JSONArray o = cr.getClientResource()
					.accept(MediaType.APPLICATION_JSON_TYPE)
					.get(JSONArray.class);
			System.out.println(o.length());
			assertTrue(o.length() == 20);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	@Test
	public void testFindLast30() {
		try {
			DSRClient cr = new DSRClient(BaseURI
					+ "/services/query?serviceType=jms&skip=20");
			// JSONObject o =
			// cr.getClientResource().accept(MediaType.APPLICATION_JSON_TYPE).get(JSONObject.class);
			JSONArray o = cr.getClientResource()
					.accept(MediaType.APPLICATION_JSON_TYPE)
					.get(JSONArray.class);
			System.out.println(o.length());
			assertTrue(o.length() == 30);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	@Test
	public void testFindLastHalfOf40() {
		try {
			DSRClient cr = new DSRClient(BaseURI
					+ "/services/query?serviceType=jms&skip=20&limit=20");
			JSONArray o = cr.getClientResource()
					.accept(MediaType.APPLICATION_JSON_TYPE)
					.get(JSONArray.class);

			assertEquals(20,o.length());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testGetServicesByType() {
		DSRClient cr = new DSRClient(BaseURI + "/services/type/jms");
		JSONArray o = cr.getClientResource()
				.accept(MediaType.APPLICATION_JSON_TYPE).get(JSONArray.class);
		assertNotNull(o);
		assertTrue(o.length() == 50);
	}
	
	@Test
	public void testGetSupportedTypes() throws JSONException {
		DSRClient cr = new DSRClient(BaseURI + "/services/types");
		JSONArray o = cr.getClientResource()
				.accept(MediaType.APPLICATION_JSON_TYPE).get(JSONArray.class);
		System.out.println(o);
		assertTrue(o.get(0).equals("jms") || o.get(0).equals("sms"));
		
	}

	
	@Test
	public void testPagedQuery() throws JSONException{
		//starting page
		DSRClient cr = new DSRClient(BaseURI + "/services/pagedquery?serviceType=jms&pageSize=10");
		JSONObject o = cr.getClientResource()
		.accept(MediaType.APPLICATION_JSON_TYPE).get(JSONObject.class);
		assertEquals(10, o.getJSONArray("result").length());
		
		
		System.out.println(BaseURI + "/services/pagedquery?serviceType=jms&pageSize=10&ref="+o.get("ref"));
		cr = new DSRClient(BaseURI + "/services/pagedquery?serviceType=jms&pageSize=10&ref="+o.get("ref"));
		o = cr.getClientResource()
		.accept(MediaType.APPLICATION_JSON_TYPE).get(JSONObject.class);
		assertEquals(10, o.getJSONArray("result").length());
		
		
		System.out.println(BaseURI + "/services/pagedquery?serviceType=jms&pageSize=10&ref="+o.get("ref"));
		cr = new DSRClient(BaseURI + "/services/pagedquery?serviceType=jms&pageSize=10&ref="+o.get("ref"));
		o = cr.getClientResource()
		.accept(MediaType.APPLICATION_JSON_TYPE).get(JSONObject.class);
		assertEquals(10, o.getJSONArray("result").length());
		
	}
	
	

	
}
