/**
 * 
 */
package eu.emi.dsr.boundry;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import eu.emi.dsr.core.ServiceManagerFactory;
import eu.emi.dsr.core.ServiceAdminManager;
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

	private static final ServiceAdminManager serviceAdmin = ServiceManagerFactory
			.getServiceAdminManager();

	@GET
	public JSONObject getServicebyUrl(@Context UriInfo infos)
			throws WebApplicationException {

		final JSONObject result;

		try {
			result = serviceAdmin.findServiceByUrl(getServiceUrlFromUri(infos));
		} catch (UnknownServiceException e) {
			throw new WebApplicationException(e);
		}


		return result;
	}

	private static String getServiceUrlFromUri(UriInfo infos) {
		MultivaluedMap<String, String> mm = infos.getQueryParameters();
		String key = (mm.containsKey("serviceurl")) ? "serviceurl" : "unknown";
		if (key == "unknown") {
			throw new WebApplicationException(new IllegalArgumentException(
					"invalid param"));
		}
		String value = mm.getFirst("serviceurl");
		return value;
	}

	

	@POST
	public String registerService(JSONObject serviceInfo)
			throws WebApplicationException {
		String id = null;
		try {
		
			id = serviceAdmin.addService(serviceInfo);
		} catch (InvalidServiceDescriptionException e) {
			throw new WebApplicationException(e);
		} catch (JSONException e) {
			throw new WebApplicationException(e);
		}		
		return id;
	}

	@PUT	
	public String updateService(JSONObject serviceInfo)
			throws WebApplicationException {
		String id = null;
		try {
			
			id = serviceAdmin.updateService(serviceInfo);
		} catch (InvalidServiceDescriptionException e) {
			throw new WebApplicationException(e);
		} catch (JSONException e) {
			throw new WebApplicationException(e);
		} catch (UnknownServiceException e) {
			throw new WebApplicationException(e);
		}		
		return id;
	}

	/**
	 * Deleting the service description
	 * 
	 * @param infos
	 *            contains a ../serviceurl=http://serviceurl
	 * */
	@DELETE
	public void deleteService(@Context UriInfo infos) {
		String serviceurl = getServiceUrlFromUri(infos);
		logger.info("deleting the service with url: " + serviceurl);
	}

}
