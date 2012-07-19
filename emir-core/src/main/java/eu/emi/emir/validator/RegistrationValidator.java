/**
 * 
 */
package eu.emi.emir.validator;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import eu.emi.emir.EMIRServer;
import eu.emi.emir.ServerProperties;
import eu.emi.emir.client.ServiceBasicAttributeNames;
import eu.emi.emir.client.util.Log;
import eu.emi.emir.exception.InvalidServiceDescriptionException;
import eu.emi.emir.util.ServiceUtil;
import eu.unicore.util.configuration.ConfigurationException;

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
	// @Override
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
			Log.logException("", e);
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
	Boolean checkDateTypes() throws InvalidServiceDescriptionException {
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
				Log.logException("", new InvalidServiceDescriptionException(
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
	Boolean checkExpiryTime() throws InvalidServiceDescriptionException, ConfigurationException, JSONException, ParseException {

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

						// creating the max expiry time calendar
						Calendar cMax = Calendar.getInstance();
						int max_def = 0;
						try {
							max_def = EMIRServer
									.getServerProperties()
									.getIntValue(
											ServerProperties.PROP_RECORD_EXPIRY_MAXIMUM);
						} catch (NumberFormatException e) {

							logger.warn("Error in reading the configuration property of maximum default expiry days - setting the value to"
									+ max_def + " days");
						}

						cMax.add(Calendar.DATE, max_def);

						if ((cMax.compareTo(c) < 0)) {
							String msg = "Failed to validate the service information: Given service expiry- "
									+ c.getTime()
									+ ", exceeds the default maximum- "
									+ cMax.getTime();
							logger.error(msg);
							throw new InvalidServiceDescriptionException(msg);
						}
						Calendar now = Calendar.getInstance();
						if (c.compareTo(Calendar.getInstance()) <= 0) {
							String msg = "Failed to validate the service information: Given service expiry- "
									+ c.getTime()
									+ ", mustn't be less than or equal-to current time - "
									+ now.getTime();
							logger.error(msg);
							throw new InvalidServiceDescriptionException(msg);
						}

					}
				} else {
					String msg = "Failed to validate the service information: invalid date format for the key: "
							+ ServiceBasicAttributeNames.SERVICE_EXPIRE_ON
									.getAttributeName();
					logger.error(msg);
					valid = false;
					throw new InvalidServiceDescriptionException(msg);
				}
			
		}

		return true;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.emi.dsr.validator.AbstractInfoValidator#checkArrays()
	 */
	// @Override
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
									+ " is an array-it should be defined as [\"value\",\"value\"...]",
							e);
					return false;

				}
			}
		}
		return true;
	}

	@Override
	Boolean checkMandatoryAttributes()
			throws InvalidServiceDescriptionException {
		StringBuilder sb = new StringBuilder(
				"Following mandatory Service Endpoint Record attributes are either 'missing', 'NULL', or 'wrongly' defined: \n");
		List<Boolean> list = new ArrayList<Boolean>();
		for (ServiceBasicAttributeNames s : ServiceBasicAttributeNames.values()) {
			if (s.isMandatory()) {
				try {
					if (!jo.has(s.getAttributeName())
							&& jo.isNull((s.getAttributeName()))) {
						sb.append("* " + s.getAttributeName())
								.append(" is a mandatory attibute, MUST be provided and not be NULL\n");
						list.add(false);
					}

					// checking the array
					if (s.getAttributeType() == JSONArray.class) {

						if (jo.has(s.getAttributeName())) {
							if (!(jo.get(s.getAttributeName()) instanceof JSONArray)) {
								sb.append("* " + s.getAttributeName())
										.append(" MUST be defined as JSON Array, e.g. [\"value1\",\"value2\"...]");
								
								list.add(false);
							}
							if (jo.getJSONArray(s.getAttributeName()).length() <= 0) {
								sb.append("* " + s.getAttributeName())
								.append(" JSON Array at least contain single element, e.g. [\"value1\",\"value2\"...]");
								list.add(false);
							}

						}
					}

				} catch (Exception e) {
					list.add(false);
					Log.logException(sb.toString(), e);
					throw new InvalidServiceDescriptionException(sb.toString());
				}

			}

		}

		if (list.contains(false)) {
			logger.error(sb.toString());
			list.clear();
			list = null;
			return false;
		}
		list.clear();
		list = null;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eu.emi.emir.validator.AbstractInfoValidator#checkMandatoryAttribute()
	 */
	// @Override
	Boolean _checkMandatoryAttributes() {
		StringBuilder sb = new StringBuilder(
				"Missing/Invalid mandatory Service Endpoint Record attributes: \n");
		List<Boolean> list = new ArrayList<Boolean>();
		if (jo.has(ServiceBasicAttributeNames.SERVICE_ID.getAttributeName())) {
			try {
				if ((jo.has(ServiceBasicAttributeNames.SERVICE_ID
						.getAttributeName()))
						&& (jo.getString(ServiceBasicAttributeNames.SERVICE_ID
								.getAttributeName()).isEmpty())) {
					sb.append("Invalid/NULL Service ID\n");
					list.add(false);
					// return false;
				}
				list.add(true);
				// valid = true;
			} catch (JSONException e) {
				Log.logException("", e);
				return false;
			}
		}

		if (jo.has(ServiceBasicAttributeNames.SERVICE_TYPE.getAttributeName())) {
			try {
				if ((jo.has(ServiceBasicAttributeNames.SERVICE_TYPE
						.getAttributeName()))
						&& (jo.getString(ServiceBasicAttributeNames.SERVICE_TYPE
								.getAttributeName()).isEmpty())) {
					sb.append("Invalid/NULL Service Type");
					list.add(false);
					// return false;
				}
				list.add(true);
				// valid = true;
			} catch (JSONException e) {
				Log.logException("", e);
				return false;
			}
		}

		if (jo.has(ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID
				.getAttributeName())) {
			try {
				if ((jo.has(ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID
						.getAttributeName()))
						&& (jo.getString(ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID
								.getAttributeName()).isEmpty())) {
					sb.append("Invalid/NULL Service Endpoint ID\n");
					list.add(false);
					// return false;
				}
				list.add(true);
				// valid = true;
			} catch (JSONException e) {
				Log.logException("", e);
				// return false;
				list.add(false);

			}
		}

		if (jo.has(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
				.getAttributeName())) {
			try {
				if ((jo.has(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
						.getAttributeName()))
						&& (jo.getString(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
								.getAttributeName()).isEmpty())) {
					sb.append("Invalid/NULL Endpoint URL");
					list.add(false);
					// return false;
				}
				list.add(true);
				// valid = true;
			} catch (JSONException e) {
				Log.logException("", e);
				return false;
			}
		}

		if (jo.has(ServiceBasicAttributeNames.SERVICE_ENDPOINT_TECHNOLOGY
				.getAttributeName())) {
			try {
				if ((jo.has(ServiceBasicAttributeNames.SERVICE_ENDPOINT_TECHNOLOGY
						.getAttributeName()))
						&& (jo.getString(ServiceBasicAttributeNames.SERVICE_ENDPOINT_TECHNOLOGY
								.getAttributeName()).isEmpty())) {
					sb.append("Invalid/NULL Service Endpoint Technology\n");
					list.add(false);
					// return false;
				}
				list.add(true);
				// valid = true;
			} catch (JSONException e) {
				Log.logException("", e);
				// return false;
				list.add(false);
			}
		}

		if (jo.has(ServiceBasicAttributeNames.SERVICE_ENDPOINT_IFACENAME
				.getAttributeName())) {
			try {
				if ((jo.has(ServiceBasicAttributeNames.SERVICE_ENDPOINT_IFACENAME
						.getAttributeName()))
						&& (jo.getString(ServiceBasicAttributeNames.SERVICE_ENDPOINT_IFACENAME
								.getAttributeName()).isEmpty())) {
					sb.append("Invalid/NULL Service Endpoint Interface Name\n");
					list.add(false);
					// return false;
				}
				list.add(true);
				// valid = true;
			} catch (JSONException e) {
				Log.logException("", e);
				// return false;
				list.add(false);
			}

			if (jo.has(ServiceBasicAttributeNames.SERVICE_ENDPOINT_IFACE_VER
					.getAttributeName())) {
				try {
					if ((jo.has(ServiceBasicAttributeNames.SERVICE_ENDPOINT_IFACE_VER
							.getAttributeName()))
							&& (jo.getString(ServiceBasicAttributeNames.SERVICE_ENDPOINT_IFACE_VER
									.getAttributeName()).isEmpty())) {
						sb.append("Invalid/NULL Service Endpoint Interface Version\n");
						list.add(false);
						// return false;
					}
					list.add(true);
					// valid = true;
				} catch (JSONException e) {
					Log.logException("", e);
					// return false;
					list.add(false);
				}

			}

		}

		if (jo.has(ServiceBasicAttributeNames.SERVICE_ENDPOINT_CAPABILITY
				.getAttributeName())) {
			try {
				if ((jo.has(ServiceBasicAttributeNames.SERVICE_ENDPOINT_CAPABILITY
						.getAttributeName()))
						&& (jo.getString(ServiceBasicAttributeNames.SERVICE_ENDPOINT_CAPABILITY
								.getAttributeName()).isEmpty())) {
					sb.append("Invalid/NULL Service Endpoint Interface Name\n");
					list.add(false);
					// return false;
				}
				list.add(true);
				// valid = true;
			} catch (JSONException e) {
				Log.logException("", e);
				// return false;
				list.add(false);
			}

			if (jo.has(ServiceBasicAttributeNames.SERVICE_ENDPOINT_IFACE_VER
					.getAttributeName())) {
				try {
					if ((jo.has(ServiceBasicAttributeNames.SERVICE_ENDPOINT_IFACE_VER
							.getAttributeName()))
							&& (jo.getString(ServiceBasicAttributeNames.SERVICE_ENDPOINT_IFACE_VER
									.getAttributeName()).isEmpty())) {
						sb.append("Invalid/NULL Service Endpoint Interface Version\n");
						list.add(false);
						// return false;
					}
					list.add(true);
					// valid = true;
				} catch (JSONException e) {
					Log.logException("", e);
					// return false;
					list.add(false);
				}

			}

		}

		if (list.contains(false)) {
			return false;
		}

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eu.emi.emir.validator.AbstractInfoValidator#checkMandatoryEndpointIDAttributes()
	 */
	@Override
	Boolean checkMandatoryEndpointIDAttributes() {
		StringBuilder sb = new StringBuilder(
				"Missing/Invalid mandatory Service Endpoint Record attributes: \n");
		List<Boolean> list = new ArrayList<Boolean>();
		if (jo.has(ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID
				.getAttributeName())) {
			try {
				if ((jo.has(ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID
						.getAttributeName()))
						&& (jo.getString(ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID
								.getAttributeName()).isEmpty())) {
					sb.append("Invalid/NULL Service Endpoint ID\n");
					list.add(false);
					// return false;
				}
				list.add(true);
				// valid = true;
			} catch (JSONException e) {
				Log.logException("", e);
				// return false;
				list.add(false);

			}
		}

		if (list.contains(false)) {
			return false;
		}

		return true;
	}

}
