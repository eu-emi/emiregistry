/**
 * 
 */
package eu.emi.emir.client.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import eu.emi.emir.client.ServiceBasicAttributeNames;
import eu.emi.emir.client.glue2.JSONToGlue2MappingException;

/**
 * @author a.memon
 * @author g.szigeti
 * 
 */
public class DateUtil {
	public static JSONObject setCreationTime(JSONObject jo, Integer days) {
		JSONObject date = new JSONObject();
		try {
			date.put("$date", DateUtil.toUTCFormat(addDays(days)));
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
			date.put("$date", DateUtil.toUTCFormat(addDays(days)));
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
			date.put("$date", DateUtil.toUTCFormat(addHours(hours)));
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
			date.put("$date", DateUtil.toUTCFormat(d));
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
			date = DateUtil.toUTCFormat(strDate);
		} catch (Exception e) {
			throw new JSONToGlue2MappingException(
					"Error converting $date to java.util.Date", e);
		}
		return date;
	}

	public static SimpleDateFormat ServiceDateFormat = new SimpleDateFormat(
	"dd-mm-yyyy,HH:mm");
	public static SimpleDateFormat ISODateFormat = new SimpleDateFormat(
	"yyyy-MM-dd'T'HH:mm:ssZ");
	public static SimpleDateFormat UTCISODateFormat = new SimpleDateFormat(
	"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	static List<String> lstNames;

	public synchronized static String toUTCFormat(Date d) {
		SimpleDateFormat formatter = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
		String timestamp = formatter.format(d);
		return timestamp;
	}

	public static Date toUTCFormat(String d) throws ParseException {
		SimpleDateFormat formatter = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
		Date timestamp = formatter.parse(d);
		return timestamp;
	}

}
