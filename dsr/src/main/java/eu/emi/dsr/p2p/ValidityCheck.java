/**
 * 
 */
package eu.emi.dsr.p2p;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.mongodb.MongoException;

import eu.emi.client.util.Log;
import eu.emi.dsr.DSRServer;
import eu.emi.dsr.core.ServerConstants;
import eu.emi.dsr.db.mongodb.MongoDBServiceDatabase;
import eu.emi.dsr.util.DateUtil;

/**
 * Periodically database entries validity checking by the GSR
 * 
 * @author g.szigeti
 * 
 */
public class ValidityCheck implements Runnable {
	private static Logger logger = Log.getLogger(Log.DSR, ValidityCheck.class);
	private int etvalid;
	private int timedelay;
	private MongoDBServiceDatabase mongoDB;
	
	/** 
	 * Constructor for a self-registration mechanism
	 * 
	 */
	public ValidityCheck() {
		mongoDB = new MongoDBServiceDatabase();

		try {
			etvalid = Integer.valueOf(DSRServer
					.getProperty(ServerConstants.REGISTRY_GLOBAL_ETVALID));
			if (etvalid < 12) {
				logger.info("Configured etvalid value (" + etvalid + ") is very low. Min value: 12 Default value will be used.");
				etvalid = 12;
			}
			logger.info("Set the etvalid to "+ etvalid);
		} catch (NumberFormatException e) {
			// set default value
			logger.info("Set the default (12hours) value of etvalid.");
			etvalid = 12;
		}
		
		try {
			timedelay = Integer.valueOf(DSRServer
					.getProperty(ServerConstants.REGISTRY_GLOBAL_ETVALID_DELAY));
			if (timedelay < 0) {
				logger.info("Configured etvalid timedelay value (" + timedelay + ") is very low. Min value: 0 Default value (2 hours) will be used.");
				timedelay = 2;
			}
			logger.info("Set the etvalid timedelay to "+ timedelay);
		} catch (NumberFormatException e) {
			// set default value
			logger.info("Set the default (2 hours) value of etvalid timedelay.");
			timedelay = 2;
		}
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
				logger.trace("Soft-State thread (validity) started");
			}
			JSONObject currentTime = DateUtil.setExpiryTimeWithHours(new JSONObject(),timedelay);
				
			// remove all GSRs from the database that are expired
			try {
				String query = "{ \"Service_ExpireOn\" : { $lt: "+ currentTime.getString("Service_ExpireOn")+"} }";
				logger.info(query);

				mongoDB.findAndDelete(query);
			} catch (MongoException e) {
				logger.warn(e.getCause());
			} catch (JSONException e) {
				logger.warn(e.getCause());
			}
			
			try {
				Thread.sleep(etvalid*60*60*1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
