/**
 * 
 */
package eu.emi.dsr.boundary;

import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.codehaus.jettison.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import eu.emi.dsr.TestRegistryBaseWithSecurity;
import eu.emi.dsr.client.ClientSecurityProperties;
import eu.emi.dsr.client.DSRClient;

/**
 * @author a.memon
 *
 */
public class TestServiceAdminResourceWithSecurity extends TestRegistryBaseWithSecurity{
	@Before
	public void setup(){
		System.out.println("base url: "+BaseURI);
		
	}
	@Test
	public void testGetServiceInfo(){
		System.out.println("/serviceadmin");
		
		DSRClient cr = new DSRClient(BaseURI + "/serviceadmin?serviceurl=http://1", getSecurityProperties());
		JSONObject o = cr.getClientResource().get(JSONObject.class);
		assertNotNull(o);
		System.out.println(o);
	}
	@Test
	public void testRegisterService(){
		Map<String, String> map = new HashMap<String, String>();
		map.put("servicetype", "jms");
		map.put("serviceurl", "http://1");
		JSONObject jo = new JSONObject(map);
		DSRClient cr = new DSRClient(BaseURI + "/serviceadmin?serviceurl=http://1", getSecurityProperties());
		String str = cr.getClientResource()
				.accept(MediaType.APPLICATION_JSON_TYPE).post(String.class,jo);
		assertNotNull(str);
		System.out.println(str);
	}
}
