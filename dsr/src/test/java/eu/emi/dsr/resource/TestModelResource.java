/**
 * 
 */
package eu.emi.dsr.resource;

import static org.junit.Assert.*;

import javax.ws.rs.core.MediaType;

import org.codehaus.jettison.json.JSONArray;
import org.junit.Test;

import eu.emi.dsr.TestRegistryBase;
import eu.emi.client.DSRClient;

/**
 * @author a.memon
 *
 */
public class TestModelResource extends TestRegistryBase{
	@Test
	public void testModel(){
		DSRClient cr1 = new DSRClient(BaseURI + "/model");
		JSONArray ja = cr1.getClientResource().accept(MediaType.APPLICATION_JSON_TYPE).get(JSONArray.class);
		assertTrue(ja.length() > 0);
	}
}	
