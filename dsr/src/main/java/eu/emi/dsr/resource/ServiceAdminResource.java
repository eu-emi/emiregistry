/**
 * 
 */
package eu.emi.dsr.resource;

import java.security.cert.X509Certificate;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
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

	private String extractServiceUrlFromUri(UriInfo infos) {
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

	/**
	 * adding array of entries
	 * */
	
	@POST
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
	public Response updateService(JSONObject serviceInfo)
			throws WebApplicationException {
		try {
			Client c = (Client) req.getAttribute("client");
			String owner = c.getDistinguishedName();
			serviceInfo
					.put(ServiceBasicAttributeNames.SERVICE_OWNER
							.getAttributeName(), c.getDistinguishedName());
			if (logger.isDebugEnabled()) {
				logger.debug("updating service by url: "
						+ serviceInfo
								.getString(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
										.getAttributeName()) + ", Owned by: "
						+ owner);
			}

			if (owner != null
					&& serviceAdmin
							.checkOwner(
									owner,
									serviceInfo
											.getString(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
													.getAttributeName()))) {
				JSONObject res = serviceAdmin.updateService(serviceInfo);
				return Response.ok(res).build();
			} else {
				return Response.status(Status.UNAUTHORIZED).build();
			}

		} catch (InvalidServiceDescriptionException e) {
			throw new WebApplicationException(e);
		} catch (JSONException e) {
			throw new WebApplicationException(e);
		} catch (UnknownServiceException e) {
			return Response.noContent().build();
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
	 *            contains a ../serviceurl=http://serviceurl
	 * */
	@DELETE
	public Response deleteService(@Context UriInfo infos) {

		try {
			Client c = (Client) req.getAttribute("client");
			String owner = c.getDistinguishedName();
			String serviceurl = extractServiceUrlFromUri(infos);
			logger.debug("deleting service by url: " + serviceurl
					+ ", Owned by: " + owner);
			if (owner != null && serviceAdmin.checkOwner(owner, serviceurl)) {
				serviceAdmin.removeService(serviceurl);
			} else {
				return Response.status(Status.UNAUTHORIZED).build();
			}

		} catch (UnknownServiceException e) {
			return Response.noContent().build();
		} catch (QueryException e) {
			throw new WebApplicationException(e);
		} catch (PersistentStoreFailureException e) {
			throw new WebApplicationException(e);
		}
		return Response.ok().build();
	}
}
