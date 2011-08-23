/**
 * 
 */
package eu.emi.dsr.infrastructure;

import static org.junit.Assert.*;
import java.io.IOException;

import javax.ws.rs.core.MediaType;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;

import eu.emi.dsr.client.DSRClient;
import eu.emi.dsr.core.ServiceBasicAttributeNames;
import eu.emi.dsr.util.DateUtil;
import eu.emi.dsr.util.ServiceUtil;

/**
 * <li>cleanup</li> <li>start child server and mongodb instance</li> <li>start
 * parent server with same mongodb instance (perhaps a different collection
 * name)</li> <li>register with the client-dsr 4. check if the parent dsr have
 * received the notification and updated its database</li> <li>cleanup</li>
 * 
 * Follow the similar steps for update, delete and expire registrations.
 * 
 * Pre-condition: Both child and parent should be running in a separate jvm
 * 
 * @author g.szigeti
 */
public class TestDBSyncTestPart2 {

	@Test
	public void testDelayedRegistrationCheck() throws JSONException, IOException, InterruptedException{
		// one registration to the child server
		JSONObject jo = new JSONObject(
				ServiceUtil
						.convertFileToString("src/test/resources/serviceinfo3.json"));
		jo = DateUtil.setExpiryTime(jo, 12);
		System.out.println("registering: " + jo);
		ClientResponse res = getChildClient("/serviceadmin").accept(
				MediaType.APPLICATION_JSON_TYPE).post(ClientResponse.class, jo);
		assertTrue(res.getStatus() == Status.OK.getStatusCode());

		Thread.sleep(1000);

		//Registration check
		JSONObject parentJO3 = getChildClient(
				"/serviceadmin?Service_Endpoint_URL=http://3").accept(
				MediaType.APPLICATION_JSON_TYPE).get(JSONObject.class);

		assertEquals(jo.get(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
				.getAttributeName()),
				parentJO3.get(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
						.getAttributeName()));
		
		Thread.sleep(1000);

		// sync messages arrive checking
		JSONObject parentJO1 = getParentClient(
				"/serviceadmin?Service_Endpoint_URL=http://1").accept(
				MediaType.APPLICATION_JSON_TYPE).get(JSONObject.class);

		assertEquals("http://1",
				parentJO1.get(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
						.getAttributeName()));


		JSONObject parentJO2 = getParentClient(
				"/serviceadmin?Service_Endpoint_URL=http://2").accept(
				MediaType.APPLICATION_JSON_TYPE).get(JSONObject.class);

		assertEquals("http://2",
				parentJO2.get(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
						.getAttributeName()));
	}

	protected WebResource getChildClient(String path) {
		DSRClient c = new DSRClient("http://localhost:9000" + path);
		return c.getClientResource();
	}

	protected WebResource getParentClient(String path) {
		DSRClient c = new DSRClient("http://localhost:9001" + path);
		return c.getClientResource();
	}

}
