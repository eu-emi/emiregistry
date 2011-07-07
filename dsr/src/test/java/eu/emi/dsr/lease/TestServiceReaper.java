/**
 * 
 */
package eu.emi.dsr.lease;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.emi.dsr.DSRServer;
import eu.emi.dsr.core.Configuration;
import eu.emi.dsr.core.ServerConstants;
import eu.emi.dsr.core.ServiceAdminManager;
import eu.emi.dsr.core.ServiceBasicAttributeNames;
import eu.emi.dsr.core.ServiceManagerFactory;
import eu.emi.dsr.exception.InvalidServiceDescriptionException;
import eu.emi.dsr.util.ServiceUtil;
/**
 * @author a.memon
 *
 */
public class TestServiceReaper {
	private static ServiceAdminManager adminMgr;
	
	@BeforeClass
	public static void setup(){
		Properties p = new Properties();
		p.put(ServerConstants.MONGODB_HOSTNAME, "localhost");
		p.put(ServerConstants.MONGODB_PORT, "27017");
		p.put(ServerConstants.MONGODB_COLLECTION_NAME, "servicestest");
		p.put(ServerConstants.MONGODB_PORT, "27017");
		p.put(ServerConstants.MONGODB_DB_NAME, "emiregistry");
		Configuration conf = new Configuration(p);
		DSRServer s = new DSRServer(conf);
		adminMgr = ServiceManagerFactory.getServiceAdminManager();
		adminMgr.removeAll();
	}
	
	@Test
	public void test() throws InvalidServiceDescriptionException, JSONException{
		//adding service entries
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(ServiceBasicAttributeNames.SERVICE_URL.getAttributeName(),
				"http://1");
		map.put(ServiceBasicAttributeNames.SERVICE_EXPIRE_ON.getAttributeName(), "06-07-2011, 13:25");
		map.put(ServiceBasicAttributeNames.SERVICE_TYPE.getAttributeName(),
				"someservice-type");
		map.put(ServiceBasicAttributeNames.SERVICE_OWNER.getAttributeName(),
				"http://1");
		//the following attributes should be added/changed at the controller side (AdminManager)
		map.put(ServiceBasicAttributeNames.SERVICE_CREATED_ON
				.getAttributeName(), "http://1");
		map.put(ServiceBasicAttributeNames.SERVICE_UPDATE_SINCE
				.getAttributeName(), "http://1");
		JSONObject jo = new JSONObject(map);
		
		for (int i = 0; i < 10; i++) {
			jo.put(ServiceBasicAttributeNames.SERVICE_URL.getAttributeName(),
					"http://"+UUID.randomUUID().toString());
			//to be expired
			Calendar c = Calendar.getInstance();
			
			String date = ServiceUtil.ServiceDateFormat.format(c.getTime());
			jo.put(ServiceBasicAttributeNames.SERVICE_EXPIRE_ON.getAttributeName(), date);
			adminMgr.addService(jo);	
		}
		
		for (int i = 0; i < 15; i++) {
			jo.put(ServiceBasicAttributeNames.SERVICE_URL.getAttributeName(),
					"http://"+UUID.randomUUID().toString());
			//to be expired in future
			Calendar c = Calendar.getInstance();
			//these records will expire on 2012
			c.add(c.YEAR, 1);
			String date = ServiceUtil.ServiceDateFormat.format(c.getTime());
			jo.put(ServiceBasicAttributeNames.SERVICE_EXPIRE_ON.getAttributeName(), date);
			adminMgr.addService(jo);		
		}
		
		int size = adminMgr.findAll().size();
		assertEquals(25, size);
		
		ServiceReaper r = new ServiceReaper();
		r.run();
		
		int size1 = adminMgr.findAll().size();
		assertEquals(15, size1);
	}
	
	
	
}
