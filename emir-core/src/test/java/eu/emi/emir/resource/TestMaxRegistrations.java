/**
 * 
 */
package eu.emi.emir.resource;

import static org.junit.Assert.*;

import java.util.UUID;

import javax.ws.rs.core.MediaType;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;

import eu.emi.emir.TestRegistryBase;
import eu.emi.emir.client.EMIRClient;
import eu.emi.emir.client.ServiceBasicAttributeNames;
import eu.emi.emir.client.TestValueConstants;

/**
 * @author a.memon
 *
 */
public class TestMaxRegistrations extends TestRegistryBase{
	@Test
	public void test() throws JSONException{
		JSONArray ja =new JSONArray();
		for (int i = 0; i < 110; i++) {
			JSONObject jo = TestValueConstants.getJSONWithMandatoryAttributes().put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID.getAttributeName(), "http://"+UUID.randomUUID());
			ja.put(jo);
		}
		EMIRClient c = new EMIRClient(BaseURI+"/serviceadmin");
		
		ClientResponse res = c.getClientResource()
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.post(ClientResponse.class, ja);
		System.out.println(res.getEntity(String.class));
		assertTrue(res.getStatus()==Status.FORBIDDEN.getStatusCode());
		
	}
}
