/**
 * 
 */
package eu.emi.core;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.junit.Test;
import org.restlet.resource.ClientResource;

import eu.emi.dsr.TestRegistryBase;

/**
 * @author a.memon
 * 
 */
public class TestService extends TestRegistryBase {

	@Test
	public void testInsertServiceDescription() {
		ClientResource cr = new ClientResource(BaseURI + "/service");

		Map<String, String> map = new HashMap<String, String>();
		map.put("id", "1");
		map.put("url", "http://jms");
		map.put("type", "job management service");
		JSONObject data = new JSONObject(map);
		String id = cr.post(data, String.class);
		assertEquals("1", map.get("id"));
	}

	@Test
	public void testGetServiceDescriptionById() {
		ClientResource cr = new ClientResource(BaseURI + "/service/1");
		JSONObject jo = cr.get(JSONObject.class);
	}
}
