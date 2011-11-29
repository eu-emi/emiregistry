/**
 * 
 */
package eu.emi.dsr.core;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import eu.emi.client.ServiceBasicAttributeNames;
import eu.emi.client.util.Log;
import eu.emi.dsr.DSRServer;
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
import eu.emi.dsr.util.DateUtil;
import eu.emi.dsr.util.ServiceUtil;

/**
 * Class to perform Service Provider related functions
 * 
 * @author a.memon
 * @author g.szigeti
 * 
 */
public class ServiceAdminManager {
	private static Logger log = Log.getLogger(Log.DSR, ServiceAdminManager.class);
	
	private ServiceDatabase serviceDB = null;

	

	/**
	 * @throws DatabaseUnavilableException 
	 * 
	 */
	public ServiceAdminManager(){
		serviceDB = new MongoDBServiceDatabase();
//		serviceDB = MongoDBServiceDatabase.getInstance();
	}

	/**
	 * 
	 */

	/**
	 * @param jo
	 * @return the inserted service description
	 * @throws JSONException
	 * @throws PersistentStoreFailureException
	 * @throws ExistingResourceException
	 * 
	 * 	 */
	public JSONObject addService(JSONObject jo)
			throws InvalidServiceDescriptionException, JSONException, ExistingResourceException {
		if (!ServiceUtil.isValidServiceInfo(jo)) {
			throw new InvalidServiceDescriptionException(
					"The service description does not contain valid attributes");
		}
		try {
			// current time and last update should be same in the beginning
			JSONObject date = new JSONObject();
			date.put("$date", ServiceUtil.toUTCFormat(new Date()));
			jo.put(ServiceBasicAttributeNames.SERVICE_CREATED_ON
					.getAttributeName(), date);
			jo.put(ServiceBasicAttributeNames.SERVICE_UPDATE_SINCE
					.getAttributeName(), date);

			// in case the expiry attribute is not mentioned in the service
			// description, then will be taken from the config or otherwise the
			// default prop. value
			if (!jo.has(ServiceBasicAttributeNames.SERVICE_EXPIRE_ON
					.getAttributeName())) {
				if (log.isDebugEnabled()) {
					log.debug("The expiry attribute is missing from the updated service information. The information will be expired in 1 day from now");
				}
				DateUtil.setExpiryTime(jo, Integer.valueOf(DSRServer
						.getProperty(ServerConstants.REGISTRY_EXPIRY_DEFAULT,
								"1")));
			}

			serviceDB.insert(new ServiceObject(jo));
			return jo;
		} catch (PersistentStoreFailureException e) {
			Log.logException(e);
		}
		return null;
	}

	/**
	 * Removing the service by url
	 * 
	 * @param url
	 * @throws PersistentStoreFailureException
	 * @throws NonExistingResourceException
	 * @throws MultipleResourceException
	 */
	public void removeService(String url) throws MultipleResourceException, NonExistingResourceException, PersistentStoreFailureException{
		serviceDB.deleteByUrl(url);
		 
	}

	/**
	 * @param jo
	 * @throws JSONException
	 * @throws InvalidServiceDescriptionException
	 * @throws NonExistingResourceException
	 * @throws MultipleResourceException
	 */
	public JSONObject updateService(JSONObject jo) throws UnknownServiceException,
			InvalidServiceDescriptionException, JSONException, WebApplicationException {
		if (!ServiceUtil.isValidServiceInfo(jo)) {
			throw new InvalidServiceDescriptionException(
					"The service description does not contain valid attributes: serviceurl and servicetype");
		}
		
		if (!jo.has(ServiceBasicAttributeNames.SERVICE_EXPIRE_ON.getAttributeName())) {
			if (log.isDebugEnabled()) {
				log.debug("The expiry attribute is missing from the updated service information. The information will be expired in 1 day from now");
			}
			DateUtil.setExpiryTime(jo, Integer.valueOf(DSRServer
					.getProperty(ServerConstants.REGISTRY_EXPIRY_DEFAULT,
							"1")));
		}
		
		// request json should not update the creation time
		if (!jo.has(ServiceBasicAttributeNames.SERVICE_CREATED_ON
				.getAttributeName())) {
			JSONObject date = new JSONObject();
			date.put("$date", ServiceUtil.toUTCFormat(new Date()));
			jo.put(ServiceBasicAttributeNames.SERVICE_CREATED_ON
					.getAttributeName(), date);
		}

		// setting the update time
		jo = DateUtil.addDate(jo,
				ServiceBasicAttributeNames.SERVICE_UPDATE_SINCE
						.getAttributeName(), new Date());

		ServiceObject sObj = new ServiceObject(jo);
		try {
			serviceDB.update(sObj);
			return jo;
		} catch (MultipleResourceException e) {
			e.printStackTrace();
		} catch (NonExistingResourceException e) {
			throw new WebApplicationException(Status.CONFLICT);
		} catch (PersistentStoreFailureException e) {
			e.printStackTrace();
		}
		return null;

	}

	/**
	 * Finding a service by its url
	 * 
	 * @param string
	 * @throws NonExistingResourceException
	 * @throws MultipleResourceException
	 * @throws PersistentStoreFailureException
	 */
	public JSONObject findServiceByUrl(String url)
			throws NonExistingResourceException,
			PersistentStoreFailureException {
		ServiceObject so = null;
		try {
			so = serviceDB.getServiceByUrl(url);
		} catch (MultipleResourceException e) {
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
			PersistentStoreFailureException{
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

	/**
	 * @param owner
	 * @param serviceurl 
	 * @return
	 * @throws PersistentStoreFailureException
	 * @throws QueryException
	 */
	public boolean checkOwner(String owner, String serviceurl) throws QueryException,
			PersistentStoreFailureException {
		// First query
		Map<String, String> map = new HashMap<String, String>();
		
		map.put(ServiceBasicAttributeNames.SERVICE_OWNER.getAttributeName(),
				owner);
		
		map.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL.getAttributeName(), serviceurl);
		
		JSONObject jo = new JSONObject(map);
		
		List<ServiceObject> objects = serviceDB.query(jo.toString());
		
		List<ServiceObject> objects2 = new ArrayList<ServiceObject>();
		if ("true".equalsIgnoreCase(DSRServer
				.getProperty(ServerConstants.REGISTRY_GLOBAL_ENABLE, "false"))){
			// Second query
			// We accept every messages from GSRs
			Map<String, String> map2 = new HashMap<String, String>();
			
			map2.put(ServiceBasicAttributeNames.SERVICE_OWNER.getAttributeName(),
					owner);
			
			map2.put(ServiceBasicAttributeNames.SERVICE_TYPE.getAttributeName(), "GSR");
			
			JSONObject jo2 = new JSONObject(map2);
			objects2 = serviceDB.query(jo2.toString());
		}

		if (!objects.isEmpty() || !objects2.isEmpty())
			return true;
		else
			return false;
	}
	
	/**
	 * @param messageGenerationTime
	 * @param serviceurl 
	 * @return
	 * @throws PersistentStoreFailureException
	 * @throws QueryException
	 */
	public boolean checkMessageGenerationTime(String messageTime, String serviceurl) throws QueryException,
			PersistentStoreFailureException {
		Map<String, String> map = new HashMap<String, String>();
			
		map.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL.getAttributeName(), serviceurl);
		
		JSONObject jo = new JSONObject(map);
		List<ServiceObject> storedEntries = serviceDB.query(jo.toString());
		if (storedEntries.size() > 0) {
			Date messageDate = new Date();
			try {
				messageDate =  ServiceUtil.UTCISODateFormat.parse(messageTime);
			} catch (ParseException e) {
				// no problem, the time was empty
				//Log.logException(e);
			}
			Date entryDate = storedEntries.get(0).getUpdateSince();
			if (messageDate.compareTo(entryDate) > 0){
				// The given message newer than the stored
				return true;
			} else {
				return false;
			}
		}
		return true;
	}
}
