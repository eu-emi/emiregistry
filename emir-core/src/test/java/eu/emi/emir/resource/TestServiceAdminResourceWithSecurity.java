/**
 * 
 */
package eu.emi.emir.resource;

import static org.junit.Assert.*;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.UniformInterfaceException;

import eu.emi.emir.TestRegistryBaseWithSecurity;
import eu.emi.emir.client.DSRClient;
import eu.emi.emir.client.ServiceBasicAttributeNames;
import eu.emi.emir.util.ServiceUtil;
import eu.unicore.bugsreporter.annotation.FunctionalTest;

/**
 * @author a.memon
 * 
 */
public class TestServiceAdminResourceWithSecurity extends
		TestRegistryBaseWithSecurity {
	private static JSONArray getDummyServiceDesc() {
		Map<String, String> map = new HashMap<String, String>();
		map.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
				.getAttributeName(), "http://1");
		map.put(ServiceBasicAttributeNames.SERVICE_TYPE.getAttributeName(),
				"jms");

		JSONObject date = new JSONObject();
		Calendar c = Calendar.getInstance();
		c.add(Calendar.MONTH, 12);
		try {
			date.put("$date", ServiceUtil.toUTCFormat(c.getTime()));
		} catch (JSONException e) {
			e.printStackTrace();
		}

		JSONObject jo = new JSONObject(map);
		JSONArray jArr = new JSONArray();
		jArr.put(jo);
		return jArr;
	}

	@Test	
	public void testRegisterService() throws JSONException,
			UnrecoverableKeyException, KeyStoreException,
			NoSuchAlgorithmException, CertificateException, IOException {
		DSRClient cr = new DSRClient(BaseURI + "/serviceadmin",getSecurityProperties_1());
		
		cr.getClientResource()
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.post(getDummyServiceDesc());
		DSRClient cr1 = new DSRClient(BaseURI
				+ "/serviceadmin?Service_Endpoint_URL=http://1",
				getSecurityProperties_1());
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
		JSONArray jo = getDummyServiceDesc();
		DSRClient cr = new DSRClient(BaseURI
				+ "/serviceadmin?Service_Endpoint_URL=http://1",
				getSecurityProperties_2());
		try {
			assertNotSame(ClientResponse.Status.OK.getStatusCode(), cr.getClientResource().accept(MediaType.APPLICATION_JSON_TYPE)
					.post(ClientResponse.class,jo).getStatus());
		} catch (UniformInterfaceException e) {
			e.printStackTrace();
			assertTrue(new Integer(Status.UNAUTHORIZED.getStatusCode())
					.compareTo(e.getResponse().getStatus()) == 0);
		}

		System.out.println("/serviceadmin");
		DSRClient cr1 = new DSRClient(BaseURI
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
