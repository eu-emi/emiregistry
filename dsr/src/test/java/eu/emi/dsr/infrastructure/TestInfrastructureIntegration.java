/**
 * 
 */
package eu.emi.dsr.infrastructure;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import javax.ws.rs.core.MediaType;

import org.apache.commons.io.FileUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;

import eu.emi.client.DSRClient;
import eu.emi.client.ServiceBasicAttributeNames;
import eu.emi.dsr.db.mongodb.MongoDBServiceDatabase;
import eu.emi.dsr.util.DateUtil;

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
 * @author a.memon
 * @author g.szigeti
 */
public class TestInfrastructureIntegration {
	private static ChildServer childs = null;
	private static ParentServer parents = null;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
            /*childs = new ChildServer();
            //(new ChildServer()).start();
            childs.start();
            parents = new ParentServer();
            parents.start();*/
            System.out.println("Child and parent server are running...");
    }

	@Before
	public void setUp() {
		final MongoDBServiceDatabase parentDB = new MongoDBServiceDatabase(
				"localhost", 27017, "emiregistry-parentdb", "services-test");
		parentDB.deleteAll();
		final MongoDBServiceDatabase childDB = new MongoDBServiceDatabase(
				"localhost", 27017, "emiregistry-childdb", "services-test");
		childDB.deleteAll();
	}

	@Test
	public void testRegister() throws JSONException, IOException,
			InterruptedException {
		JSONObject jo = new JSONObject(FileUtils.readFileToString(new File("src/test/resources/json/serviceinfo.json")));
		jo = DateUtil.setExpiryTime(jo, 12);
		System.out.println("registering: " + jo);
		ClientResponse res = getChildClient("/serviceadmin").accept(
				MediaType.APPLICATION_JSON_TYPE).post(ClientResponse.class, jo);
		assertTrue(res.getStatus() == Status.OK.getStatusCode());

		Thread.sleep(2000);

		JSONObject parentJO = getParentClient(
				"/serviceadmin?Service_Endpoint_URL=http://1").accept(
				MediaType.APPLICATION_JSON_TYPE).get(JSONObject.class);

		assertEquals(jo.get(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
				.getAttributeName()),
				parentJO.get(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
						.getAttributeName()));
	}

	@Test
	public void testUpdate() throws JSONException, IOException,
			InterruptedException {
		JSONObject jo = new JSONObject(FileUtils.readFileToString(new File("src/test/resources/json/serviceinfo.json")));
		jo = DateUtil.setExpiryTime(jo, 12);
		System.out.println("registering the service: " + jo);
		getChildClient("/serviceadmin").accept(MediaType.APPLICATION_JSON_TYPE)
				.post(jo);

		JSONObject parentJO = getParentClient(
				"/serviceadmin?Service_Endpoint_URL=http://1").accept(
				MediaType.APPLICATION_JSON_TYPE).get(JSONObject.class);

		assertEquals(jo.get(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
				.getAttributeName()),
				parentJO.get(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
						.getAttributeName()));

		System.out.println("updating the registration");
		jo.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_HEALTH_STATEINFO
				.getAttributeName(), "health-state-info-changed");

		getChildClient("/serviceadmin?Service_Endpoint_URL").accept(
				MediaType.APPLICATION_JSON_TYPE).put(jo);

		parentJO = getParentClient(
				"/serviceadmin?Service_Endpoint_URL=http://1").accept(
				MediaType.APPLICATION_JSON_TYPE).get(JSONObject.class);

		System.out.println(parentJO);
		assertEquals(
				jo.get(ServiceBasicAttributeNames.SERVICE_ENDPOINT_HEALTH_STATEINFO
						.getAttributeName()),
				parentJO.get(ServiceBasicAttributeNames.SERVICE_ENDPOINT_HEALTH_STATEINFO
						.getAttributeName()));

	}

	@Test
	public void testDelete() throws JSONException, IOException {
		JSONObject jo = new JSONObject(FileUtils.readFileToString(new File("src/test/resources/json/serviceinfo.json")));
		jo = DateUtil.setExpiryTime(jo, 12);
		System.out.println("registering: " + jo);
		getChildClient("/serviceadmin").accept(MediaType.APPLICATION_JSON_TYPE)
				.post(jo);

		JSONObject parentJO = getParentClient(
				"/serviceadmin?Service_Endpoint_URL=http://1").accept(
				MediaType.APPLICATION_JSON_TYPE).get(JSONObject.class);

		assertEquals(jo.get(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
				.getAttributeName()),
				parentJO.get(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
						.getAttributeName()));

		getChildClient("/serviceadmin?Service_Endpoint_URL=http://1").delete();
		try {
			getParentClient("/serviceadmin")
					.queryParam(
							ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
									.getAttributeName(),
							"http://1").get(JSONObject.class);
		} catch (UniformInterfaceException e) {
			assertTrue(e.getResponse().getStatus() == Status.NO_CONTENT
					.getStatusCode());

		}

	}


	protected WebResource getChildClient(String path) {
		DSRClient c = new DSRClient("http://localhost:9000" + path);
		return c.getClientResource();
	}

	protected WebResource getParentClient(String path) {
		DSRClient c = new DSRClient("http://localhost:9001" + path);
		return c.getClientResource();
	}

	@After
	public void tearDown() {
		final MongoDBServiceDatabase parentDB = new MongoDBServiceDatabase(
				"localhost", 27017, "emiregistry-parentdb", "services-test");
		parentDB.deleteAll();
		final MongoDBServiceDatabase childDB = new MongoDBServiceDatabase(
				"localhost", 27017, "emiregistry-childdb", "services-test");
		childDB.deleteAll();
	}

}
