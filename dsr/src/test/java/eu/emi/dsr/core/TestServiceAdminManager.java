/**
 * 
 */
package eu.emi.dsr.core;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jettison.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author a.memon
 *
 */

public class TestServiceAdminManager {
	ServiceAdminManager adminMgr;
	@Before
	public void setup(){
		adminMgr = new ServiceAdminManager();
	}
	
	@Test
	public void addService() throws Exception{
		Map<String, String> map = new HashMap<String, String>();
		map.put("serviceurl", "http://1");
		map.put("servicetype", "jms");
		JSONObject jo = new JSONObject(map);
		adminMgr.addService(jo);
	}
	@Test
	public void removeService()throws Exception{
		adminMgr.removeService("http://1");
	}
	@Test
	public void updateService() throws Exception{
		Map<String, String> map = new HashMap<String, String>();
		map.put("serviceurl", "http://1");
		map.put("servicetype", "jms");
		JSONObject jo = new JSONObject(map);
		adminMgr.updateService(jo);
	}
	@Test
	public void findService() throws Exception{
		adminMgr.findServiceByUrl("http://1");
	}
}
