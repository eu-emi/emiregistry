/**
 * 
 */
package eu.emi.emir.p2p;


import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import eu.emi.emir.EMIRServer;
import eu.emi.emir.ServerProperties;
import eu.emi.emir.client.EMIRClient;
import eu.emi.emir.client.ServiceBasicAttributeNames;
import eu.emi.emir.client.util.DateUtil;
import eu.emi.emir.client.util.Log;

/**
 * GSR Service periodically self registration
 * 
 * @author g.szigeti
 * 
 */
public class SelfRegistration implements Runnable {
	private static Logger logger = Log.getLogger(Log.EMIR_CORE, SelfRegistration.class);
	private final WebResource selfRegisterClient;
	private JSONObject myInfos;
	private Boolean firstUsage;
	
	/** 
	 * Constructor for a self-registration mechanism
	 * @param url
	 * @throws Throwable 
	 * 
	 */
	public SelfRegistration(String myUrl) throws Throwable {
		EMIRClient sc = new EMIRClient(myUrl + "/serviceadmin");
		if (EMIRServer.getServerSecurityProperties().isSslEnabled()) {
			sc = new EMIRClient(myUrl + "/serviceadmin",
										EMIRServer.getClientSecurityProperties());
		}
		selfRegisterClient = sc.getClientResource();
		firstUsage = true;
		try {
			URL tmp = new URL(myUrl);
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
			Log.logException("", e);
		}
		// Fill the infos
		myInfos = new JSONObject();
		myInfos.put(ServiceBasicAttributeNames.SERVICE_ID.getAttributeName(), myUrl);
		myInfos.put("Service_Name", "EMI IndexService");
		myInfos.put("Service_Type", "GSR");	// in GLUE2 ServiceType_t syntax: eu.emi.emir.gsr
		myInfos.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID.getAttributeName(), myUrl);
		myInfos.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL.getAttributeName(), myUrl);
		JSONArray capability = new JSONArray();
		capability.put("information.model");
		capability.put("information.discovery");
		myInfos.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_CAPABILITY.getAttributeName(), capability);
		myInfos.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_TECHNOLOGY.getAttributeName(), "webservice");
		String ifaceName = "http";
		if (myUrl.substring(0, 5).equals("https")) {
			ifaceName += "s";
		}
		myInfos.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_IFACENAME.getAttributeName(), ifaceName);
		myInfos.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_IFACE_VER.getAttributeName(), "1.0.0");

		try {
			String DN = eu.emi.emir.security.SecurityManager.getServerDistinguishedName();
			myInfos.put(ServiceBasicAttributeNames.SERVICE_DN.getAttributeName(), DN);
			logger.info("Server's DN: "+DN);
				
		} catch(NullPointerException e){
			logger.error("No DN. Please turn on the SSL with registry.scheme=https!");
		}
		// current time and last update should be same in the beginning
		JSONObject date = new JSONObject();
		date.put("$date", DateUtil.toUTCFormat(new Date()));
		myInfos.put(ServiceBasicAttributeNames.SERVICE_CREATED_ON
				.getAttributeName(), date);
		myInfos.put(ServiceBasicAttributeNames.SERVICE_EXPIRE_ON
				.getAttributeName(), date);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		while(true){
			if (logger.isTraceEnabled()) {
				logger.trace("self registration entry");
			}
			// update the expiration time
			DateUtil.setExpiryTime(myInfos, EMIRServer.getServerProperties().getIntValue(ServerProperties.PROP_RECORD_EXPIRY_DEFAULT));
			JSONArray message = new JSONArray();
			message.put(myInfos);
			// message sending
			try{
				if (firstUsage){
					ClientResponse res = selfRegisterClient.accept(MediaType.APPLICATION_JSON_TYPE)
						.post(ClientResponse.class, message);
					if ( res.getStatus() != Status.BAD_REQUEST.getStatusCode() ){
						// Next message will be send as UPDATE
						firstUsage = false;
					}
				} else {
					ClientResponse res = selfRegisterClient.accept(MediaType.APPLICATION_JSON_TYPE)
						.put(ClientResponse.class, message);
					if ( res.getStatus() == Status.BAD_REQUEST.getStatusCode() ){
						// Next message will be send as REGISTER
						firstUsage = true;
					}
				}
			} catch(ClientHandlerException e) {
				logger.trace("Self-registration problem: " + e.getMessage());
			}
			
			try {
				Thread.sleep(60*60*1000);
			} catch (InterruptedException e) {
				Log.logException("", e);
			}
		}
	}

}
