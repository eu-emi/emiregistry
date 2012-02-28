package eu.emi.dsr.resource;

import java.util.ConcurrentModificationException;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.codehaus.jettison.json.JSONArray;

import eu.emi.client.ServiceBasicAttributeNames;
import eu.emi.dsr.infrastructure.ChildrenManager;
import eu.emi.dsr.infrastructure.EmptyIdentifierFailureException;
import eu.emi.dsr.infrastructure.NullPointerFailureException;

/**
 * Checking to the emiregistry and get list of childs
 * @author g.szigeti
 *
 */
@Path("/children")
public class ChildrenResource {

	@POST
	public Response checkin(@Context UriInfo infos){
		System.out.println("checkin !!");
		try {
			if ( ChildrenManager.getInstance().addChildDSR(extractServiceUrlFromUri(infos)) ) {
				return Response.ok().entity(new String("First registration")).build();
			}
		} catch (IllegalArgumentException e) {
			return Response.status(Status.BAD_REQUEST).entity("No endpoint given!").build();
		} catch (EmptyIdentifierFailureException e) {
			return Response.status(Status.BAD_REQUEST).entity("Empty endpoint given!").build();
		} catch (NullPointerFailureException e) {
			return Response.status(Status.BAD_REQUEST).entity("No endpoint given!").build();
		}
		return Response.ok().build();
	}
	
	@GET
	public Response childDSRs(){
		System.out.println("checkout !!");
		List<String> resp;
		try {
			resp = ChildrenManager.getInstance().getChildDSRs();
		} catch (ConcurrentModificationException e) {
			// try again
			resp = ChildrenManager.getInstance().getChildDSRs();
		}
		JSONArray respArray = new JSONArray();
		for (int i=0; i< resp.size(); i++){
			System.out.println(i +". value: " + resp.get(i));
			respArray.put(resp.get(i));
		}
		return Response.ok().entity(respArray).build();
	}

	private String extractServiceUrlFromUri(UriInfo infos) throws IllegalArgumentException{
		MultivaluedMap<String, String> mm = infos.getQueryParameters();
		String attrName = ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
				.getAttributeName();
		String key = (mm.containsKey(attrName)) ? attrName : "unknown";
		if (key == "unknown") {
			throw new IllegalArgumentException();
		}
		String value = mm
				.getFirst(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
						.getAttributeName());
		return value;
	}

}
