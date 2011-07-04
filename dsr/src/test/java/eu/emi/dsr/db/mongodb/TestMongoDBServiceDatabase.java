package eu.emi.dsr.db.mongodb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.codehaus.jackson.map.util.JSONPObject;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import eu.emi.dsr.db.ExistingResourceException;
import eu.emi.dsr.db.MultipleResourceException;
import eu.emi.dsr.db.NonExistingResourceException;
import eu.emi.dsr.db.PersistentStoreFailureException;

/**
 * @author martoni
 * 
 */
public class TestMongoDBServiceDatabase {

	@Test
	public void testInsertExistingServiceEntry() {
		MongoDBServiceDatabase db = new MongoDBServiceDatabase();
		boolean foundExistingElement = false;

		// Create information to be stored
		JSONObject entry = new JSONObject();
		try {
			entry.put("id", "testEntry");
			entry.put("content", "testData");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Store information twice!
		try {
			db.insert(entry.toString());
			db.insert(entry.toString());
		} catch (ExistingResourceException e) {
			foundExistingElement = true;
		} catch (PersistentStoreFailureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Delete inserted information
		try {
			db.delete("testEntry");
		} catch (MultipleResourceException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (NonExistingResourceException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (PersistentStoreFailureException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		if (foundExistingElement) {
			assertTrue("Detecting already existing element", true);
		} else {
			fail("Not detecting already existing element.");
		}
	}
	
	@Test
	public void testInsertNewServiceEntry() {
		MongoDBServiceDatabase db = new MongoDBServiceDatabase();
		
		// Create information to be stored
		JSONObject entry = new JSONObject();
		try {
			entry.put("id", "testEntry");
			entry.put("content", "testData");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Store information
		try {
			db.insert(entry.toString());
		} catch (ExistingResourceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (PersistentStoreFailureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Retrieve stored information
		Object result = new Object();
		try {
			result = db.get("testEntry");
		} catch (MultipleResourceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NonExistingResourceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (PersistentStoreFailureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Delete inserted information
		try {
			db.delete("testEntry");
		} catch (MultipleResourceException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (NonExistingResourceException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (PersistentStoreFailureException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		// Check resulted information
		JSONObject resultObject = new JSONObject();
		try {
			resultObject = new JSONObject(result.toString());
		} catch (JSONException e1) {
			fail("Not well-formed JSON result retrieved from database.");
		}
		try {
			assertEquals(resultObject.get("content"), "testData");
		} catch (JSONException e) {
			fail("Missing content from retrieved data.");
		}
	}
}
