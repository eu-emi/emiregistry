/**
 * 
 */
package eu.emi.emir.resource;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.After;
import org.junit.Test;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.ClientResponse.Status;

import eu.emi.emir.TestRegistryBase;
import eu.emi.emir.client.EMIRClient;
import eu.emi.emir.client.ServiceBasicAttributeNames;
import eu.emi.emir.db.ServiceDatabase;
import eu.emi.emir.db.mongodb.MongoDBServiceDatabase;
import eu.emi.emir.util.DateUtil;
import eu.emi.emir.util.ServiceUtil;
import eu.unicore.bugsreporter.annotation.FunctionalTest;
import static org.junit.Assert.*;

/**
 * @author a.memon
 * 
 */
public class TestServiceAdminResource extends TestRegistryBase {

	private static JSONArray getDummyServiceDesc() {
		Map<String, String> map = new HashMap<String, String>();
		map.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
				.getAttributeName(), "http://1");
		map.put(ServiceBasicAttributeNames.SERVICE_TYPE.getAttributeName(),
				"jms");

		JSONObject jo = new JSONObject(map);
		
		JSONArray arr = new JSONArray();
		arr.put(jo);
		
		return arr;
	}

	@SuppressWarnings("unused")
	private static JSONObject getOutdatedServiceDesc() {
		Map<String, String> map = new HashMap<String, String>();
		map.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
				.getAttributeName(), "http://1");
		map.put(ServiceBasicAttributeNames.SERVICE_TYPE.getAttributeName(),
				"sms");

		map.put(ServiceBasicAttributeNames.SERVICE_OWNER.getAttributeName(),
				"http://1");		

		JSONObject jo = new JSONObject(map);

		DateUtil.setExpiryTime(jo, -2);

		return jo;
	}

	@Test
	@FunctionalTest(id="ServiceRegistrationTest", description="Test registration of a service record")
	public void testRegisterService() throws JSONException,
			InterruptedException {
		EMIRClient cr = new EMIRClient(BaseURI + "/serviceadmin");

		ClientResponse res = cr.getClientResource()
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.post(ClientResponse.class, getDummyServiceDesc());
		JSONArray resArr = res.getEntity(JSONArray.class);
		assertNotNull(resArr);

		JSONObject jo = cr.getClientResource().queryParam(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL.getAttributeName(), "http://1").accept(MediaType.APPLICATION_JSON_TYPE).get(JSONObject.class);
		assertEquals("http://1",
				jo.get(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
								.getAttributeName()));
		
		System.out.println(jo);
		
		//adding second service
		JSONArray j = getDummyServiceDesc();
		JSONObject j1 = new JSONObject();
		
		j1.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL.getAttributeName(), "http://2");
		
		j.put(j1);
		System.out.println(j);
		ClientResponse cRes = cr.getClientResource()
		.accept(MediaType.APPLICATION_JSON_TYPE)
		.post(ClientResponse.class, j);
		if (cRes.getStatus() == Status.OK.getStatusCode()) {
			JSONArray resJo = cRes.getEntity(JSONArray.class);
			System.out.println("insert response: "+resJo);
			assertTrue(resJo.getJSONObject(0).getString(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL.getAttributeName()).equalsIgnoreCase("http://2"));
			
			
			JSONObject res1 = cr.getClientResource().queryParam(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL.getAttributeName(), "http://2").accept(MediaType.APPLICATION_JSON_TYPE).get(JSONObject.class);
			assertEquals("http://2",
					res1.get(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
							.getAttributeName()));
			System.out.println(res1);	
		} else {
			assertTrue(Status.fromStatusCode(cRes.getStatus()).compareTo(Status.CONFLICT) == 0);
		}
		

	}

	private static JSONArray getUpdatedServiceDesc() {
		
		Map<String, String> map = new HashMap<String, String>();
		map.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
				.getAttributeName(), "http://1");
		map.put(ServiceBasicAttributeNames.SERVICE_TYPE.getAttributeName(),
				"sms");

		map.put(ServiceBasicAttributeNames.SERVICE_OWNER.getAttributeName(),
				"http://1");
		JSONObject date = new JSONObject();
		Calendar c = Calendar.getInstance();
		c.add(Calendar.MONTH, 12);
		try {
			date.put("$date", ServiceUtil.toUTCFormat(c.getTime()));
		} catch (JSONException e) {
			e.printStackTrace();
		}

		JSONObject jo = new JSONObject(map);
		JSONArray jArr = new JSONArray();
		jArr.put(jo);
		return jArr;
	}

	@Test
	public void testUpdateService() throws JSONException {
		EMIRClient cr1 = new EMIRClient(BaseURI + "/serviceadmin");


		cr1.getClientResource()
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.post(getDummyServiceDesc()
						);
		

		EMIRClient cr = new EMIRClient(BaseURI + "/serviceadmin");
		Map<String, String> map = new HashMap<String, String>();
		map.put(ServiceBasicAttributeNames.SERVICE_TYPE.getAttributeName(),
				"sms");
		map.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
				.getAttributeName(), "http://1");
		//removing the expiry time
		JSONObject j = getUpdatedServiceDesc().getJSONObject(0);
		JSONArray ja = new JSONArray();
		j.remove(ServiceBasicAttributeNames.SERVICE_EXPIRE_ON.getAttributeName());
		ja.put(j);
		cr.getClientResource().accept(MediaType.APPLICATION_JSON_TYPE)
				.put(ja);
		
		System.out.println("being updated json document: "+j.toString(2));
		
		EMIRClient cr2 = new EMIRClient(BaseURI
				+ "/serviceadmin?Service_Endpoint_URL=http://1");
		JSONObject jo1 = cr2.getClientResource()
				.accept(MediaType.APPLICATION_JSON_TYPE).get(JSONObject.class);
		System.out.println(jo1);
		//asserting the type
		assertEquals("sms", jo1.get(ServiceBasicAttributeNames.SERVICE_TYPE
				.getAttributeName()));
		//asserting the expiry time
		assertTrue(jo1.has(ServiceBasicAttributeNames.SERVICE_EXPIRE_ON.getAttributeName()));
		
		System.out.println("updated json: "+jo1.toString(2));
		
		
	}

	@Test
	@FunctionalTest(id="ServiceDeletionTest", description="Test deletion of a service record")
	public void testDeleteResource() throws JSONException {
		EMIRClient cr1 = new EMIRClient(BaseURI + "/serviceadmin");
		
		cr1.getClientResource()
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.post(getDummyServiceDesc());
		EMIRClient cr = new EMIRClient(BaseURI
				+ "/serviceadmin?Service_Endpoint_URL=http://1");
		cr.getClientResource().delete();

		EMIRClient cr2 = new EMIRClient(BaseURI
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
