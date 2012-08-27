package eu.emi.emir.p2p;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;

import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.mongodb.MongoException;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;

import eu.emi.emir.EMIRServer;
import eu.emi.emir.ServerProperties;
import eu.emi.emir.client.EMIRClient;
import eu.emi.emir.client.ServiceBasicAttributeNames;
import eu.emi.emir.client.util.Log;
import eu.emi.emir.core.ServiceAdminManager;
import eu.emi.emir.db.ExistingResourceException;
import eu.emi.emir.db.PersistentStoreFailureException;
import eu.emi.emir.db.QueryException;
import eu.emi.emir.db.ServiceDatabase;
import eu.emi.emir.db.mongodb.MongoDBServiceDatabase;
import eu.emi.emir.event.EventTypes;
import eu.emi.emir.validator.InvalidServiceDescriptionException;


/**
 * @author g.szigeti
 * 
 */
public class NeighborsManager {
	private static Logger logger = Log.getLogger(Log.EMIR_CORE,
								NeighborsManager.class);
	private static NeighborsManager instance = null;
	private List<String> neighbors;
	private List<String> unavailableNeighbors;
	private ArrayList<String> infoProviders;
	int neighbors_count;
	private Hashtable<String, String> hash;
	private ServiceDatabase serviceDB = null;
	private String myURL;
	private List<String> providerListURL;
	private int sparsity;
	private int retry;
	private boolean dowloadedProviderList;
	private boolean connected;
	private int maxEntriesNr;

	/** 
	 * Default constructor if you don't want to use as a singleton class 
	 * @param None
	 */
	protected NeighborsManager() {
		neighbors = new ArrayList<String>();
		unavailableNeighbors = new ArrayList<String>();
		neighbors_count = 0;
		myURL = EMIRServer.getServerProperties().getValue(ServerProperties.PROP_ADDRESS);
		hash = new Hashtable<String, String>();
		serviceDB = new MongoDBServiceDatabase();
		// config parse
		/*
		 * providerlist
		 * retry
		 * sparsity
		 */
		providerListURL = GetInfoProvidersFromConfiguration();

		if (providerListURL.isEmpty()){
			logger.warn("Configured providerlist value is empty. Please set it!");
		}
		try {
			sparsity = EMIRServer.getServerProperties().getIntValue(ServerProperties.PROP_GLOBAL_SPARSITY);
			if (sparsity < 2) {
				logger.info("Configured sparsity value (" + sparsity + ") is very low. Min value: 2 Default value will be used.");
				sparsity = 2;
			}
			logger.info("Set the sparsity to "+ sparsity);		
		} catch (NumberFormatException e) {
			// set default value
			logger.info("Set the default (2) value of sparsity.");
			sparsity = 2;
		}
		try {
			retry = EMIRServer.getServerProperties().getIntValue(ServerProperties.PROP_GLOBAL_RETRY);
			if (retry < 1) {
				logger.info("Configured retry value (" + retry + ") is very low. Min value: 1 Default value will be used.");
				retry = 5;
			}
			logger.info("Set the retry to "+ retry);
		} catch (NumberFormatException e) {
			// set default value
			logger.info("Set the default (5) value of retry.");
			retry = 5;
		}

		// Connection to the cloud in 6 steps.
        // 1. step: Download and set the list of the InfoProvider URL(s).
		dowloadedProviderList = false;
		infoProviders = DownloadProviderList(providerListURL);
		// 2.-6. steps are in the BootStrap function.
		connected = false;
		maxEntriesNr = 1000;
		BootStrap(retry);
	}

	/**
	 * Get only one instance for the neighbors manager class.
	 * Use this operation if you want to use this class as a Singleton.
	 * @param None
	 * @return NeighborsManager instance
	 */
	public static synchronized NeighborsManager getInstance() {
	      if(instance == null) {
	         instance = new NeighborsManager();
	      }
	      return instance;
	}
	
	/**
	 * Clear the hash table and set the neighbors count to 0.
	 *  
	 * @param None
	 */
	public void hashClear(){
		hash.clear();
		neighbors_count =0;
	}

	/**
	 * Get value of retry.
	 *  
	 * @param None
	 * @return retry
	 */
	public int getRetry(){
		return retry;
	}

	/**
	 * Get value of connected.
	 *  
	 * @param None
	 * @return connected or not
	 */
	public boolean getConnected(){
		return connected;
	}
	
	/**
	 * Get list of neighbors.
	 *  
	 * @param None
	 * @return list of neighbors URLs or own URL
	 */
	public synchronized List<String> getNeighbors(){
		// Reconnection section
		if (!dowloadedProviderList){
			infoProviders = DownloadProviderList(providerListURL);
		}
		if (dowloadedProviderList && !connected){
			BootStrap(retry);
		}
		// End of the reconnection section
		if (neighbors.isEmpty()){
			List<String> tmp = new ArrayList<String>();	
			tmp.add(myURL);
			return tmp;
		}
		return neighbors;
	}

	/**
	 * Add neighbors GSRs.
	 *  
	 * @param List of entries for global DSRs
	 * @param Type of the message (Register or Delete)
	 */
	public synchronized void addNeighborsDSRs(JSONArray entries, String type){
		if (logger.isDebugEnabled()) {
			logger.debug("addNeighborsDSRs called");
			logger.debug("hash: "+ hash.toString());
		}
		boolean updateNeed = false;
		if (type.equalsIgnoreCase(EventTypes.SERVICE_DELETE)){
			updateNeed = true;
		} else {
			for (int i=0; i<entries.length(); i++){
				try {
					if (!hash.containsValue(entries.getJSONObject(i).get(
							ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
										.getAttributeName()))){
						updateNeed = true;
					}
				} catch (JSONException e) {
					logger.warn(e.getCause());
				}
			}
		}
        if (updateNeed){
        	Neighbors_Update();
        }
	}
	
	/**
	 * Set unavailable neighbor GSR.
	 * @param URL of the unavailable neighbor GSR
	 * 
	 */
	public synchronized void setUnavailableNeighbor(String url){
		logger.warn("Unavailable neighbor: " + url);
		if (!unavailableNeighbors.contains(url)){
			unavailableNeighbors.add(url);
		}
		if (unavailableNeighbors.size() > (neighbors_count/2)){
			Neighbors_Update();
			unavailableNeighbors.clear();
		}
	}

	/**
	 * Reset unavailable neighbor GSR if it is need.
	 * @param URL of the available neighbor GSR
	 * 
	 */
	public synchronized void resetUnavailableNeighbor(String url){
		if (unavailableNeighbors.remove(url)){
			if (logger.isDebugEnabled()) {
				logger.debug("Remove "+ url + "from the list of unavailable GSR.");
			}
		}
	}

	/**
	 * Update the neighbors count and the hash table
	 * @param None
	 */
	private void Neighbors_Update(){
		if (logger.isDebugEnabled()) {
			logger.debug("Neighbors_Update called");
		}

		JSONArray gsrList = new JSONArray();
		// get the list of GSRs from the database
		try {
			gsrList = serviceDB.queryJSON("{\"Service_Type\" : \"GSR\"}");
		} catch (MongoException e) {
			logger.warn(e.getCause());
		} catch (QueryException e) {
			logger.warn(e.getCause());
		} catch (PersistentStoreFailureException e) {
			logger.warn(e.getCause());
		} catch (JSONException e) {
			logger.warn(e.getCause());
		}
		
		// reconnect to the P2P network, because it contains only the own GSR entry
		if (connected && gsrList.length() < 2) {
			connected = false;
		}
		
		// hash_table recalculate
		hash.clear();
		for(int i=0; i<gsrList.length(); i++){
			String url;
			try {
				url = gsrList.getJSONObject(i).getString(ServiceBasicAttributeNames
								.SERVICE_ENDPOINT_URL.getAttributeName());
				// hash calculation
				byte[] bytesOfMessage = url.getBytes("UTF-8");
				MessageDigest md = MessageDigest.getInstance("MD5");
				byte[] digest = md.digest(bytesOfMessage);
				BigInteger bigInt = new BigInteger(1,digest);
				String hashtext = bigInt.toString(16);
				
				// put an entry into the hash table
				hash.put(hashtext, url);

			} catch (JSONException e) {
				logger.warn(e.getCause());
			} catch (NoSuchAlgorithmException e) {
				logger.warn(e.getCause());
			} catch (UnsupportedEncodingException e) {
				logger.warn(e.getCause());
			}
		}
		// neighbors count update
        // log(2)x = (log(10)x)/(log(10)2)
		int new_neighbors_count = 0;
        if (hash.size() > 0){
            new_neighbors_count = (int)Math.ceil(Math.log10((double)hash.size())
            		/Math.log10((double)sparsity));
        }
        logger.info("Neighbors count recalculate from " + neighbors_count +
        		                                " to "+ new_neighbors_count);

		// neighbors list filling
        Neighbors_Calculate(new_neighbors_count);
        neighbors_count = new_neighbors_count;
	}
	
	/**
	 * Calculate the new list of neighbors
	 * @param count of neighbors
	 */
	private void Neighbors_Calculate(int count){
		if (logger.isDebugEnabled()) {
			logger.debug("Neighbors_Calculate called");
		}

		// Find own position in the hash table
        Collection<String> c = hash.values();
        Iterator<String> itr = c.iterator();
        Iterator<String> itrFirst = c.iterator();
        while(itr.hasNext()){ 
        	if (itr.next().equals(myURL)){
        		break;
        	}
        }

        int sum_step = 1;
        int previous_step = 0;
        String currentEntry = "";
        neighbors.clear();
         
        for (int i=0; i<count; i++) {
             //calculate the next neighbors
            for (int step=0; step<(sum_step-previous_step); step++){
                if (!itr.hasNext()){
            	    itr = itrFirst;
                }
            	currentEntry = itr.next();
            }
            previous_step = sum_step;
            sum_step = sum_step*sparsity;
            neighbors.add(currentEntry);
        }
	}
	
	/**
	 * Connect to the global GSR network
	 * @param how many time(s) try to connect to the unavailable global DSR server
	 * 
	 */
	private void BootStrap(int retry_count){
		// 2. step: goto InfoProviderGSR (one of the list)
		ArrayList<String> listOfGSRs = new ArrayList<String>();
		Collections.shuffle(infoProviders, new Random());
		for (int i=0; i<infoProviders.size(); i++){
		    // 3. step: Send Query message to the providerGSR with Filter
		    listOfGSRs = GSRList(infoProviders.get(i), retry_count);
		    if ( (listOfGSRs != null) && !listOfGSRs.isEmpty() ){
		    	break;
		    }
		}
		
		// 4. step: Extend the list of GSRs with the InfoProviders if it is needed.
		listOfGSRs = GSRPriorities(listOfGSRs);
		
		// 5. step: Get the DB from one GSR
		GetDB(listOfGSRs, retry);
		
		// 6. step: Neighbors calculation
		Neighbors_Update();
	}
	
	/**
	 * Get the list of the GSRs from the given URL.
	 * @param URL of the GSR
	 * @param how many time(s) try to connect to the unavailable global DSR server
	 * @return list of GSRs
	 */
	private ArrayList<String> GSRList(String url, int retry){
		EMIRClient c = new EMIRClient(url + "/services?Service_Type=GSR");
		if (EMIRServer.getServerSecurityProperties().isSslEnabled()) {

			c = new EMIRClient(url + "/services?Service_Type=GSR",
										EMIRServer.getClientSecurityProperties());
		}
		logger.info("Get the list of GSRs from " + url);
		for (int i=0; i<retry; i++){
			JSONArray o = new JSONArray();
			try {
				o = c.getClientResource()
						.accept(MediaType.APPLICATION_JSON_TYPE)
							.get(JSONArray.class);
			} catch (ClientHandlerException e) {
				if (logger.isDebugEnabled()) {
					logger.debug("Unreachable host: " + url);
				}
			}
			if (o.length() == 0){
				continue;
			}
			ArrayList<String> listOfGSRs = new ArrayList<String>();
			for (int j=0; j<o.length(); j++){
				try {
					listOfGSRs.add(o.getJSONObject(j).getString(
							ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL.getAttributeName()));
				} catch (JSONException e) {
					Log.logException("", e);
				}
			}
			return listOfGSRs;
		}
		return null;
	}
	
	/**
	 * Priorities the list of the GSRs. All InfoProviders move to the end of the list.
	 * @param list of GSRs
	 * @return priorities list of the GSRs and InfoProviders 
	 */
	private ArrayList<String> GSRPriorities(ArrayList<String> list){
		if (list == null){
			list = new ArrayList<String>();
		}
		if (list.removeAll(infoProviders)){
			// The element was exist in the list and removed it.
		}
		//Collections.shuffle(list, new Random()); // if needed this shuffle
		// Put all providers at the end of the list
		list.addAll(infoProviders);
		return list;
	}
	
	/**
	 * Get and store the Database from the given URL.
	 * @param list of GSRs
	 * @param how many time(s) try to connect to the unavailable global DSR server
	 */
	private void GetDB(ArrayList<String> list, int retry){
		JSONArray newDB = new JSONArray();
		for (int j=0; j<list.size(); j++){
			if (list.get(j).equals(myURL)){
				continue;
			}
			String ref = null;
			// Fetch the DB from the GSR
			EMIRClient c = new EMIRClient(list.get(j) + "/services/pagedquery?pageSize="+maxEntriesNr);
			if (EMIRServer.getServerSecurityProperties().isSslEnabled()) {

				c = new EMIRClient(list.get(j) + "/services/pagedquery",
											EMIRServer.getClientSecurityProperties());
			}
			boolean found = false;
			/*
			 * States:
			 *    0  start state
			 *    1  try to communicate with the server
			 *    2  get information from the server
			 *    3  server unreachable
			 */
			int state = 0;
			while ( (ref == null && state == 0) || (newDB.length() > 0 && !found && state != 3) ) {
				logger.info("Fetch DB from  " + list.get(j));
				for (int i=0; i<retry; i++){
					JSONObject o = new JSONObject();
					try {
						state = 1;
						o = c.getClientResource()
								.accept(MediaType.APPLICATION_JSON_TYPE)
									.get(JSONObject.class);
					} catch (ClientHandlerException e) {
						state = 3;
						logger.debug("DB query, unreachable host: " + list.get(j));
						continue;
					} catch (UniformInterfaceException e) {
						// No more entry get from the server.
						found = true;
						break;
					}
					state = 2;
					if (!o.isNull("result")){
						try {
							ref = o.getString("ref");
							logger.debug("New ref: " + ref);
							newDB = o.getJSONArray("result");
							logger.debug("New DB: " + newDB.toString());
							// Store the new DB part
							if (!DBStore(newDB)){
								logger.warn("Some failure happend during the DB store.");
							}
							// next part of the database
							c = new EMIRClient(list.get(j) + "/services/pagedquery?pageSize="+maxEntriesNr+"&ref="+ref);
							if (EMIRServer.getServerSecurityProperties().isSslEnabled()) {
								c = new EMIRClient(list.get(j) + "/services/pagedquery?pageSize="+maxEntriesNr+"&ref="+ref,
															EMIRServer.getClientSecurityProperties());
							}
							break;
						} catch (JSONException e) {
							logger.debug("The got message is not JSONArray! message: " + o.toString());
						}
					} else {
						// don't get more data from the server
						found = true;
						break;
					}
				} // end of the for
			} // end of while
			if (found){
				connected = true;
				break;
			}
		}
		
	}
	
	/**
	 * Store the given DB entries into the local Database.
	 * @param list of the DB entries
	 * 
	 * @return boolean, all elements can be stored without any failure or not.
	 * @throws Exception 
	 */
	private boolean DBStore(JSONArray newDB){
		ServiceAdminManager serviceAdmin = new ServiceAdminManager();
		boolean retval = true;
		for (int i=0; i<newDB.length(); i++){
			JSONObject jo = null;
			try {
				jo = new JSONObject(newDB.getString(i));
				String sendpointID = jo
						.getString(ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID
								.getAttributeName());
				if (sendpointID.equals(myURL)) {
					continue;
				}
				String messageTime = "";
				if (jo.has(ServiceBasicAttributeNames.SERVICE_UPDATE_SINCE
								.getAttributeName())){
					messageTime = (jo
							.getJSONObject(ServiceBasicAttributeNames.SERVICE_UPDATE_SINCE
									.getAttributeName())).getString("$date");
				}
				if (serviceAdmin.checkMessageGenerationTime(messageTime, sendpointID)){
					// Insert the entry to the database
					@SuppressWarnings("unused")
					JSONObject res = serviceAdmin.addService(jo);
				}
			} catch (JSONException e) {
				if (logger.isDebugEnabled()) {
					logger.debug("Some attribute(s) is/are missing: " + e.getMessage());
				}
				retval = false;
			} catch (InvalidServiceDescriptionException e) {	//addService
				Log.logException("", e,logger);
				retval = false;
			} catch (ExistingResourceException e) {
				if (logger.isDebugEnabled()) {
					logger.warn("This entry is exist in the DB: " + jo.toString());
				}
				retval = false;
			} catch (QueryException e) {	//checkMessageGenerationTime
				Log.logException("", e,logger);
				retval = false;
			} catch (PersistentStoreFailureException e) {	//checkMessageGenerationTime
				Log.logException("", e,logger);
				retval = false;
			} catch (Exception e) {
				Log.logException("", e,logger);
			}
		}
		return retval;
	}

	/**
	 * Download the list of the InfoProvider GSR from the given URL.
	 * @param URL for the list of the InfoProvider
	 * @return list of the InfoProviders
	 */
	private ArrayList<String> DownloadProviderList(List<String> urls) {
		ArrayList<String> providers = new ArrayList<String>();
		for (int i=0; i<urls.size(); i++){
			// Download the list of the URLs
			EMIRClient c = new EMIRClient(urls.get(i));
			if (EMIRServer.getServerSecurityProperties().isSslEnabled()) {

				c = new EMIRClient(urls.get(i),
											EMIRServer.getClientSecurityProperties());
			}
			try {
				String content = c.getClientResource()
						.accept(MediaType.TEXT_PLAIN)
							.get(String.class);
				// Replace the following characters (' ', '[', ']', '\n') 
				// with empty character.
				content = content.replaceAll(" |\\[|\\]|\n", "");
				// Tokenize the input string and add into the list
				StringTokenizer tokens = new StringTokenizer(content,",");
			    while(tokens.hasMoreTokens()){
					// Put the URL into the provider list
			    	providers.add((String)tokens.nextElement());
			    }
			    dowloadedProviderList = true;
			    break;
			} catch (ClientHandlerException e) {
				dowloadedProviderList = false;
				if (logger.isDebugEnabled()) {
					logger.debug("Unreachable provider list from " + urls.get(i));
				}
			}
		}
		return providers;
	}

	/*
	 * 
	 * 
	 */
	private List<String> GetInfoProvidersFromConfiguration(){
		List<String> listOfURLs = new ArrayList<String>();
		
		String configValue = EMIRServer.getServerProperties().getValue(ServerProperties.PROP_GLOBAL_PROVIDERLIST);
		// Replace the following characters (' ', '[', ']', '\n') 
		// with empty character.
		configValue = configValue.replaceAll(" |\\[|\\]|\n", "");
		// Tokenize the input string and add into the list
		StringTokenizer tokens = new StringTokenizer(configValue,",");
	    while(tokens.hasMoreTokens()){
			// Put the URL into the provider list
	    	listOfURLs.add((String)tokens.nextElement());
	    }
		return listOfURLs;
	}
}
