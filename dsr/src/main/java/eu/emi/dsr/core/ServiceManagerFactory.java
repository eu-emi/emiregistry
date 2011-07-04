/**
 * 
 */
package eu.emi.dsr.core;

import org.apache.log4j.Logger;

import eu.emi.dsr.util.Log;

/**
 * @author a.memon
 * 
 */
public class ServiceManagerFactory {
	private static Logger logger = Log.getLogger(Log.DSR, ServiceManagerFactory.class);

	private static ServiceAdminManager adminMgr = null;
	private static ServiceColManager colMgr = null;

	public static ServiceAdminManager getServiceAdminManager() {
		if (adminMgr == null) {
			adminMgr = new ServiceAdminManager();
		}
		return adminMgr;
	}

	public static ServiceColManager getServiceColManager() {
		if (colMgr == null) {
			colMgr = new ServiceColManager();
		}
		return colMgr;
	}

}
