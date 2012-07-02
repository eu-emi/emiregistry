/**
 * 
 */
package eu.emi.emir.resource;

import static org.junit.Assert.*;

import javax.ws.rs.core.MediaType;

import org.codehaus.jettison.json.JSONArray;
import org.junit.Test;

import eu.emi.emir.TestRegistryBase;
import eu.emi.emir.client.EMIRClient;

/**
 * @author a.memon
 *
 */
public class TestModelResource extends TestRegistryBase{
	@Test
	public void testModel(){
		EMIRClient cr1 = new EMIRClient(BaseURI + "/model");
		JSONArray ja = cr1.getClientResource().accept(MediaType.APPLICATION_JSON_TYPE).get(JSONArray.class);
		assertTrue(ja.length() > 0);
	}
}	
