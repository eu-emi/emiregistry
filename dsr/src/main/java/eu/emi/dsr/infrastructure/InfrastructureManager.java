/**
 * 
 */
package eu.emi.dsr.infrastructure;

import java.util.ArrayList;
import java.util.List;

/**
 * @author szigeti
 *
 */
public class InfrastructureManager implements ServiceInfrastructure {

	public InfrastructureManager() {
		parentsRoute = new ArrayList<String>();
		childServices = new ArrayList<String>();
		//parentsRoute.clear();
		//childServices.clear();
	}
	
	/* 
	 * @see eu.emi.dsr.infrastructure.ServiceInfrastructure#SetParentsRoute(java.util.List)
	 */
	@Override
	public void setParentsRoute(List<String> identifiers) throws EmptyIdentifierFailureException, NullPointerFailureException{
		if (identifiers == null) throw new NullPointerFailureException();
		if (identifiers.isEmpty()) throw new EmptyIdentifierFailureException();
		
		parentsRoute.clear();
		parentsRoute.addAll(identifiers);
	}

	/* 
	 * @see eu.emi.dsr.infrastructure.ServiceInfrastructure#GetParentsRoute()
	 */
	@Override
	public List<String> getParentsRoute() {
		return parentsRoute;
	}

	/* 
	 * @see eu.emi.dsr.infrastructure.ServiceInfrastructure#GetChildServices()
	 */
	@Override
	public List<String> getChildServices() {
		return childServices;
	}

	/* 
	 * @see eu.emi.dsr.infrastructure.ServiceInfrastructure#AddChildService(java.util.String)
	 */
	@Override
	public void addChildService(String identifier) throws AlreadyExistFailureException, EmptyIdentifierFailureException, NullPointerFailureException {
		if (identifier == null) throw new NullPointerFailureException();
		if (identifier.isEmpty()) throw new EmptyIdentifierFailureException();
		
		if (childServices.contains(identifier)) throw new AlreadyExistFailureException();
		childServices.add(identifier);
	}

	private List<String> parentsRoute;
	private List<String> childServices;
}
