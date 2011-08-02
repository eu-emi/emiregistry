/**
 * 
 */
package eu.emi.dsr.resource;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import eu.emi.dsr.TestRegistryBaseWithSecurity;
import eu.emi.dsr.client.DSRClient;
import eu.emi.dsr.core.ServiceBasicAttributeNames;
import eu.emi.dsr.util.ServiceUtil;

/**
 * @author a.memon
 * 
 */
public class TestServiceAdminResourceWithSecurity extends
		TestRegistryBaseWithSecurity {
	private static JSONObject getDummyServiceDesc() {
		Map<String, String> map = new HashMap<String, String>();
		map.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
				.getAttributeName(), "http://1");
		map.put(ServiceBasicAttributeNames.SERVICE_TYPE.getAttributeName(),
				"jms");
		
		JSONObject date = new JSONObject();
		Calendar c = Calendar.getInstance();
		c.add(c.MONTH, 12);
		try {
			date.put("$date", ServiceUtil.toUTCFormat(c.getTime()));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		
		JSONObject jo = new JSONObject(map);
		try {
			jo.put(ServiceBasicAttributeNames.SERVICE_EXPIRE_ON.getAttributeName(),
					date);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jo;
	}

	@Test
	public void testRegisterService() throws JSONException {
		JSONObject jo = getDummyServiceDesc();
		DSRClient cr = new DSRClient(BaseURI
				+ "/serviceadmin?Service_Endpoint_URL=http://1", getSecurityProperties());
		cr.getClientResource()
				.accept(MediaType.APPLICATION_JSON_TYPE).post(String.class, jo);
		System.out.println("/serviceadmin");
		DSRClient cr1 = new DSRClient(BaseURI
				+ "/serviceadmin?Service_Endpoint_URL=http://1", getSecurityProperties());
		JSONObject jo1 = cr1.getClientResource()
				.accept(MediaType.APPLICATION_JSON_TYPE).get(JSONObject.class);
		assertEquals("http://1",jo1.get(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL.getAttributeName()));
	}
	
}
