/**
 * 
 */
package eu.emi.emir.validator;

import java.text.ParseException;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import eu.emi.emir.exception.InvalidServiceDescriptionException;
import eu.unicore.util.configuration.ConfigurationException;

/**
 * @author a.memon
 *
 */
public interface InfoValidator {
	public Boolean validateInfo(JSONObject jo) throws InvalidServiceDescriptionException, ConfigurationException, JSONException, ParseException;
}