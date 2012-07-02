/**
 * 
 */
package eu.emi.emir.lease;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.emi.emir.EMIRServer;
import eu.emi.emir.client.ServiceBasicAttributeNames;
import eu.emi.emir.core.ServiceAdminManager;
import eu.emi.emir.db.ExistingResourceException;
import eu.emi.emir.db.NonExistingResourceException;
import eu.emi.emir.db.PersistentStoreFailureException;
import eu.emi.emir.db.mongodb.MongoDBServiceDatabase;
import eu.emi.emir.db.mongodb.MongoDBTestBase;
import eu.emi.emir.exception.InvalidServiceDescriptionException;
import eu.emi.emir.lease.ServiceReaper;
import eu.emi.emir.util.DateUtil;
import eu.emi.emir.util.ServiceUtil;
import eu.unicore.bugsreporter.annotation.FunctionalTest;

/**
 * @author a.memon
 * 
 */
public class TestServiceReaper extends MongoDBTestBase{
	private static ServiceAdminManager adminMgr;
	Properties p;
	
	
	
	@Before
	public void setup() {
		adminMgr = new ServiceAdminManager(new MongoDBServiceDatabase("localhost", 27017, "emiregistry",
				"services"));
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
	@FunctionalTest(id="RunTTLTest", description="Test TTL of a service record")
	public void testReaping() throws InvalidServiceDescriptionException,
			JSONException, InterruptedException, ExistingResourceException {
		// adding service entries
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
				.getAttributeName(), "http://1");

		map.put(ServiceBasicAttributeNames.SERVICE_TYPE.getAttributeName(),
				"someservice-type");
		map.put(ServiceBasicAttributeNames.SERVICE_OWNER.getAttributeName(),
				"http://1");

		JSONObject date = new JSONObject();
		Calendar c = Calendar.getInstance();
//		c.add(Calendar.MONTH, 12);
		try {
			date.put("$date", ServiceUtil.toUTCFormat(c.getTime()));
		} catch (JSONException e) {
			e.printStackTrace();
		}

		JSONObject jo = new JSONObject(map);
		try {
			jo.put(ServiceBasicAttributeNames.SERVICE_EXPIRE_ON
					.getAttributeName(), date);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		Calendar c1 = Calendar.getInstance();
		for (int i = 0; i < 2; i++) {
			jo.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
					.getAttributeName(), "http://"
					+ UUID.randomUUID().toString());
			JSONObject date1 = new JSONObject();
			
			c1.add(Calendar.SECOND, 1);
			try {
				date1.put("$date", ServiceUtil.toUTCFormat(c1.getTime()));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			jo.put(ServiceBasicAttributeNames.SERVICE_EXPIRE_ON
					.getAttributeName(), date1);
			adminMgr.addService(jo);
		}

		int size = adminMgr.findAll().size();
		assertEquals(2, size);
		
		Thread.sleep(3000);
		
		ServiceReaper r = new ServiceReaper();
		r.run();

		System.out.println(adminMgr.findAll());
		
		int size1 = adminMgr.findAll().size();
		assertEquals(0, size1);
	}

	@Test
	public void testAddingDefaultExpiry() throws JSONException, InvalidServiceDescriptionException, NonExistingResourceException, PersistentStoreFailureException, ExistingResourceException {
		// adding service entries
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
				.getAttributeName(), "http://1");

		map.put(ServiceBasicAttributeNames.SERVICE_TYPE.getAttributeName(),
				"someservice-type");
		map.put(ServiceBasicAttributeNames.SERVICE_OWNER.getAttributeName(),
				"http://1");

		JSONObject jo = new JSONObject(map);
		jo.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
				.getAttributeName(), "http://1");
		
		adminMgr.addService(jo);
		
		JSONObject r = adminMgr.findServiceByUrl("http://1");
		assertTrue(r.has(ServiceBasicAttributeNames.SERVICE_EXPIRE_ON.getAttributeName()));
		
	}
	
	@Test
	public void testAddingMaxExpiry() throws Exception{
		// adding service entries
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
						.getAttributeName(), "http://1");

		map.put(ServiceBasicAttributeNames.SERVICE_TYPE.getAttributeName(),
						"someservice-type");
		map.put(ServiceBasicAttributeNames.SERVICE_OWNER.getAttributeName(),
						"http://1");

		JSONObject jo = new JSONObject(map);
		jo.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
						.getAttributeName(), "http://1");
		DateUtil.setExpiryTime(jo, 10);		
		adminMgr.addService(jo);
		JSONObject r = adminMgr.findServiceByUrl("http://1");
		assertTrue(r.has(ServiceBasicAttributeNames.SERVICE_EXPIRE_ON.getAttributeName()));
	}
	
	
	
	

}
