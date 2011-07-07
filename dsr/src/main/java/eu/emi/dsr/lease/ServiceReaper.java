/**
 * 
 */
package eu.emi.dsr.lease;


import org.apache.log4j.Logger;

import eu.emi.dsr.core.ServiceAdminManager;
import eu.emi.dsr.core.ServiceManagerFactory;
import eu.emi.dsr.util.Log;

/**
 * Checks the service entry lease and deletes it if expired
 * 
 * @author a.memon
 * 
 */
public class ServiceReaper implements Runnable {
	private static Logger logger = Log.getLogger(Log.DSR, ServiceReaper.class);
	private ServiceAdminManager sm;
	
	/**
	 * 
	 */
	public ServiceReaper() {
		sm = ServiceManagerFactory.getServiceAdminManager();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			if (logger.isTraceEnabled()) {
				logger.trace("purging service entry");
			}
			sm.removeExpiredEntries();
		} catch (Exception e) {
			Log.logException(e);
		} 
	}
	
	

}
