/**
 * 
 */
package eu.emi.emir.db.mongodb;

import java.text.ParseException;
import java.util.Date;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

import eu.emi.emir.client.ServiceBasicAttributeNames;
import eu.emi.emir.client.util.DateUtil;
import eu.emi.emir.client.util.Log;

/**
 * Mongo flavor of EMIRegistry Service description
 * 
 * @author a.memon
 * 
 */
public class ServiceObject {
	private static Logger logger = Log.getLogger(Log.EMIR_CORE, ServiceObject.class);
	/**
	 * 
	 */
	@SuppressWarnings("unused")
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
	
	public ServiceObject(DBObject jo) throws JSONException {
		this.jo = new JSONObject(JSON.serialize(jo));
	}

	public String getServiceOwner() {
		String owner = null;
		try {
			owner = jo
					.get(ServiceBasicAttributeNames.SERVICE_OWNER_DN
							.getAttributeName()).toString();
		} catch (JSONException e) {
			Log.logException("serviceOwner not exist!", e);
		}
		return owner;
	}

	public String getUrl() {
		String serviceUrl = null;
		try {
			serviceUrl = jo.get(
					ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL.getAttributeName())
					.toString();
		} catch (JSONException e) {
			Log.logException("", e);
		}
		return serviceUrl;
	}
	
	public String getEndpointID() {
		String serviceEndpointID = null;
		try {
			serviceEndpointID = jo.get(
					ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID.getAttributeName())
					.toString();
		} catch (JSONException e) {
			Log.logException("", e);
		}
		return serviceEndpointID;
	}
	
	public String getServiceID() {
		String serviceID = null;
		try {
			serviceID = jo.get(
					ServiceBasicAttributeNames.SERVICE_ID.getAttributeName())
					.toString();
		} catch (JSONException e) {
			Log.logException("", e);
		}
		return serviceID;
	}

	public Date getCreationTime() {
		Date d = null;
		try {

			d = DateUtil.ServiceDateFormat.parse((String) jo
					.get(ServiceBasicAttributeNames.SERVICE_CREATED_ON
							.getAttributeName()));
		} catch (ParseException e) {
			Log.logException("",e,logger);
		} catch (JSONException e) {
			Log.logException("", e);
		}
		return d;
	}

	public Date getUpdateSince() {
		Date d = null;
		try {
			d = DateUtil.UTCISODateFormat.parse(((JSONObject)jo.get(
					ServiceBasicAttributeNames.SERVICE_UPDATE_SINCE
							.getAttributeName())).get("$date").toString());
		} catch (ParseException e) {
			Log.logException("",e,logger);
		} catch (JSONException e) {
			Log.logException("", e);
		} catch (ClassCastException e){
			Log.logException("No updateSince attribute by the \"deleted\" entry!", e);
			d = new Date(1);
		}
		return d;
	}

	public Date getExpireOn() {
		Date d = null;
		try {
			d = DateUtil.ServiceDateFormat.parse((String) jo
					.get(ServiceBasicAttributeNames.SERVICE_EXPIRE_ON
							.getAttributeName()));
		} catch (ParseException e) {
			Log.logException("",e,logger);
		} catch (JSONException e) {
			Log.logException("", e);
		}
		return d;
	}

	public JSONObject toJSON() {
		if (jo == null)
			return null;
		return jo;
	}

	public DBObject toDBObject() {
		DBObject d = null;
		d = (DBObject) JSON.parse(toString());
		// changing dates

		// for the newly created service
//		if (d.get(ServiceBasicAttributeNames.SERVICE_CREATED_ON
//				.getAttributeName()) != null)
//			d.put(ServiceBasicAttributeNames.SERVICE_CREATED_ON
//					.getAttributeName(), getCreationTime());
//
//		if (d.get(ServiceBasicAttributeNames.SERVICE_EXPIRE_ON
//				.getAttributeName()) != null)
//			d.put(ServiceBasicAttributeNames.SERVICE_EXPIRE_ON
//					.getAttributeName(), getExpireOn());
//
//		if (d.get(ServiceBasicAttributeNames.SERVICE_UPDATE_SINCE
//				.getAttributeName()) != null)
//			d.put(ServiceBasicAttributeNames.SERVICE_UPDATE_SINCE
//					.getAttributeName(), getUpdateSince());
		return d;
	}

	/**
	 * Returns a JSON serialization of this object
	 * 
	 * @return JSON serialization
	 */
	@Override
	public String toString() {
		return jo.toString();
	}

}
