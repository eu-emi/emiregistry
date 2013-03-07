/**
 * 
 */
package eu.emi.emir;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import eu.emi.emir.resource.FacetQueryResource;
import eu.emi.emir.resource.PingResource;
import eu.emi.emir.resource.ServiceCollectionResource;
import eu.emi.emir.resource.StatusResource;

/**
 * The JAX-RS application to expose resources on anonymous rest interface
 * 
 * @author a.memon
 *
 */
public class EMIRAnonymousApplication extends Application{
	/* (non-Javadoc)
	 * @see javax.ws.rs.core.Application#getClasses()
	 */
	@Override
	public Set<Class<?>> getClasses() {
		Set<Class<?>> rrcs = new HashSet<Class<?>>();
        rrcs.add(ServiceCollectionResource.class);
        rrcs.add(PingResource.class);
        rrcs.add(StatusResource.class);
        rrcs.add(FacetQueryResource.class);
        return rrcs;
	}
}
