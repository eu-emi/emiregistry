/**
 * 
 */
package eu.emi.dsr.boundary;

import java.util.HashMap;
import java.util.Map;

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
	public void testRegisterService() throws JSONException {
		DSRClient cr = new DSRClient(BaseURI + "/serviceadmin");
		
		
		Map<String, String> map = new HashMap<String, String>();
		map.put("servicetype", "jms");
		map.put("serviceurl", "http://1");
		JSONObject jo = new JSONObject(map);
		String str = cr.getClientResource()
				.accept(MediaType.APPLICATION_JSON_TYPE).post(String.class,jo);
		assertNotNull(str);
		System.out.println(str);
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
		Map<String, String> map = new HashMap<String, String>();
		map.put("servicetype", "jms");
		map.put("serviceurl", "http://1");
		JSONObject jo = new JSONObject(map);
		String result = cr.getClientResource().accept(MediaType.APPLICATION_JSON_TYPE).put(String.class,jo);
		assertNotNull(result);
	}
}
