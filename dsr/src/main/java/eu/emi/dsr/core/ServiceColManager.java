/**
 * 
 */
package eu.emi.dsr.core;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;


import eu.emi.dsr.util.Log;

/**
 * @author a.memon
 *
 */
public class ServiceColManager {
	Logger logger = Log.getLogger(Log.DSR, ServiceColManager.class);
	public JSONObject getServiceReferences() throws JSONException{
		JSONArray j = new JSONArray();
		j.put("http://1");
		j.put("http://1");
		JSONObject o = new JSONObject();
		o.put("references",j);
		return o;
	}
	public JSONObject getAllServiceTypes() throws JSONException{
		JSONArray j = new JSONArray();
		j.put("jms");
		j.put("tms");
		JSONObject o = new JSONObject();
		o.put("types",j);
		return o;
	}
	public JSONObject getServicesByType(String serviceType) throws JSONException{
		if (serviceType.isEmpty()) {
			throw new IllegalArgumentException("\"servicetype\" is not defined");
		}
		JSONObject obj = new JSONObject();
		obj.put("servicetype", "jms");
		obj.put("serviceurl", "http://1");
		obj.put("servicename", "jms");
		return obj;
	}
	/**
	 * @param jo
	 * @throws JSONException 
	 */
	public JSONObject queryServiceCollection(JSONObject jo) throws JSONException {
		JSONArray ja = new JSONArray();
		JSONObject resultSet = new JSONObject();
		for(int i=0; i < 500000; i++){
			JSONObject j = new JSONObject();
			j.put("serviceurl", "http://url-"+i);
			j.put("servicetype", "type-"+i);
			j.put("servicetype", "1type-"+i);
			j.put("servicetype", "2type-"+i);
			j.put("servicetype", "3type-"+i);
			j.put("servicetype", "4type-"+i);
			j.put("servicetype", "5type-"+i);
			j.put("servicetype", "6type-"+i);
			j.put("servicetype", "7type-"+i);
			ja.put(j);
		}
		resultSet.put("result", ja);
		return resultSet;
	}
}
