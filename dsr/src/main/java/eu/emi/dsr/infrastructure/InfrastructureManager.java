/**
 * 
 */
package eu.emi.dsr.infrastructure;

import java.util.List;
import java.util.Vector;

/**
 * @author szigeti
 *
 */
public class InfrastructureManager implements ServiceInfrastructure {

	public InfrastructureManager() {
		parentsRoute = new Vector<String>();
		childServices = new Vector<String>();
		//parentsRoute.clear();
		//childServices.clear();
	}
	
	/* 
	 * @see eu.emi.dsr.infrastructure.ServiceInfrastructure#SetParentsRoute(java.util.List)
	 */
	@Override
	public void SetParentsRoute(List<String> identifiers) throws EmptyIdentifierFailureException, NullPointerFailureException{
		if (identifiers == null) throw new NullPointerFailureException();
		if (identifiers.isEmpty()) throw new EmptyIdentifierFailureException();
		
		parentsRoute.clear();
		parentsRoute.addAll(identifiers);
	}

	/* 
	 * @see eu.emi.dsr.infrastructure.ServiceInfrastructure#GetParentsRoute()
	 */
	@Override
	public List<String> GetParentsRoute() {
		return parentsRoute;
	}

	/* 
	 * @see eu.emi.dsr.infrastructure.ServiceInfrastructure#GetChildServices()
	 */
	@Override
	public List<String> GetChildServices() {
		return childServices;
	}

	/* 
	 * @see eu.emi.dsr.infrastructure.ServiceInfrastructure#AddChildService(java.util.String)
	 */
	@Override
	public void AddChildService(String identifier) throws AlreadyExistFailureException, EmptyIdentifierFailureException, NullPointerFailureException {
		if (identifier == null) throw new NullPointerFailureException();
		if (identifier.isEmpty()) throw new EmptyIdentifierFailureException();
		
		if (childServices.contains(identifier)) throw new AlreadyExistFailureException();
		childServices.add(identifier);
	}

	private List<String> parentsRoute;
	private List<String> childServices;
}
