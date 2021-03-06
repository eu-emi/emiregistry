/**
 * 
 */
package eu.emi.emir.p2p;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.mongodb.MongoException;

import eu.emi.emir.EMIRServer;
import eu.emi.emir.ServerProperties;
import eu.emi.emir.client.util.DateUtil;
import eu.emi.emir.client.util.Log;
import eu.emi.emir.db.mongodb.MongoDBServiceDatabase;

/**
 * Periodically database entries validity checking by the GSR
 * 
 * @author g.szigeti
 * 
 */
public class ValidityCheck implements Runnable {
	private static Logger logger = Log.getLogger(Log.EMIR_CORE, ValidityCheck.class);
	private int etvalid;
	private int timedelay;
	private MongoDBServiceDatabase mongoDB;
	
	/** 
	 * Constructor for a Soft-State mechanism
	 * 
	 */
	public ValidityCheck(int delay) {
		timedelay = delay;
		mongoDB = new MongoDBServiceDatabase();

		try {
			etvalid = EMIRServer.getServerProperties().getIntValue(ServerProperties.PROP_GLOBAL_ETVALID);
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
				Log.logException("", e);
			}
		}
	}

}
