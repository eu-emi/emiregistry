/**
 * 
 */
package eu.emi.dsr.resource;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import eu.emi.dsr.core.ServiceAdminManager;
import eu.emi.dsr.core.ServiceBasicAttributeNames;
import eu.emi.dsr.core.ServiceManagerFactory;
import eu.emi.dsr.exception.InvalidServiceDescriptionException;
import eu.emi.dsr.exception.UnknownServiceException;
import eu.emi.dsr.util.Log;

/**
 * Resource for the service providers (privileged) to manage their services
 * 
 * @author a.memon
 */
@Path("/serviceadmin")
public class ServiceAdminResource {
	private static Logger logger = Log.getLogger(Log.DSR,
			ServiceAdminResource.class);

	private final ServiceAdminManager serviceAdmin;

	/**
	 * 
	 */
	public ServiceAdminResource() {
		serviceAdmin = ServiceManagerFactory.getServiceAdminManager();
	}

	@GET	
	public JSONObject getServicebyUrl(@Context UriInfo infos)
			throws WebApplicationException{
		logger.debug("getting service by url");
		final JSONObject result;
		result = serviceAdmin.findServiceByUrl(extractServiceUrlFromUri(infos));
		
		return result;
	}

	private static String extractServiceUrlFromUri(UriInfo infos) {
		MultivaluedMap<String, String> mm = infos.getQueryParameters();
		String attrName = ServiceBasicAttributeNames.SERVICE_URL
				.getAttributeName();
		String key = (mm.containsKey(attrName)) ? attrName : "unknown";
		if (key == "unknown") {
			throw new WebApplicationException(new IllegalArgumentException(
					"invalid param"));
		}
		String value = mm.getFirst(ServiceBasicAttributeNames.SERVICE_URL.getAttributeName());
		return value;
	}

	@POST
	public Response registerService(JSONObject serviceInfo)
			throws WebApplicationException {
		try {
			serviceAdmin.addService(serviceInfo);
		} catch (InvalidServiceDescriptionException e) {
			throw new WebApplicationException(e);
		} catch (JSONException e) {
			throw new WebApplicationException(e);
		} 
		
		return Response.ok().build();
	}

	@PUT
	public Response updateService(JSONObject serviceInfo)
			throws WebApplicationException {
		try {
			serviceAdmin.updateService(serviceInfo);
		} catch (InvalidServiceDescriptionException e) {
			throw new WebApplicationException(e);
		} catch (JSONException e) {
			throw new WebApplicationException(e);
		} catch (UnknownServiceException e) {
			throw new WebApplicationException(e);
		} 
		return Response.ok().build();
	}

	/**
	 * Deleting the service description
	 * 
	 * @param infos
	 *            contains a ../serviceurl=http://serviceurl
	 * */
	@DELETE
	public Response deleteService(@Context UriInfo infos) {
		
		try {
			logger.debug("deleting service by url");
			String serviceurl = extractServiceUrlFromUri(infos);
			serviceAdmin.removeService(serviceurl);
		} catch (UnknownServiceException e) {
			throw new WebApplicationException(e);
		} 
		return Response.ok().build();
	}

}
