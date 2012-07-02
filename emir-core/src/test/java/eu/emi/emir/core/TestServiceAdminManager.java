/**
 * 
 */
package eu.emi.emir.core;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.emi.emir.EMIRServer;
import eu.emi.emir.client.ServiceBasicAttributeNames;
import eu.emi.emir.core.ServiceAdminManager;
import eu.emi.emir.db.MultipleResourceException;
import eu.emi.emir.db.NonExistingResourceException;
import eu.emi.emir.db.PersistentStoreFailureException;
import eu.emi.emir.db.mongodb.MongoDBServiceDatabase;
import eu.emi.emir.db.mongodb.MongoDBTestBase;
import eu.emi.emir.exception.UnknownServiceException;
import eu.emi.emir.util.ServiceUtil;
import static org.junit.Assert.*;
/**
 * @author a.memon
 * 
 */

public class TestServiceAdminManager extends MongoDBTestBase{
	static ServiceAdminManager adminMgr;

	@Before
	public void setup() {
		Properties p = new Properties();
		p.setProperty("emir.address", "http://localhost:54321");
		@SuppressWarnings("unused")
		EMIRServer s = new EMIRServer(p);		
		adminMgr = new ServiceAdminManager(new MongoDBServiceDatabase("localhost",27017, "emiregistry", "services"));
		adminMgr.removeAll();
		
	}

	private static JSONObject getDummyServiceDesc() {
		Map<String, String> map = new HashMap<String, String>();
		map.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
				.getAttributeName(), "http://1");
		map.put(ServiceBasicAttributeNames.SERVICE_TYPE.getAttributeName(),
				"jms");
		
		JSONObject date = new JSONObject();
		Calendar c = Calendar.getInstance();
		c.add(Calendar.MONTH, 12);
		try {
			date.put("$date", ServiceUtil.toUTCFormat(c.getTime()));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		
		JSONObject jo = new JSONObject(map);
		
		return jo;
	}
	
	@Test
	public void addServiceDescription() throws Exception {		
		System.out.println(adminMgr.addService(getDummyServiceDesc()));
		System.out.println("service added");
	}

	

	@Test
	public void updateService() throws Exception {
		adminMgr.addService(getDummyServiceDesc());
		JSONObject jo = getDummyServiceDesc();
		jo.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_IFACENAME.getAttributeName(), "emi-es");		
		adminMgr.updateService(jo);
		System.out.println("service updated");
		
		//asserting
		assertEquals("emi-es", adminMgr.findServiceByUrl("http://1").get(ServiceBasicAttributeNames.SERVICE_ENDPOINT_IFACENAME.getAttributeName()));
	}

	@Test
	public void findService() throws Exception {
		adminMgr.addService(getDummyServiceDesc());
		System.out.println(adminMgr.findServiceByUrl("http://1").get(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL.getAttributeName()));
		assertEquals("http://1",adminMgr.findServiceByUrl("http://1").get(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL.getAttributeName()));
	}
	
	@After
	public void cleanUp() throws UnknownServiceException, MultipleResourceException, NonExistingResourceException, PersistentStoreFailureException{
		adminMgr.removeAll();
	}
}
