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
public interface InfoValidator {
	public Boolean validateInfo(JSONObject jo) throws InvalidServiceDescriptionException, ConfigurationException, JSONException, ParseException;
	public Boolean validateEndpointIDInfo(JSONObject jo) throws InvalidServiceDescriptionException;
}
