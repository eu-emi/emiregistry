/**
 * 
 */
package eu.emi.dsr.db;

import java.util.List;

import org.codehaus.jettison.json.JSONException;

import eu.emi.dsr.db.mongodb.ServiceObject;

/**
 * @author martoni
 *
 */
public interface ServiceDatabase {

	/**
	 * Insert service description to the persistent store. If it throws an exception if the item already exists
	 * @param item an item to insert into the database
	 * @throws ExistingResourceException throws exception if the element already exists
	 * @throws PersistentStoreFailureException throws exception if the persistent store is unavailable
	 * @return the identifier of the new element
	 */
	public void insert(ServiceObject item) throws ExistingResourceException, PersistentStoreFailureException;
	
	/**
	 * Get an item from the persistent store
	 * @param identifier unique identifier of an object in the persistent store (url)
	 * @return the stored content of the specified object
	 * @throws MultipleResourceException throws exception if the identifier specifies more than one item
	 * @throws NonExistingResourceException throws exception if the item specified by identifier is not existing in the persistent store
	 * @throws PersistentStoreFailureException throws exception if persistent store level error occurs 
	 */
	public ServiceObject getServiceByUrl(String identifier) throws MultipleResourceException, NonExistingResourceException, PersistentStoreFailureException;
	
	/**
	 * Delete multiple items from the persistent store
	 * @param array of service urls
	 * @throws MultipleResourceException throws exception if the identifier specifies more than one item
	 * @throws NonExistingResourceException throws exception if the item specified by identifier is not existing in the persistent store
	 * @throws PersistentStoreFailureException throws exception if persistent store level error occurs
	 * TODO: support list of urls 
	 */
	public void deleteByUrl(String url) throws MultipleResourceException, NonExistingResourceException, PersistentStoreFailureException;
	
	public void deleteAll();
	
	/**
	 * Query and delete the matching documents
	 * */
	public void findAndDelete(String query);
//	/**
//	 * Check whether any item specified by the identifier exists in the persistent store
//	 * @param url service url
//	 * @return true if there is exactly one item in the persistent store specified by the identifier or false if there is no such element
//	 * @throws MultipleResourceException throws exception if the identifier specifies more than one item
//	 * @throws PersistentStoreFailureException throws exception if persistent store level error occurs 
//	 */
//	public boolean contains(String url) throws MultipleResourceException, PersistentStoreFailureException;
	
	/**
	 * Update an existing item in the persistent store 
	 * @param identifier unique identifier of an object in the persistent store
 	 * @param newItem the new item to replace the former one in the persistent store
	 * @throws MultipleResourceException throws exception if the identifier specifies more than one item
	 * @throws NonExistingResourceException throws exception if the item specified by identifier is not existing in the persistent store
	 * @throws PersistentStoreFailureException throws exception if persistent store level error occurs 
	 */
	public void update(ServiceObject sObj) throws MultipleResourceException, NonExistingResourceException, PersistentStoreFailureException;
	
	/**
	 * Query the persistent store
	 * @param query (a JSON String)
	 * @return a collection of matching entries from the persistent store if any or an empty collection
	 * @throws PersistentStoreFailureException throws exception if persistent store level error occurs 
	 * @throws QueryException throws exception if the query contains any query specific error
	 */
	public List<ServiceObject> query(String query) throws QueryException, PersistentStoreFailureException;
	public List<ServiceObject> findAll() throws JSONException;
	
	
}
