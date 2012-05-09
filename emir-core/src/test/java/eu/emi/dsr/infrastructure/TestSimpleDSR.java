/**
 * 
 */
package eu.emi.dsr.infrastructure;

import static org.junit.Assert.*;


import java.io.File;
import java.io.IOException;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXB;

import org.apache.commons.io.FileUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;

import eu.emi.client.DSRClient;
import eu.emi.client.ServiceBasicAttributeNames;
import eu.emi.emir.util.DateUtil;
import eu.eu_emi.emiregistry.QueryResult;

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

public class TestSimpleDSR {
	private static String serverUrl = "http://sl5-test2.grid.niif.hu:54321";
	//private static String serverUrl = "http://dhcp16.ki.iif.hu:54321";
	//private static String serverUrl = "http://193.6.222.69:54321";

	
	
	private static JSONObject jo = null;
	private static JSONObject jo2 = null;
	private String child = "http://chirld.url";
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
		jo = new JSONObject(FileUtils.readFileToString(new File("src/test/resources/json/serviceinfo.json")));
		jo2 = new JSONObject(FileUtils.readFileToString(new File("src/test/resources/json/serviceinfo2.json")));

		//Delete all elements
		String url = jo.getString(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
				.getAttributeName());
		String url2 = jo2.getString(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
				.getAttributeName());
		try{
			getChildClient("/serviceadmin?Service_Endpoint_URL=" + url).delete();	
			System.out.println("clean up: " + url);
			getChildClient("/serviceadmin?Service_Endpoint_URL=" + url2).delete();	
			System.out.println("clean up: " + url2);
		} catch (UniformInterfaceException e){
			System.out.println("DB clean");
		} catch (ClientHandlerException e){
			System.out.println("No route to host ("+serverUrl+")!");
			fail();
		}
    }

	@Test
	public void testChildrenPOST() throws JSONException, IOException, InterruptedException{
		System.out.println("/children POST test");
		assertTrue(getChildClient("/children")
				.queryParam(
						ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
						.getAttributeName(),
						child).post(ClientResponse.class).getStatus() == Status.OK
				.getStatusCode());
		
		System.out.println("	"+"OK");
	}
	
	@Test
	public void testChildrenGET() throws JSONException, IOException, InterruptedException{
		System.out.println("/children GET test");
		ClientResponse res = getChildClient("/children").accept(MediaType.APPLICATION_JSON_TYPE)
				.get(ClientResponse.class);
		assertTrue(res.getStatus() == Status.OK.getStatusCode());

		JSONArray jo = res.getEntity(JSONArray.class);
		
		System.out.println("	"+jo);
		assertNotNull(jo);
		System.out.println("	"+"OK");
	}
	
	@Test
	public void testModelGETJSON() throws JSONException, IOException, InterruptedException{
		System.out.println("/model GET JSON test");
		// JSON
		ClientResponse res = getChildClient("/model").accept(MediaType.APPLICATION_JSON_TYPE)
				.get(ClientResponse.class);
		assertTrue(res.getStatus() == Status.OK.getStatusCode());

		String ret = res.getEntity(String.class);
		
		System.out.println("	"+ret);

		System.out.println("	"+"OK");
	}
	
	@Test
	public void testModelGETHTML() throws JSONException, IOException, InterruptedException{
		System.out.println("/model GET HTML test");
		//TEXT_HTML
		ClientResponse res = getChildClient("/model").accept(MediaType.TEXT_HTML)
				.get(ClientResponse.class);
		assertTrue(res.getStatus() == Status.OK.getStatusCode());

		String ret = res.getEntity(String.class);
		
		System.out.println("	"+ret);

		System.out.println("	"+"OK");
	}
	
	@Test
	public void testNeighborsGET() throws JSONException, IOException, InterruptedException{
		System.out.println("/neighbors GET test");
		ClientResponse res = getChildClient("/neighbors").accept(MediaType.APPLICATION_JSON_TYPE)
				.get(ClientResponse.class);
		assertTrue(res.getStatus() == Status.OK.getStatusCode());

		JSONArray jo = res.getEntity(JSONArray.class);
		
		System.out.println("	"+jo);

		System.out.println("	"+"OK");
	}

	@Test
	public void testParentGET() throws JSONException, IOException, InterruptedException{
		System.out.println("/parent GET test");
		ClientResponse res = getChildClient("/parent").accept(MediaType.APPLICATION_JSON_TYPE)
				.get(ClientResponse.class);
		assertTrue(res.getStatus() == Status.OK.getStatusCode());

		String jo = res.getEntity(String.class);
		
		System.out.println("	"+jo);
		System.out.println("	"+"OK");
	}

	@Test
	public void testPing() throws JSONException, IOException, InterruptedException{
		System.out.println("Ping test");
		ClientResponse res = getChildClient("/ping").accept(MediaType.APPLICATION_JSON_TYPE)
				.get(ClientResponse.class);
		assertTrue(res.getStatus() == Status.OK.getStatusCode());

		JSONObject jo = res.getEntity(JSONObject.class);
		
		System.out.println("	"+jo);
		assertNotNull(jo);
		System.out.println("	"+"OK");
	}

	@Test
	public void testServiceadminPOST() throws JSONException, IOException, InterruptedException{
		System.out.println("/serviceadmin POST test");
		// one registration to the child server
		jo = DateUtil.setCreationTime(jo, 0);
		jo = DateUtil.setExpiryTimeWithHours(jo, 1);

		JSONArray jos = new JSONArray();
		jos.put(jo);

		Thread.sleep(1000);

		// 2nd registration to the child server
		jo2 = DateUtil.setCreationTime(jo2, 0);
		jo2 = DateUtil.setExpiryTimeWithHours(jo2, 1);
		jos.put(jo2);
		//System.out.println("registering: " + jos.toString());
		ClientResponse res = getChildClient("/serviceadmin").accept(MediaType.APPLICATION_JSON_TYPE)
				.post(ClientResponse.class, jos);
		assertTrue(res.getStatus() == Status.OK.getStatusCode());

		JSONArray rjo = res.getEntity(JSONArray.class);
		System.out.println("	"+rjo.toString());
        assertFalse(rjo.length()==0);
		System.out.println("	"+"OK");
	}

	@Test
	public void testServiceadminGET() throws JSONException, IOException, InterruptedException{
		System.out.println("/serviceadmin GET test");
		//Registration check
		String url = jo.getString(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
				.getAttributeName());

		ClientResponse res = getChildClient(
				"/serviceadmin?Service_Endpoint_URL="+url).accept(
				MediaType.APPLICATION_JSON_TYPE).get(ClientResponse.class);

		assertTrue(res.getStatus() == Status.OK.getStatusCode());

		JSONObject responseJO = res.getEntity(JSONObject.class);
	
		assertEquals(jo.get(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
				.getAttributeName()),
				responseJO.get(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
						.getAttributeName()));
		System.out.println("	"+"OK");
	}
	
	@Test
	public void testServiceadminPUT() throws JSONException, IOException, InterruptedException{
		System.out.println("/serviceadmin PUT test");
		jo.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_HEALTH_STATEINFO
				.getAttributeName(), "health-state-info-changed");
		JSONArray jos = new JSONArray();
		jos.put(jo);

		ClientResponse res = getChildClient("/serviceadmin?Service_Endpoint_URL").accept(
				MediaType.APPLICATION_JSON_TYPE).put(ClientResponse.class, jos);
		assertTrue(res.getStatus() == Status.OK.getStatusCode());

		JSONArray rjo = res.getEntity(JSONArray.class);
		System.out.println("	"+rjo.toString());

		System.out.println("	"+"OK");
	}
	
	@Test
	public void testServiceadminDELETE() throws JSONException, IOException, InterruptedException{
		System.out.println("/serviceadmin DELETE test");
		String url = jo.getString(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
				.getAttributeName());
		getChildClient("/serviceadmin?Service_Endpoint_URL=" + url).delete();	
		System.out.println("	"+"OK");

		System.out.println("registration again the entry for the next resource");
		JSONArray jos = new JSONArray();
		jos.put(jo);

		ClientResponse res = getChildClient("/serviceadmin").accept(MediaType.APPLICATION_JSON_TYPE)
				.post(ClientResponse.class, jos);
		assertTrue(res.getStatus() == Status.OK.getStatusCode());
	
	}

	@Test
	public void testServicesUrlsGET() throws JSONException, IOException, InterruptedException{
		System.out.println("/services/urls GET test");
		ClientResponse res = getChildClient("/services/urls").accept(MediaType.APPLICATION_JSON_TYPE)
				.get(ClientResponse.class);
		assertTrue(res.getStatus() == Status.OK.getStatusCode());

		JSONObject jo = res.getEntity(JSONObject.class);
		
		System.out.println("	"+jo);

		System.out.println("	"+"OK");
	}
	
	@Test
	public void testServicesTypesGET() throws JSONException, IOException, InterruptedException{
		System.out.println("/services/types GET test");
		ClientResponse res = getChildClient("/services/types").accept(MediaType.APPLICATION_JSON_TYPE)
				.get(ClientResponse.class);
		assertTrue(res.getStatus() == Status.OK.getStatusCode());

		JSONArray jo = res.getEntity(JSONArray.class);
		
		System.out.println("	"+jo);

		System.out.println("	"+"OK");
	}
	
	@Test
	public void testServicesGETJSON() {
		System.out.println("/services GET JSON test");
		try {
			String url = jo.getString(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
					.getAttributeName());
			JSONArray o = getChildClient("/services?Service_Endpoint_URL="+url)
					.accept(MediaType.APPLICATION_JSON_TYPE)
					.get(JSONArray.class);
			System.out.println("	"+o.toString());
			assertTrue(o.length() == 1);
		} catch (Exception e) {
			fail();
			e.printStackTrace();
		}
		System.out.println("	"+"OK");

	}
	
	@Test
	public void testServicesPOSTJSON() throws JSONException, IOException, InterruptedException{
		System.out.println("/services POST JSON test");
		try {
			String url = jo.getString(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
					.getAttributeName());
			JSONObject  query = new JSONObject("{Service_Endpoint_URL: \""+url+"\"}");
			JSONArray o = getChildClient("/services")
					.accept(MediaType.APPLICATION_JSON_TYPE)
					.post(JSONArray.class, query);
			
			System.out.println("	"+o.toString());
			assertTrue(o.length() == 1);
		} catch (Exception e) {
			fail();
			e.printStackTrace();
		}
		System.out.println("	"+"OK");
	}
	
	@Test
	public void testServicesPOSTXML() throws JSONException, IOException, InterruptedException{
		System.out.println("/services POST XML test");
		try {
			String url = jo.getString(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
					.getAttributeName());
			JSONObject  query = new JSONObject("{Service_Endpoint_URL: \""+url+"\"}");
			QueryResult o = getChildClient("/services")
					.accept(MediaType.APPLICATION_XML_TYPE)
					.post(QueryResult.class, query);
			
			System.out.println("	"+o.toString());
			JAXB.marshal(o, System.out);
		} catch (Exception e) {
			fail();
			e.printStackTrace();
		}
		System.out.println("	"+"OK");
	}
	
	@Test
	public void testServicesGETXML() throws JSONException, IOException, InterruptedException{
		System.out.println("/services GET XML test");
		try {
			QueryResult o = getChildClient("/services")
					.accept(MediaType.APPLICATION_XML_TYPE)
					.get(QueryResult.class);
			
			System.out.println("	"+o.toString());
			JAXB.marshal(o, System.out);
		} catch (Exception e) {
			fail();
			e.printStackTrace();
		}
		System.out.println("	"+"OK");
	}
	
	@Test
	public void testServicesQueryXMLGET() throws JSONException, IOException, InterruptedException{
		System.out.println("/services/query.xml GET test");
		System.out.println("Deprecated");
		try {
			String stype = jo.getString(ServiceBasicAttributeNames.SERVICE_TYPE
					.getAttributeName());

			QueryResult o = getChildClient("/services/query.xml?Service_Type="+stype)
					.accept(MediaType.APPLICATION_XML_TYPE)
					.get(QueryResult.class);
			
			System.out.println("	"+o.toString());
			JAXB.marshal(o, System.out);
		} catch (Exception e) {
			fail();
			e.printStackTrace();
		}
		System.out.println("	"+"OK");
	}
	
	@Test
	public void testServicesPagedQueryGET() throws JSONException, IOException, InterruptedException{
		System.out.println("/services/pagedquery GET test");
		try {
			String stype = jo.getString(ServiceBasicAttributeNames.SERVICE_TYPE
					.getAttributeName());

			JSONObject o = getChildClient("/services/pagedquery?Service_Type="+stype+"&pageSize=10")
					.accept(MediaType.APPLICATION_JSON_TYPE)
					.get(JSONObject.class);
			
			System.out.println("	"+o.toString());
		} catch (Exception e) {
			fail();
			e.printStackTrace();
		}

		System.out.println("	"+"OK");
	}
	
	protected static WebResource getChildClient(String path) {
		DSRClient c = new DSRClient(serverUrl + path);
		return c.getClientResource();
	}

	protected WebResource getParentClient(String path) {
		DSRClient c = new DSRClient("http://localhost:9001" + path);
		return c.getClientResource();
	}

}
