/**
 * 
 */
package eu.emi.dsr.core.db;

import java.util.Collection;

/**
 * @author martoni
 *
 */
public interface ServiceDatabase {

	/**
	 * Insert a new element to the persistent store. If it throws an exception if the item already exists
	 * @param item an item to insert into the database
	 * @throws ExistingResourceException throws exception if the element already exists
	 * @throws PersistentStoreFailureException throws exception if the persistent store is unavailable
	 * @return the identifier of the new element
	 */
	public Object insert(Object item) throws ExistingResourceException, PersistentStoreFailureException;
	
	/**
	 * Get an item from the persistent store
	 * @param identifier unique identifier of an object in the persistent store
	 * @return the stored content of the specified object
	 * @throws MultipleResourceException throws exception if the identifier specifies more than one item
	 * @throws NonExistingResourceException throws exception if the item specified by identifier is not existing in the persistent store
	 * @throws PersistentStoreFailureException throws exception if persistent store level error occurs 
	 */
	public Object get(Object identifier) throws MultipleResourceException, NonExistingResourceException, PersistentStoreFailureException;
	
	/**
	 * Delete an item from the persistent store
	 * @param identifier unique identifier of an object in the persistent store
	 * @throws MultipleResourceException throws exception if the identifier specifies more than one item
	 * @throws NonExistingResourceException throws exception if the item specified by identifier is not existing in the persistent store
	 * @throws PersistentStoreFailureException throws exception if persistent store level error occurs 
	 */
	public void delete(Object identifier) throws MultipleResourceException, NonExistingResourceException, PersistentStoreFailureException;
	
	/**
	 * Check whether any item specified by the identifier exists in the persistent store
	 * @param identifier unique identifier of an object in the persistent store
	 * @return true if there is exactly one item in the persistent store specified by the identifier or false if there is no such element
	 * @throws MultipleResourceException throws exception if the identifier specifies more than one item
	 * @throws PersistentStoreFailureException throws exception if persistent store level error occurs 
	 */
	public boolean contains(Object identifier) throws MultipleResourceException, PersistentStoreFailureException;
	
	/**
	 * Update an existing item in the persistent store 
	 * @param identifier unique identifier of an object in the persistent store
 	 * @param newItem the new item to replace the former one in the persistent store
	 * @throws MultipleResourceException throws exception if the identifier specifies more than one item
	 * @throws NonExistingResourceException throws exception if the item specified by identifier is not existing in the persistent store
	 * @throws PersistentStoreFailureException throws exception if persistent store level error occurs 
	 */
	public void update(Object identifier, Object newItem) throws MultipleResourceException, NonExistingResourceException, PersistentStoreFailureException;
	
	/**
	 * Query the persistent store
	 * @param query 
	 * @return a collection of matching entries from the persistent store if any or an empty collection
	 * @throws PersistentStoreFailureException throws exception if persistent store level error occurs 
	 * @throws QueryException throws exception if the query contains any query specific error
	 */
	public Collection<Object> query(Object query) throws QueryException, PersistentStoreFailureException;
}
