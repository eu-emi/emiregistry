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
	public static void setup() {
		Properties p = new Properties();
		p.put(ServerConstants.MONGODB_HOSTNAME, "localhost");
		p.put(ServerConstants.MONGODB_PORT, "27017");
		p.put(ServerConstants.MONGODB_COLLECTION_NAME, "servicestest");
		p.put(ServerConstants.MONGODB_PORT, "27017");
		p.put(ServerConstants.MONGODB_DB_NAME, "emiregistry");
		Configuration conf = new Configuration(p);
		DSRServer s = new DSRServer(conf);
		adminMgr = new ServiceAdminManager();
		adminMgr.removeAll();
	}

	@Test
	public void test() throws InvalidServiceDescriptionException, JSONException, InterruptedException {
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
		c.add(c.MONTH, 12);
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

		for (int i = 0; i < 10; i++) {
			jo.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
					.getAttributeName(), "http://"
					+ UUID.randomUUID().toString());
			JSONObject date1 = new JSONObject();
			Calendar c1 = Calendar.getInstance();
			c1.add(c1.SECOND, 2);
			try {
				date1.put("$date", ServiceUtil.toUTCFormat(c1.getTime()));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			jo.put(ServiceBasicAttributeNames.SERVICE_EXPIRE_ON
					.getAttributeName(), date1);
			adminMgr.addService(jo);
		}

		Thread.sleep(3000);
		
		for (int i = 0; i < 15; i++) {
			jo.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
					.getAttributeName(), "http://"
					+ UUID.randomUUID().toString());
			// to be expired in future
			Calendar c1 = Calendar.getInstance();
			c1.add(c1.MONTH, 12);
			JSONObject date1 = new JSONObject();
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
		assertEquals(25, size);

		ServiceReaper r = new ServiceReaper();
		r.run();

		int size1 = adminMgr.findAll().size();
		assertEquals(15, size1);
	}

}
