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
import org.junit.BeforeClass;
import org.junit.Test;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;

import eu.emi.emir.client.EMIRClient;
import eu.emi.emir.client.ServiceBasicAttributeNames;
import eu.emi.emir.client.util.DateUtil;
import eu.emi.emir.client.util.ServiceUtil;
import eu.emi.emir.db.mongodb.MongoDBServiceDatabase;

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

public class TestDBSyncTestPart1 {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    	ServiceUtil.initLogger("src/test/resources/conf/log4j.properties");
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
		JSONObject jo = new JSONObject(FileUtils.readFileToString(new File("src/test/resources/json/serviceinfo.json")));
		jo = DateUtil.setExpiryTime(jo, 12);
		JSONArray jos = new JSONArray();
		jos.put(jo);

		Thread.sleep(1000);

		// 2nd registration to the child server
		JSONObject jo2 = new JSONObject(FileUtils.readFileToString(new File("src/test/resources/json/serviceinfo2.json")));
		jo2 = DateUtil.setExpiryTime(jo2, 12);
		jos.put(jo2);
		System.out.println("registering: " + jos.toString());
		ClientResponse res = getChildClient("/serviceadmin").accept(MediaType.APPLICATION_JSON_TYPE)
				.post(ClientResponse.class, jos);
		assertTrue(res.getStatus() == Status.OK.getStatusCode());

		Thread.sleep(1000);

		//Registration check
		JSONObject childJO = getChildClient(
				"/serviceadmin?Service_Endpoint_URL=http://1").accept(
				MediaType.APPLICATION_JSON_TYPE).get(JSONObject.class);

		assertEquals(jo.get(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
				.getAttributeName()),
				childJO.get(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
						.getAttributeName()));

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
				MediaType.APPLICATION_JSON_TYPE).put(ClientResponse.class, jos);
		assertTrue(resu.getStatus() == Status.OK.getStatusCode());
		Thread.sleep(1000);

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
		JSONObject jo = new JSONObject(FileUtils.readFileToString(new File("src/test/resources/json/serviceinfo.json")));
		jo = DateUtil.setExpiryTime(jo, 12);

		// Updating the entry
		System.out.println("updating: " + jo);
		jo.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_HEALTH_STATEINFO
				.getAttributeName(), "health-state-info-changed");
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

	protected WebResource getChildClient(String path) {
		EMIRClient c = new EMIRClient("http://localhost:9000" + path);
		return c.getClientResource();
	}

	protected WebResource getParentClient(String path) {
		EMIRClient c = new EMIRClient("http://localhost:9001" + path);
		return c.getClientResource();
	}

}
