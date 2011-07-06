/**
 * 
 */
package eu.emi.dsr.core;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import eu.emi.dsr.db.ExistingResourceException;
import eu.emi.dsr.db.MultipleResourceException;
import eu.emi.dsr.db.NonExistingResourceException;
import eu.emi.dsr.db.PersistentStoreFailureException;
import eu.emi.dsr.db.ServiceDatabase;
import eu.emi.dsr.db.mongodb.MongoDBServiceDatabase;
import eu.emi.dsr.db.mongodb.ServiceObject;
import eu.emi.dsr.exception.InvalidServiceDescriptionException;
import eu.emi.dsr.exception.UnknownServiceException;
import eu.emi.dsr.util.ServiceUtil;

/**
 * Class to perform Service Provider related functions
 * 
 * @author a.memon
 * 
 */
public class ServiceAdminManager {

	private static ServiceDatabase serviceDB = null;
	
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
			throws InvalidServiceDescriptionException, JSONException
			{
		if (!ServiceUtil.isValidServiceInfo(jo)) {
			throw new InvalidServiceDescriptionException(
					"The service description does not contain valid attributes");
		}
		try {
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
	public void removeService(String url) throws UnknownServiceException
			 {
		try {
			serviceDB.delete(url);
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
			InvalidServiceDescriptionException, JSONException
			 {
		if (!ServiceUtil.isValidServiceInfo(jo)) {
			throw new InvalidServiceDescriptionException(
					"The service description does not contain valid attributes: serviceurl and servicetype");
		}

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
	public JSONObject findServiceByUrl(String url)	 {
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

}
