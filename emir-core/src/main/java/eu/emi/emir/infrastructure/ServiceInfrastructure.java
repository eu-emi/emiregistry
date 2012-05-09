/**
 * 
 */
package eu.emi.emir.infrastructure;

import java.util.List;

import eu.emi.emir.infrastructure.AlreadyExistFailureException;


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
	 * @return None
	 */
	public void setParentsRoute(List<String> identifiers) throws EmptyIdentifierFailureException, NullPointerFailureException;

	/**
	 * Set the parent DSR
	 * @param parent's identifier
	 * @throws EmptyIdentifierFailureException throws exception if identifiers is empty
	 * @throws NullPointerFailureException throws exception if identifiers is a NULL pointer
	 * @return None
	 */
	public void setParent(String identifier) throws EmptyIdentifierFailureException, NullPointerFailureException;

	/**
	 * Get parent DSR
	 * @param None
	 * @return parent component's URL
	 */
	public String getParent();

	/**
	 * Get parents route
	 * @param None
	 * @return a collection of network entries from this component to the global peer to peer network
	 */
	public List<String> getParentsRoute();

	/**
	 * Get every child DSR services
	 * @param None
	 * @return a collection of child entries, that registered directly for this service
	 */
	public List<String> getChildDSRs();

	/**
	 * Add child DSR service url
	 * @param url of the child service
	 * @throws EmptyIdentifierFailureException throws exception if identifier is empty
	 * @throws NullPointerFailureException throws exception if identifier is a NULL pointer
	 * @return None
	 */
	public void addChildDSR(String identifier) throws AlreadyExistFailureException, EmptyIdentifierFailureException, NullPointerFailureException;

}
