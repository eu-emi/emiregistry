package eu.emi.emir.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import eu.emi.emir.DSRServer;
import eu.emi.emir.core.ServerConstants;

/**
 * Checking to the emiregistry and get list of childs
 * @author g.szigeti
 *
 */
@Path("/parent")
public class ParentResource {

	@GET
	public Response childDSRs(){
		if ("true".equalsIgnoreCase(DSRServer.getProperty(
				ServerConstants.REGISTRY_GLOBAL_ENABLE, "false").toString())){
			return Response.noContent().entity("Not supported method by the global DSR.").build();
		}
		String parent = DSRServer.getProperty(ServerConstants.REGISTRY_PARENT_URL);
		if ( parent == null || parent.isEmpty()) {
			parent = "No parent set!";
		}
		return Response.ok().entity(parent).build();
	}

}
