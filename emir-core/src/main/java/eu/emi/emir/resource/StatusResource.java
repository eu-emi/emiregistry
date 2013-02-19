/**
 * 
 */
package eu.emi.emir.resource;

import java.net.UnknownHostException;
import java.util.Date;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.mongodb.DB;
import com.mongodb.DBAddress;
import com.mongodb.Mongo;

import eu.emi.emir.EMIRServer;
import eu.emi.emir.ServerProperties;
import eu.emi.emir.client.util.Log;
import eu.unicore.util.configuration.ConfigurationException;

/**
 * The resource to show server status, such as, server version, mongodb version,
 * mongodb version and status etc...
 * 
 * @author a.memon
 * 
 */
@Path("/status")
public class StatusResource {
	private static final Logger logger = Log.getLogger(Log.EMIR_HTTPSERVER,
			StatusResource.class);

	private String serverVersion = StatusResource.class.getPackage()
			.getImplementationVersion();

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN })
	public Response getServerStatus() throws WebApplicationException {
		JSONObject jo = new JSONObject();
		Date d = EMIRServer.getRunningSince();

		try {
			if (!(serverVersion == null || serverVersion.isEmpty())) {
				jo.put("EMIRServerVersion", serverVersion);
			}
			jo.put("MongoDBVersion", EMIRServer.getMongoDBVersion());
			jo.put("JavaVersion", System.getProperty("java.version"));
			jo.put("OSName", System.getProperty("os.name"));
			jo.put("OSArchitecture", System.getProperty("os.arch"));
			jo.put("OSVersion", System.getProperty("os.version"));
			if(EMIRServer.getServerProperties().isGlobalEnabled())
				jo.put("EMIRServerComponentName", "Global Service Registry (GSR)");
			else
				jo.put("EMIRServerComponentName", "Domain Service Registry (DSR)");
			if (EMIRServer.getServerProperties().isAnonymousAccessEnabled()) 
				jo.put("AnonymousAccessPortNumber", EMIRServer.getServerProperties().getAnonymousPortNumber());
			jo.put("RunningSince", d.toString());
			try {
				DB db = Mongo.connect(new DBAddress(EMIRServer.getServerProperties().getValue(
						ServerProperties.PROP_MONGODB_HOSTNAME),
						EMIRServer.getServerProperties().getIntValue(
								ServerProperties.PROP_MONGODB_PORT),
						EMIRServer.getServerProperties().getValue(
								ServerProperties.PROP_MONGODB_DB_NAME)));

				String colName = EMIRServer.getServerProperties().getValue(ServerProperties.PROP_MONGODB_COLLECTION_NAME);
				jo.put("NumberofEntries",+db.getCollection(colName).count());

			} catch (UnknownHostException e) {
				Log.logException("Error in probing the mongodb status", e, logger);
			} catch (ConfigurationException e) {
				Log.logException("Error in probing the mongodb status", e, logger);
			}

		} catch (JSONException e) {
			Log.logException("Error in probing the EMIR status", e, logger);
			throw new WebApplicationException(e);
		}
		return Response.ok(jo).build();
	}

}