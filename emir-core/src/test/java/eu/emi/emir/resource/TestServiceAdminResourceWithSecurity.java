/**
 * 
 */
package eu.emi.emir.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.ws.rs.core.MediaType;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.UniformInterfaceException;

import eu.emi.emir.TestRegistryBaseWithSecurity;
import eu.emi.emir.TestValueConstants;
import eu.emi.emir.client.EMIRClient;
import eu.emi.emir.client.ServiceBasicAttributeNames;

/**
 * @author a.memon
 * 
 */
public class TestServiceAdminResourceWithSecurity extends
		TestRegistryBaseWithSecurity {
	@Test	
	public void testRegisterService() throws JSONException,
			UnrecoverableKeyException, KeyStoreException,
			NoSuchAlgorithmException, CertificateException, IOException {
		EMIRClient cr = new EMIRClient(BaseURI + "/serviceadmin",getSecurityProperties_2());
		
		JSONObject jo = TestValueConstants.getJSONWithMandatoryAttributes();
		JSONArray ja = new JSONArray();
		ja.put(jo);
		
		cr.getClientResource()
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.post(ja);
		EMIRClient cr1 = new EMIRClient(BaseURI
				+ "/serviceadmin?Service_Endpoint_ID=1",
				getSecurityProperties_2());
		JSONObject jo1 = cr1.getClientResource()
				.accept(MediaType.APPLICATION_JSON_TYPE).get(JSONObject.class);
		assertEquals("http://1",
				jo1.get(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
						.getAttributeName()));
	}
	
	
	@Test
	public void testUnAuthzRegisterService() throws JSONException,
			UnrecoverableKeyException, KeyStoreException,
			NoSuchAlgorithmException, CertificateException, IOException {
		JSONObject jo = TestValueConstants.getJSONWithMandatoryAttributes();
		JSONArray ja = new JSONArray();
		ja.put(jo);
		EMIRClient cr = new EMIRClient(BaseURI
				+ "/serviceadmin?Service_Endpoint_URL=http://1",
				getSecurityProperties_2());
		try {
			assertNotSame(ClientResponse.Status.OK.getStatusCode(), cr.getClientResource().accept(MediaType.APPLICATION_JSON_TYPE)
					.post(ClientResponse.class,ja).getStatus());
		} catch (UniformInterfaceException e) {
			e.printStackTrace();
			assertTrue(new Integer(Status.UNAUTHORIZED.getStatusCode())
					.compareTo(e.getResponse().getStatus()) == 0);
		}

		System.out.println("/serviceadmin");
		EMIRClient cr1 = new EMIRClient(BaseURI
				+ "/serviceadmin?Service_Endpoint_URL=http://1",
				getSecurityProperties_2());
		try {
			assertNotSame(ClientResponse.Status.OK.getStatusCode(), cr1.getClientResource()
					.accept(MediaType.APPLICATION_JSON_TYPE)
					.get(ClientResponse.class));
		} catch (UniformInterfaceException e) {
			assertTrue(new Integer(Status.UNAUTHORIZED.getStatusCode())
					.compareTo(e.getResponse().getStatus()) == 0);
		}
		
	}

}
