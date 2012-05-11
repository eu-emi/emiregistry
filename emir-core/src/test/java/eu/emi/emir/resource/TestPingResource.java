/**
 * 
 */
package eu.emi.emir.resource;

import static org.junit.Assert.*;

import javax.ws.rs.core.MediaType;

import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;

import eu.emi.emir.TestRegistryBase;
import eu.emi.emir.client.DSRClient;

/**
 * @author a.memon
 * 
 */
public class TestPingResource extends TestRegistryBase {

	@Test
	public void test() throws Exception {
		DSRClient cr1 = new DSRClient(BaseURI + "/ping");
		assertTrue(cr1.getClientResource()
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.get(ClientResponse.class).getStatus() == Status.OK
				.getStatusCode());
		
		JSONObject jo = cr1.getClientResource()
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.get(ClientResponse.class).getEntity(JSONObject.class);
		
		System.out.println(jo);
		
		assertNotNull(jo);
	}

}
