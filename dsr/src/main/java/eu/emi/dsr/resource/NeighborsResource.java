package eu.emi.dsr.resource;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONArray;

import eu.emi.dsr.p2p.NeighborsManager;

/**
 * Return the emiregistry with the list of neighbors
 * @author g.szigeti
 *
 */
@Path("/neighbors")
public class NeighborsResource {

	@GET
	public Response childDSRs(){
		List<String> resp;
		resp = NeighborsManager.getInstance().getNeighbors();
		JSONArray respArray = new JSONArray();
		for (int i=0; i< resp.size(); i++){
			respArray.put(resp.get(i));
		}
		return Response.ok().entity(respArray).build();
	}

}
