/**
 * 
 */
package eu.emi.dsr.core;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.emi.dsr.DSRServer;
import eu.emi.dsr.db.MultipleResourceException;
import eu.emi.dsr.db.NonExistingResourceException;
import eu.emi.dsr.db.PersistentStoreFailureException;
import eu.emi.dsr.db.mongodb.MongoDBTestBase;
import eu.emi.dsr.exception.UnknownServiceException;
import eu.emi.dsr.util.ServiceUtil;
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
		p.put(ServerConstants.MONGODB_HOSTNAME, "localhost");
		p.put(ServerConstants.MONGODB_PORT, "27017");
		p.put(ServerConstants.MONGODB_COLLECTION_NAME, "services-test");
		p.put(ServerConstants.MONGODB_PORT, "27017");
		p.put(ServerConstants.MONGODB_DB_NAME, "emiregistry");
		Configuration conf = new Configuration(p);
		new DSRServer(conf);
		adminMgr = new ServiceAdminManager();
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
		adminMgr.addService(getDummyServiceDesc());
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
