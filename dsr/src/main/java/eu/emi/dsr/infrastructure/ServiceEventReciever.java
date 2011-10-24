/**
 * 
 */
package eu.emi.dsr.infrastructure;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.ClientResponse.Status;

import eu.emi.client.DSRClient;
import eu.emi.dsr.core.Configuration;
import eu.emi.dsr.core.ServiceBasicAttributeNames;
import eu.emi.dsr.event.Event;
import eu.emi.dsr.event.EventDispatcher;
import eu.emi.dsr.event.EventListener;
import eu.emi.dsr.event.EventTypes;
import eu.emi.dsr.util.Log;

/**
 * @author a.memon
 * @author g.szigeti
 * 
 */
public class ServiceEventReciever implements EventListener, Runnable {
	private static Logger logger = Log.getLogger(Log.DSR,
			ServiceEventReciever.class);
	private static Configuration conf;
	private final WebResource client;
	private static InfrastructureManager infrastructure;
	private static boolean parent_lost;

	/**
	 * @param property
	 */
	public ServiceEventReciever(String parentUrl, Configuration config) {
		conf = config;
		infrastructure = new InfrastructureManager(conf);
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
		List<String> IDs = new ArrayList<String>();
		JSONArray jos = new JSONArray();
		try {
			jos = (JSONArray) event.getData();
		} catch (ClassCastException e) {
			if (logger.isDebugEnabled()) {
				logger.debug("event.data to JSONObject cast problem. May be delete message.");
			}
		}
		try {
			for (int i=0; i<jos.length(); i++){
				IDs.add(jos.getJSONObject(i).getString("Service_Endpoint_URL"));
			}
		} catch (JSONException e1) {
			if (logger.isDebugEnabled()) {
				logger.debug(e1);
			}
		}
		// here sending messages to the parent DSR's
		if (event.getType().equalsIgnoreCase(EventTypes.SERVICE_ADD)) {
			if (logger.isDebugEnabled()) {
				logger.debug("service added event fired");
			}
			try{
				ClientResponse res = client.accept(MediaType.APPLICATION_JSON_TYPE)
					    .post(ClientResponse.class, jos);
				if ( res.getStatus() == Status.OK.getStatusCode() ||
					 res.getStatus() == Status.CONFLICT.getStatusCode() ){
					if (parent_lost){
						// remove the wrong IDs from the ID list
						if ( res.getStatus() == Status.CONFLICT.getStatusCode() ){
							JSONArray errors = res.getEntity(JSONArray.class);
							for (int i=0; i<errors.length(); i++){
								try {
									IDs.remove(errors.getJSONObject(i).getString("Service_Endpoint_URL"));
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}
						// DB sync
						parent_lost = !infrastructure.dbSynchronization(IDs, Method.REGISTER, res.getStatus());
					}
				}
			} catch(ClientHandlerException e){
				parent_lost = true;
				infrastructure.handleRegistration(IDs);
			} 
		}
		if (event.getType().equalsIgnoreCase(EventTypes.SERVICE_UPDATE)) {
			if (logger.isDebugEnabled()) {
				logger.debug("service update event fired");
			}
			try {
				ClientResponse res = client.accept(MediaType.APPLICATION_JSON_TYPE)
						.put(ClientResponse.class, jos);
				if ( res.getStatus() == Status.OK.getStatusCode() ||
					 res.getStatus() == Status.CONFLICT.getStatusCode() ){
					if (parent_lost){
						// remove the wrong IDs from the ID list
						if ( res.getStatus() == Status.CONFLICT.getStatusCode() ){
							JSONArray errors = res.getEntity(JSONArray.class);
							for (int i=0; i<errors.length(); i++){
								IDs.remove(errors.getJSONObject(i).getString("Service_Endpoint_URL"));
							}
						}
						// DB sync
						parent_lost = !infrastructure.dbSynchronization(IDs, Method.UPDATE, res.getStatus());
					}
				}
			} catch(ClientHandlerException e){
				parent_lost = true;
				infrastructure.handleUpdate(IDs);
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
						List<String> ID = new ArrayList<String>();
						ID.add(event.getData().toString());
						parent_lost = !infrastructure.dbSynchronization(ID, Method.DELETE, res.getStatus());
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
		EventDispatcher.add(this);
	}

}
