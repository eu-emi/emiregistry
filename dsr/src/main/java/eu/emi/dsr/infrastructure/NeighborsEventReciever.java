/**
 * 
 */
package eu.emi.dsr.infrastructure;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;

import eu.emi.dsr.event.Event;
import eu.emi.dsr.event.EventDispatcher;
import eu.emi.dsr.event.EventListener;
import eu.emi.dsr.event.EventTypes;
import eu.emi.dsr.util.Log;

/**
 * @author g.szigeti
 * 
 */
public class NeighborsEventReciever implements EventListener, Runnable {
	private static Logger logger = Log.getLogger(Log.DSR,
			NeighborsEventReciever.class);

	/**
	 * @param property
	 */
	public NeighborsEventReciever() {
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.emi.dsr.event.EventReciever#recieve(eu.emi.dsr.event.Event)
	 */
	@Override
	public void recieve(Event event) {
		JSONArray recievedjos = new JSONArray();
		JSONArray relevantEntries = new JSONArray();
		if (event.getType().equalsIgnoreCase(EventTypes.SERVICE_ADD) ||
			event.getType().equalsIgnoreCase(EventTypes.SERVICE_DELETE)) {
			if (logger.isDebugEnabled()) {
				logger.debug("neighbors service added/delete event fired");
			}
			try {
				recievedjos = (JSONArray) event.getData();
			} catch (ClassCastException e) {
				if (logger.isDebugEnabled()) {
					logger.debug("event.data to JSONArray cast problem. May be delete message.");
				}
			}
			try {
				for (int i=0; i<recievedjos.length(); i++){
					if (recievedjos.getJSONObject(i).getString("Service_Type").equals("GSR")){
						relevantEntries.put(recievedjos.getJSONObject(i));
					}
				}
			} catch (JSONException e1) {
				if (logger.isDebugEnabled()) {
					logger.debug(e1);
				}
			}
			
			// Transmit to the Neighbors singleton
			if (relevantEntries.length() > 0){
				//Singleton.method
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		EventDispatcher.add(this);
	}

}
