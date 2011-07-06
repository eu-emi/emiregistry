/**
 * 
 */
package eu.emi.dsr.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.codehaus.jettison.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.emi.dsr.DSRServer;
import eu.emi.dsr.db.MultipleResourceException;
import eu.emi.dsr.db.NonExistingResourceException;
import eu.emi.dsr.db.PersistentStoreFailureException;
import eu.emi.dsr.exception.UnknownServiceException;
import static org.junit.Assert.*;
/**
 * @author a.memon
 * 
 */

public class TestServiceAdminManager {
	static ServiceAdminManager adminMgr;

	@BeforeClass
	public static void setup() {
		Properties p = new Properties();
		p.put(ServerConstants.MONGODB_HOSTNAME, "localhost");
		p.put(ServerConstants.MONGODB_PORT, "27017");
		p.put(ServerConstants.MONGODB_COLLECTION_NAME, "services");
		p.put(ServerConstants.MONGODB_PORT, "27017");
		p.put(ServerConstants.MONGODB_DB_NAME, "emiregistry");
		Configuration conf = new Configuration(p);
		DSRServer s = new DSRServer(conf);
		adminMgr = ServiceManagerFactory.getServiceAdminManager();
		
	}

	@Test
	public void addServiceDescription() throws Exception {
		
		Map<String, String> map = new HashMap<String, String>();
		map.put(ServiceBasicAttributeNames.SERVICE_URL.getAttributeName(),
				"http://1");
		map.put(ServiceBasicAttributeNames.SERVICE_TYPE.getAttributeName(),
				"http://1");
		map.put(ServiceBasicAttributeNames.SERVICE_EXPIRE_ON.getAttributeName(),
				"http://1");
		map.put(ServiceBasicAttributeNames.SERVICE_OWNER.getAttributeName(),
				"http://1");
		map.put(ServiceBasicAttributeNames.SERVICE_CREATED_ON
				.getAttributeName(), "http://1");
		map.put(ServiceBasicAttributeNames.SERVICE_UPDATE_SINCE
				.getAttributeName(), "http://1");
		JSONObject jo = new JSONObject(map);
		adminMgr.removeService("http://1");
		adminMgr.addService(jo);
	}

	

	@Test
	public void updateService() throws Exception {
		Map<String, String> map = new HashMap<String, String>();
		map.put(ServiceBasicAttributeNames.SERVICE_URL.getAttributeName(),
				"http://1");
		map.put(ServiceBasicAttributeNames.SERVICE_TYPE.getAttributeName(),
				"myType");
		map.put(ServiceBasicAttributeNames.SERVICE_EXPIRE_ON.getAttributeName(),
				"sometime");
		map.put(ServiceBasicAttributeNames.SERVICE_OWNER.getAttributeName(),
				"someowner");
		map.put(ServiceBasicAttributeNames.SERVICE_CREATED_ON
				.getAttributeName(), "sometime");
		map.put(ServiceBasicAttributeNames.SERVICE_UPDATE_SINCE
				.getAttributeName(), "sometime");
		JSONObject jo = new JSONObject(map);
		adminMgr.updateService(jo);
	}

	@Test
	public void findService() throws Exception {
		assertEquals("http://1",adminMgr.findServiceByUrl("http://1").get("serviceUrl"));
	}
	
	@AfterClass
	public static void cleanUp() throws UnknownServiceException, MultipleResourceException, NonExistingResourceException, PersistentStoreFailureException{
		adminMgr.removeService("http://1");
	}
}
