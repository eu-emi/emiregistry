/**
 * 
 */
package eu.emi.dsr.core;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.eclipse.jetty.io.View;
import org.junit.Test;


import eu.emi.dsr.TestRegistryBase;
import eu.emi.dsr.client.DSRClient;

/**
 * @author a.memon
 * 
 */
public class TestService extends TestRegistryBase {

	@Test
	public void testInsertServiceDescription() {
		
		DSRClient cr = new DSRClient(BaseURI + "/service");
		
		Map<String, String> map = new HashMap<String, String>();
		map.put("id", "1");
		map.put("serviceurl", "http://jms");
		map.put("servicetype", "job management service");
		JSONObject data = new JSONObject(map);
		String id = cr.getClientResource().post(String.class,data);
		assertEquals("1", map.get("id"));
	}

	@Test
	public void testGetService() {
		DSRClient cr = new DSRClient(BaseURI + "/service/id");
		Map<String, String> map = new HashMap<String, String>();
		map.put("url", "http://jms");
		map.put("type", "job management service");
		JSONObject data = new JSONObject(map);
		JSONObject jo = cr.getClientResource().get(JSONObject.class);
		
		System.out.println(jo);
	}
	
	
	@Test
	public void testUpdateServiceDescription() throws JSONException {
		DSRClient cr = new DSRClient(BaseURI + "/service");
		JSONObject serviceDescription = new JSONObject();
		serviceDescription.append("serviceurl", "http://serviceurl");
		serviceDescription.append("servicetype", "jms");
		cr.getClientResource().accept(MediaType.APPLICATION_JSON_TYPE).put(serviceDescription);
	}
	
	@Test
	public void testRemoveServiceDescription() {
		DSRClient cr = new DSRClient(BaseURI + "/service/1");
		cr.getClientResource().delete();
//		JSONObject jo = cr.get(JSONObject.class);
	}
}
