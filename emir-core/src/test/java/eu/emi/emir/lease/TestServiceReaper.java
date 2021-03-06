/**
 * 
 */
package eu.emi.emir.lease;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Properties;
import java.util.UUID;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.emi.emir.EMIRServer;
import eu.emi.emir.client.ServiceBasicAttributeNames;
import eu.emi.emir.client.TestValueConstants;
import eu.emi.emir.client.util.DateUtil;
import eu.emi.emir.core.ServiceAdminManager;
import eu.emi.emir.db.mongodb.MongoDBServiceDatabase;
import eu.emi.emir.db.mongodb.MongoDBTestBase;
import eu.unicore.bugsreporter.annotation.FunctionalTest;

/**
 * @author a.memon
 * 
 */
public class TestServiceReaper extends MongoDBTestBase {
	private static ServiceAdminManager adminMgr;
	Properties p;

	@Before
	public void setup() {
		adminMgr = new ServiceAdminManager(new MongoDBServiceDatabase(
				"localhost", 27017, "emiregistry", "services"));
		adminMgr.removeAll();
		p = new Properties();
		@SuppressWarnings("unused")
		EMIRServer server = new EMIRServer(p);
	}

	@After
	public void tearDown() {
		adminMgr.removeAll();
	}

	@Test
	@FunctionalTest(id = "RunTTLTest", description = "Test TTL of a service record")
	public void testReaping() throws Exception {
		JSONObject date = new JSONObject();
		Calendar c = Calendar.getInstance();
		// c.add(Calendar.MONTH, 12);
		try {
			date.put("$date", DateUtil.toUTCFormat(c.getTime()));
		} catch (JSONException e) {
			e.printStackTrace();
		}

		Calendar c1 = Calendar.getInstance();
		
		JSONObject jo1 = TestValueConstants.getJSONWithMandatoryAttributes();
		for (int i = 0; i < 2; i++) {
			jo1.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID.getAttributeName(),
					 UUID.randomUUID().toString());
			JSONObject date1 = new JSONObject();

			c1.add(Calendar.SECOND, 1);
			try {
				date1.put("$date", DateUtil.toUTCFormat(c1.getTime()));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			jo1.put(ServiceBasicAttributeNames.SERVICE_EXPIRE_ON
					.getAttributeName(), date1);
			adminMgr.addService(jo1);
		}
		

		int size = adminMgr.findAll().size();
		
		assertEquals(2, size);

		Thread.sleep(3000);

		ServiceReaper r = new ServiceReaper();

		r.run();

		int size1 = adminMgr.findAll().size();
		assertEquals(0, size1);
	}

	@Test
	public void testAddingDefaultExpiry() throws Exception {
		JSONObject jo = TestValueConstants.getJSONWithMandatoryAttributes();
		jo.remove(ServiceBasicAttributeNames.SERVICE_EXPIRE_ON
						.getAttributeName());
		adminMgr.addService(jo);
		JSONObject r = adminMgr.findServiceByUrl("http://1");
		assertTrue(r.has(ServiceBasicAttributeNames.SERVICE_EXPIRE_ON
				.getAttributeName()));

	}

	@Test
	public void testAddingExpiry() throws Exception {
		JSONObject jo = TestValueConstants.getJSONWithMandatoryAttributes();
		DateUtil.setExpiryTime(jo, 10);
		adminMgr.addService(jo);
		JSONObject r = adminMgr.findServiceByUrl("http://1");
		assertTrue(r.has(ServiceBasicAttributeNames.SERVICE_EXPIRE_ON
				.getAttributeName()));
	}

}
