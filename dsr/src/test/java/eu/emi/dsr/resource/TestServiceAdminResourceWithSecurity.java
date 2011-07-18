/**
 * 
 */
package eu.emi.dsr.resource;

import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import eu.emi.dsr.TestRegistryBaseWithSecurity;
import eu.emi.dsr.client.DSRClient;
import eu.emi.dsr.core.ServiceBasicAttributeNames;

/**
 * @author a.memon
 * 
 */
public class TestServiceAdminResourceWithSecurity extends
		TestRegistryBaseWithSecurity {
	private static JSONObject getDummyServiceDesc() {
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
	public void testRegisterService() {
		JSONObject jo = getDummyServiceDesc();
		DSRClient cr = new DSRClient(BaseURI
				+ "/serviceadmin?serviceurl=http://1", getSecurityProperties());
		cr.getClientResource()
				.accept(MediaType.APPLICATION_JSON_TYPE).post(String.class, jo);
	}

	@Test
	public void testGetServiceInfo() {
		System.out.println("/serviceadmin");
		DSRClient cr = new DSRClient(BaseURI
				+ "/serviceadmin?serviceUrl=http://1", getSecurityProperties());
		JSONObject jo = cr.getClientResource()
				.accept(MediaType.APPLICATION_JSON_TYPE).get(JSONObject.class);
		assertNotNull(jo);
		System.out.println(jo);
	}

	
	@Test
	public void testDeleteResource() throws JSONException {
		DSRClient cr = new DSRClient(BaseURI
				+ "/serviceadmin?serviceUrl=http://1", getSecurityProperties());
		cr.getClientResource().delete();

	}
}
