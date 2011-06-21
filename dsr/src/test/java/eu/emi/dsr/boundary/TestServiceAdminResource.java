/**
 * 
 */
package eu.emi.dsr.boundary;

import javax.ws.rs.core.MediaType;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import eu.emi.dsr.TestRegistryBase;
import eu.emi.dsr.client.DSRClient;

import static org.junit.Assert.*;

/**
 * @author a.memon
 * 
 */
public class TestServiceAdminResource extends TestRegistryBase {

	@Test
	public void testGetServiceByUrl() throws JSONException {
		DSRClient cr = new DSRClient(BaseURI
				+ "/serviceadmin?serviceurl=http://1");
		JSONObject jo = cr.getClientResource()
				.accept(MediaType.APPLICATION_JSON_TYPE).get(JSONObject.class);

		System.out.println(jo.get("serviceurl"));
	}

	@Test
	public void testAddService() throws JSONException {
		DSRClient cr = new DSRClient(BaseURI + "/serviceadmin");
		JSONObject jo = new JSONObject();
		jo.append("serviceurl", "http://1");
		JSONObject result = cr.getClientResource()
				.accept(MediaType.APPLICATION_JSON_TYPE).post(JSONObject.class,jo);
		assertNotNull(result.get("serviceurl"));
	}

	@Test
	public void testDeleteResource() throws JSONException {
		DSRClient cr = new DSRClient(BaseURI
				+ "/serviceadmin?serviceurl=http://1");
		cr.getClientResource().delete();

	}

	@Test
	public void testUpdateService() throws JSONException {
		DSRClient cr = new DSRClient(BaseURI + "/serviceadmin");
		JSONObject jo = new JSONObject();
		jo.append("serviceurl", "http://1");
		JSONObject result = cr.getClientResource().accept(MediaType.APPLICATION_JSON_TYPE).put(JSONObject.class,jo);
		assertNotNull(result.get("serviceurl"));
	}
}
