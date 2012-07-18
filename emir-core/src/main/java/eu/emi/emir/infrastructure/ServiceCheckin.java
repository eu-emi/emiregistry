/**
 * 
 */
package eu.emi.emir.infrastructure;


import java.net.MalformedURLException;
import java.net.URL;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import eu.emi.emir.EMIRServer;
import eu.emi.emir.client.EMIRClient;
import eu.emi.emir.client.ServiceBasicAttributeNames;
import eu.emi.emir.client.util.Log;
import eu.emi.emir.db.ServiceDatabase;
import eu.emi.emir.db.mongodb.MongoDBServiceDatabase;

/**
 * Service periodically checkin to the parent DSR
 * 
 * @author g.szigeti
 * 
 */
public class ServiceCheckin implements Runnable {
	private static Logger logger = Log.getLogger(Log.EMIR_CORE, ServiceCheckin.class);
	private final WebResource childClient;
	private final WebResource synchClient;
	private String myURL;
	private String parentURL;
	private Long max;
	private ServiceDatabase serviceDB;
	private Filters filters;
	
	/**
	 * Constructor for a service checkin mechanism
	 * @param URL of the parent DSR
	 * @param own URL
	 * @param maximum number of entries per message
	 * @throws Throwable 
	 * 
	 */
	public ServiceCheckin(String parentUrl, String url, Long maxmessage) throws Throwable {
		// configuration error handling
		String slash = "/";
		if (parentUrl.charAt(parentUrl.length()-1) == '/' ){
			slash = "";
		}
		parentURL = parentUrl;
		EMIRClient cc = new EMIRClient(parentUrl + slash + "children");
		EMIRClient sc = new EMIRClient(parentUrl + slash + "serviceadmin");
		if (EMIRServer.getServerSecurityProperties().isSslEnabled()) {

			cc = new EMIRClient(parentUrl + slash + "children",
										EMIRServer.getClientSecurityProperties());
			sc = new EMIRClient(parentUrl + slash + "serviceadmin",
										EMIRServer.getClientSecurityProperties());
		}
		childClient = cc.getClientResource();
		synchClient = sc.getClientResource();
		serviceDB = new MongoDBServiceDatabase();
		try {
			URL tmp = new URL(url);
			if (tmp.getProtocol().isEmpty()) {
				logger.error("The registry.scheme element is empty in the configuration!");
				this.finalize();
			}
			if (tmp.getHost().isEmpty()) {
				logger.error("The registry.hostname element is empty in the configuration!");
				this.finalize();
			}
			if (tmp.getHost().equals("localhost")) {
				logger.error("You added 'localhost' value in the registry.hostname element. Please modified it to the real DNS name or IP address!");			
				this.finalize();
			}
			if (tmp.getPort() == -1 ) {
				logger.error("The registry.post element is empty in the configuration!");		
				this.finalize();
			}
		} catch (MalformedURLException e) {
			Log.logException("", e, logger);
		}
		myURL = url;
		max = maxmessage;
		filters = new Filters();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		String refID = null;
		while(true){
			logger.debug("checkin service entry");
			try {
				ClientResponse res = childClient.queryParam(
						ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
							.getAttributeName(),
								myURL).post(ClientResponse.class);
				if ( res.getStatus() == Status.BAD_REQUEST.getStatusCode() ||
						res.getStatus() == Status.NOT_FOUND.getStatusCode()){
					logger.error("Please modified the server's configuration, because the following error (" +res.getEntity(String.class) + ") given from the parent DSR. Checkin stopped!");
					return;
				}
				if ( res.hasEntity() && 
						(res.getEntity(String.class).equals("First registration")
						 || refID != null) ){
					// Full DB need to be send
					try {
						JSONArray message = serviceDB.paginatedQuery("{}", max.intValue(), refID, 
								ServiceBasicAttributeNames.SERVICE_UPDATE_SINCE.getAttributeName());
						logger.debug("Synchronisation STARTED with the parent EMIR: "+ parentURL);
						while (message.length() > 0){
							logger.trace("Send SYNCH message with reference DB record ID _id: "+ refID
													+", to the parent: "+ parentURL);
							message = convert(message);
							// message sending
							synchClient.accept(MediaType.APPLICATION_JSON_TYPE)
										.post(ClientResponse.class, filters.outputFilter(message));
							// next message creation
							JSONObject doc = new JSONObject(message.get(message.length()-1).toString());
							refID = doc.getJSONObject("_id").get("$oid").toString();
							
							message = serviceDB.paginatedQuery("{}", max.intValue(), refID, 
									ServiceBasicAttributeNames.SERVICE_UPDATE_SINCE.getAttributeName());
						}
						refID = null;
						logger.debug("Synchronisation FINISHED with the parent EMIR: "+ parentURL);
					} catch (JSONException e) {
						Log.logException("", e, logger);
					}
				}
			} catch (ClientHandlerException e){
				logger.info("The parent DSR is not available.");
			}
			try {
				Thread.sleep(60*60*1000);
			} catch (InterruptedException e) {
				Log.logException("", e, logger);
			}
		}
	}
	
	/*
	 * Convert the JSONArray's type of elements 
	 * from com.mongodb.BasicDBObject
	 * to   org.codehaus.jettison.json.JSONObject
	 * 
	 */
	private JSONArray convert(JSONArray array){
		JSONArray result = new JSONArray();
		for (int i=0; i<array.length(); i++){
			try {
				result.put(new JSONObject(array.getString(i)));
			} catch (JSONException e) {
				Log.logException("JSONObject convertation problem: ", e, logger);
			}
		}
		return result;
	}

}
