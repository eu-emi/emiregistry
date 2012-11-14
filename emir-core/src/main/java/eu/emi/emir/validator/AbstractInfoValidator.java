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
public abstract class AbstractInfoValidator implements InfoValidator{
	Boolean valid = null;
	JSONObject jo = null;
	
	
	@Override
	public Boolean validateInfo(JSONObject jo) throws InvalidServiceDescriptionException, ConfigurationException, JSONException, ParseException{
		valid = false;
		this.jo = jo;
		
		
		if (checkMandatoryAttributes() && checkDateTypes() && checkExpiryTime()) {
			return true;
		} else {
			return false;
		}
		
	}
	
	@Override
	public Boolean validateEndpointIDInfo(JSONObject jo) throws InvalidServiceDescriptionException{
		valid = false;
		this.jo = jo;
		
		
		if (checkMandatoryEndpointIDAttributes()) {
			return true;
		} else {
			return false ;
		}
		
	}

	/**
	 * @return
	 */
//	abstract boolean checkArrays();
	/**
	 * 
	 */
//	abstract Boolean checkUrl();
	abstract Boolean checkDateTypes() throws InvalidServiceDescriptionException ;
	abstract Boolean checkExpiryTime() throws InvalidServiceDescriptionException, ConfigurationException, JSONException, ParseException ;
	/**
	 * Check service mandatory attributes (see mandatory attributes <a href="https://twiki.cern.ch/twiki/bin/view/EMI/EMIRSERDesc">EMIR Mandatory Attributes</a>)
	 * */
	abstract Boolean checkMandatoryAttributes() throws InvalidServiceDescriptionException;

	/**
	 * Check service mandatory attributes by the removed status entry at GSR
	 * 		Service_Endpoint_ID
	 * 
	 **/
	abstract Boolean checkMandatoryEndpointIDAttributes() throws InvalidServiceDescriptionException;

}
