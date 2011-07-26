/**
 * 
 */
package eu.emi.dsr.util;

import java.util.Calendar;
import java.util.Date;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import eu.emi.dsr.core.ServiceBasicAttributeNames;

/**
 * @author a.memon
 *
 */
public class DateUtil {
	public static JSONObject setExpiryTime(JSONObject jo, int numMonths){
		JSONObject date = new JSONObject();
		Calendar c = Calendar.getInstance();
		c.add(c.MONTH, numMonths);
		try {
			date.put("$date", ServiceUtil.toUTCFormat(c.getTime()));
			jo.put(ServiceBasicAttributeNames.SERVICE_EXPIRE_ON.getAttributeName(), date);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return jo;
	}
	
	public static JSONObject addDate(JSONObject jo, String attrName, Date d){
		JSONObject date = new JSONObject();
		try {
			date.put("$date", ServiceUtil.toUTCFormat(d));
			jo.put(attrName, date);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jo;
	}
}
