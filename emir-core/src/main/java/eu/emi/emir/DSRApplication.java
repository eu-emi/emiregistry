/**
 * 
 */
package eu.emi.emir;

import javax.ws.rs.ApplicationPath;

import com.sun.jersey.api.core.PackagesResourceConfig;

/**
 * @author a.memon
 *
 */
@ApplicationPath("resources")
public class DSRApplication extends PackagesResourceConfig{

	/**
	 * 
	 */
	public DSRApplication() {
		super("eu.emi.emir.resource");
		
	}
	
	
	
	
	

}