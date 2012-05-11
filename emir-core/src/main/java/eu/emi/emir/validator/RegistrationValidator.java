/**
 * 
 */
package eu.emi.emir.validator;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import eu.emi.emir.DSRServer;
import eu.emi.emir.client.ServiceBasicAttributeNames;
import eu.emi.emir.client.util.Log;
import eu.emi.emir.core.ServerConstants;
import eu.emi.emir.exception.InvalidServiceDescriptionException;
import eu.emi.emir.util.ServiceUtil;

/**
 * @author a.memon
 * 
 */
public class RegistrationValidator extends AbstractInfoValidator {
	private static Logger logger = Log.getLogger(Log.EMIR_CORE,
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
							.getAttributeName()).isEmpty())) {
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
		for (Iterator<?> iterator = jo.keys(); iterator.hasNext();) {
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
				Log.logException(new InvalidServiceDescriptionException(
						"invalid date format for the key: " + key, e));
				valid = false;
				return false;
			}

		}

		for (ServiceBasicAttributeNames s : ServiceBasicAttributeNames.values()) {
			if (s.getAttributeType() == Date.class) {
				try {
					if (jo.has(s.getAttributeName())) {
						if ((jo.get(s.getAttributeName()) instanceof JSONObject)
								&& (jo.getJSONObject(s.getAttributeName())
										.has("$date"))) {
							// do nothing
						} else {
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
						
						//creating the max expiry time calendar
						Calendar cMax = Calendar.getInstance();
						int max_def=0;
						try {
							max_def=Integer.valueOf(DSRServer.getProperty(ServerConstants.REGISTRY_EXPIRY_MAXIMUM, "730"));
						} catch (NumberFormatException e) {
							logger.warn("Error in reading the configuration property of maximum default expiry days - setting the value to 730 days");
							max_def = 730;
						}
						
						cMax.add(Calendar.DATE, max_def);
						
						if ((cMax.compareTo(c) < 0)) {
							logger.error("Failed to validate the service information: Given service expiry- "+ c.getTime() +", exceeds the default maximum- " + cMax.getTime());							
							return false;
						} 
						Calendar now = Calendar.getInstance();
						if (c.compareTo(Calendar.getInstance())<=0) {
							logger.error("Failed to validate the service information: Given service expiry- "+ c.getTime() +", mustn't be less than or equal-to current time - " + now.getTime());
							return false;
						}
						
					}
				} else {
					logger.error("Failed to validate the service information: invalid date format for the key: "
							+ ServiceBasicAttributeNames.SERVICE_EXPIRE_ON
									.getAttributeName());
					valid = false;
					return false;
				}

			} 
			
			// else {
				// if expiry is not mentioned then will be added and set to 6
				// months from now
//				Calendar c = Calendar.getInstance();
//				c.add(c.MONTH, 6);
//				JSONObject j = new JSONObject();				
//				j.put("$date", ServiceUtil.toUTCFormat(c.getTime()));
//				jo.put(ServiceBasicAttributeNames.SERVICE_EXPIRE_ON
//						.getAttributeName(), j);
				
//				jo = DateUtil.setExpiryTime(jo, Integer.valueOf(DSRServer.getProperty(ServerConstants.REGISTRY_EXPIRY_DEFAULT, "1")));
//				valid = true;
//				logger.error("missing expiry, added new field "
//						+ ServiceBasicAttributeNames.SERVICE_EXPIRE_ON
//								.getAttributeName());
//			}
		} catch (Exception e) {
			Log.logException("Invalid expiry time", e);
			return false;
		}
		return true;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.emi.dsr.validator.AbstractInfoValidator#checkArrays()
	 */
	@Override
	boolean checkArrays() {
		for (ServiceBasicAttributeNames s : ServiceBasicAttributeNames.values()) {
			if (s.getAttributeType() == JSONArray.class) {
				try {
					if (jo.has(s.getAttributeName())) {
						if (jo.get(s.getAttributeName()) instanceof JSONArray) {
							// do nothing
						} else {
							return false;
						}

					}
				} catch (JSONException e) {
					Log.logException(
							s.getAttributeName()
									+ " is an array-it should be defined as [\"object\",\"object\"...]",
							e);
					return false;

				}
			}
		}
		return true;
	}
}
