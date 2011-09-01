/**
 * 
 */
package eu.emi.dsr.resource;

import java.security.cert.X509Certificate;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
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
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import eu.emi.dsr.core.ServiceAdminManager;
import eu.emi.dsr.core.ServiceBasicAttributeNames;
import eu.emi.dsr.db.ExistingResourceException;
import eu.emi.dsr.db.NonExistingResourceException;
import eu.emi.dsr.db.PersistentStoreFailureException;
import eu.emi.dsr.db.QueryException;
import eu.emi.dsr.exception.InvalidServiceDescriptionException;
import eu.emi.dsr.exception.UnknownServiceException;
import eu.emi.dsr.security.Client;
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
	@Context
	HttpServletRequest req;

	/**
	 * 
	 */
	public ServiceAdminResource() {
		// serviceAdmin = ServiceManagerFactory.getServiceAdminManager();
		serviceAdmin = new ServiceAdminManager();
	}

	protected String getUserPrincipalName() {
		String p = null;
		if (req.isSecure()) {
			X509Certificate[] cert = (X509Certificate[]) req
					.getAttribute("javax.servlet.request.X509Certificate");
			p = cert[0].getSubjectDN().getName();
		} else {
			return "";
		}

		return p;
	}

	@GET
	@Produces({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON})
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

	/**
	 * adding array of entries
	 * */
	
	
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public Response registerService(JSONObject serviceInfo)
			throws WebApplicationException{
		Integer length = serviceInfo.length();
		if (length <= 0 || length > 100) {
			throw new WebApplicationException(Status.FORBIDDEN);
		}

		
		try {
			Client c = (Client) req.getAttribute("client");
			serviceInfo.put(ServiceBasicAttributeNames.SERVICE_OWNER
					.getAttributeName(), c.getDistinguishedName());
			JSONObject res = serviceAdmin.addService(serviceInfo);
			return Response.ok(res).build();
		} catch (JSONException e) {
			throw new WebApplicationException(e);
		} catch (InvalidServiceDescriptionException e) {
			throw new WebApplicationException(e);
		} catch (NullPointerException e){
			throw new WebApplicationException(e);
		} catch (ExistingResourceException e) {
			return Response.status(Status.CONFLICT).entity(serviceInfo).build();
		}
	}

	@PUT
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
	public Response updateService(JSONObject serviceInfo)
			throws WebApplicationException {
		try {
			Client c = (Client) req.getAttribute("client");
			String owner = c.getDistinguishedName();
			String url = serviceInfo
			.getString(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
					.getAttributeName());
			serviceInfo
					.put(ServiceBasicAttributeNames.SERVICE_OWNER
							.getAttributeName(), c.getDistinguishedName());
			if (logger.isDebugEnabled()) {
				logger.debug("updating service by url: "
						+ url + ", Owned by: "
						+ owner);
			}

			if (owner != null
					&& serviceAdmin
							.checkOwner(
									owner,
									serviceInfo
											.getString(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
													.getAttributeName()))) {
				JSONObject res;
				try {
					res = serviceAdmin.updateService(serviceInfo);					
				} catch (UnknownServiceException e) {
					return Response.status(Status.NOT_FOUND).build();
				}
				return Response.ok(res).build();
			} else {
				if (logger.isDebugEnabled()) {
					logger.debug("Service with url: "+serviceInfo.getString(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL.getAttributeName())+" does not exist.");
				}
				return Response.status(Status.UNAUTHORIZED).entity("Access denied for DN - "+owner+" to update service with the URL - "+url).build();
			}

		} catch (InvalidServiceDescriptionException e) {
			throw new WebApplicationException(e);
		} catch (JSONException e) {
			throw new WebApplicationException(e);
		} catch (QueryException e) {
			throw new WebApplicationException(e);
		} catch (PersistentStoreFailureException e) {
			throw new WebApplicationException(e);
		}
	}

	/**
	 * Deleting the service description
	 * 
	 * @param infos
	 *            contains a ..?SERVICE_ENDPOINT_URL=http://serviceurl
	 * */
	@DELETE
	public Response deleteService(@Context UriInfo infos) {
		String serviceurl = null;
		try {
			Client c = (Client) req.getAttribute("client");
			String owner = c.getDistinguishedName();
			serviceurl = extractServiceUrlFromUri(infos);
			logger.debug("deleting service by url: " + serviceurl
					+ ", Owned by: " + owner);
			if (owner != null && serviceAdmin.checkOwner(owner, serviceurl)) {
				serviceAdmin.removeService(serviceurl);
			} else {
				return Response.status(Status.UNAUTHORIZED).entity("Access denied for DN - "+owner+" to update service with the URL - "+serviceurl).build();
			}

		} catch (UnknownServiceException e) {
			return Response.status(Status.NOT_FOUND).entity("Service with URL: "+serviceurl+" does not exist").build();
		} catch (QueryException e) {
			throw new WebApplicationException(e);
		} catch (PersistentStoreFailureException e) {
			throw new WebApplicationException(e);
		} catch(IllegalArgumentException e){
			return Response.status(Status.BAD_REQUEST).entity("Missing/Invalid query parameter: The delete request must contain a query parameter: /serviceadmin?"+ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL.getAttributeName()+" = <SERVICE URL>").build();
		}
		return Response.ok().build();
	}
}
