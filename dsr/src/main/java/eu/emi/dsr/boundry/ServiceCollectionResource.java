/**
 * 
 */
package eu.emi.dsr.boundry;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import eu.emi.dsr.core.ServiceColManager;
import eu.emi.dsr.util.Log;

/**
 * @author a.memon
 * 
 */
@Path("/services")
public class ServiceCollectionResource {
	Logger logger = Log.getLogger(Log.DSR, ServiceCollectionResource.class);
	ServiceColManager col;

	/**
	 * 
	 */
	public ServiceCollectionResource() {
		col = new ServiceColManager();
	}

	/** query method */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/refs")
	public JSONObject getServiceReferences() throws WebApplicationException {
		JSONObject o = null;
		try {
			o = col.getServiceReferences();
		} catch (JSONException e) {
			throw new WebApplicationException(e);
		}

		return o;
	}

	/** query method */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/types")
	public JSONObject getServiceTypes() throws WebApplicationException {
		JSONObject o = null;
		try {
			o = col.getAllServiceTypes();
		} catch (JSONException e) {
			throw new WebApplicationException(e);
		}

		return o;
	}

	/** query method */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/types/{servicetype:.+}")
	public JSONObject getServiceByType(@PathParam("servicetype") String type)
			throws WebApplicationException {
		JSONObject o = null;
		try {
			o = col.getServicesByType(type);
		} catch (JSONException e) {
			throw new WebApplicationException(e);
		}

		return o;
	}

	/** query method 
	 * @throws JSONException */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/query")	
	public JSONObject queryServiceCollection(@Context UriInfo infos)
			throws WebApplicationException {
		MultivaluedMap<String, String> mm = infos.getQueryParameters();
		
		JSONObject jo = new JSONObject(mm);
		JSONObject lst = null;
		try {
			lst =  col.queryServiceCollection(jo);
			 
		} catch (JSONException e) {
			throw new WebApplicationException(e);
		}
		return lst;
	}
}
