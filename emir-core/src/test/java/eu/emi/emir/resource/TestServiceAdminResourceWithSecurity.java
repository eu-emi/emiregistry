/**
 * 
 */
package eu.emi.emir.resource;

import static org.junit.Assert.assertEquals;
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

import eu.emi.emir.TestRegistryBaseWithSecurity;
import eu.emi.emir.client.EMIRClient;
import eu.emi.emir.client.ServiceBasicAttributeNames;
import eu.emi.emir.client.TestValueConstants;

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
		EMIRClient cr = new EMIRClient(BaseURI + "/serviceadmin",
				getSecurityProperties_2());

		JSONObject jo = TestValueConstants.getJSONWithMandatoryAttributes();
		JSONArray ja = new JSONArray();
		ja.put(jo);

		cr.getClientResource().accept(MediaType.APPLICATION_JSON_TYPE).post(ja);
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
	public void testAccessAuthecticatedServices() {
		// EMIRClient cr2 = new EMIRClient(BaseURI +
		// the undefined users should be able to access the services
		EMIRClient cr1 = new EMIRClient(BaseURI + "/services",
				getSecurityProperties_2());
		JSONArray o = cr1.getClientResource()
				.accept(MediaType.APPLICATION_JSON_TYPE).get(JSONArray.class);
		System.out.println(o);
		assertTrue(o != null);

		// and the users should be able to access the services too
		EMIRClient cr2 = new EMIRClient(BaseURI + "/services",
				getSecurityProperties_2());
		JSONArray o1 = cr2.getClientResource()
				.accept(MediaType.APPLICATION_JSON_TYPE).get(JSONArray.class);
		assertTrue(o1 != null);

	}

	@Test
	public void testUnAuthzRegisterService() throws JSONException,
			UnrecoverableKeyException, KeyStoreException,
			NoSuchAlgorithmException, CertificateException, IOException {
		EMIRClient cr2 = new EMIRClient(BaseURI
				+ "/serviceadmin?Service_Endpoint_ID=1",
				getSecurityProperties_3());

		assertEquals(
				Status.UNAUTHORIZED,
				Status.fromStatusCode(cr2.getClientResource()
						.accept(MediaType.APPLICATION_JSON_TYPE)
						.get(ClientResponse.class).getStatus()));

	}

}
