/**
 * 
 */
package eu.emi.emir.validator;

import org.codehaus.jettison.json.JSONObject;

/**
 * @author a.memon
 *
 */
public abstract class AbstractInfoValidator implements InfoValidator{
	Boolean valid = null;
	JSONObject jo = null;
	
	
	@Override
	public Boolean validateInfo(JSONObject jo){
		valid = false;
		this.jo = jo;
		
		
		if (checkUrl() && checkDateTypes() && checkExpiryTime() && checkArrays()) {
			return true;
		} else {
			return false;
		}
		
	}
	/**
	 * @return
	 */
	abstract boolean checkArrays();
	/**
	 * 
	 */
	abstract Boolean checkUrl();
	abstract Boolean checkDateTypes();
	abstract Boolean checkExpiryTime();
}
