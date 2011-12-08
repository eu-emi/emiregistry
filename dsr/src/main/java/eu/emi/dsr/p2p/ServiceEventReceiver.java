/**
 * 
 */
package eu.emi.dsr.p2p;

import java.util.List;

import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.ClientResponse.Status;

import eu.emi.client.DSRClient;
import eu.emi.client.ServiceBasicAttributeNames;
import eu.emi.client.util.Log;
import eu.emi.dsr.DSRServer;
import eu.emi.dsr.core.ServerConstants;
import eu.emi.dsr.event.Event;
import eu.emi.dsr.event.EventDispatcher;
import eu.emi.dsr.event.EventListener;
import eu.emi.dsr.event.EventTypes;
import eu.emi.dsr.infrastructure.Filters;

/**
 * @author g.szigeti
 * 
 */
public class ServiceEventReceiver implements EventListener, Runnable {
	private static Logger logger = Log.getLogger(Log.DSR,
			ServiceEventReceiver.class);
	private WebResource client;

	private Filters filter;
	private String myURL;

	/** Constructor for this event receiver class
	 * @param None
	 * 
	 */
	public ServiceEventReceiver() {
		client = null;
		filter = new Filters();
		myURL = DSRServer.getProperty(ServerConstants.REGISTRY_SCHEME).toString() +"://"+
				DSRServer.getProperty(ServerConstants.REGISTRY_HOSTNAME).toString() +":"+
				DSRServer.getProperty(ServerConstants.REGISTRY_PORT).toString();
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
			boolean connected = true; 
			for (int count=0; count<retry; count++ ){
				try {
					if (logger.isDebugEnabled()) {
						logger.debug("Send service " + eventType 
								+ " message to " + neighbors.get(i)
									+ " " + (count+1) + " time(s).");
					}
					messageSend(neighbors.get(i), jos, event.getType());
					connected = true;
					break;
				} catch(ClientHandlerException e){
					connected = false;
				}
			}
			if (!connected){
				NeighborsManager.getInstance().setUnavailableNeighbor(neighbors.get(i));
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

	private void messageSend(String url, JSONArray message, String method) throws ClientHandlerException{
		client = getClient(url);
		ClientResponse res = null;
		if (method.equals(EventTypes.SERVICE_ADD)){
			res = client.accept(MediaType.APPLICATION_JSON_TYPE)
				    .post(ClientResponse.class, message);
		}
		if (method.equals(EventTypes.SERVICE_UPDATE)){
			res = client.accept(MediaType.APPLICATION_JSON_TYPE)
					.put(ClientResponse.class, message);
		}
		if (method.equals(EventTypes.SERVICE_DELETE)){
			String deleteURL;
			try {
				deleteURL = message.getJSONObject(0).getString("Service_Endpoint_URL");
				String updateSince = ((JSONObject)message.getJSONObject(0)
							.get("updateSince")).getString("$date");
				res = client.queryParam(
						ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
								.getAttributeName(),
								deleteURL)
							.queryParam(
						ServiceBasicAttributeNames.SERVICE_UPDATE_SINCE
								.getAttributeName(),
								updateSince).delete(ClientResponse.class);
			} catch (JSONException e) {
				logger.warn(e.getCause());
			}
		}
		if ( res.getStatus() == Status.OK.getStatusCode() ||
			 res.getStatus() == Status.CONFLICT.getStatusCode() ){
			if (logger.isDebugEnabled()) {
				logger.debug("Message sent succesfully to " + url);
			}
		}
	}
	
	protected WebResource getClient(String url) {
		DSRClient c = new DSRClient(url + "/serviceadmin");
		return c.getClientResource();
	}
}
	

