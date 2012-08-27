/**
 * 
 */
package eu.emi.emir.resource;

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
import eu.emi.emir.client.TestValueConstants;
import eu.emi.emir.client.util.DateUtil;
import eu.emi.emir.db.ServiceDatabase;
import eu.emi.emir.db.mongodb.MongoDBServiceDatabase;
import eu.unicore.bugsreporter.annotation.FunctionalTest;
import static org.junit.Assert.*;

/**
 * @author a.memon
 * 
 */
public class TestServiceAdminResource extends TestRegistryBase {

	@SuppressWarnings("unused")
	private static JSONObject getOutdatedServiceDesc() {
		Map<String, String> map = new HashMap<String, String>();
		map.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
				.getAttributeName(), "http://1");
		map.put(ServiceBasicAttributeNames.SERVICE_TYPE.getAttributeName(),
				"sms");

		map.put(ServiceBasicAttributeNames.SERVICE_OWNER_DN.getAttributeName(),
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
				.post(ClientResponse.class, TestValueConstants.getJSONArrayWithMandatoryAttributes());
		JSONArray resArr = res.getEntity(JSONArray.class);
		assertNotNull(resArr);

		EMIRClient cr1 = new EMIRClient(BaseURI + "/services");
		
		JSONArray jo = cr1.getClientResource().queryParam(ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID.getAttributeName(), "1").accept(MediaType.APPLICATION_JSON_TYPE).get(JSONArray.class);
		assertEquals("1",
				jo.getJSONObject(0).get(ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID
								.getAttributeName()));
		
		System.out.println(jo);
		
		//adding second service
		JSONArray j = new JSONArray();
		JSONObject j1 = TestValueConstants.getJSONWithMandatoryAttributes();
		j1.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID.getAttributeName(), "2");
		j.put(j1);
		System.out.println(j);
		ClientResponse cRes = cr.getClientResource()
		.accept(MediaType.APPLICATION_JSON_TYPE)
		.post(ClientResponse.class, j);
		
		
		if (cRes.getStatus() == Status.OK.getStatusCode()) {
			JSONArray resJo = cRes.getEntity(JSONArray.class);
			System.out.println("insert response: "+resJo);
			assertTrue(resJo.getJSONObject(0).getString(ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID.getAttributeName()).equalsIgnoreCase("2"));
			
			
			JSONArray res1 = cr1.getClientResource().queryParam(ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID.getAttributeName(), "2").accept(MediaType.APPLICATION_JSON_TYPE).get(JSONArray.class);
			assertEquals("2",
					res1.getJSONObject(0).get(ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID
							.getAttributeName()));
			System.out.println(res1);	
		} else {
			
			if(Status.fromStatusCode(cRes.getStatus()).compareTo(Status.CONFLICT) == 0)
				fail();
		}
		

	}

	@Test
	public void testUpdateService() throws JSONException {
		EMIRClient cr1 = new EMIRClient(BaseURI + "/serviceadmin");


		cr1.getClientResource()
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.post(TestValueConstants.getJSONArrayWithMandatoryAttributes()
						);
		

		EMIRClient cr = new EMIRClient(BaseURI + "/serviceadmin");
		JSONObject j = TestValueConstants.getJSONWithMandatoryAttributes();
		j.put(ServiceBasicAttributeNames.SERVICE_TYPE.toString(), "sms");
		JSONArray ja = new JSONArray();
		ja.put(j);
		cr.getClientResource().accept(MediaType.APPLICATION_JSON_TYPE)
				.put(ja);
		
		System.out.println("being updated json document: "+j.toString(2));
		
		EMIRClient cr2 = new EMIRClient(BaseURI
				+ "/serviceadmin?Service_Endpoint_ID=1");
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
				.post(TestValueConstants.getJSONArrayWithMandatoryAttributes());
		EMIRClient cr = new EMIRClient(BaseURI
				+ "/serviceadmin");
		cr.getClientResource().queryParam(ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID.toString(), "1").delete();

		EMIRClient cr2 = new EMIRClient(BaseURI
				+ "/serviceadmin?Service_Endpoint_ID=1");
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
