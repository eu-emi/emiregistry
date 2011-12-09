package eu.emi.dsr.p2p;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;

import com.mongodb.MongoException;

import eu.emi.client.util.Log;
import eu.emi.dsr.DSRServer;
import eu.emi.dsr.core.ServerConstants;
import eu.emi.dsr.db.PersistentStoreFailureException;
import eu.emi.dsr.db.QueryException;
import eu.emi.dsr.db.ServiceDatabase;
import eu.emi.dsr.db.mongodb.MongoDBServiceDatabase;
import eu.emi.dsr.event.EventTypes;



public class NeighborsManager {
	private static Logger logger = Log.getLogger(Log.DSR,
								NeighborsManager.class);
	private static NeighborsManager instance = null;
	private List<String> neighbors;
	int neighbors_count;
	private Hashtable<String, String> hash;
	private ServiceDatabase serviceDB = null;
	private String myURL;
	private int sparsity;
	private int retry;
	private int etvalid;
	private int etremove;

	/** 
	 * Default constructor if you don't want to use as a singleton class 
	 * @param None
	 */
	protected NeighborsManager() {
		neighbors = new ArrayList<String>();
		neighbors_count = 0;
		myURL = DSRServer.getProperty(ServerConstants.REGISTRY_SCHEME).toString() +"://"+
				DSRServer.getProperty(ServerConstants.REGISTRY_HOSTNAME).toString() +":"+
				DSRServer.getProperty(ServerConstants.REGISTRY_PORT).toString();
		hash = new Hashtable<String, String>();
		serviceDB = new MongoDBServiceDatabase();
		// config parse
		/*
		 * retry
		 * sparsity
		 * etvalid
		 * etremove
		 */
		try {
			sparsity = Integer.valueOf(DSRServer
					.getProperty(ServerConstants.REGISTRY_GLOBAL_SPARSITY));
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
			retry = Integer.valueOf(DSRServer
					.getProperty(ServerConstants.REGISTRY_GLOBAL_RETRY));
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
		try {
			etvalid = Integer.valueOf(DSRServer
					.getProperty(ServerConstants.REGISTRY_GLOBAL_ETVALID));
			if (retry < 12) {
				logger.info("Configured etvalid value (" + etvalid + ") is very low. Min value: 12 Default value will be used.");
				retry = 12;
			}
			logger.info("Set the etvalid to "+ etvalid);
		} catch (NumberFormatException e) {
			// set default value
			logger.info("Set the default (12hours) value of etvalid.");
			etvalid = 12;
		}
		try {
			etremove = Integer.valueOf(DSRServer
					.getProperty(ServerConstants.REGISTRY_GLOBAL_ETREMOVE));
			if (retry < 24) {
				logger.info("Configured etremove value (" + etremove + ") is very low. Min value: 24 Default value will be used.");
				retry = 24;
			}
			logger.info("Set the etremove to "+ etremove);
		} catch (NumberFormatException e) {
			// set default value
			logger.info("Set the default (24hours) value of etremove.");
			etremove = 24;
		}
		// connect to the network
		// Connection to the cloud in 6 steps.
        // 1. step: Put it's own InfoProvider URL(s) from configuration in the set of providers.
		// 2.-6. steps are in the BootStrap function.
		//BootStrap(retry);
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
	 * Get list of neighbors.
	 *  
	 * @param None
	 * @return list of neighbors URLs or own URL
	 */

	public synchronized List<String> getNeighbors(){
		if (neighbors.isEmpty()){
			List<String> tmp = new ArrayList<String>();	
			tmp.add(myURL);
			return tmp;
		}
		return neighbors;
	}

	/**
	 * Add neighbors DSRs.
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
					if (!hash.containsValue(entries.getJSONObject(i).get("Service_Endpoint_URL"))){
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
	 * Set unavailable neighbor DSR.
	 * @param URL of the unavailable neighbor DSR
	 * 
	 * Not implemented yet.
	 */
	public synchronized void setUnavailableNeighbor(String url){
		logger.warn("Unavailable neighbor: " + url);
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
		
		// hash_table recalculate
		hash.clear();
		for(int i=0; i<gsrList.length(); i++){
			String url;
			try {
				url = gsrList.getJSONObject(i).getString("Service_Endpoint_URL");
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
        logger.debug("Neighbors count recalculate from " + neighbors_count +
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
	 * Not implemented yet.
	 */
	private void BootStrap(int retry_count){
		// 2. step: goto InfoProviderGSR (one of the list)
		// 3. step: Send Query message to the providerGSR with Filter
		// 4. step: Hash table and neighbors filling
		// 5. step: Connect message send to one ISIS of the neighbors
		// 6. step: response data processing (DB sync, Config saving)
	}
}
