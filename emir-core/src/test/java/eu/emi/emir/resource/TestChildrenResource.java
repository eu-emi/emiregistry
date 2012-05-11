/**
 * 
 */
package eu.emi.emir.resource;

import static org.junit.Assert.*;

import javax.ws.rs.core.MediaType;

import org.codehaus.jettison.json.JSONArray;
import org.junit.Test;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;

import eu.emi.emir.TestRegistryBase;
import eu.emi.emir.client.DSRClient;
import eu.emi.emir.client.ServiceBasicAttributeNames;

/**
 * @author g.szigeti
 * 
 */
public class TestChildrenResource extends TestRegistryBase {

	@Test
	public void emptyTest() throws Exception {
		// get the list of children's
		DSRClient client = new DSRClient(BaseURI + "/children");
		ClientResponse res = client.getClientResource()
				.accept(MediaType.APPLICATION_JSON_TYPE)
					.get(ClientResponse.class);

		assertTrue(res.getStatus() == Status.OK.getStatusCode());		
		JSONArray jos = res.getEntity(JSONArray.class);
		
		assertTrue(jos.length()==0);
		System.out.println(jos.toString());
		
		assertNotNull(jos);
	}

	@Test
	public void childCheckinTest() throws Exception {
		DSRClient client = new DSRClient(BaseURI + "/children");
		// checkin one child
		assertTrue(client.getClientResource()
				.queryParam(
						ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
						.getAttributeName(),
				"http://1").post(ClientResponse.class).getStatus() == Status.OK
				.getStatusCode());
		
		// get the list of children's
		ClientResponse res = client.getClientResource()
						.accept(MediaType.APPLICATION_JSON_TYPE)
							.get(ClientResponse.class);
		
		assertTrue(res.getStatus() == Status.OK.getStatusCode());		
		JSONArray jos = res.getEntity(JSONArray.class);
		
		assertTrue(jos.length()==1);
		System.out.println(jos.toString());
		
		assertNotNull(jos);
	}

	@Test
	public void moreChildCheckinTest() throws Exception {
		int modulo = 5;
		DSRClient client = new DSRClient(BaseURI + "/children");
		// checkin more the one child
		for (int i=0; i < 10; i++) {
			assertTrue(client.getClientResource()
					.queryParam(
							ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
							.getAttributeName(),
					"http://"+i%modulo).post(ClientResponse.class).getStatus() == Status.OK
					.getStatusCode());
		}
		
		// get the list of children's
		ClientResponse res = client.getClientResource()
						.accept(MediaType.APPLICATION_JSON_TYPE)
							.get(ClientResponse.class);
		
		assertTrue(res.getStatus() == Status.OK.getStatusCode());		
		JSONArray jos = res.getEntity(JSONArray.class);
		
		assertTrue(jos.length()==modulo);
		System.out.println(jos.toString());
		
		assertNotNull(jos);
	}
	
	@Test
	public void firstCheckinTest() throws Exception {
		DSRClient client = new DSRClient(BaseURI + "/children");
		// checkin one child
		ClientResponse res = client.getClientResource()
				.queryParam(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
						.getAttributeName(),
				"http://first_checkin").post(ClientResponse.class);
		assertTrue(res.getStatus() == Status.OK.getStatusCode());
		assertTrue(res.getEntity(String.class).equals("First registration"));
	}
	
	@Test
	public void emptyURLCheckinTest() throws Exception {
		DSRClient client = new DSRClient(BaseURI + "/children");
		// checkin one child
		ClientResponse res = client.getClientResource()
				.queryParam(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
						.getAttributeName(),
				"").post(ClientResponse.class);
		assertTrue(res.getStatus() == Status.BAD_REQUEST.getStatusCode());
		assertTrue(res.getEntity(String.class).equals("Empty endpoint given!"));
	}
	
}
