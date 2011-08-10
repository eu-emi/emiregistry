/**
 * 
 */
package eu.emi.dsr.resource;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.After;
import org.junit.Test;

import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.ClientResponse.Status;

import eu.emi.dsr.TestRegistryBase;
import eu.emi.dsr.client.DSRClient;
import eu.emi.dsr.core.ServiceBasicAttributeNames;
import eu.emi.dsr.db.ServiceDatabase;
import eu.emi.dsr.db.mongodb.MongoDBServiceDatabase;
import eu.emi.dsr.util.DateUtil;
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

		return jo;
	}

	private static JSONObject getOutdatedServiceDesc() {
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

		DateUtil.setExpiryTime(jo, -2);

		return jo;
	}

	@Test
	public void testRegisterService() throws JSONException,
			InterruptedException {
		DSRClient cr = new DSRClient(BaseURI + "/serviceadmin");

		JSONArray j = new JSONArray();

		j.put(getDummyServiceDesc());

		JSONArray arr = cr.getClientResource()
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.post(JSONArray.class, j);
		System.out.println("res" + arr);

		DSRClient cr1 = new DSRClient(BaseURI
				+ "/serviceadmin?Service_Endpoint_URL=http://1");
		JSONObject jo = cr1.getClientResource()
				.accept(MediaType.APPLICATION_JSON_TYPE).get(JSONObject.class);
		assertEquals("http://1",
				jo.get(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
						.getAttributeName()));

	}

	@Test
	public void testRegisterOutdatedService() throws JSONException,
			InterruptedException {
		DSRClient cr = new DSRClient(BaseURI + "/serviceadmin");

		JSONArray j = new JSONArray();

		j.put(getDummyServiceDesc());
		j.put(getOutdatedServiceDesc());

		JSONArray arr = cr.getClientResource()
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.post(JSONArray.class, j);
		System.out.println("res" + arr);

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

		return jo;
	}

	@Test
	public void testUpdateService() throws JSONException {
		DSRClient cr1 = new DSRClient(BaseURI + "/serviceadmin");
		JSONArray j = new JSONArray();

		j.put(getDummyServiceDesc());

		JSONArray arr = cr1.getClientResource()
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.post(JSONArray.class, j);
		System.out.println("res" + arr);
		

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
		System.out.println(jo1);
		assertEquals("sms", jo1.get(ServiceBasicAttributeNames.SERVICE_TYPE
				.getAttributeName()));
	}

	@Test
	public void testDeleteResource() throws JSONException {
		DSRClient cr1 = new DSRClient(BaseURI + "/serviceadmin");
		JSONArray j = new JSONArray();

		j.put(getDummyServiceDesc());

		JSONArray arr = cr1.getClientResource()
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.post(JSONArray.class, j);
		System.out.println("res" + arr);
		DSRClient cr = new DSRClient(BaseURI
				+ "/serviceadmin?Service_Endpoint_URL=http://1");
		cr.getClientResource().delete();

		DSRClient cr2 = new DSRClient(BaseURI
				+ "/serviceadmin?Service_Endpoint_URL=http://1");
		try {
			cr2.getClientResource().get(javax.ws.rs.core.Response.class);
		} catch (UniformInterfaceException e) {
			assertTrue(e.getResponse().getStatus() == Status.NO_CONTENT
					.getStatusCode());

		}

	}

	@After
	public void cleanup() {
		ServiceDatabase sd = new MongoDBServiceDatabase();
		sd.deleteAll();
	}
}
