/**
 * 
 */
package eu.emi.dsr.boundry;

import java.net.URI;
import java.net.URISyntaxException;
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
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.ext.jaxrs.internal.core.ResponseBuilderImpl;
import org.restlet.ext.jaxrs.internal.provider.WebAppExcMapper;

import eu.emi.dsr.util.ServiceUtil;


/**
 * @author a.memon
 *
 */
@Path("/service")
public class ServiceResource{
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{id}")
	public JSONObject getServiceDescription(@PathParam("id") String id) throws WebApplicationException {
		if ((id == null)||(id.isEmpty())) {
			throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
		}
		System.out.println("getting service description");
		System.out.println("given parameter: "+id);
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
		System.out.println("inserting service description");
		System.out.println("inserting the service: "+serviceDesc);
		return Integer.toString(serviceDesc.hashCode());
	}
	
	
	/** updates the service description
	 *  "privileged method"
	 * @throws JSONException 
	 *  */
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/{id}")
	public Response updateServiceDescription(String serviceDesc) throws JSONException {
		ServiceUtil.isValid(serviceDesc);
		System.out.println("inserting service description");
		System.out.println("inserting the service: "+serviceDesc);
		//TODO updating the service description
		return Response.noContent().build();
	}

	
	/** 
	 * updates the service description
	 * "privileged method"
	 *  
	 *  */
	@DELETE
	@Path("/{id}")
	public Response deleteServiceDescription() {
		System.out.println("deleting the service description");
		//TODO deleting the service description
		return Response.noContent().build();
	}
	

}
