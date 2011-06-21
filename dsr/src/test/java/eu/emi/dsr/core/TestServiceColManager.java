/**
 * 
 */
package eu.emi.dsr.core;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
/**
 * @author a.memon
 * 
 */
public class TestServiceColManager {
	ServiceColManager mgr = null;
	@Before
	public void setup(){
		mgr = new ServiceColManager();
	}
	@Test
	public void testGetServiceByRefernces() {
		try {
			JSONObject o = mgr.getServiceReferences();
			assertNotNull(o);
			System.out.println(o);
		} catch (JSONException e) {
			fail();
			e.printStackTrace();
		}
		
	}
	
	@Test
	public void testGetAllServiceTypes() {
		try {
			JSONObject o = mgr.getAllServiceTypes();
			assertNotNull(o);
			System.out.println(o);
		} catch (JSONException e) {
			fail();
			e.printStackTrace();
		}
		
	}
	
	
	@Test
	public void testGetServiceByType() {
		try {
			JSONObject o = mgr.getServicesByType("tms");
			assertNotNull(o);
			System.out.println(o);
		} catch (JSONException e) {
			fail();
			e.printStackTrace();
		}
		
	}
	
	
	@Test
	public void testQueryServiceCollection(){
		Map<String, String> map = new HashMap<String, String>();
		map.put("serviceurl", "http://1");
		map.put("servicetype", "jms");
		JSONObject j = new JSONObject(map);
		try {
			mgr.queryServiceCollection(j);
		} catch (JSONException e) {
			fail();
			e.printStackTrace();
		}
	}

}
