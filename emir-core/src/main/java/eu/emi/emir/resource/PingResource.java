/**
 * 
 */
package eu.emi.emir.resource;

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

import eu.emi.emir.EMIRServer;
import eu.emi.emir.client.util.Log;

/**
 * Pinging the emiregistry
 * @author a.memon
 *
 */
@Path("/ping")
public class PingResource {
	private static final Logger logger = Log.getLogger(Log.EMIR_HTTPSERVER, PingResource.class);
	@GET
	@Produces({MediaType.APPLICATION_JSON,MediaType.TEXT_PLAIN})
	public Response ping() throws WebApplicationException{
		JSONObject jo = new JSONObject();
		Date d = EMIRServer.getRunningSince();
		try {
			jo.put("RunningSince", d.toString());
		} catch (JSONException e) {
			Log.logException("Error in pinging the EMIR",e,logger);
			throw new WebApplicationException(e);
		}
		
		return Response.ok(jo).build();
	}
}
