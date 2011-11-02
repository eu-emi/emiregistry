package eu.emi.dsr.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import eu.emi.dsr.DSRServer;
import eu.emi.dsr.core.ServerConstants;

/**
 * Checking to the emiregistry and get list of childs
 * @author g.szigeti
 *
 */
@Path("/parent")
public class ParentResource {

	@GET
	public Response childDSRs(){
		String parent = DSRServer.getProperty(ServerConstants.REGISTRY_PARENT_URL);
		if ( parent.isEmpty()) {
			parent = "No parent set!";
		}
		return Response.ok().entity(parent).build();
	}

}
