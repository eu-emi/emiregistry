/**
 * 
 */
package eu.emi.emir.core;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import eu.emi.emir.EMIRServer;
import eu.emi.emir.ServerProperties;
import eu.emi.emir.client.ServiceBasicAttributeNames;
import eu.emi.emir.client.util.Log;
import eu.emi.emir.db.ExistingResourceException;
import eu.emi.emir.db.MultipleResourceException;
import eu.emi.emir.db.NonExistingResourceException;
import eu.emi.emir.db.PersistentStoreFailureException;
import eu.emi.emir.db.QueryException;
import eu.emi.emir.db.ServiceDatabase;
import eu.emi.emir.db.mongodb.MongoDBServiceDatabase;
import eu.emi.emir.db.mongodb.ServiceObject;
import eu.emi.emir.event.Event;
import eu.emi.emir.event.EventDispatcher;
import eu.emi.emir.event.EventTypes;
import eu.emi.emir.exception.InvalidServiceDescriptionException;
import eu.emi.emir.exception.UnknownServiceException;
import eu.emi.emir.util.DateUtil;
import eu.emi.emir.util.ServiceUtil;
import eu.unicore.util.configuration.ConfigurationException;

/**
 * Class to perform Service Provider related functions
 * 
 * @author a.memon
 * @author g.szigeti
 * 
 */
public class ServiceAdminManager {
	private static Logger log = Log.getLogger(Log.EMIR_CORE, ServiceAdminManager.class);
	
	private ServiceDatabase serviceDB = null;

	

	/**
	 * @throws DatabaseUnavilableException 
	 * 
	 */
	public ServiceAdminManager(){
		serviceDB = new MongoDBServiceDatabase();
	}
	
	public ServiceAdminManager(MongoDBServiceDatabase mongoDB){
		serviceDB = mongoDB;
	}

	/**
	 * 
	 */

	/**
	 * @param jo
	 * @return the inserted service description
	 * @throws InvalidServiceDescriptionException 
	 * @throws JSONException 
	 * @throws ExistingResourceException 
	 * @throws ParseException 
	 * @throws ConfigurationException 
	 * @throws Exception 
	 * 
	 * 	 */
	public JSONObject addService(JSONObject jo) throws InvalidServiceDescriptionException, JSONException, ExistingResourceException, ConfigurationException, ParseException
			 {
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
			if (!jo.has(ServiceBasicAttributeNames.SERVICE_UPDATE_SINCE
					.getAttributeName())) {
				jo.put(ServiceBasicAttributeNames.SERVICE_UPDATE_SINCE
					.getAttributeName(), date);
			}

			// in case the expiry attribute is not mentioned in the service
			// description, then will be taken from the config or otherwise the
			// default prop. value
			if (!jo.has(ServiceBasicAttributeNames.SERVICE_EXPIRE_ON
					.getAttributeName())) {
				Integer expTime = EMIRServer.getServerProperties().getIntValue(ServerProperties.PROP_RECORD_EXPIRY_DEFAULT);
				if (log.isDebugEnabled()) {
					log.debug("The expiry attribute is missing from the registered service information. The information will be expired in "+ expTime +"day from now");
				}
				DateUtil.setExpiryTime(jo, expTime);
			}

			serviceDB.insert(new ServiceObject(jo));
			return jo;
		} catch (PersistentStoreFailureException e) {
			Log.logException("",e,log);
		}catch (ExistingResourceException e) {
			if (EMIRServer.getServerProperties().isGlobalEnabled()) {
				try {
					if (serviceDB.getServiceByEndpointID(jo.getString(
							ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID.getAttributeName()))
									.getServiceOwner() == null
						|| jo.getString(
								ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID.getAttributeName())
									.equals(EMIRServer.getServerProperties()
											.getValue(ServerProperties.PROP_ADDRESS))){
						/* 
						 * The stored entry was removed and it contains very base
						 * (Endpoint_URL and updateSince) information.
						 */
						serviceDB.update(new ServiceObject(jo));
						return jo;
					} else {
						/* 
						 * The stored entry has owner attribute (it is not removed entry),
						 * and it can not insert again.
						 */ 
						throw new ExistingResourceException(e);
					}
				} catch (MultipleResourceException e1) {
					Log.logException("",e,log);
				} catch (NonExistingResourceException e1) {
					Log.logException("",e,log);
				} catch (PersistentStoreFailureException e1) {
					Log.logException("",e,log);
				}
			} else {
				// Throw forward/higher the exception.
				throw new ExistingResourceException(e);
			}
		}
		return null;
	}

	/**
	 * Removing the service by endpoint ID
	 * 
	 * @param url
	 * @param messageTime
	 * @throws PersistentStoreFailureException
	 * @throws NonExistingResourceException
	 * @throws MultipleResourceException
	 * @throws JSONException 
	 */
	public void removeService(String endpointID, String messageTime) throws MultipleResourceException, NonExistingResourceException, PersistentStoreFailureException, JSONException{
		if (EMIRServer.getServerProperties().isGlobalEnabled()) {
			// Update message will be contains only the URL and the update since attributes.
			JSONObject newEntry = new JSONObject();
			newEntry.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID
							.getAttributeName(),endpointID);
			
			JSONObject date = new JSONObject();
			date.put("$date", messageTime);
			newEntry.put(ServiceBasicAttributeNames.SERVICE_UPDATE_SINCE
							.getAttributeName(), date);

			ServiceObject sObj = new ServiceObject(newEntry);
			try {
				serviceDB.update(sObj);
				JSONArray arr = new JSONArray();
				arr.put(newEntry);
				EventDispatcher.notifyRecievers(new Event(
						EventTypes.SERVICE_UPDATE, arr));
			} catch (MultipleResourceException e) {
				Log.logException("",e,log);
			} catch (NonExistingResourceException e) {
				throw new WebApplicationException(Status.CONFLICT);
			} catch (PersistentStoreFailureException e) {
				Log.logException("",e,log);
			}
		} else {
			serviceDB.deleteByEndpointID(endpointID);
		}
	}

	/**
	 * @param jo
	 * @throws JSONException
	 * @throws InvalidServiceDescriptionException
	 * @throws ParseException 
	 * @throws ConfigurationException 
	 * @throws NonExistingResourceException
	 * @throws MultipleResourceException
	 */
	public JSONObject updateService(JSONObject jo) throws UnknownServiceException,
			InvalidServiceDescriptionException, JSONException, WebApplicationException, ConfigurationException, ParseException {
		if (!ServiceUtil.isValidServiceInfo(jo) ) {
			if (EMIRServer.getServerProperties().isGlobalEnabled() &&
						!ServiceUtil.isValidRemovedServiceInfo(jo)) {
				throw new InvalidServiceDescriptionException(
						"The service description does not contain valid attributes: serviceurl and servicetype");
			}
			//TODO: accepted message with only one simple Endpoint_ID
		}
		
		Integer expTime = EMIRServer.getServerProperties().getIntValue(ServerProperties.PROP_RECORD_EXPIRY_DEFAULT);
		if (log.isDebugEnabled()) {
			log.debug("The expiry attribute is missing from the updated service information. The information will be expired in "+ expTime +"day from now");
		}
		DateUtil.setExpiryTime(jo, expTime);
		
		// request json should not update the creation time
		if (!jo.has(ServiceBasicAttributeNames.SERVICE_CREATED_ON
				.getAttributeName())) {
			JSONObject date = new JSONObject();
			date.put("$date", ServiceUtil.toUTCFormat(new Date()));
			jo.put(ServiceBasicAttributeNames.SERVICE_CREATED_ON
					.getAttributeName(), date);
		}

		// setting the update time
		if (!jo.has(ServiceBasicAttributeNames.SERVICE_UPDATE_SINCE
				.getAttributeName())) {
			jo = DateUtil.addDate(jo,
				ServiceBasicAttributeNames.SERVICE_UPDATE_SINCE
						.getAttributeName(), new Date());
		}

		ServiceObject sObj = new ServiceObject(jo);
		try {
			serviceDB.update(sObj);
			return jo;
		} catch (MultipleResourceException e) {
			Log.logException("", e,log);
		} catch (NonExistingResourceException e) {
			throw new WebApplicationException(Status.CONFLICT);
		} catch (PersistentStoreFailureException e) {
			Log.logException("", e,log);
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
			Log.logException("", e, log);
		}
		if (so == null) {
			return null;
		}
		return so.toJSON();
	}
	
	
	/**
	 * Finding a service by endpoint id
	 * 
	 * @param string
	 * @throws NonExistingResourceException
	 * @throws MultipleResourceException
	 * @throws PersistentStoreFailureException
	 */
	public JSONObject findServiceByEndpointID(String endpointID)
			throws NonExistingResourceException,
			PersistentStoreFailureException {
		ServiceObject so = null;
		try {
			so = serviceDB.getServiceByEndpointID(endpointID);
		} catch (MultipleResourceException e) {
			Log.logException("", e);
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
	 * @param sendpointID 
	 * @return
	 * @throws PersistentStoreFailureException
	 * @throws QueryException
	 * @throws JSONException 
	 */
	public boolean checkOwner(String owner, String sendpointID) throws QueryException,
			PersistentStoreFailureException, JSONException {
		List<ServiceObject> query = new ArrayList<ServiceObject>();

		// since 2.0.1 supported the "and" operation
		/* Query structure:
		 *        {"$or":[{"$and":[{"serviceOwner":"<DN>"},
		 *                         {"Service_Endpoint_ID":"<ID>"}]},
		 *                {"$and":[{"Service_DN":"<DN>"},
		 *                         {"Service_Type":"GSR"}]}
		 *                 ]}
		 */
		// AND1 structure
		JSONArray and1 = new JSONArray();
		JSONObject andParam1 = new JSONObject();
		andParam1.put(ServiceBasicAttributeNames.SERVICE_OWNER_DN.getAttributeName(), owner);
		and1.put(andParam1);

		JSONObject andParam2 = new JSONObject();
		andParam2.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID.getAttributeName(), sendpointID);
		and1.put(andParam2);

		JSONObject orParam1 = new JSONObject();
		orParam1.put("$and", and1);
			
		// AND2 structure
		JSONArray and2 = new JSONArray();
		JSONObject andParam21 = new JSONObject();
		andParam21.put(ServiceBasicAttributeNames.SERVICE_DN.getAttributeName(), owner);
		and2.put(andParam21);

		JSONObject andParam22 = new JSONObject();
		andParam22.put(ServiceBasicAttributeNames.SERVICE_TYPE.getAttributeName(), "GSR");
		and2.put(andParam22);

		JSONObject orParam2 = new JSONObject();
		orParam2.put("$and", and2);

		JSONArray or = new JSONArray();
		or.put(orParam1);
		or.put(orParam2);

		// OR structure
		JSONObject OR = new JSONObject();
		OR.put("$or", or);
		//System.out.println(OR.toString());
		query = serviceDB.query(OR.toString());
		//System.out.println(objects.toString());

		if (!query.isEmpty()) {
			return true;
		} else {
			JSONObject entryExist = new JSONObject();
			entryExist.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID.getAttributeName(), sendpointID);

			if (serviceDB.query(entryExist.toString()).isEmpty()) {
				// The entry does not exist
				return true;
			}
			log.debug("Wrong owner ("+ owner +") try to modify this Service Endpoint ID: "+ sendpointID);
			return false;
		}
	}
	
	/**
	 * @param messageGenerationTime
	 * @param sendpointID 
	 * @return
	 * @throws PersistentStoreFailureException
	 * @throws QueryException
	 * TODO: What this method is doing? document it!!!
	 */
	public boolean checkMessageGenerationTime(String messageTime, String sendpointID) throws QueryException,
			PersistentStoreFailureException {
		// Message time checking need only by the Global EMIR
		if (!EMIRServer.getServerProperties().isGlobalEnabled()) {
			return true;
		}
		
		Map<String, String> map = new HashMap<String, String>();
			
		map.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID.getAttributeName(), sendpointID);
		
		JSONObject jo = new JSONObject(map);
		List<ServiceObject> storedEntries = serviceDB.query(jo.toString());
		if (storedEntries.size() > 0) {
			Date messageDate = new Date();
			try {
				// set the message date to GMT time
				messageDate =  ServiceUtil.UTCISODateFormat.parse(ServiceUtil.toUTCFormat(messageDate));
				// if possible set the message date from the message time
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
				log.debug("The incoming entry was too old and don't accept it.");
				return false;
			}
		}
		return true;
	}
}
