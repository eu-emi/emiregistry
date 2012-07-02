package eu.emi.emir.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import eu.emi.emir.EMIRServer;
import eu.emi.emir.ServerProperties;

/**
 * Checking to the emiregistry and get list of childs
 * @author g.szigeti
 *
 */
@Path("/parent")
public class ParentResource {

	@GET
	public Response childDSRs(){
		if (EMIRServer.getServerProperties().isGlobalEnabled()){
			return Response.noContent().entity("Not supported method by the global DSR.").build();
		}
		String parent = EMIRServer.getServerProperties().getValue(ServerProperties.PROP_PARENT_ADDRESS);
		if ( parent == null || parent.isEmpty()) {
			parent = "No parent set!";
		}
		return Response.ok().entity(parent).build();
	}

}
