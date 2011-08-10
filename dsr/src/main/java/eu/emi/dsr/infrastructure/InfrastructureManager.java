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
	}
	
	/* 
	 * @see eu.emi.dsr.infrastructure.ServiceInfrastructure#setParentsRoute(java.util.List)
	 */
	@Override
	public void setParentsRoute(List<String> identifiers) throws EmptyIdentifierFailureException, NullPointerFailureException{
		if (identifiers == null) throw new NullPointerFailureException();
		if (identifiers.isEmpty()) throw new EmptyIdentifierFailureException();
		
		parentsRoute.clear();
		parentsRoute.addAll(identifiers);
	}

	/* 
	 * @see eu.emi.dsr.infrastructure.ServiceInfrastructure#getParentsRoute()
	 */
	@Override
	public List<String> getParentsRoute() {
		return parentsRoute;
	}

	/* 
	 * @see eu.emi.dsr.infrastructure.ServiceInfrastructure#getChildDSRs()
	 */
	@Override
	public List<String> getChildDSRs() {
		return childServices;
	}

	/* 
	 * @see eu.emi.dsr.infrastructure.ServiceInfrastructure#addChildDSR(java.util.String)
	 */
	@Override
	public void addChildDSR(String identifier) throws AlreadyExistFailureException, EmptyIdentifierFailureException, NullPointerFailureException {
		if (identifier == null) throw new NullPointerFailureException();
		if (identifier.isEmpty()) throw new EmptyIdentifierFailureException();
		
		if (childServices.contains(identifier)) throw new AlreadyExistFailureException();
		childServices.add(identifier);
	}

	/* 
	 * @see eu.emi.dsr.infrastructure.ServiceInfrastructure#setParent(java.util.String)
	 */
	@Override
	public void setParent(String identifier)
			throws EmptyIdentifierFailureException, NullPointerFailureException {
		if (identifier == null) throw new NullPointerFailureException();
		if (identifier.isEmpty()) throw new EmptyIdentifierFailureException();
		
		parentsRoute.clear();
		parentsRoute.add(identifier);
		
	}

	/* 
	 * @see eu.emi.dsr.infrastructure.ServiceInfrastructure#getParent()
	 */
	@Override
	public String getParent() {
		if (parentsRoute.isEmpty()){
			return "";
		}
		return parentsRoute.get(0);
	}

	private List<String> parentsRoute;
	private List<String> childServices;
}
