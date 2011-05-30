/**
 * 
 */
package eu.emi.dsr.boundary;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.restlet.resource.ClientResource;

import eu.emi.dsr.TestRegistryBase;


import static org.junit.Assert.*;
/**
 * @author a.memon
 *
 */
public class TestServiceAdminResource extends TestRegistryBase{
	
	@Test
	public void testGetServiceByUrl() throws JSONException{
		ClientResource cr = new ClientResource(BaseURI+"/serviceadmin?serviceurl=http://1");
		JSONObject jo = cr.get(JSONObject.class);
		
		System.out.println(jo.get("serviceurl"));
	}
	@Test
	public void testAddService() throws JSONException{
		ClientResource cr = new ClientResource(BaseURI+"/serviceadmin");
		JSONObject jo = new JSONObject();
		jo.append("serviceurl", "http://1");
		JSONObject result = cr.post(jo,JSONObject.class);
		assertNotNull(result.get("serviceurl"));
	}
	
	@Test
	public void testDeleteResource() throws JSONException{
		ClientResource cr = new ClientResource(BaseURI+"/serviceadmin?serviceurl=http://1");
		cr.delete();
		
		
	}
	
	@Test
	public void testUpdateService() throws JSONException{
		ClientResource cr = new ClientResource(BaseURI+"/serviceadmin");
		JSONObject jo = new JSONObject();
		jo.append("serviceurl", "http://1");
		JSONObject result = cr.put(jo,JSONObject.class);
		assertNotNull(result.get("serviceurl"));
	}
}
