/**
 * 
 */
package eu.emi.dsr.infrastructure;

import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;

import eu.emi.dsr.client.DSRClient;
import eu.emi.dsr.core.ServiceBasicAttributeNames;
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
	private static Logger logger = Log.getLogger(Log.DSR,
			ServiceEventReciever.class);
	private final WebResource client;

	/**
	 * @param property
	 */
	public ServiceEventReciever(String parentUrl) {
		DSRClient c = new DSRClient(parentUrl + "/serviceadmin");
		client = c.getClientResource();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.emi.dsr.event.EventReciever#recieve(eu.emi.dsr.event.Event)
	 */
	@Override
	public void recieve(Event event) {
		// here sending messages to the parent DSR's
		if (event.getType().equalsIgnoreCase(EventTypes.SERVICE_ADD)) {
			if (logger.isDebugEnabled()) {
				logger.debug("service added event fired");
			}
			client.accept(MediaType.APPLICATION_JSON_TYPE)
					.post(event.getData());
		}
		if (event.getType().equalsIgnoreCase(EventTypes.SERVICE_UPDATE)) {
			if (logger.isDebugEnabled()) {
				logger.debug("service update event fired");
			}
			JSONObject j = (JSONObject) event.getData();
			try {
				client.accept(MediaType.APPLICATION_JSON_TYPE)
						.put(j);
			} catch (Exception e) {
				Log.logException("Error making update on the parent dsr",e);
			}
		}
		if (event.getType().equalsIgnoreCase(EventTypes.SERVICE_DELETE)) {
			if (logger.isDebugEnabled()) {
				logger.debug("service added delete event fired");
			}
			client.queryParam(
					ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
							.getAttributeName(),
					event.getData().toString()).delete();
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
