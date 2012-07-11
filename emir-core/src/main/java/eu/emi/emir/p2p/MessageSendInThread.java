/**
 * 
 */
package eu.emi.emir.p2p;

import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.ClientResponse.Status;

import eu.emi.emir.EMIRServer;
import eu.emi.emir.client.EMIRClient;
import eu.emi.emir.client.ServiceBasicAttributeNames;
import eu.emi.emir.client.util.Log;
import eu.emi.emir.event.Event;
import eu.emi.emir.event.EventTypes;

/**
 * @author g.szigeti
 *
 */
public class MessageSendInThread extends Thread {
	private static Logger logger = Log.getLogger(Log.EMIR_CORE,
			MessageSendInThread.class);
	private int retry;
	private String neighbor;
	private JSONArray message;
	private String eventType;
	private Event event;
	private WebResource client;

	/**
	 * @param neighbor_
	 * @param retry_
	 * @param message_
	 * @param eventType_
	 * @param event_
	 * 
	 */
	public MessageSendInThread(String neighbor_, int retry_, JSONArray message_, String eventType_, Event event_) {
		super(neighbor_);
		retry = retry_;
		neighbor = neighbor_;
		message = message_;
		eventType = eventType_;
		event = event_;
		client = null;
		
		// Start the thread right now.
		start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		boolean connected = true; 
		for (int count=0; count<retry; count++ ){
			try {
				if (logger.isDebugEnabled()) {
					logger.debug("Send service " + eventType 
							+ " message to " + neighbor
								+ " " + (count+1) + " time(s).");
				}
				messageSend(neighbor, message, event.getType());
				connected = true;
				break;
			} catch(ClientHandlerException e){
				connected = false;
			}
		}
		if (!connected){
			NeighborsManager.getInstance().setUnavailableNeighbor(neighbor);
		} else {
			NeighborsManager.getInstance().resetUnavailableNeighbor(neighbor);
		}
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
			String deleteEndpointID;
			try {
				deleteEndpointID = message.getJSONObject(0)
							.getString(ServiceBasicAttributeNames.
											SERVICE_ENDPOINT_ID.getAttributeName());
				String updateSince = ((JSONObject)message.getJSONObject(0)
							.get("updateSince")).getString("$date");
				res = client.queryParam(
						ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID
								.getAttributeName(),
								deleteEndpointID)
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
			logger.info("("+ eventType +") Message sent succesfully to " + url);
		} else if ( res.getStatus() == Status.UNAUTHORIZED.getStatusCode() ) {
			logger.info("Does not have access for the following neighbors: " + url);
		}
	}
	
	protected WebResource getClient(String url) {
		EMIRClient c = new EMIRClient(url + "/serviceadmin");
		if (EMIRServer.getServerSecurityProperties().isSslEnabled()) {

			c = new EMIRClient(url + "/serviceadmin",
										EMIRServer.getClientSecurityProperties());
		}
		return c.getClientResource();
	}
	
}
