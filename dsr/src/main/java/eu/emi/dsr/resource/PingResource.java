/**
 * 
 */
package eu.emi.dsr.resource;

import java.util.Date;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import eu.emi.dsr.DSRServer;

/**
 * Pinging the emiregistry
 * @author a.memon
 *
 */
@Path("/ping")
public class PingResource {
	@GET
	public Response ping() throws WebApplicationException{
		System.out.println("pinging me!!");
		JSONObject jo = new JSONObject();
		Date d = DSRServer.getRunningSince();
		try {
			jo.put("RunningSince", d.toString());
		} catch (JSONException e) {
			throw new WebApplicationException(e);
		}
		
		return Response.ok(jo).build();
	}
}
