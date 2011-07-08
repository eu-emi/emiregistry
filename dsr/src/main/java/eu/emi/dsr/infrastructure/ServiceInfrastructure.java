/**
 * 
 */
package eu.emi.dsr.infrastructure;

import java.util.List;

import eu.emi.dsr.infrastructure.AlreadyExistFailureException;


/**
 * @author szigeti
 *
 */
public interface ServiceInfrastructure {
	
	/**
	 * Set the parents route
	 * @param list of the parents identifier
	 * @throws EmptyIdentifierFailureException throws exception if identifiers is empty
	 * @throws NullPointerFailureException throws exception if identifiers is a NULL pointer
	 * @return a collection of network entries from this component to the global peer to peer network
	 */
	public void SetParentsRoute(List<String> identifiers) throws EmptyIdentifierFailureException, NullPointerFailureException;

	/**
	 * Get parents route
	 * @param None
	 * @return a collection of network entries from this component to the global peer to peer network
	 */
	public List<String> GetParentsRoute();

	/**
	 * Get every child services
	 * @param None
	 * @return a collection of child entries, that registered directly for this service
	 */
	public List<String> GetChildServices();

	/**
	 * Add child service url
	 * @param url of the child service
	 * @throws AlreadyExistFailureException throws exception if child entry add before the collection
	 * @throws EmptyIdentifierFailureException throws exception if identifier is empty
	 * @throws NullPointerFailureException throws exception if identifier is a NULL pointer
	 * @return a collection of child entries, that registered directly for this service
	 */
	public void AddChildService(String identifier) throws AlreadyExistFailureException, EmptyIdentifierFailureException, NullPointerFailureException;

}
