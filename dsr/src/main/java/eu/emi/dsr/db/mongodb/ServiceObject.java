/**
 * 
 */
package eu.emi.dsr.db.mongodb;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.mongodb.DBObject;
import com.mongodb.util.JSON;

import eu.emi.dsr.core.ServiceBasicAttributeNames;
import eu.emi.dsr.util.Log;

/**
 * Mongo flavor of EMIRegistry Service description
 * 
 * @author a.memon
 * 
 */
public class ServiceObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8819684158264382427L;
	
	
	private final JSONObject jo;
	
	/**
	 * @throws JSONException 
	 * 
	 */
	public ServiceObject(String jsonString) throws JSONException {
		this.jo = new JSONObject(jsonString);
	}
	
	public ServiceObject(JSONObject jo) throws JSONException {
		this.jo = jo;
	}
	
	
	private static SimpleDateFormat sf = new SimpleDateFormat(
			"dd-mm-yyyy, HH:mm");

	public String getServiceOwner() {
		String owner = null;
		try {
			owner = jo.get(ServiceBasicAttributeNames.SERVICE_OWNER.getAttributeName())
					.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return owner;
	}

	public String getUrl() {
		String serviceUrl = null;
		try {
			serviceUrl = jo.get(ServiceBasicAttributeNames.SERVICE_URL.getAttributeName())
					.toString();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return serviceUrl;
	}

	public Date getCreationTime() {
		Date d = null;
		try {
			d = sf.parse(jo.get(
					ServiceBasicAttributeNames.SERVICE_CREATED_ON
							.getAttributeName()).toString());
		} catch (ParseException e) {
			Log.logException(e);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return d;
	}

	public Date getUpdateSince() {
		Date d = null;
		try {
			d = sf.parse(jo.get(
					ServiceBasicAttributeNames.SERVICE_UPDATE_SINCE
							.getAttributeName()).toString());
		} catch (ParseException e) {
			Log.logException(e);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return d;
	}

	public Date getExpireOn() {
		Date d = null;
		try {
			d = sf.parse(jo.get(
					ServiceBasicAttributeNames.SERVICE_EXPIRE_ON
							.getAttributeName()).toString());
		} catch (ParseException e) {
			Log.logException(e);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return d;
	}

	public JSONObject toJSON() {
		if(jo == null)
			return null;
		return jo;
	}
	
	public DBObject toDBObject() {
		DBObject d = null; 
		d = (DBObject) JSON.parse(toString()); 
		return d;
	}
	
   
    /**
     * Returns a JSON serialization of this object
     * @return JSON serialization
     */    
    @Override
    public String toString(){
        return jo.toString();
    }

    


}
