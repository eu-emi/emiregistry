/**
 * 
 */
package eu.emi.emir.util;

import java.util.Calendar;
import java.util.Date;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import eu.emi.emir.client.ServiceBasicAttributeNames;
import eu.emi.emir.client.util.Log;
import eu.emi.emir.glue2.JSONToGlue2MappingException;

/**
 * @author a.memon
 * @author g.szigeti
 * 
 */
public class DateUtil {
	public static JSONObject setCreationTime(JSONObject jo, Integer days) {
		JSONObject date = new JSONObject();
		try {
			date.put("$date", ServiceUtil.toUTCFormat(addDays(days)));
			jo.put(ServiceBasicAttributeNames.SERVICE_CREATED_ON
					.getAttributeName(), date);
		} catch (JSONException e) {
			Log.logException("", e);
		}

		return jo;
	}
	
	public static JSONObject setExpiryTime(JSONObject jo, Integer days) {
		JSONObject date = new JSONObject();
		try {
			date.put("$date", ServiceUtil.toUTCFormat(addDays(days)));
			jo.put(ServiceBasicAttributeNames.SERVICE_EXPIRE_ON
					.getAttributeName(), date);
		} catch (JSONException e) {
			Log.logException("", e);
		}

		return jo;
	}

	public static JSONObject setExpiryTimeWithHours(JSONObject jo, Integer hours) {
		JSONObject date = new JSONObject();
		try {
			date.put("$date", ServiceUtil.toUTCFormat(addHours(hours)));
			jo.put(ServiceBasicAttributeNames.SERVICE_EXPIRE_ON
					.getAttributeName(), date);
		} catch (JSONException e) {
			Log.logException("", e);
		}

		return jo;
	}

	public static JSONObject addDate(JSONObject jo, String attrName, Date d) {
		JSONObject date = new JSONObject();
		try {
			date.put("$date", ServiceUtil.toUTCFormat(d));
			jo.put(attrName, date);
		} catch (JSONException e) {
			Log.logException("", e);
		}
		return jo;
	}

	/**
	 * Add days to the given date
	 * **/
	public static Date addDays(Date date, Integer days) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.DATE, days);
		return c.getTime();
	}

	/**
	 * Add days to the current date
	 * **/
	public static Date addDays(Integer days) {
		return addDays(new Date(), days);
	}

	/**
	 * Add hours to the given date
	 * **/
	public static Date addHours(Date date, Integer hours) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.HOUR, hours);
		return c.getTime();
	}

	/**
	 * Add hours to the current date
	 * **/
	public static Date addHours(Integer hours) {
		return addHours(new Date(), hours);
	}
	
	/**
	 * @param resJson
	 */
	public static Date getDate(JSONObject json)
			throws JSONToGlue2MappingException {
		Date date = null;
		try {
			String strDate = json.get("$date").toString();
			date = ServiceUtil.toUTCFormat(strDate);
		} catch (Exception e) {
			throw new JSONToGlue2MappingException(
					"Error converting $date to java.util.Date", e);
		}
		return date;
	}

}
