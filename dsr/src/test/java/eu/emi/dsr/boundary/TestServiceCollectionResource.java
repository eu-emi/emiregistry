/**
 * 
 */
package eu.emi.dsr.boundary;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import javax.ws.rs.core.MediaType;

import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import eu.emi.dsr.TestRegistryBase;
import eu.emi.dsr.client.DSRClient;

/**
 * Integration test
 * 
 * @author a.memon
 * 
 */
public class TestServiceCollectionResource extends TestRegistryBase {

	/**
	 * @param ServiceManagerFactory
	 * 
	 */

	@Test
	public void testGetAllRefs() {
		System.out.println("/services/refs");
		DSRClient cr = new DSRClient(BaseURI + "/services/refs");
		JSONObject o = cr.getClientResource()
				.accept(MediaType.APPLICATION_JSON_TYPE).get(JSONObject.class);
		assertNotNull(o);
		System.out.println(o);
		// assert that the server returns references array
		assertFalse(o.isNull("references"));
	}

	@Test
	public void testGetAllTypes() {
		System.out.println("/services/types");
		DSRClient cr = new DSRClient(BaseURI + "/services/types");
		JSONObject o = cr.getClientResource()
		.accept(MediaType.APPLICATION_JSON_TYPE).get(JSONObject.class);
		assertNotNull(o);
		System.out.println(o);
		// assert that the server returns references array
		assertFalse(o.isNull("types"));
	}

	@Test
	public void testGetServiceByType() {
		// test return all the service of certain types
		System.out.println("/services/types/{servicetype}");
		DSRClient cr = new DSRClient(BaseURI + "/services/types/jms");
		JSONObject o = cr.getClientResource()
		.accept(MediaType.APPLICATION_JSON_TYPE).get(JSONObject.class);
		assertNotNull(o);
		System.out.println(o);
		// assert that the server returns references array
		// assertFalse(o.isNull("types"));
	}

	@Test
	public void testQueryServiceCollection() {
		System.out
				.println("/services/query?serviceurl=\"value\"&servicetype=\"jms\"");
		DSRClient cr = new DSRClient(BaseURI
				+ "/services/query?serviceurl=http://&servicetype=jms");
		@SuppressWarnings("unchecked")
		JSONObject lst = cr.getClientResource()
		.accept(MediaType.APPLICATION_JSON_TYPE).get(JSONObject.class);
		System.out.println(lst);
	}

	@Test
	public void testFindServiceUsingJSON() {
		// kind of html form
		System.out.println("/services");
	}

}
