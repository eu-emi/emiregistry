/**
 * 
 */
package eu.emi.dsr.infrastructure;

import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.ClientResponse.Status;

import eu.emi.dsr.client.DSRClient;
import eu.emi.dsr.core.ServiceBasicAttributeNames;
import eu.emi.dsr.event.Event;
import eu.emi.dsr.event.EventManager;
import eu.emi.dsr.event.EventReciever;
import eu.emi.dsr.event.EventTypes;
import eu.emi.dsr.util.Log;

/**
 * @author a.memon
 * @author g.szigeti
 * 
 */
public class ServiceEventReciever implements EventReciever, Runnable {
	private static Logger logger = Log.getLogger(Log.DSR,
			ServiceEventReciever.class);
	private final WebResource client;
	private static InfrastructureManager infrastructure;
	private static boolean parent_lost;

	/**
	 * @param property
	 */
	public ServiceEventReciever(String parentUrl) {
		infrastructure = new InfrastructureManager();
		try {
			infrastructure.setParent(parentUrl);
		} catch (EmptyIdentifierFailureException e) {
			logger.error("Empty parent URL added!");
		} catch (NullPointerFailureException e) {
			logger.error("NULL point error by the parent URL!");
		}
		DSRClient c = new DSRClient(parentUrl + "/serviceadmin");
		client = c.getClientResource();
		parent_lost = false;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.emi.dsr.event.EventReciever#recieve(eu.emi.dsr.event.Event)
	 */
	@Override
	public void recieve(Event event) {
		String ID = null;
		JSONObject jo = (JSONObject) event.getData();
		try {
			ID = jo.getString("Service_Endpoint_URL");
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// here sending messages to the parent DSR's
		if (event.getType().equalsIgnoreCase(EventTypes.SERVICE_ADD)) {
			if (logger.isDebugEnabled()) {
				logger.debug("service added event fired");
			}
			try{
				ClientResponse res = client.accept(MediaType.APPLICATION_JSON_TYPE)
					    .post(ClientResponse.class, event.getData());
				if ( res.getStatus() == Status.OK.getStatusCode() ||
					 res.getStatus() == Status.NOT_ACCEPTABLE.getStatusCode() ){
					/*{//test block
						logger.debug("teszt üzenet elött");
						ClientResponse res2 = client.accept(MediaType.APPLICATION_JSON_TYPE)
							    .post(ClientResponse.class, event.getData());
						logger.debug("teszt uzenet utan, a response: " + res2.getStatus());
						if ( res2.getStatus() == Status.OK.getStatusCode() ){
							
						}	
					}*/
					if (parent_lost){
						// DB sync
						parent_lost = !infrastructure.dbSynchronization(ID, Method.REGISTER, res.getStatus());
					}
				}
			} catch(ClientHandlerException e){
				parent_lost = true;
				infrastructure.handleRegistration(ID);
			} 
		}
		if (event.getType().equalsIgnoreCase(EventTypes.SERVICE_UPDATE)) {
			if (logger.isDebugEnabled()) {
				logger.debug("service update event fired");
			}
			try {
				ClientResponse res = client.accept(MediaType.APPLICATION_JSON_TYPE)
						.put(ClientResponse.class, jo);
				if ( res.getStatus() == Status.OK.getStatusCode() ||
					 res.getStatus() == Status.NOT_ACCEPTABLE.getStatusCode() ){
					if (parent_lost){
						// DB sync
						parent_lost = !infrastructure.dbSynchronization(ID, Method.UPDATE, res.getStatus());
					}
				}
			} catch(ClientHandlerException e){
				parent_lost = true;
				infrastructure.handleUpdate(ID);
			} catch (Exception e) {
				Log.logException("Error making update on the parent dsr",e);
			}
		}
		if (event.getType().equalsIgnoreCase(EventTypes.SERVICE_DELETE)) {
			if (logger.isDebugEnabled()) {
				logger.debug("service added delete event fired");
			}
			try{
				ClientResponse res = client.queryParam(
					ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
							.getAttributeName(),
					event.getData().toString()).delete(ClientResponse.class);
				if ( res.getStatus() == Status.OK.getStatusCode() ){
					if (parent_lost){
						// DB sync
						parent_lost = !infrastructure.dbSynchronization(event.getData().toString(), Method.DELETE, res.getStatus());
					}
				}
			} catch(ClientHandlerException e){
				parent_lost = true;
				infrastructure.handleDelete(event.getData().toString());
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
		EventManager.add(this);
	}

}
