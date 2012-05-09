package eu.emi.emir.resource;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONArray;

import eu.emi.emir.DSRServer;
import eu.emi.emir.core.ServerConstants;
import eu.emi.emir.p2p.NeighborsManager;

/**
 * Return the emiregistry with the list of neighbors
 * @author g.szigeti
 *
 */
@Path("/neighbors")
public class NeighborsResource {

	@GET
	public Response childDSRs(){
		if ("false".equalsIgnoreCase(DSRServer.getProperty(
							ServerConstants.REGISTRY_GLOBAL_ENABLE, "false").toString())){
			return Response.noContent().entity("Not supported method by the federated DSR.").build();
		}
		List<String> resp;
		resp = NeighborsManager.getInstance().getNeighbors();
		JSONArray respArray = new JSONArray();
		for (int i=0; i< resp.size(); i++){
			respArray.put(resp.get(i));
		}
		return Response.ok().entity(respArray).build();
	}

}
