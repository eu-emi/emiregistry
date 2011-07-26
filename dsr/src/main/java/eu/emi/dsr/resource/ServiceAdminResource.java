/**
 * 
 */
package eu.emi.dsr.resource;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import eu.emi.dsr.core.ServiceAdminManager;
import eu.emi.dsr.core.ServiceBasicAttributeNames;
import eu.emi.dsr.core.ServiceManagerFactory;
import eu.emi.dsr.db.NonExistingResourceException;
import eu.emi.dsr.db.PersistentStoreFailureException;
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
		// serviceAdmin = ServiceManagerFactory.getServiceAdminManager();
		serviceAdmin = new ServiceAdminManager();
	}

	@GET
	public JSONObject getServicebyUrl(@Context UriInfo infos)
			throws WebApplicationException {
		logger.debug("getting service by url");
		final JSONObject result;
		try {
			result = serviceAdmin
					.findServiceByUrl(extractServiceUrlFromUri(infos));
		} catch (NonExistingResourceException e) {
			return null;
		} catch (PersistentStoreFailureException e) {
			throw new WebApplicationException(e);
		}

		return result;
	}

	private static String extractServiceUrlFromUri(UriInfo infos) {
		MultivaluedMap<String, String> mm = infos.getQueryParameters();
		String attrName = ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
				.getAttributeName();
		String key = (mm.containsKey(attrName)) ? attrName : "unknown";
		if (key == "unknown") {
			throw new WebApplicationException(new IllegalArgumentException(
					"invalid param"));
		}
		String value = mm
				.getFirst(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
						.getAttributeName());
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
			if (logger.isDebugEnabled()) {
				logger.debug("updating service by url: "
						+ serviceInfo
								.getString(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
										.getAttributeName()));
			}
			serviceAdmin.updateService(serviceInfo);

		} catch (InvalidServiceDescriptionException e) {
			throw new WebApplicationException(e);
		} catch (JSONException e) {
			throw new WebApplicationException(e);
		} catch (UnknownServiceException e) {
			return Response.noContent().build();
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

			String serviceurl = extractServiceUrlFromUri(infos);
			logger.debug("deleting service by url: " + serviceurl);
			serviceAdmin.removeService(serviceurl);
		} catch (UnknownServiceException e) {
			return Response.noContent().build();
		}
		return Response.ok().build();
	}
}
