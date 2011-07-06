/**
 * 
 */
package eu.emi.dsr.util;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import eu.emi.dsr.core.ServiceBasicAttributeNames;

/**
 * 
 * Utility class to (Un)Marshal the Service information
 * 
 * @author a.memon
 * 
 */
public class ServiceUtil {

	// public static Service fromJSON(JSONObject obj){
	// return null;
	// }
	//
	// public static JSONObject toJSON(Service serviceDescription){
	// return null;
	// }

	/**
	 * Checks if the description contains the minimal valid information i.e.
	 * <b>url</b> and <b>type</b>
	 * 
	 * @param serviceDesc
	 * @throws JSONException
	 */
	public static void isValid(String serviceDesc) throws JSONException {
		JSONObject jo = new JSONObject(serviceDesc);
		jo.get("serviceurl");
		jo.get("servicetype");
	}

	/**
	 * Checks the service description being registered contains the mandatory service attributes
	 * 
	 * @param serviceDesc
	 * @throws JSONException
	 */
	public static boolean isValidServiceInfo(JSONObject jo) throws JSONException {
		
//		if ((jo.get(ServiceBasicAttributeNames.SERVICE_TYPE.getAttributeName()) == null)
//				&& (jo.get(ServiceBasicAttributeNames.SERVICE_URL
//						.getAttributeName()) == null)
//				&& (jo.get(ServiceBasicAttributeNames.SERVICE_EXPIRE_ON
//						.getAttributeName()) == null)) {
//			return false;
//		}
		
		ServiceBasicAttributeNames[] s = ServiceBasicAttributeNames.values();
		for (int i = 0; i < s.length; i++) {
			if(jo.get(s[i].getAttributeName()) == null){
				return false;
			}
			
		}		
		return true;
	}
	
	
	public static void main(String[] args) {
		
	}
}
