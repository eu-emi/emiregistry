/**
 * 
 */
package eu.emi.dsr.infrastructure;

import static org.junit.Assert.*;


import java.io.IOException;
import javax.ws.rs.core.MediaType;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;

import eu.emi.dsr.client.DSRClient;
import eu.emi.dsr.core.ServiceBasicAttributeNames;
import eu.emi.dsr.db.mongodb.MongoDBServiceDatabase;
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
 * Pre-condition: child should be running in a separate jvm
 * 
 * @author g.szigeti
 */
/** Creates and runs a new JVM.  This method is private now because it cannot change the classpath entries to 
       *  absolute paths, so it should not be used.
       *
       *  @param mainClass Class to run
       *  @param classParams Parameters to pass to the main class
       *  @param jvmParams Array of additional command-line parameters to pass to JVM
       *  @param workDir working directory
       *
       *  @return {@link Process} object corresponding to the executed JVM
       *
       *  @throws IOException
       */

public class TestInfrastructureIntegrationSyncTestPart1 {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
 		final MongoDBServiceDatabase parentDB = new MongoDBServiceDatabase(
				"localhost", 27017, "emiregistry-parentdb", "services-test");
		parentDB.deleteAll();
		final MongoDBServiceDatabase childDB = new MongoDBServiceDatabase(
				"localhost", 27017, "emiregistry-childdb", "services-test");
		childDB.deleteAll();
	}

	@Test
	public void testDelayedRegistration() throws JSONException, IOException, InterruptedException{
		// one registration to the child server
		JSONObject jo = new JSONObject(
				ServiceUtil
						.convertFileToString("src/test/resources/serviceinfo.json"));
		jo = DateUtil.setExpiryTime(jo, 12);
		System.out.println("registering: " + jo);
		ClientResponse res = getChildClient("/serviceadmin").accept(
				MediaType.APPLICATION_JSON_TYPE).post(ClientResponse.class, jo);
		assertTrue(res.getStatus() == Status.OK.getStatusCode());

		Thread.sleep(2000);

		//Registration check
		JSONObject childJO = getChildClient(
				"/serviceadmin?Service_Endpoint_URL=http://1").accept(
				MediaType.APPLICATION_JSON_TYPE).get(JSONObject.class);

		assertEquals(jo.get(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
				.getAttributeName()),
				childJO.get(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
						.getAttributeName()));
		
		Thread.sleep(2000);

		// 2nd registration to the child server
		JSONObject jo2 = new JSONObject(
				ServiceUtil
						.convertFileToString("src/test/resources/serviceinfo2.json"));
		jo2 = DateUtil.setExpiryTime(jo2, 12);
		System.out.println("registering2: " + jo2);
		ClientResponse res2 = getChildClient("/serviceadmin").accept(MediaType.APPLICATION_JSON_TYPE)
				.post(ClientResponse.class, jo2);
		assertTrue(res2.getStatus() == Status.OK.getStatusCode());

		Thread.sleep(2000);

		// 2nd Registration check
		JSONObject childJO2 = getChildClient(
				"/serviceadmin?Service_Endpoint_URL=http://2").accept(
				MediaType.APPLICATION_JSON_TYPE).get(JSONObject.class);

		assertEquals(jo2.get(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
				.getAttributeName()),
				childJO2.get(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
						.getAttributeName()));
		
		// Updating the entry
		jo.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_HEALTH_STATEINFO
				.getAttributeName(), "health-state-info-changed");
		System.out.println("updating: " + jo);

		ClientResponse resu = getChildClient("/serviceadmin?Service_Endpoint_URL").accept(
				MediaType.APPLICATION_JSON_TYPE).put(ClientResponse.class, jo);
		assertTrue(resu.getStatus() == Status.OK.getStatusCode());
		Thread.sleep(2000);

		//Update check
		JSONObject childJOu = getChildClient(
				"/serviceadmin?Service_Endpoint_URL=http://1").accept(
				MediaType.APPLICATION_JSON_TYPE).get(JSONObject.class);

		assertEquals(jo.get(ServiceBasicAttributeNames.SERVICE_ENDPOINT_HEALTH_STATEINFO
				.getAttributeName()),
				childJOu.get(ServiceBasicAttributeNames.SERVICE_ENDPOINT_HEALTH_STATEINFO
						.getAttributeName()));
	}

	@Test
	public void testDelayedUpdate() throws InterruptedException, JSONException, IOException{
		// one registration to the child server
		JSONObject jo = new JSONObject(
				ServiceUtil
						.convertFileToString("src/test/resources/serviceinfo.json"));
		jo = DateUtil.setExpiryTime(jo, 12);

		// Updating the entry
		System.out.println("updateing: " + jo);
		jo.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_HEALTH_STATEINFO
				.getAttributeName(), "health-state-info-changed");

		ClientResponse res = getChildClient("/serviceadmin?Service_Endpoint_URL").accept(
				MediaType.APPLICATION_JSON_TYPE).put(ClientResponse.class, jo);
		assertTrue(res.getStatus() == Status.OK.getStatusCode());
		Thread.sleep(2000);

		//Update check
		JSONObject childJO = getChildClient(
				"/serviceadmin?Service_Endpoint_URL=http://1").accept(
				MediaType.APPLICATION_JSON_TYPE).get(JSONObject.class);

		assertEquals(jo.get(ServiceBasicAttributeNames.SERVICE_ENDPOINT_HEALTH_STATEINFO
				.getAttributeName()),
				childJO.get(ServiceBasicAttributeNames.SERVICE_ENDPOINT_HEALTH_STATEINFO
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
