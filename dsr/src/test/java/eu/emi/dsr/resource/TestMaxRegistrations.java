/**
 * 
 */
package eu.emi.dsr.resource;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.core.MediaType;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;

import eu.emi.dsr.TestRegistryBase;
import eu.emi.client.DSRClient;
import eu.emi.dsr.core.ServiceBasicAttributeNames;

/**
 * @author a.memon
 *
 */
public class TestMaxRegistrations extends TestRegistryBase{
	@Test
	public void test(){
		JSONArray ja =new JSONArray();
		Map<String, String> map = new HashMap<String, String>();
		for (int i = 0; i < 110; i++) {
			map.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL.getAttributeName(), "http://"+UUID.randomUUID());
			ja.put(new JSONObject(map));
		}
		DSRClient c = new DSRClient(BaseURI+"/serviceadmin");
		
		ClientResponse res = c.getClientResource()
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.post(ClientResponse.class, ja);
		System.out.println(res.getEntity(String.class));
		assertTrue(res.getStatus()==Status.FORBIDDEN.getStatusCode());
		
	}
}
