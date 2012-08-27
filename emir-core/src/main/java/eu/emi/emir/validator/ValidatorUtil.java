/**
 * 
 */
package eu.emi.emir.validator;

import java.text.ParseException;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import eu.unicore.util.configuration.ConfigurationException;

/**
 * @author a.memon
 *
 */
public class ValidatorUtil {
	/**
	 * Checks the service description being registered contains the mandatory
	 * service attributes
	 * 
	 * @param serviceDesc
	 * @throws InvalidServiceDescriptionException
	 * @throws ParseException 
	 * @throws ConfigurationException 
	 * @throws JSONException
	 */
	public synchronized static boolean isValidServiceInfo(JSONObject jo)
			throws InvalidServiceDescriptionException, ConfigurationException, JSONException, ParseException {
		return ValidatorFactory.getRegistrationValidator().validateInfo(jo);
	}

	/**
	 * Checks the service description being updated contains the service
	 * endpoint ID attributes
	 * 
	 * @param serviceDesc
	 * @throws InvalidServiceDescriptionException 
	 */
	public synchronized static boolean isValidRemovedServiceInfo(JSONObject jo)
			throws InvalidServiceDescriptionException {
		return ValidatorFactory.getRegistrationValidator().validateEndpointIDInfo(jo);
	}
	
}
