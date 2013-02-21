/**
 * 
 */
package eu.emi.emir.db.mongodb;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.emi.emir.EMIRServer;
import eu.emi.emir.client.ServiceBasicAttributeNames;
import eu.emi.emir.client.TestValueConstants;
import eu.emi.emir.db.ExistingResourceException;
import eu.emi.emir.db.PersistentStoreFailureException;

/**
 * Test to use faceted search while using the aggregation framework of MongoDB
 * 
 * @author a.memon
 * 
 */
public class FacetedSearch extends MongoDBTestBase {
	public static MongoDBServiceDatabase db;

	@Before
	public void setup() {
		EMIRServer s = new EMIRServer(new Properties());
		db = new MongoDBServiceDatabase("localhost", 27017, "emiregistry",
				"services-test");
		db.deleteAll();

		for (int i = 0; i < 100; i++) {
			JSONObject j;
			try {
				j = TestValueConstants.getJSONWithMandatoryAttributes();
				j.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID.toString(),
						i + 1);
				// insert different un-identical objects
				if ((i >= 0) && (i <= 25)) {
					j.put(ServiceBasicAttributeNames.SERVICE_NAME.toString(),
							"arc");
					j.put(ServiceBasicAttributeNames.SERVICE_TYPE.toString(),
							"job management");
					ServiceObject so = new ServiceObject(j);
					db.insert(so);
				}

				if ((i >= 26) && (i <= 60)) {
					j.put(ServiceBasicAttributeNames.SERVICE_NAME.toString(),
							"u6");
					j.put(ServiceBasicAttributeNames.SERVICE_TYPE.toString(),
							"workflow");
					ServiceObject so = new ServiceObject(j);
					db.insert(so);
				}

				if ((i >= 61) && (i <= 100)) {
					j.put(ServiceBasicAttributeNames.SERVICE_NAME.toString(),
							"gridgain");
					j.put(ServiceBasicAttributeNames.SERVICE_TYPE.toString(),
							"storage management");
					ServiceObject so = new ServiceObject(j);
					db.insert(so);
				}

			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (ExistingResourceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (PersistentStoreFailureException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	@Test
	public void test() throws Exception {
		Set<String> j = new HashSet<String>();
		j.add(ServiceBasicAttributeNames.SERVICE_NAME.toString());
		j.add(ServiceBasicAttributeNames.SERVICE_TYPE.toString());

		JSONArray ja = db.facetedQuery(j);
		assertEquals(
				"arc",
				(ja.getJSONObject(0)
						.getJSONArray(
								ServiceBasicAttributeNames.SERVICE_NAME
										.toString()).getJSONObject(0)
						.getString("_id")));
	}

	@After
	public void cleanUp() throws JSONException {
		db.deleteAll();
		assertTrue(db.findAll().size() == 0);
	}

}
