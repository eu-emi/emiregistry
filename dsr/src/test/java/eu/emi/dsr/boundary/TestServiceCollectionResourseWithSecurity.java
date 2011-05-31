/**
 * 
 */
package eu.emi.dsr.boundary;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.restlet.resource.ClientProxy;
import org.restlet.resource.ClientResource;

import eu.emi.dsr.TestRegistryBaseWithSecurity;
import eu.emi.dsr.client.ClientSecurityProperties;
import eu.emi.dsr.client.DSRClient;

/**
 * @author a.memon
 *
 */
public class TestServiceCollectionResourseWithSecurity extends TestRegistryBaseWithSecurity{
	@Before
	public void setup(){
		System.out.println("base url: "+BaseURI);
	}
	@Test
	public void testGetAllRefs(){
		System.out.println("/services/refs");
		
		ClientSecurityProperties csp = new ClientSecurityProperties();
		csp.setKeystorePassword("emi");
		csp.setKeystorePath("src/main/certs/demo-user.p12");
		csp.setKeystoreType("pkcs12");
		csp.setTruststoreType("jks");
		csp.setTruststorePassword("emi");
		csp.setTruststorePath("src/main/certs/demo-user.jks");
		
		
		
		DSRClient cr = new DSRClient(BaseURI + "/services/refs", csp);
		JSONObject o = cr.getClientResource().get(JSONObject.class);
		assertNotNull(o);
		System.out.println(o);
		//assert that the server returns references array
		assertFalse(o.isNull("references"));
	}
}
