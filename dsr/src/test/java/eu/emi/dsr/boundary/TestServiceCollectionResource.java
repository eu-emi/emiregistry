/**
 * 
 */
package eu.emi.dsr.boundary;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.json.JSONObject;
import org.junit.Test;
import org.restlet.resource.ClientResource;

import eu.emi.dsr.TestRegistryBase;
/**
 * Integration test
 * @author a.memon
 *
 */
public class TestServiceCollectionResource extends TestRegistryBase{
	
	
	/**
	 * @param ServiceManagerFactory 
	 * 
	 */
	
	
	@Test
	public void testGetAllRefs(){
		System.out.println("/services/refs");
		ClientResource cr = new ClientResource(BaseURI + "/services/refs");
		JSONObject o = cr.get(JSONObject.class);
		assertNotNull(o);
		System.out.println(o);
		//assert that the server returns references array
		assertFalse(o.isNull("references"));
	}
	
	@Test
	public void testGetAllTypes(){
		System.out.println("/services/types");
		ClientResource cr = new ClientResource(BaseURI + "/services/types");
		JSONObject o = cr.get(JSONObject.class);
		assertNotNull(o);
		System.out.println(o);
		//assert that the server returns references array
		assertFalse(o.isNull("types"));
	}
	
	@Test
	public void testGetServiceByType(){
		//test return all the service of certain types
		System.out.println("/services/types/{servicetype}");
		ClientResource cr = new ClientResource(BaseURI + "/services/types/jms");
		JSONObject o = cr.get(JSONObject.class);
		assertNotNull(o);
		System.out.println(o);
		//assert that the server returns references array
//		assertFalse(o.isNull("types"));
	}
	
	@Test
	public void testQueryServiceCollection(){
		System.out.println("/services/query?serviceurl=\"value\"&servicetype=\"jms\"");
		ClientResource cr = new ClientResource(BaseURI + "/services/query?serviceurl=http://&servicetype=jms");
		@SuppressWarnings("unchecked")
		JSONObject lst = cr.get(JSONObject.class);
		System.out.println(lst);
	}
	
	@Test
	public void testFindServiceUsingJSON(){
		//kind of html form
		System.out.println("/services");
	}
	
	
	
}
