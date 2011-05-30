/**
 * 
 */
package eu.emi.dsr;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import eu.emi.dsr.boundry.ServiceAdminResource;
import eu.emi.dsr.boundry.ServiceCollectionResource;
import eu.emi.dsr.boundry.ServiceResource;

/**
 * @author a.memon
 *
 */
public class DSRApplication extends Application{

	/* (non-Javadoc)
	 * @see javax.ws.rs.core.Application#getClasses()
	 */
	@Override
	public Set<Class<?>> getClasses() {
		Set<Class<?>> rrcs = new HashSet<Class<?>>();
        rrcs.add(ServiceResource.class);
        rrcs.add(ServiceCollectionResource.class);
        rrcs.add(ServiceAdminResource.class);
//        rrcs.add(ServiceCollectionResource.class);
        return rrcs;
	}
	
	

	

}
