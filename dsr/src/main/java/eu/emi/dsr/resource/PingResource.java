/**
 * 
 */
package eu.emi.dsr.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 * Pinging the emiregistry
 * @author a.memon
 *
 */
@Path("/ping")
public class PingResource {
	@GET
	public Response ping(){
		System.out.println("pinging me!!");
		return Response.ok().build();
	}
}
