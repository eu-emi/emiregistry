/**
 * 
 */
package eu.emi.emir.infrastructure;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import javax.ws.rs.core.MediaType;

import org.apache.commons.io.FileUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;

import eu.emi.emir.client.EMIRClient;
import eu.emi.emir.client.ServiceBasicAttributeNames;
import eu.emi.emir.client.util.DateUtil;

/**
 * <li>cleanup</li> <li>start child server and mongodb instance</li> <li>start
 * parent server with same mongodb instance (perhaps a different collection
 * name)</li> <li>register with the client-dsr 4. check if the parent dsr have
 * received the notification and updated its database</li> <li>cleanup</li>
 * 
 * Follow the similar steps for update, delete and expire registrations.
 * 
 * Pre-condition: stopped the parent server only child should be running in a separate jvm
 * 
 * @author g.szigeti
 */
public class TestDBSyncTestPart3 {

	@Test
	public void testDelayedRemove() throws JSONException, IOException, InterruptedException{
		//remove one exist entry
		getChildClient("/serviceadmin?Service_Endpoint_URL=http://3").delete();
		try {
			getChildClient("/serviceadmin")
					.queryParam(
							ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
									.getAttributeName(),
							"http://3").get(JSONObject.class);
		} catch (UniformInterfaceException e) {
			assertTrue(e.getResponse().getStatus() == Status.NO_CONTENT
					.getStatusCode());
		}

		
		//remove one none exist entry
		try {
			getChildClient("/serviceadmin?Service_Endpoint_URL=http://non_exist").delete();
			getChildClient("/serviceadmin")
					.queryParam(
							ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
									.getAttributeName(),
							"http://non_exist").get(JSONObject.class);
		} catch (UniformInterfaceException e) {
			assertTrue(e.getResponse().getStatus() == Status.UNAUTHORIZED
					.getStatusCode());
		}
	}

	@Test
	public void testDelayedUpdateRemove() throws JSONException, IOException, InterruptedException{
		// one update to the child server
		JSONObject jo = new JSONObject(FileUtils.readFileToString(new File("src/test/resources/json/serviceinfo2.json")));
		jo = DateUtil.setExpiryTime(jo, 12);

		// Updating the entry
		jo.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_HEALTH_STATEINFO
				.getAttributeName(), "health-state-info-changed");
		System.out.println("updating: " + jo);
		JSONArray jos = new JSONArray();
		jos.put(jo);

		ClientResponse res = getChildClient("/serviceadmin?Service_Endpoint_URL").accept(
				MediaType.APPLICATION_JSON_TYPE).put(ClientResponse.class, jos);
		assertTrue(res.getStatus() == Status.OK.getStatusCode());
		
		Thread.sleep(1000);

		//remove one exist entry
		getChildClient("/serviceadmin?Service_Endpoint_URL=http://2").delete();
		try {
			getChildClient("/serviceadmin")
					.queryParam(
							ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
									.getAttributeName(),
							"http://2").get(JSONObject.class);
		} catch (UniformInterfaceException e) {
			assertTrue(e.getResponse().getStatus() == Status.NO_CONTENT
					.getStatusCode());
		}
	}
	
	@Test
	public void testDelayedUpdate() throws JSONException, IOException, InterruptedException{
		// one update to the child server
		JSONObject jo = new JSONObject(FileUtils.readFileToString(new File("src/test/resources/json/serviceinfo.json")));
		jo = DateUtil.setExpiryTime(jo, 12);

		// Updating the entry
		jo.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_HEALTH_STATE
				.getAttributeName(), "bussy");
		System.out.println("updating: " + jo);
		JSONArray jos = new JSONArray();
		jos.put(jo);

		ClientResponse res = getChildClient("/serviceadmin?Service_Endpoint_URL").accept(
				MediaType.APPLICATION_JSON_TYPE).put(ClientResponse.class, jos);
		assertTrue(res.getStatus() == Status.OK.getStatusCode());
		
		Thread.sleep(1000);

		//Update check
		JSONObject childJO = getChildClient(
				"/serviceadmin?Service_Endpoint_URL=http://1").accept(
				MediaType.APPLICATION_JSON_TYPE).get(JSONObject.class);

		assertEquals(jo.get(ServiceBasicAttributeNames.SERVICE_ENDPOINT_HEALTH_STATEINFO
				.getAttributeName()),
				childJO.get(ServiceBasicAttributeNames.SERVICE_ENDPOINT_HEALTH_STATEINFO
						.getAttributeName()));
	}
	
	@Test
	public void testDelayedRegistrationRemove() throws JSONException, IOException, InterruptedException{
		// one registration to the child server
		JSONObject jo = new JSONObject(FileUtils.readFileToString(new File("src/test/resources/json/serviceinfo4.json")));
		jo = DateUtil.setExpiryTime(jo, 12);
		JSONArray jos = new JSONArray();
		jos.put(jo);
		System.out.println("registering: " + jo);
		ClientResponse res = getChildClient("/serviceadmin").accept(
				MediaType.APPLICATION_JSON_TYPE).post(ClientResponse.class, jos);
		assertTrue(res.getStatus() == Status.OK.getStatusCode());

		Thread.sleep(2000);

		//Registration check
		JSONObject childJO4 = getChildClient(
				"/serviceadmin?Service_Endpoint_URL=http://4").accept(
				MediaType.APPLICATION_JSON_TYPE).get(JSONObject.class);

		assertEquals(jo.get(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
				.getAttributeName()),
				childJO4.get(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
						.getAttributeName()));
		
		Thread.sleep(2000);
		
		//remove one exist entry
		getChildClient("/serviceadmin?Service_Endpoint_URL=http://4").delete();
		try {
			getChildClient("/serviceadmin")
					.queryParam(
							ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
									.getAttributeName(),
							"http://4").get(JSONObject.class);
		} catch (UniformInterfaceException e) {
			assertTrue(e.getResponse().getStatus() == Status.NO_CONTENT
					.getStatusCode());
		}

	}
	
	protected WebResource getChildClient(String path) {
		EMIRClient c = new EMIRClient("http://localhost:9000" + path);
		return c.getClientResource();
	}

	protected WebResource getParentClient(String path) {
		EMIRClient c = new EMIRClient("http://localhost:9001" + path);
		return c.getClientResource();
	}

}
