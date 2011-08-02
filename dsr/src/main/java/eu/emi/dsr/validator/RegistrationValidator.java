/**
 * 
 */
package eu.emi.dsr.validator;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import eu.emi.dsr.core.ServiceBasicAttributeNames;
import eu.emi.dsr.exception.InvalidServiceDescriptionException;
import eu.emi.dsr.util.Log;
import eu.emi.dsr.util.ServiceUtil;

/**
 * @author a.memon
 * 
 */
public class RegistrationValidator extends AbstractInfoValidator {
	private static Logger logger = Log.getLogger(Log.DSR,
			RegistrationValidator.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.emi.dsr.info.AbstractInformationValidator#checkUrl()
	 */
	@Override
	Boolean checkUrl() {
		try {
			if ((jo.has(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
					.getAttributeName()))
					&& (jo.getString(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
							.getAttributeName()).isEmpty())){
				logger.error("Invalid url");
				return false;
			}
				
			valid = true;
		} catch (JSONException e) {
			Log.logException(e);
			return false;
		}

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.emi.dsr.info.AbstractInformationValidator#checkDateTypes()
	 */
	@Override
	Boolean checkDateTypes() {
		// the format should be utc
		for (Iterator iterator = jo.keys(); iterator.hasNext();) {
			String key = null;
			try {
				key = (String) iterator.next();
				if ((jo.get(key) instanceof JSONObject)
						&& (jo.getJSONObject(key).has("$date"))) {
					ServiceUtil.toUTCFormat(jo.getJSONObject(key).getString(
							"$date"));
					valid = true;
				} 

				// some of the glue2 attributes should be defined as date
				// if
				// ((ServiceBasicAttributeNames.valueOf(key).getAttributeType()
				// == Date.class)
				// && (jo.get(key) instanceof JSONObject)
				// && (jo.getJSONObject(key).has("$date"))) {
				// valid = true;
				// }

			} catch (Exception e) {
				e.printStackTrace();
				Log.logException(new InvalidServiceDescriptionException(
						"invalid date format for the key: " + key, e));
				valid = false;
				return false;
			}

		}

		for (ServiceBasicAttributeNames s : ServiceBasicAttributeNames.values()) {
			if (s.getAttributeType() == Date.class) {
				try {
					if (jo.has(s.getAttributeName())){
						if ((jo.get(s.getAttributeName()) instanceof JSONObject) && (jo
								.getJSONObject(s.getAttributeName()).has("$date"))) {
							//do nothing
						}else {
							return false;
						}
					
						
					} 
				} catch (JSONException e) {
					return false;

				}
			}
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.emi.dsr.info.AbstractInformationValidator#checkExpiryTime()
	 */
	@Override
	Boolean checkExpiryTime() {

		try {
			if (jo.has(ServiceBasicAttributeNames.SERVICE_EXPIRE_ON
					.getAttributeName())) {
				if (jo.get(ServiceBasicAttributeNames.SERVICE_EXPIRE_ON
						.getAttributeName()) instanceof JSONObject) {
					if (jo.getJSONObject(
							ServiceBasicAttributeNames.SERVICE_EXPIRE_ON
									.getAttributeName()).has("$date")) {
						Date d = ServiceUtil
								.toUTCFormat(jo
										.getJSONObject(
												ServiceBasicAttributeNames.SERVICE_EXPIRE_ON
														.getAttributeName())
										.getString("$date"));
						Calendar c = Calendar.getInstance();
						c.setTime(d);
						if ((c.compareTo(Calendar.getInstance()) <= 0)) {
							logger.error("service expiry should not be the date in past");
							return false;
						} else {
							return true;
						}
					}
				} else {
					logger.error("invalid date format for the key: "
							+ ServiceBasicAttributeNames.SERVICE_EXPIRE_ON
									.getAttributeName());
					valid = false;
					return false;
				}

			} else {
				// if expiry is not mentioned then will be added and set to 6
				// months from now
				Calendar c = Calendar.getInstance();
				c.add(c.MONTH, 6);
				JSONObject j = new JSONObject();
				j.put("$date", ServiceUtil.toUTCFormat(c.getTime()));
				jo.put(ServiceBasicAttributeNames.SERVICE_EXPIRE_ON
						.getAttributeName(), j);
				valid = true;
				logger.error("missing expiry, added new field "
						+ ServiceBasicAttributeNames.SERVICE_EXPIRE_ON
								.getAttributeName());
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.logException("Invalid expiry time", e);
			return false;
		}
		return true;

	}

	/* (non-Javadoc)
	 * @see eu.emi.dsr.validator.AbstractInfoValidator#checkArrays()
	 */
	@Override
	boolean checkArrays() {
		for (ServiceBasicAttributeNames s : ServiceBasicAttributeNames.values()) {
			if (s.getAttributeType() == JSONArray.class) {
				try {
					if (jo.has(s.getAttributeName())){
						if (jo.get(s.getAttributeName()) instanceof JSONArray) {
							//do nothing
						}else {
							return false;
						}
					
						
					} 
				} catch (JSONException e) {
					Log.logException(s.getAttributeName() +" is an array-it should be defined as [\"object\",\"object\"...]",e);
					return false;

				}
			}
		}
		return true;
	}
}
