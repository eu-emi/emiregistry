/**
 * 
 */
package eu.emi.emir.validator;

import org.codehaus.jettison.json.JSONObject;

/**
 * @author a.memon
 *
 */
public interface InfoValidator {
	public Boolean validateInfo(JSONObject jo);
}
