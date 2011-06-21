/**
 * 
 */
package eu.emi.dsr.util;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;



/**
 * 
 * Utility class to (Un)Marshal the Service information
 * 
 * @author a.memon
 *
 */
public class ServiceUtil {
	
//	public static Service fromJSON(JSONObject obj){
//		return null;
//	}
//	
//	public static JSONObject toJSON(Service serviceDescription){
//		return null;
//	}

	/**
	 * Checks if the description contains the minimal valid information i.e.
	 * <b>url</b> and <b>type</b>
	 * @param serviceDesc
	 * @throws JSONException 
	 */
	public static void isValid(String serviceDesc) throws JSONException {
		JSONObject jo = new JSONObject(serviceDesc);
		jo.get("url");
		jo.get("type");
	}
}
