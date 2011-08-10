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
	public static JSONObject setExpiryTime(JSONObject jo, Integer days){
		JSONObject date = new JSONObject();		
		try {
			date.put("$date", ServiceUtil.toUTCFormat(addDays(days)));
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
	
	/** 
	 * Add days to the given date
	 * **/
	public static Date addDays(Date date, Integer days){
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(c.DATE, days);
		return c.getTime();
	}
	
	/** 
	 * Add days to the current date
	 * **/
	public static Date addDays(Integer days){
		return addDays(new Date(), days);
	}
	
}
