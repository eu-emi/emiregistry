package eu.emi.dsr.resource;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.codehaus.jettison.json.JSONArray;

import eu.emi.dsr.core.ServiceBasicAttributeNames;
import eu.emi.dsr.infrastructure.EmptyIdentifierFailureException;
import eu.emi.dsr.infrastructure.NullPointerFailureException;

/**
 * Checking to the emiregistry and get list of childs
 * @author g.szigeti
 *
 */
@Path("/children")
public class ChildrenResource {
	private static Map<String, Date> childServices = new HashMap<String, Date>();
	
	@POST
	public Response checkin(@Context UriInfo infos){
		System.out.println("checkin !!");
		try {
			if ( addChildDSR(extractServiceUrlFromUri(infos)) ) {
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
		resp = getChildDSRs();
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

	public List<String> getChildDSRs() {
		List<String> result = new ArrayList<String>();
		Date currentTime = new Date();
		
		Set<String> s=childServices.keySet();
		Iterator<String> it=s.iterator();
		while(it.hasNext()) {
            String key=it.next();
            Date value=childServices.get(key);
            long hour = 60*60*1000;
            if ( value.getTime()+hour > currentTime.getTime()) {
				result.add(key);
			} else {
				// expired checkin entry
				childServices.remove(key);
			}
		}
		return result;
	}

	public boolean addChildDSR(String identifier)
			throws EmptyIdentifierFailureException, NullPointerFailureException {
		if (identifier == null)
			throw new NullPointerFailureException();
		if (identifier.isEmpty())
			throw new EmptyIdentifierFailureException();

		boolean retval = false;
		if (childServices.containsKey(identifier)) {
			childServices.remove(identifier);
		} else {
			// First time put the identifier into the list
			retval = true;
		}
        childServices.put(identifier, new Date());
        return retval;
	}

}
