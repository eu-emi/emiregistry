/**
 * 
 */
package eu.emi.dsr.resource;

import static org.junit.Assert.*;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.Test;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;

import eu.emi.dsr.TestRegistryBase;
import eu.emi.dsr.client.DSRClient;

/**
 * @author a.memon
 * 
 */
public class TestPingResource extends TestRegistryBase {

	@Test
	public void test() {
		DSRClient cr1 = new DSRClient(BaseURI + "/ping");
		assertTrue(cr1.getClientResource()
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.get(ClientResponse.class).getStatus() == Status.OK
				.getStatusCode());
	}

}
