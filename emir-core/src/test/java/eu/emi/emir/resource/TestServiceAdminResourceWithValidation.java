/**
 * 
 */
package eu.emi.emir.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.ws.rs.core.MediaType;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.After;
import org.junit.Test;

import com.sun.jersey.api.client.ClientResponse;

import eu.emi.emir.TestRegistryBase;
import eu.emi.emir.client.EMIRClient;
import eu.emi.emir.client.ServiceBasicAttributeNames;
import eu.emi.emir.client.TestValueConstants;
import eu.emi.emir.db.ServiceDatabase;
import eu.emi.emir.db.mongodb.MongoDBServiceDatabase;

/**
 * @author a.memon
 * 
 */
public class TestServiceAdminResourceWithValidation extends TestRegistryBase {

	@Test
	public void testInvalidServiceRecord() throws JSONException {
		EMIRClient cr = new EMIRClient(BaseURI + "/serviceadmin");
		JSONObject j = TestValueConstants.getJSONWithMandatoryAttributes();
		j.put(ServiceBasicAttributeNames.SERVICE_TYPE.toString(), "sms");
		j.remove(ServiceBasicAttributeNames.SERVICE_ENDPOINT_CAPABILITY.toString());
		JSONObject jx = TestValueConstants.getJSONWithMandatoryAttributes();
		jx.put(ServiceBasicAttributeNames.SERVICE_TYPE.toString(), "jms");
		jx.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID.toString(), "2");
		JSONArray ja = new JSONArray();
		ja.put(j);
		ja.put(jx);
		
		ClientResponse c = cr.getClientResource().accept(MediaType.APPLICATION_JSON_TYPE).put(ClientResponse.class,ja);
		System.out.println(c.getStatus());
		
		System.out.println("being updated json document: "+j.toString(2));
		
		EMIRClient cr2 = new EMIRClient(BaseURI
				+ "/serviceadmin?Service_Endpoint_ID=2");
		JSONObject jo1 = cr2.getClientResource()
				.accept(MediaType.APPLICATION_JSON_TYPE).get(JSONObject.class);
		System.out.println(jo1);
		//asserting the type
		assertEquals("jms", jo1.get(ServiceBasicAttributeNames.SERVICE_TYPE
				.getAttributeName()));
		//asserting the expiry time
		assertTrue(jo1.has(ServiceBasicAttributeNames.SERVICE_EXPIRE_ON.getAttributeName()));
		
		System.out.println("updated json: "+jo1.toString(2));
		
		
	}
	
	@After
	public void cleanup() {
		ServiceDatabase sd = new MongoDBServiceDatabase();
		sd.deleteAll();
	}
}
