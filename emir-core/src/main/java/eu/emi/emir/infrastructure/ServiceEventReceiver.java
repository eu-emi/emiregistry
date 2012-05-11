/**
 * 
 */
package eu.emi.emir.infrastructure;

import java.util.ArrayList;
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
import eu.emi.client.security.ISecurityProperties;
import eu.emi.client.util.Log;
import eu.emi.emir.DSRServer;
import eu.emi.emir.core.Configuration;
import eu.emi.emir.event.Event;
import eu.emi.emir.event.EventDispatcher;
import eu.emi.emir.event.EventListener;
import eu.emi.emir.event.EventTypes;

/**
 * @author a.memon
 * @author g.szigeti
 * 
 */
public class ServiceEventReceiver implements EventListener, Runnable {
	private static Logger logger = Log.getLogger(Log.EMIR_CORE,
			ServiceEventReceiver.class);
	private static Configuration conf;
	private final WebResource client;
	private static InfrastructureManager infrastructure;
	private static boolean parent_lost;
	private Filters filter;

	/** 
	 * Constructor for this event receiver class
	 * @param URL of the parent
	 * @param configuration
	 */
	public ServiceEventReceiver(String parentUrl, Configuration config) {
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
		if ("true".equalsIgnoreCase(DSRServer.getProperty(ISecurityProperties.REGISTRY_SSL_ENABLED, "false"))) {

			c = new DSRClient(parentUrl + "/serviceadmin",
										DSRServer.getClientSecurityProperties());
		}

		client = c.getClientResource();
		parent_lost = false;
		filter = new Filters();
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
			jos = filter.outputFilter((JSONArray) event.getData());
		} catch (ClassCastException e) {
			if (logger.isDebugEnabled()) {
				logger.debug("event.data to JSONArray cast problem. May be delete message.");
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
									Log.logException("", e);
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
			String deleteURL = null;
			try{
				deleteURL = ((JSONObject) event.getData()).getString("Service_Endpoint_URL");
				ClientResponse res = client.queryParam(
					ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
							.getAttributeName(),
							deleteURL).delete(ClientResponse.class);
				if ( res.getStatus() == Status.OK.getStatusCode() ){
					if (parent_lost){
						// DB sync
						List<String> ID = new ArrayList<String>();
						ID.add(deleteURL);
						parent_lost = !infrastructure.dbSynchronization(ID, Method.DELETE, res.getStatus());
					}
				}
			} catch(ClientHandlerException e){
				parent_lost = true;
				infrastructure.handleDelete(deleteURL);
			} catch (JSONException e) {
				logger.warn(e.getCause());
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
