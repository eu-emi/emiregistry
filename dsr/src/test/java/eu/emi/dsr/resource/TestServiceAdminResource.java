/**
 * 
 */
package eu.emi.dsr.resource;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.After;
import org.junit.Test;

import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import com.sun.jersey.api.client.UniformInterfaceException;


import eu.emi.dsr.TestRegistryBase;
import eu.emi.dsr.client.DSRClient;
import eu.emi.dsr.core.ServiceBasicAttributeNames;
import eu.emi.dsr.db.ServiceDatabase;
import eu.emi.dsr.db.mongodb.MongoDBServiceDatabase;
import eu.emi.dsr.util.ServiceUtil;

import static org.junit.Assert.*;

/**
 * @author a.memon
 * 
 */
public class TestServiceAdminResource extends TestRegistryBase {

	private static JSONObject getDummyServiceDesc() {
		Map<String, String> map = new HashMap<String, String>();
		map.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
				.getAttributeName(), "http://1");
		map.put(ServiceBasicAttributeNames.SERVICE_TYPE.getAttributeName(),
				"jms");
		
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
			jo.put(ServiceBasicAttributeNames.SERVICE_EXPIRE_ON.getAttributeName(),
					date);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jo;
	}

	@Test
	public void testRegisterService() throws JSONException,
			InterruptedException {
		DSRClient cr = new DSRClient(BaseURI + "/serviceadmin");
		cr.getClientResource().accept(MediaType.APPLICATION_JSON_TYPE)
				.post(getDummyServiceDesc());

		DSRClient cr1 = new DSRClient(BaseURI
				+ "/serviceadmin?Service_Endpoint_URL=http://1");
		JSONObject jo = cr1.getClientResource()
				.accept(MediaType.APPLICATION_JSON_TYPE).get(JSONObject.class);
		assertEquals("http://1",
				jo.get(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
						.getAttributeName()));

	}

	private static JSONObject getUpdatedServiceDesc() {
		Map<String, String> map = new HashMap<String, String>();
		map.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
				.getAttributeName(), "http://1");
		map.put(ServiceBasicAttributeNames.SERVICE_TYPE.getAttributeName(),
				"sms");
		
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
			jo.put(ServiceBasicAttributeNames.SERVICE_EXPIRE_ON.getAttributeName(),
					date);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jo;
	}

	@Test
	public void testUpdateService() throws JSONException {
		DSRClient cr1 = new DSRClient(BaseURI + "/serviceadmin");
		cr1.getClientResource().accept(MediaType.APPLICATION_JSON_TYPE)
				.post(getDummyServiceDesc());

		DSRClient cr = new DSRClient(BaseURI + "/serviceadmin");
		Map<String, String> map = new HashMap<String, String>();
		map.put(ServiceBasicAttributeNames.SERVICE_TYPE.getAttributeName(),
				"sms");
		map.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
				.getAttributeName(), "http://1");
		cr.getClientResource().accept(MediaType.APPLICATION_JSON_TYPE)
				.put(getUpdatedServiceDesc());

		DSRClient cr2 = new DSRClient(BaseURI
				+ "/serviceadmin?Service_Endpoint_URL=http://1");
		JSONObject jo1 = cr2.getClientResource()
				.accept(MediaType.APPLICATION_JSON_TYPE).get(JSONObject.class);
		assertEquals("sms", jo1.get(ServiceBasicAttributeNames.SERVICE_TYPE
				.getAttributeName()));
	}

	@Test(expected=UniformInterfaceException.class)
	public void testDeleteResource() throws JSONException {
		DSRClient cr1 = new DSRClient(BaseURI + "/serviceadmin");
		cr1.getClientResource().accept(MediaType.APPLICATION_JSON_TYPE)
				.post(getDummyServiceDesc());

		DSRClient cr = new DSRClient(BaseURI
				+ "/serviceadmin?Service_Endpoint_URL=http://1");
		cr.getClientResource().delete();
		
		DSRClient cr2 = new DSRClient(BaseURI
				+ "/serviceadmin?Service_Endpoint_URL=http://1");
		Response res = cr2.getClientResource().get(javax.ws.rs.core.Response.class);
		System.out.println(res.getStatus());
		
		Object o = cr2.getClientResource().accept("application/vnd.sun.wadl+xml").get(Object.class);
		System.out.println(o);
	}

	
	
	@After
	public void cleanup() {
		ServiceDatabase sd = new MongoDBServiceDatabase();
		sd.deleteAll();
	}
}
