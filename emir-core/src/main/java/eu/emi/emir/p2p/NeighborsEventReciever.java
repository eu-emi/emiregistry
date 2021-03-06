/**
 * 
 */
package eu.emi.emir.p2p;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import eu.emi.emir.client.util.Log;
import eu.emi.emir.event.Event;
import eu.emi.emir.event.EventDispatcher;
import eu.emi.emir.event.EventListener;
import eu.emi.emir.event.EventTypes;

/**
 * @author g.szigeti
 * 
 */
public class NeighborsEventReciever implements EventListener, Runnable {
	private static Logger logger = Log.getLogger(Log.EMIR_CORE,
			NeighborsEventReciever.class);

	/**
	 * Constructor for the neighbors event receiver class.
	 * This class transmit relevant (for only global DSR) messages to the 
	 * neighbors manager class.
	 * @param None
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
				recievedjos.put((JSONObject) event.getData());
			} catch (ClassCastException e) {
				if (logger.isDebugEnabled()) {
					logger.debug("event.data to JSONObject cast problem. NOT delete message.");
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
				NeighborsManager.getInstance().addNeighborsDSRs(relevantEntries,event.getType());
			}
		} else if (event.getType().equalsIgnoreCase(EventTypes.SERVICE_UPDATE)
			       && (!NeighborsManager.getInstance().getConnected()
			               || event.getData().toString().trim().contains("\"Service_Type\":\"GSR\"")) ) {
			// (re)connection trigger
			NeighborsManager.getInstance().getNeighbors();
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
