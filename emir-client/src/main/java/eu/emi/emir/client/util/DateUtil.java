/**
 * 
 */
package eu.emi.emir.client.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

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
	public static final SimpleDateFormat ServiceDateFormat = new SimpleDateFormat(
			"dd-mm-yyyy,HH:mm");
	public static final SimpleDateFormat ISODateFormat = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ssZ");
	public static final SimpleDateFormat UTCISODateFormat = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

	public enum DurationType {
		DAYS, MINUTES, SECONDS, HOURS, YEARS;
	}
	
	private static final int[] MONTH  = new int[]{ 31, -1, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };  

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

	public static XMLGregorianCalendar toXmlGregorian(Date d)
			throws DatatypeConfigurationException {
		GregorianCalendar gcal = new GregorianCalendar();
		gcal.setTime(d);
		XMLGregorianCalendar xgcal = DatatypeFactory.newInstance()
				.newXMLGregorianCalendar(gcal);
		return xgcal;
	}

	public static Date fromXmlGregorian(XMLGregorianCalendar xmlCal)
			throws DatatypeConfigurationException {
		return xmlCal.toGregorianCalendar().getTime();
	}

	/**
	 * Checks whether given json document has nested json date object
	 * 
	 * @param object
	 * @return boolean value asserting the presence of nested date object
	 */
	public static Boolean hasDateSubObject(Object object) {
		if (object instanceof JSONObject) {
			JSONObject j = (JSONObject) object;
			if (j.has("$date")) {
				return true;
			} else {
				return false;
			}

		}

		return false;
	}

	/***
	 * Calculate duration between different times
	 * 
	 * @param fromDate
	 * @param toDate The date in future or greater than the fromDate
	 * @return
	 */
	public static Duration duration(Calendar fromDate, Calendar toDate) {
		
		Integer monthResult = 0;
		Integer dayResult = 0;
		Integer yearResult = 0;
		Integer hourResult = 0;
		Integer hourIncrement = 0;
		
		Integer minutesResult = 0;
		Integer minutesIncrement = 0;
		
		Integer increment = 0;
		
		if (toDate.get(Calendar.MINUTE) < fromDate.get(Calendar.MINUTE)) {
			minutesIncrement = 1;
			minutesResult = (toDate.get(Calendar.MINUTE) + 60) - fromDate.get(Calendar.MINUTE);
		} else {
			minutesIncrement = 0;
			minutesResult = toDate.get(Calendar.MINUTE) - fromDate.get(Calendar.MINUTE);
		}
		
		if (toDate.get(Calendar.HOUR_OF_DAY) < (fromDate.get(Calendar.HOUR_OF_DAY) + minutesIncrement)) {
			hourResult = (toDate.get(Calendar.HOUR_OF_DAY) + 24) - fromDate.get(Calendar.HOUR_OF_DAY);
			hourIncrement = 1;
		} else {
			hourResult = toDate.get(Calendar.HOUR_OF_DAY)  - fromDate.get(Calendar.HOUR_OF_DAY);
			hourIncrement = 0;
		}
		
		
		//calculate day
		if( toDate.get(Calendar.DATE) < fromDate.get(Calendar.DATE) ){
			increment = MONTH[fromDate.get(Calendar.MONTH)-1];
		}
		
		if (increment == -1) {
			Boolean leapYear = fromDate.getActualMaximum(Calendar.DAY_OF_YEAR) > 365;
			if (leapYear) {
				increment = 29;
			} else {
				increment = 28;
			}
		}
		
		if (increment != 0) {
			dayResult = (toDate.get(Calendar.DATE) + increment) -  fromDate.get(Calendar.DATE);
			increment = 1;
		} else {
			dayResult = toDate.get(Calendar.DATE) -  fromDate.get(Calendar.DATE);
			increment = 0;
		}
		
		
		//calculate month
		if( toDate.get(Calendar.MONTH) < (fromDate.get(Calendar.MONTH) + increment) ){
			monthResult = (toDate.get(Calendar.MONTH)+12) - fromDate.get(Calendar.MONTH);
			increment = 1;
		} else {
			monthResult = toDate.get(Calendar.MONTH) - fromDate.get(Calendar.MONTH);
			increment = 0;
		}
		
		//calculate year
		yearResult = toDate.get(Calendar.YEAR) - (fromDate.get(Calendar.YEAR)+1);
		
		Duration d = new Duration();
		
		d.setYears(yearResult);
		d.setDays(dayResult);
		d.setMonths(monthResult);
		d.setHours(hourResult);
		d.setMinutes(minutesResult);
		
		return d;
	}
	

}
