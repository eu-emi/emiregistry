/**
 * 
 */
package eu.emi.dsr.infrastructure;

import org.apache.log4j.Logger;

import eu.emi.dsr.event.Event;
import eu.emi.dsr.event.EventManager;
import eu.emi.dsr.event.EventReciever;
import eu.emi.dsr.event.EventTypes;
import eu.emi.dsr.util.Log;

/**
 * @author a.memon
 * 
 */
public class ServiceEventReciever implements EventReciever, Runnable {
	private static Logger logger = Log.getLogger(Log.DSR, ServiceEventReciever.class);
	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.emi.dsr.event.EventReciever#recieve(eu.emi.dsr.event.Event)
	 */
	@Override
	public void recieve(Event event) {
		//here sending messages to the parent DSR's
		if (event.getType().equalsIgnoreCase(EventTypes.SERVICE_ADD)) {
			if (logger.isDebugEnabled()) {
				logger.debug("service added event fired");
			}
		}
		if (event.getType().equalsIgnoreCase(EventTypes.SERVICE_UPDATE)) {
			if (logger.isDebugEnabled()) {
				logger.debug("service update event fired");
			}
		}
		if (event.getType().equalsIgnoreCase(EventTypes.SERVICE_DELETE)) {
			if (logger.isDebugEnabled()) {
				logger.debug("service added delete event fired");
			}
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		EventManager.add(this);		
	}

}
