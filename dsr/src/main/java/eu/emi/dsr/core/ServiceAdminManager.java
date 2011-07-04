/**
 * 
 */
package eu.emi.dsr.core;


import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.WebApplicationException;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import eu.emi.dsr.exception.InvalidServiceDescriptionException;
import eu.emi.dsr.exception.UnknownServiceException;
import eu.emi.dsr.util.Log;
import eu.emi.dsr.util.ServiceUtil;

/**
 * @author a.memon
 *
 */
public class ServiceAdminManager {
	
	
	
	/**
	 * @param jo
	 * @return the service id
	 * @throws JSONException 
	 */
	public String addService(JSONObject jo) throws InvalidServiceDescriptionException, JSONException{
			if(!ServiceUtil.isValid(jo)){
				throw new InvalidServiceDescriptionException("The service description does not contain valid attributes: serviceurl and servicetype");
			}
		return "id";
	}

	/**
	 * @param string
	 */
	public void removeService(String string) throws UnknownServiceException{
		// TODO Auto-generated method stub
		
	}

	/**
	 * @param jo
	 * @throws JSONException 
	 * @throws InvalidServiceDescriptionException 
	 */
	public String updateService(JSONObject jo) throws UnknownServiceException, InvalidServiceDescriptionException, JSONException{
		if(!ServiceUtil.isValid(jo)){
			throw new InvalidServiceDescriptionException("The service description does not contain valid attributes: serviceurl and servicetype");
		}
	return "id";
		
	}

	/**
	 * @param string
	 */
	public JSONObject findServiceByUrl(String string) throws UnknownServiceException{
		//TODO
		Map<String, String> map = new HashMap<String, String>();
		map.put("serviceurl", "url");
		return new JSONObject(map);
		
	}

}
