package eu.emi.dsr.db.mongodb;

import static org.junit.Assert.*;

import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import eu.emi.client.ServiceBasicAttributeNames;
import eu.emi.emir.DSRServer;
import eu.emi.emir.core.Configuration;
import eu.emi.emir.db.ExistingResourceException;
import eu.emi.emir.db.MultipleResourceException;
import eu.emi.emir.db.NonExistingResourceException;
import eu.emi.emir.db.PersistentStoreFailureException;
import eu.emi.emir.db.mongodb.MongoDBServiceDatabase;
import eu.emi.emir.db.mongodb.ServiceObject;

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
		new DSRServer(new Configuration(new Properties()));
		db = new MongoDBServiceDatabase("localhost", 27017, "emiregistry",
				"services-test");
		db.deleteAll();
	}

	@Test
	public void testInsertServiceEntry() {

		// Create information to be stored
		JSONObject entry = new JSONObject();
		try {
			entry.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
					.getAttributeName(), "http://1");
			entry.put(
					ServiceBasicAttributeNames.SERVICE_TYPE.getAttributeName(),
					"some_service");
			ServiceObject s = new ServiceObject(entry);

			db.insert(s);

			assertEquals("http://1", db.getServiceByUrl("http://1").getUrl());
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

	}

	@Test(expected = NonExistingResourceException.class)
	public void testDeleteNonExistingServiceByURL()
			throws MultipleResourceException, NonExistingResourceException,
			PersistentStoreFailureException {
		db.deleteByUrl("http://NA");
	}

	@Test(expected = ExistingResourceException.class)
	public void testRedundantServiceEntries() throws JSONException,
			PersistentStoreFailureException, ExistingResourceException {
		JSONObject entry = new JSONObject();
		entry.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
				.getAttributeName(), "http://1");
		entry.put(ServiceBasicAttributeNames.SERVICE_TYPE.getAttributeName(),
				"some_service_1");
		ServiceObject s1 = new ServiceObject(entry);
		db.insert(s1);

		JSONObject entry1 = new JSONObject();
		entry1.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
				.getAttributeName(), "http://1");
		entry1.put(ServiceBasicAttributeNames.SERVICE_TYPE.getAttributeName(),
				"some_service_2");
		ServiceObject s2 = new ServiceObject(entry1);
		db.insert(s2);

		JSONObject entry2 = new JSONObject();
		entry2.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
				.getAttributeName(), "http://2");
		entry2.put(ServiceBasicAttributeNames.SERVICE_TYPE.getAttributeName(),
				"some_service_3");
		ServiceObject s3 = new ServiceObject(entry2);
		db.insert(s3);

	}

	@Test
	public void testDeleteServiceByUrl() throws MultipleResourceException,
			NonExistingResourceException, PersistentStoreFailureException,
			JSONException, ExistingResourceException {
		JSONObject entry1 = new JSONObject();
		entry1.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
				.getAttributeName(), "http://2");
		entry1.put(ServiceBasicAttributeNames.SERVICE_TYPE.getAttributeName(),
				"some_service_2");
		ServiceObject so = new ServiceObject(entry1);
		db.insert(so);
		ServiceObject s = db.getServiceByUrl("http://2");
		assertEquals("http://2", s.getUrl());
		db.deleteByUrl("http://2");
		ServiceObject s1 = db.getServiceByUrl("http://2");
		assertTrue(s1 == null);
	}

	@After
	public void cleanUp() throws JSONException {
		db.deleteAll();
		assertTrue(db.findAll().size() == 0);
	}
}
