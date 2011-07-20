/**
 * 
 */
package eu.emi.dsr.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.spi.TimeZoneNameProvider;

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

	public static SimpleDateFormat ServiceDateFormat = new SimpleDateFormat(
	"dd-mm-yyyy,HH:mm");
	
	public static SimpleDateFormat ISODateFormat = new SimpleDateFormat(
	"yyyy-MM-dd'T'HH:mm:ssZ");
	
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
		
		if ((jo.get(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
						.getAttributeName()) == null)
				&& (jo.get(ServiceBasicAttributeNames.SERVICE_EXPIRE_ON
						.getAttributeName()) == null)) {
			return false;
		}
		
//		ServiceBasicAttributeNames[] s = ServiceBasicAttributeNames.values();
//		for (int i = 0; i < s.length; i++) {
//			if(jo.get(s[i].getAttributeName()) == null){
//				return false;
//			}
//			
//		}		
		return true;
	}
	
	
	public static String toUTCFormat(Date d){
			SimpleDateFormat formatter = new SimpleDateFormat(
					"yyyy-MM-dd'T'HH:mm:ss'Z'");
//			SimpleDateFormat formatter = new SimpleDateFormat();
			formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
			String timestamp = formatter.format(d);
			return timestamp;
	}
}
