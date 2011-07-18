/**
 * 
 */
package eu.emi.dsr.resource;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import eu.emi.dsr.TestRegistryBase;
import eu.emi.dsr.client.DSRClient;
import eu.emi.dsr.core.ServiceBasicAttributeNames;

import static org.junit.Assert.*;

/**
 * @author a.memon
 * 
 */
public class TestServiceAdminResource extends TestRegistryBase {

	private static JSONObject getDummyServiceDesc(){
		Map<String, String> map = new HashMap<String, String>();
		map.put(ServiceBasicAttributeNames.SERVICE_URL.getAttributeName(),
				"http://1");
		map.put(ServiceBasicAttributeNames.SERVICE_TYPE.getAttributeName(),
				"jms");
		map.put(ServiceBasicAttributeNames.SERVICE_EXPIRE_ON.getAttributeName(),
				"12-12-2121,12:12");
		JSONObject jo = new JSONObject(map);
		return jo;
	}
	
	

	@Test
	public void testRegisterService() throws JSONException {
		DSRClient cr = new DSRClient(BaseURI + "/serviceadmin");
		cr.getClientResource()
				.accept(MediaType.APPLICATION_JSON_TYPE).post(getDummyServiceDesc());
		
		DSRClient cr1 = new DSRClient(BaseURI
				+ "/serviceadmin?serviceUrl=http://1");
		JSONObject jo = cr1.getClientResource()
				.accept(MediaType.APPLICATION_JSON_TYPE).get(JSONObject.class);
		assertEquals("http://1",jo.get("serviceUrl"));
	}

	private static JSONObject getUpdatedServiceDesc(){
		Map<String, String> map = new HashMap<String, String>();
		map.put(ServiceBasicAttributeNames.SERVICE_URL.getAttributeName(),
				"http://1");
		map.put(ServiceBasicAttributeNames.SERVICE_TYPE.getAttributeName(),
				"sms");
		map.put(ServiceBasicAttributeNames.SERVICE_EXPIRE_ON.getAttributeName(),
				"12-12-2121,12:12");
		map.put(ServiceBasicAttributeNames.SERVICE_OWNER.getAttributeName(),
				"http://1");
		JSONObject jo = new JSONObject(map);
		return jo;
	}
	

	@Test
	public void testUpdateService() throws JSONException {
		DSRClient cr = new DSRClient(BaseURI + "/serviceadmin");
		Map<String, String> map = new HashMap<String, String>();
		map.put("serviceType", "jms");
		map.put("serviceUrl", "http://1");
		JSONObject jo = new JSONObject(map);
		cr.getClientResource().accept(MediaType.APPLICATION_JSON_TYPE).put(getUpdatedServiceDesc());
	}
	
	@Test
	public void testDeleteResource() throws JSONException {
		DSRClient cr = new DSRClient(BaseURI
				+ "/serviceadmin?serviceUrl=http://1");
		cr.getClientResource().delete();

	}
}
