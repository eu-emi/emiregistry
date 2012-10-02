/**
 * 
 */
package eu.emi.emir.resource;

import static org.junit.Assert.assertTrue;

import java.util.UUID;

import javax.ws.rs.core.MediaType;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.emi.emir.TestRegistryBase;
import eu.emi.emir.client.EMIRClient;
import eu.emi.emir.client.ServiceBasicAttributeNames;
import eu.emi.emir.client.TestValueConstants;
import eu.emi.emir.db.ExistingResourceException;
import eu.emi.emir.db.PersistentStoreFailureException;
import eu.emi.emir.db.mongodb.MongoDBServiceDatabase;
import eu.emi.emir.db.mongodb.ServiceObject;
import static org.junit.Assert.*;

/**
 * @author a.memon
 * 
 */
public class TestAutoPaginate extends TestRegistryBase {
	public static MongoDBServiceDatabase db;

	@Before
	public void setUp() throws JSONException, ExistingResourceException,
			PersistentStoreFailureException {
		JSONObject jo = TestValueConstants.getJSONWithMandatoryAttributes();
		db = new MongoDBServiceDatabase("localhost", 27017, "emiregistry",
				"services");
		db.deleteAll();
		// Adding thousand records
		for (int i = 0; i < 500; i++) {
			jo.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID
					.getAttributeName(), "http://" + UUID.randomUUID());
			jo.put(ServiceBasicAttributeNames.SERVICE_TYPE.getAttributeName(),
					"jms");
			jo.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_HEALTH_STATE
					.getAttributeName(), "critical");
			ServiceObject so = new ServiceObject(jo);
			db.insert(so);
		}
		for (int i = 0; i < 500; i++) {
			jo.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID
					.getAttributeName(), "http://" + UUID.randomUUID());
			jo.put(ServiceBasicAttributeNames.SERVICE_TYPE.getAttributeName(),
					"sms");
			jo.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_HEALTH_STATE
					.getAttributeName(), "ok");
			ServiceObject so = new ServiceObject(jo);
			db.insert(so);
		}
	}

	@After
	public void cleanUp() throws JSONException {
		db.deleteAll();
		assertTrue(db.findAll().size() == 0);
	}

	@Test
	public void testAutoPaginate() throws JSONException {
		EMIRClient c = new EMIRClient(BaseURI + "/services");
		JSONArray ja = c.getClientResource()
				.accept(MediaType.APPLICATION_JSON_TYPE).get(JSONArray.class);

		System.out.println("The size is: " + (ja.length() - 1));

		assertEquals(100, ja.length() - 1);

		String ref = ja.getJSONObject(ja.length() - 1).getString("ref");

		// explicit checking for the next 700 records
		c = new EMIRClient(BaseURI + "/services?pageSize=700&ref=" + ref);
		ja = c.getClientResource().accept(MediaType.APPLICATION_JSON_TYPE)
				.get(JSONArray.class);
		System.out.println("The size is: " + (ja.length() - 1));
		assertEquals(700, ja.length() - 1);		

	}

}
