/**
 * 
 */
package eu.emi.dsr.core;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import eu.emi.dsr.db.ExistingResourceException;
import eu.emi.dsr.db.MultipleResourceException;
import eu.emi.dsr.db.NonExistingResourceException;
import eu.emi.dsr.db.PersistentStoreFailureException;
import eu.emi.dsr.db.QueryException;
import eu.emi.dsr.db.ServiceDatabase;
import eu.emi.dsr.db.mongodb.MongoDBServiceDatabase;
import eu.emi.dsr.db.mongodb.ServiceObject;
import eu.emi.dsr.exception.InvalidServiceDescriptionException;
import eu.emi.dsr.exception.UnknownServiceException;
import eu.emi.dsr.util.Log;
import eu.emi.dsr.util.ServiceUtil;

/**
 * Class to perform Service Provider related functions
 * 
 * @author a.memon
 * 
 */
public class ServiceAdminManager {
	private static Logger logger = Log.getLogger(Log.DSR,
			ServiceAdminManager.class);
	private ServiceDatabase serviceDB = null;

	/**
	 * 
	 */
	public ServiceAdminManager() {
		serviceDB = new MongoDBServiceDatabase();
	}

	/**
	 * 
	 */

	/**
	 * @param jo
	 * @return the service id
	 * @throws JSONException
	 * @throws PersistentStoreFailureException
	 * @throws ExistingResourceException
	 */
	public void addService(JSONObject jo)
			throws InvalidServiceDescriptionException, JSONException {
		if (!ServiceUtil.isValidServiceInfo(jo)) {
			throw new InvalidServiceDescriptionException(
					"The service description does not contain valid attributes");
		}
		try {
			// current time and last update should be same in the beginning
			jo.put(ServiceBasicAttributeNames.SERVICE_CREATED_ON
					.getAttributeName(), ServiceUtil.ServiceDateFormat
					.format(new Date()));
			jo.put(ServiceBasicAttributeNames.SERVICE_UPDATE_SINCE
					.getAttributeName(), ServiceUtil.ServiceDateFormat
					.format(new Date()));
			serviceDB.insert(new ServiceObject(jo));
		} catch (ExistingResourceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (PersistentStoreFailureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Removing the service by url
	 * 
	 * @param url
	 * @throws PersistentStoreFailureException
	 * @throws NonExistingResourceException
	 * @throws MultipleResourceException
	 */
	public void removeService(String url) throws UnknownServiceException {
		try {
			serviceDB.deleteByUrl(url);
		} catch (MultipleResourceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NonExistingResourceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (PersistentStoreFailureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @param jo
	 * @throws JSONException
	 * @throws InvalidServiceDescriptionException
	 * @throws NonExistingResourceException
	 * @throws MultipleResourceException
	 */
	public void updateService(JSONObject jo) throws UnknownServiceException,
			InvalidServiceDescriptionException, JSONException {
		if (!ServiceUtil.isValidServiceInfo(jo)) {
			throw new InvalidServiceDescriptionException(
					"The service description does not contain valid attributes: serviceurl and servicetype");
		}

		// request should json should not update the creation time
		if (jo.has(ServiceBasicAttributeNames.SERVICE_CREATED_ON
				.getAttributeName())) {
			jo.remove(ServiceBasicAttributeNames.SERVICE_CREATED_ON
					.getAttributeName());
		}

		// setting the update time
		jo.put(ServiceBasicAttributeNames.SERVICE_UPDATE_SINCE
				.getAttributeName(), ServiceUtil.ServiceDateFormat
				.format(new Date()));

		ServiceObject sObj = new ServiceObject(jo);
		try {
			serviceDB.update(sObj);
		} catch (MultipleResourceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NonExistingResourceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (PersistentStoreFailureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	

	/**
	 * Finding a service by its url
	 * 
	 * @param string
	 * @throws NonExistingResourceException
	 * @throws MultipleResourceException
	 * @throws PersistentStoreFailureException
	 */
	public JSONObject findServiceByUrl(String url) {
		ServiceObject so = null;
		try {
			so = serviceDB.getServiceByUrl(url);
		} catch (MultipleResourceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NonExistingResourceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (PersistentStoreFailureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (so == null) {
			return null;
		}
		return so.toJSON();
	}

	/**
	 * Remove expired entries
	 * 
	 * @throws JSONException
	 * @throws PersistentStoreFailureException
	 * @throws QueryException
	 * */
	public void removeExpiredEntries() throws JSONException, QueryException,
			PersistentStoreFailureException {
		JSONObject date = new JSONObject();

		JSONObject predicate = new JSONObject();
		JSONObject query = new JSONObject();
		// { "serviceExpireOn" : { "$lte" : { "$date" :
		// "2011-07-06T16:05:40Z"}}}

		date.put("$date", ServiceUtil.toUTCFormat(new Date()));
		predicate.put("$lte", date);
		query.put(
				ServiceBasicAttributeNames.SERVICE_EXPIRE_ON.getAttributeName(),
				predicate);
		serviceDB.findAndDelete(query.toString());
		// j.put(ServiceBasicAttributeNames.SERVICE_EXPIRE_ON.getAttributeName(),
		// quer)

		// serviceDB.findAndDelete(j.toString());
	}

	public void removeAll() {
		serviceDB.deleteAll();
	}

	public List<ServiceObject> findAll() throws JSONException {
		return serviceDB.findAll();
	}

}
