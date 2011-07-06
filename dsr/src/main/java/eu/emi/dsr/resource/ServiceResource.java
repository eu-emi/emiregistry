/**
 * 
 */
package eu.emi.dsr.resource;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import eu.emi.dsr.util.Log;
import eu.emi.dsr.util.ServiceUtil;



/**
 * 
 * @author a.memon
 *
 */
@Path("/service")
public class ServiceResource{
	Logger logger = Log.getLogger(Log.DSR, ServiceResource.class);
	/** query method */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{id}")
	public JSONObject getServiceDescription(@PathParam("id") String id) throws WebApplicationException {
//		if ((id == null)||(id.isEmpty())) {
//			throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
//		}
		logger.debug("getting service description");
		logger.debug("given parameter: "+id);
		Map<String, String> map = new HashMap<String, String>();
		map.put("id", "1");
		map.put("type", "job.datamangement");
		map.put("url", "http://jms");
		return new JSONObject(map);
	}
	
	/** inserts the service description
	 *  "privileged method"
	 * @throws JSONException 
	 *  */
	@POST
	public String insertServiceDescription(String serviceDesc) throws JSONException {
		ServiceUtil.isValid(serviceDesc);
		logger.debug("inserting service description"+serviceDesc);
		return Integer.toString(serviceDesc.hashCode());
	}
	
	
	/** updates the service description
	 *  "privileged method"
	 * @throws JSONException 
	 *  */
	@PUT
	
	//@Path("{id}/")
	public String updateServiceDescription(/*@PathParam("id") String id, String serviceDesc*/JSONObject serviceDesc) throws JSONException {
		logger.debug("updating service description: "+serviceDesc);
		return "id";
	}

	
	/** 
	 * updates the service description
	 * "privileged method"
	 *  
	 *  */
	@DELETE
	@Path("/{id}")
	public Response deleteServiceDescription(@PathParam("id") String id) {
		System.out.println("deleting the service description");
		//TODO deleting the service description
		return Response.noContent().build();
	}
	

}
