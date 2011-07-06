/**
 * 
 */
package eu.emi.dsr;

import com.sun.jersey.api.core.PackagesResourceConfig;

import eu.emi.dsr.resource.ServiceResource;

/**
 * @author a.memon
 *
 */
public class DSRApplication extends PackagesResourceConfig{

	/**
	 * 
	 */
	public DSRApplication() {
		super("eu.emi.dsr.resource");
		String name = ServiceResource.class.getPackage().getName();
		
//		super("eu.emi.dsr.boundry");
		
	}
	
	
	
	
	

}
