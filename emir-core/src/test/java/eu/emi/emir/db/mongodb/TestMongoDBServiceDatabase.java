package eu.emi.emir.db.mongodb;

import static org.junit.Assert.*;


import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import eu.emi.emir.client.ServiceBasicAttributeNames;
import eu.emi.emir.db.ExistingResourceException;
import eu.emi.emir.db.MultipleResourceException;
import eu.emi.emir.db.NonExistingResourceException;
import eu.emi.emir.db.PersistentStoreFailureException;
import eu.emi.emir.db.mongodb.MongoDBServiceDatabase;
import eu.emi.emir.db.mongodb.ServiceObject;
import eu.emi.emir.validator.InvalidServiceDescriptionException;

/**
 * @author martoni
 * @author a.memon
 */
public class TestMongoDBServiceDatabase extends MongoDBTestBase{
	public static MongoDBServiceDatabase db;

	@Rule
	public ExpectedException exception = ExpectedException.none();

	
	@Before
	public void setUp() {
		db = new MongoDBServiceDatabase("localhost", 27017, "emiregistry",
				"services-test");
		db.deleteAll();
	}

	@Test
	public void testInsertServiceEntry() {

		// Create information to be stored
		JSONObject entry = new JSONObject();
		try {
			entry.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID
					.getAttributeName(), "http://1");
			entry.put(
					ServiceBasicAttributeNames.SERVICE_TYPE.getAttributeName(),
					"some_service");
			ServiceObject s = new ServiceObject(entry);

			db.insert(s);

			assertEquals("http://1", db.getServiceByEndpointID("http://1").getEndpointID());
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

	}

	@Test(expected = NonExistingResourceException.class)
	public void testDeleteNonExistingServiceByEndpointID()
			throws MultipleResourceException, NonExistingResourceException,
			PersistentStoreFailureException {
		db.deleteByEndpointID("http://NA");
	}

	@Test(expected = ExistingResourceException.class)
	public void testRedundantServiceEntries() throws JSONException,
			PersistentStoreFailureException, ExistingResourceException {
		JSONObject entry = new JSONObject();
		entry.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID
				.getAttributeName(), "http://1");
		entry.put(ServiceBasicAttributeNames.SERVICE_TYPE.getAttributeName(),
				"some_service_1");
		ServiceObject s1 = new ServiceObject(entry);
		db.insert(s1);

		JSONObject entry1 = new JSONObject();
		entry1.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID
				.getAttributeName(), "http://1");
		entry1.put(ServiceBasicAttributeNames.SERVICE_TYPE.getAttributeName(),
				"some_service_2");
		ServiceObject s2 = new ServiceObject(entry1);
		db.insert(s2);

		JSONObject entry2 = new JSONObject();
		entry2.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID
				.getAttributeName(), "http://2");
		entry2.put(ServiceBasicAttributeNames.SERVICE_TYPE.getAttributeName(),
				"some_service_3");
		ServiceObject s3 = new ServiceObject(entry2);
		db.insert(s3);

	}

	@Test
	public void testDeleteServiceByEndpointID() throws MultipleResourceException,
			NonExistingResourceException, PersistentStoreFailureException,
			JSONException, ExistingResourceException {
		JSONObject entry1 = new JSONObject();
		entry1.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID
				.getAttributeName(), "http://2");
		entry1.put(ServiceBasicAttributeNames.SERVICE_TYPE.getAttributeName(),
				"some_service_2");
		ServiceObject so = new ServiceObject(entry1);
		db.insert(so);
		ServiceObject s = db.getServiceByEndpointID("http://2");
		assertEquals("http://2", s.getEndpointID());
		db.deleteByEndpointID("http://2");
		ServiceObject s1 = db.getServiceByEndpointID("http://2");
		assertTrue(s1 == null);
	}

	@Test
	public void testSimpleUpdateServiceEntry() {
		// Create information to be stored
		JSONObject entry = new JSONObject();
		try {
			entry.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID
					.getAttributeName(), "http://1");
			entry.put(
					ServiceBasicAttributeNames.SERVICE_TYPE.getAttributeName(),
					"some_service");
			ServiceObject s = new ServiceObject(entry);

			db.insert(s);

			assertEquals("http://1", db.getServiceByEndpointID("http://1").getEndpointID());
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		try {
			entry.put(ServiceBasicAttributeNames.SERVICE_ID
					.getAttributeName(), "http://1");
			
			ServiceObject s = new ServiceObject(entry);
			db.update(s);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testUpdateInsertServiceEntry() {
		// Create information to be stored
		JSONObject entry = new JSONObject();
		try {
			entry.put(ServiceBasicAttributeNames.SERVICE_NAME
					.getAttributeName(), "service name");
			entry.put(ServiceBasicAttributeNames.SERVICE_ID
					.getAttributeName(), "http://1");
			entry.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID
					.getAttributeName(), "http://service_endpoint_ID");
			entry.put(
					ServiceBasicAttributeNames.SERVICE_TYPE.getAttributeName(),
					"service_type");
			entry.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
					.getAttributeName(), "http://1");
			entry.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_CAPABILITY
					.getAttributeName(), new JSONArray("[ \"http://1\" ]"));
			entry.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_TECHNOLOGY
					.getAttributeName(), "webservice");
			entry.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_IFACENAME
					.getAttributeName(), "ldap");
			entry.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_IFACE_VER
					.getAttributeName(), "1.0");
			ServiceObject s = new ServiceObject(entry);

			db.update(s);

			assertEquals("http://service_endpoint_ID", db.getServiceByEndpointID("http://service_endpoint_ID").getEndpointID());
		} catch (PersistentStoreFailureException e) {
			System.out.print("Not updated an entry.\n");
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}		
	}

	@Test (expected = NullPointerException.class)
	public void testUpdateInsertNotValidServiceEntry() throws NullPointerException  {
		// Create information to be stored
		JSONObject entry = new JSONObject();
		try {
			entry.put(ServiceBasicAttributeNames.SERVICE_ID
					.getAttributeName(), "http://1");
			entry.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID
					.getAttributeName(), "http://1");
			entry.put(
					ServiceBasicAttributeNames.SERVICE_TYPE.getAttributeName(),
					"some_service");
			ServiceObject s = new ServiceObject(entry);

			db.update(s);

			assertEquals("http://1", db.getServiceByEndpointID("http://1").getEndpointID());
		} catch (MultipleResourceException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (NonExistingResourceException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (PersistentStoreFailureException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (JSONException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}		
	}

	@After
	public void cleanUp() throws JSONException {
		db.deleteAll();
		assertTrue(db.findAll().size() == 0);
	}
}
