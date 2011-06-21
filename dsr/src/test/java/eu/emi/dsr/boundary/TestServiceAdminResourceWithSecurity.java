/**
 * 
 */
package eu.emi.dsr.boundary;

import static org.junit.Assert.assertNotNull;

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
	public void testGetAllRefs(){
		System.out.println("/serviceadmin");
		
		ClientSecurityProperties csp = new ClientSecurityProperties();
		csp.setKeystorePassword("emi");
		csp.setKeystorePath("src/main/certs/demo-user.p12");
		csp.setKeystoreType("pkcs12");
		csp.setTruststoreType("jks");
		csp.setTruststorePassword("emi");
		csp.setTruststorePath("src/main/certs/demo-user.jks");
		
		
		
		DSRClient cr = new DSRClient(BaseURI + "/serviceadmin?serviceurl=http://1", csp);
		JSONObject o = cr.getClientResource().get(JSONObject.class);
		assertNotNull(o);
		System.out.println(o);
	}
}
