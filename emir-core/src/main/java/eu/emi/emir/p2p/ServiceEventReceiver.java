/**
 * 
 */
package eu.emi.emir.p2p;

import java.util.List;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import eu.emi.emir.EMIRServer;
import eu.emi.emir.ServerProperties;
import eu.emi.emir.client.util.Log;
import eu.emi.emir.event.Event;
import eu.emi.emir.event.EventDispatcher;
import eu.emi.emir.event.EventListener;
import eu.emi.emir.event.EventTypes;
import eu.emi.emir.infrastructure.Filters;

/**
 * @author g.szigeti
 * 
 */
public class ServiceEventReceiver implements EventListener, Runnable {
	private static Logger logger = Log.getLogger(Log.EMIR_CORE,
			ServiceEventReceiver.class);

	private Filters filter;
	private String myURL;
	
	/** 
	 * Constructor for this event receiver class
	 * @param None
	 */
	public ServiceEventReceiver() {
		filter = new Filters();
		myURL = EMIRServer.getServerProperties().getValue(ServerProperties.PROP_ADDRESS);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.emi.dsr.event.EventReciever#recieve(eu.emi.dsr.event.Event)
	 */
	@Override
	public void recieve(Event event) {
		List<String> neighbors;
		neighbors = NeighborsManager.getInstance().getNeighbors();
		if (neighbors.size()==1 && neighbors.get(0).equals(myURL)){
			// Don't want to send message to myself.
			return;
		}

		JSONArray jos = new JSONArray();
		try {
			jos = filter.outputFilter((JSONArray) event.getData());
			if (jos.length() == 0){
				return;
			}
		} catch (ClassCastException e) {
			if (logger.isDebugEnabled()) {
				logger.debug("event.data to JSONArray cast problem. May be delete message.");
			}
		}

		String eventType = null;
		if (event.getType().equalsIgnoreCase(EventTypes.SERVICE_ADD)) {
			eventType = "add";
		}
		if (event.getType().equalsIgnoreCase(EventTypes.SERVICE_UPDATE)) {
			eventType = "update";
		}
		if (event.getType().equalsIgnoreCase(EventTypes.SERVICE_DELETE)) {
			eventType = "delete";
			jos.put((JSONObject) event.getData());
		}

		// here sending messages to the neighbors
		int retry = NeighborsManager.getInstance().getRetry();
		for (int i=0; i<neighbors.size(); i++){
			new MessageSendInThread(neighbors.get(i), retry, jos, eventType, event);
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
	

