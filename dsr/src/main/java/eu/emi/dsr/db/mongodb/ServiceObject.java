/**
 * 
 */
package eu.emi.dsr.db.mongodb;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.eclipse.jetty.util.DateCache;
import org.eclipse.jetty.util.ajax.JSONDateConvertor;

import com.mongodb.DBObject;
import com.mongodb.util.JSON;

import eu.emi.dsr.core.ServiceBasicAttributeNames;
import eu.emi.dsr.util.Log;
import eu.emi.dsr.util.ServiceUtil;

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
	
	public ServiceObject(DBObject jo) throws JSONException {
		this.jo = new JSONObject(JSON.serialize(jo));
	}

	public String getServiceOwner() {
		String owner = null;
		try {
			owner = jo
					.get(ServiceBasicAttributeNames.SERVICE_OWNER
							.getAttributeName()).toString();
		} catch (JSONException e) {
			e.printStackTrace();
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return serviceUrl;
	}

	public Date getCreationTime() {
		Date d = null;
		try {

			d = ServiceUtil.ServiceDateFormat.parse((String) jo
					.get(ServiceBasicAttributeNames.SERVICE_CREATED_ON
							.getAttributeName()));
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
			d = ServiceUtil.ServiceDateFormat.parse(jo.get(
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
			d = ServiceUtil.ServiceDateFormat.parse((String) jo
					.get(ServiceBasicAttributeNames.SERVICE_EXPIRE_ON
							.getAttributeName()));
		} catch (ParseException e) {
			Log.logException(e);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		if (d.get(ServiceBasicAttributeNames.SERVICE_CREATED_ON
				.getAttributeName()) != null)
			d.put(ServiceBasicAttributeNames.SERVICE_CREATED_ON
					.getAttributeName(), getCreationTime());

		if (d.get(ServiceBasicAttributeNames.SERVICE_EXPIRE_ON
				.getAttributeName()) != null)
			d.put(ServiceBasicAttributeNames.SERVICE_EXPIRE_ON
					.getAttributeName(), getExpireOn());

		if (d.get(ServiceBasicAttributeNames.SERVICE_UPDATE_SINCE
				.getAttributeName()) != null)
			d.put(ServiceBasicAttributeNames.SERVICE_UPDATE_SINCE
					.getAttributeName(), getUpdateSince());
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
