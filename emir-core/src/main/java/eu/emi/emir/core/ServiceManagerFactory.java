/**
 * 
 */
package eu.emi.emir.core;


/**
 * @author a.memon
 * 
 */
public class ServiceManagerFactory {
	public ServiceAdminManager getServiceAdminManager() {
		return new ServiceAdminManager();
	}

	public static ServiceColManager getServiceColManager(){
			return  new ServiceColManager();
	}

}
