/**
 * 
 */
package eu.emi.emir.db.mongodb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.UUID;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.emi.emir.client.ServiceBasicAttributeNames;
import eu.emi.emir.db.ExistingResourceException;
import eu.emi.emir.db.PersistentStoreFailureException;
import eu.emi.emir.db.QueryException;
import eu.emi.emir.db.mongodb.MongoDBServiceDatabase;
import eu.emi.emir.db.mongodb.ServiceObject;

/**
 * @author a.memon
 * 
 */
public class TestQueryCollection extends MongoDBTestBase{
	public static MongoDBServiceDatabase db;

	@Before
	public void setUp() {
		db = new MongoDBServiceDatabase("localhost", 27017, "emiregistry",
				"services-test");
		db.deleteAll();
		db.dropDB();
		
	}

	@Test
	public void testQueryWithLimitAndSkip() throws JSONException,
			ExistingResourceException, PersistentStoreFailureException,
			QueryException {
		for (int i = 0; i < 100; i++) {
			JSONObject entry1 = new JSONObject();
			entry1.put(
					ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID.getAttributeName(),
					"http://" + UUID.randomUUID());
			entry1.put(
					ServiceBasicAttributeNames.SERVICE_TYPE.getAttributeName(),
					"some_service_2");
			ServiceObject so = new ServiceObject(entry1);
			db.insert(so);
		}
		int s = db.query("{}").size();
		System.out.println("number of records inserted: " + s);
		assertTrue(s == 100);

		List<ServiceObject> lst = db.query("{}", 100, 0);
		assertEquals(100, lst.size());
		// limit-skip
		lst = db.query("{}", 100, 20);
		assertEquals(80, lst.size());
		// limit-skip
		lst = db.query("{}", 100, 30);
		assertEquals(70, lst.size());
		// limit-skip
		lst = db.query("{}", 100, 40);
		assertEquals(60, lst.size());

	}

	@Test
	public void testQueryWithSkip() throws JSONException,
			ExistingResourceException, PersistentStoreFailureException,
			QueryException {
		for (int i = 0; i < 30; i++) {
			JSONObject entry1 = new JSONObject();
			entry1.put(
					ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID.getAttributeName(),
					"http://" + UUID.randomUUID());
			entry1.put(
					ServiceBasicAttributeNames.SERVICE_TYPE.getAttributeName(),
					"some_service_2");
			ServiceObject so = new ServiceObject(entry1);
			db.insert(so);
		}

		assertTrue(db.query("{}", 10).size() == 20);
		assertTrue(db.query("{}", 20).size() == 10);
		assertTrue(db.query("{}", 30).size() == 0);

	}

	@Test
	public void testQueryJSONWithSkip() throws JSONException,
			ExistingResourceException, PersistentStoreFailureException,
			QueryException {
		for (int i = 0; i < 30; i++) {
			JSONObject entry1 = new JSONObject();
			entry1.put(
					ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID.getAttributeName(),
					"http://" + UUID.randomUUID());
			entry1.put(
					ServiceBasicAttributeNames.SERVICE_TYPE.getAttributeName(),
					"some_service_2");
			ServiceObject so = new ServiceObject(entry1);
			db.insert(so);
		}

		assertTrue(db.queryJSON("{}", 10).length() == 20);
		assertTrue(db.queryJSON("{}", 20).length() == 10);
		assertTrue(db.queryJSON("{}", 30).length() == 0);

	}

	@Test
	public void testQueryJSONWithLimit() throws JSONException,
			ExistingResourceException, PersistentStoreFailureException,
			QueryException {
		for (int i = 0; i < 30; i++) {
			JSONObject entry1 = new JSONObject();
			entry1.put(
					ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID.getAttributeName(),
					"http://" + UUID.randomUUID());
			entry1.put(
					ServiceBasicAttributeNames.SERVICE_TYPE.getAttributeName(),
					"some_service_2");
			ServiceObject so = new ServiceObject(entry1);
			db.insert(so);
		}

		assertTrue(db.queryJSONWithLimit("{}", 20).length() == 20);
	}

	@Test
	public void testQueryJSONWithLimitAndSkip() throws JSONException,
			ExistingResourceException, PersistentStoreFailureException,
			QueryException {
		for (int i = 0; i < 100; i++) {
			JSONObject entry1 = new JSONObject();
			entry1.put(
					ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID.getAttributeName(),
					"http://" + UUID.randomUUID());
			entry1.put(
					ServiceBasicAttributeNames.SERVICE_TYPE.getAttributeName(),
					"some_service_2");
			ServiceObject so = new ServiceObject(entry1);
			db.insert(so);
		}
		int s = db.query("{}").size();
		System.out.println("number of records inserted: " + s);
		assertTrue(s == 100);

		JSONArray lst = db.queryJSON("{}", 100, 0);
		assertEquals(100, lst.length());
		// limit-skip
		lst = db.queryJSON("{}", 100, 20);
		assertEquals(80, lst.length());
		// limit-skip
		lst = db.queryJSON("{}", 100, 30);
		assertEquals(70, lst.length());
		// limit-skip
		lst = db.queryJSON("{}", 100, 40);
		assertEquals(60, lst.length());

	}

	@After
	public void cleanUp() throws JSONException {
		db.deleteAll();
		assertTrue(db.findAll().size() == 0);
	}

}
