/**
 * 
 */
package eu.emi.emir.lease;

import org.apache.log4j.Logger;

import eu.emi.emir.client.util.Log;
import eu.emi.emir.core.ServiceAdminManager;

/**
 * Checks the service entry lease and deletes it if expired
 * 
 * @author a.memon
 * 
 */
public class ServiceReaper implements Runnable {
	private static Logger logger = Log.getLogger(Log.EMIR_CORE, ServiceReaper.class);
	private ServiceAdminManager sm;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			if (logger.isTraceEnabled()) {
				logger.trace("Purging expired service records");
			}
			
			if (sm==null) {
				sm = new ServiceAdminManager();
			}
			sm.removeExpiredEntries();
		} catch (Exception e) {
			logger.warn(e.getCause());
		}
	}

}
