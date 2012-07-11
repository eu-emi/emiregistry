/**
 * 
 */
package eu.emi.emir.resource;

import java.security.cert.X509Certificate;
import java.util.Date;

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
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import eu.emi.emir.EMIRServer;
import eu.emi.emir.ServerProperties;
import eu.emi.emir.client.ServiceBasicAttributeNames;
import eu.emi.emir.client.util.Log;
import eu.emi.emir.core.ServiceAdminManager;
import eu.emi.emir.db.ExistingResourceException;
import eu.emi.emir.event.Event;
import eu.emi.emir.event.EventDispatcher;
import eu.emi.emir.event.EventTypes;
import eu.emi.emir.exception.UnknownServiceException;
import eu.emi.emir.security.Client;
import eu.emi.emir.util.ServiceUtil;

/**
 * Resource for the service providers (privileged) to manage their services
 * 
 * @author a.memon
 * @author g.szigeti
 */
@Path("/serviceadmin")
public class ServiceAdminResource {
	private static Logger logger = Log.getLogger(Log.EMIR_CORE,
			ServiceAdminResource.class);

	private ServiceAdminManager serviceAdmin;
	
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
	@Produces({ MediaType.APPLICATION_JSON })
	public JSONObject getServicebyID(@Context UriInfo infos)
			throws WebApplicationException, JSONException {
		final JSONObject result;
		try {
			String id = extractServiceEndpointIDFromUri(infos);
			if (logger.isDebugEnabled()) {
				logger.debug("Finding Service by Endpoint ID: "+id);	
			}
			result = serviceAdmin
					.findServiceByEndpointID(extractServiceEndpointIDFromUri(infos));			
		} catch (Exception e) {
			Log.logException("Error in finding SER by Endpoint ID", e, logger);
			JSONObject jErr = new JSONObject();
			jErr.put("error", e.getCause());
			throw new WebApplicationException(Response
					.status(Status.INTERNAL_SERVER_ERROR).entity(jErr).build());
		}

		return result;
	}
	
	private String extractServiceEndpointIDFromUri(UriInfo infos)
			throws IllegalArgumentException {
		MultivaluedMap<String, String> mm = infos.getQueryParameters();
		String attrName = ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID
				.getAttributeName();
		String key = (mm.containsKey(attrName)) ? attrName : "unknown";
		if (key == "unknown") {
			Log.logException("Error in getting Service by ID", new IllegalArgumentException("illegal argument"), logger);
			throw new IllegalArgumentException();
		}
		String value = mm
				.getFirst(ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID
						.getAttributeName());
		return value;
	}

	/**
	 * adding only one entry
	 * 
	 * @throws JSONException
	 * */

	public Response registerService(JSONObject serviceInfo)
			throws WebApplicationException, JSONException {
		Integer length = serviceInfo.length();
		if (length <= 0 || length > 100) {
			throw new WebApplicationException(Status.FORBIDDEN);
		}

		try {
			Client c = (Client) req.getAttribute("client");
			if (!EMIRServer.getServerSecurityProperties().isSslEnabled() && c == null) {
				c = Client.getAnonymousClient();
			}
			serviceInfo
					.put(ServiceBasicAttributeNames.SERVICE_OWNER_DN
							.getAttributeName(), c.getDistinguishedName());
			JSONObject res = serviceAdmin.addService(serviceInfo);
			return Response.ok(res).build();
		} catch (ExistingResourceException e) {
			return Response.status(Status.CONFLICT).entity(serviceInfo).build();
		} catch (Exception e) {
			JSONObject jErr = new JSONObject();
			jErr.put("error", e.getCause());
			throw new WebApplicationException(Response
					.status(Status.INTERNAL_SERVER_ERROR).entity(jErr).build());
		}
	}

	/**
	 * adding an array of json documents, where each item in the array is
	 * service endpoint information
	 * 
	 * @throws InterruptedException
	 *             TODO: polymorphic registrations: Supporting JSONObject as
	 *             well as Array
	 * @throws JSONException
	 * */
	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response registerServices(JSONArray serviceInfos)
			throws WebApplicationException, InterruptedException, JSONException {
		
		Long max = EMIRServer.getServerProperties().getLongValue(ServerProperties.PROP_RECORD_MAXIMUM);
		
		if (serviceInfos.length() > max) {
			return Response
					.status(Status.FORBIDDEN)
					.entity(new String(
							"Number of entries/json objects in the array must not exceed: "
									+ max)).build();
		}
		JSONObject serviceInfo = null;
		JSONArray arr = new JSONArray();
		JSONArray errorArray = new JSONArray();
		for (int i = 0; i < serviceInfos.length(); i++) {
			try {
				serviceInfo = serviceInfos.getJSONObject(i);
				Integer length = serviceInfo.length();
				if (length <= 0 || length > 100) {
					throw new WebApplicationException(Status.FORBIDDEN);
				}
				// Get the Endpoint URL and a time of the message from the entry
//				String serviceurl = serviceInfo
//						.getString(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
//								.getAttributeName());
				String serviceID = serviceInfo
						.getString(ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID
								.getAttributeName());
				String messageTime = "";
				if (serviceInfo.has(ServiceBasicAttributeNames.SERVICE_UPDATE_SINCE
								.getAttributeName())){
					messageTime = (serviceInfo
							.getJSONObject(ServiceBasicAttributeNames.SERVICE_UPDATE_SINCE
									.getAttributeName())).getString("$date");
				}
				
				Client c = (Client) req.getAttribute("client");
				if (!EMIRServer.getServerSecurityProperties().isSslEnabled() && c == null) {
					c = Client.getAnonymousClient();
				}
				JSONObject res = null;
				// let the admin add entries from others
				if (c.getRole().getName().equalsIgnoreCase("admin")) {
					if (!serviceInfo
							.has(ServiceBasicAttributeNames.SERVICE_OWNER_DN
									.getAttributeName())) {

						serviceInfo.put(
								ServiceBasicAttributeNames.SERVICE_OWNER_DN
										.getAttributeName(), c
										.getDistinguishedName());

					}
					if (serviceAdmin.checkMessageGenerationTime(messageTime, serviceID)){
						res = serviceAdmin.addService(serviceInfo);
					}

				} else {
					if (serviceAdmin.checkMessageGenerationTime(messageTime, serviceID)){
						// add if the owner is missing
						serviceInfo.put(ServiceBasicAttributeNames.SERVICE_OWNER_DN
								.getAttributeName(), c.getDistinguishedName());
						res = serviceAdmin.addService(serviceInfo);
					}
				}
				if (res != null){
					arr.put(res);
				}
				continue;
				// return Response.ok(res).build();
			} catch (ExistingResourceException e) {
				errorArray.put(serviceInfo);
			} catch (Exception e) {
				JSONObject jErr = new JSONObject();
				jErr.put("error", e.getCause());
				Log.logException("Error in registering the information", e, logger);
				throw new WebApplicationException(Response
						.status(Status.INTERNAL_SERVER_ERROR).entity(jErr)
						.build());
			}

		}
		if (arr.length() > 0){
			EventDispatcher.notifyRecievers(new Event(EventTypes.SERVICE_ADD, arr));
		}
		if (errorArray.length() > 0) {
			logger.warn("Conflict found in service registration");
			return Response.status(Status.CONFLICT).entity(errorArray).build();
		}
		return Response.ok(arr).build();
	}

	/**
	 * updating only one entry
	 * 
	 * @throws JSONException
	 * */

	public Response updateService(JSONObject serviceInfo)
			throws WebApplicationException, JSONException {
		try {
			Client c = (Client) req.getAttribute("client");
			if (!EMIRServer.getServerSecurityProperties().isSslEnabled() && c == null) {
				c = Client.getAnonymousClient();
			}
			String owner = c.getDistinguishedName();
			String url = serviceInfo
					.getString(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
							.getAttributeName());
			serviceInfo
					.put(ServiceBasicAttributeNames.SERVICE_OWNER_DN
							.getAttributeName(), c.getDistinguishedName());
			if (logger.isDebugEnabled()) {
				logger.debug("updating service by url: " + url + ", Owned by: "
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
					logger.debug("Service with url: "
							+ serviceInfo
									.getString(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
											.getAttributeName())
							+ " does not exist.");
				}
				return Response
						.status(Status.UNAUTHORIZED)
						.entity("Access denied for DN - " + owner
								+ " to update service with the URL - " + url)
						.build();
			}

		} catch (Exception e) {
			JSONObject jErr = new JSONObject();
			jErr.put("error", e.getCause());
			throw new WebApplicationException(Response
					.status(Status.INTERNAL_SERVER_ERROR).entity(jErr).build());
		}
	}

	/**
	 * updating array of entries
	 * 
	 * @throws JSONException
	 * */

	@PUT
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response updateServices(JSONArray serviceInfos)
			throws WebApplicationException, JSONException {
		try {
			JSONArray arr = new JSONArray();
			JSONArray errorArray = new JSONArray();
			Client c = (Client) req.getAttribute("client");
			if (!EMIRServer.getServerSecurityProperties().isSslEnabled() && c == null) {
				c = Client.getAnonymousClient();
			}
			for (int i = 0; i < serviceInfos.length(); i++) {
				JSONObject serviceInfo = serviceInfos.getJSONObject(i);				
				String owner = c.getDistinguishedName();
				String sendpointID = serviceInfo
						.getString(ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID
								.getAttributeName());
				serviceInfo.put(ServiceBasicAttributeNames.SERVICE_OWNER_DN
						.getAttributeName(), c.getDistinguishedName());
				if (logger.isDebugEnabled()) {
					logger.debug("updating service by ID: " + sendpointID
							+ ", Owned by: " + owner);
				}
				
				String messageTime = "";
				if (serviceInfo.has(ServiceBasicAttributeNames.SERVICE_UPDATE_SINCE
								.getAttributeName())){
					messageTime = (serviceInfo
							.getJSONObject(ServiceBasicAttributeNames.SERVICE_UPDATE_SINCE
									.getAttributeName())).getString("$date");
				}
				if (c.getRole().getName().equalsIgnoreCase("admin")
						&& serviceAdmin.checkMessageGenerationTime(messageTime, sendpointID)) {
					// let the admin update any service
					JSONObject res;
					try {
						res = serviceAdmin.updateService(serviceInfo);
						arr.put(res);
					} catch (UnknownServiceException e) {
						return Response.status(Status.NOT_FOUND).build();
					} catch (WebApplicationException e) {
						errorArray.put(serviceInfo);
					}
					continue;

				} else if (owner != null
						&& serviceAdmin
								.checkOwner(
										owner,
										serviceInfo
												.getString(ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID
														.getAttributeName()))
								&& serviceAdmin.checkMessageGenerationTime(messageTime, sendpointID)) {
					JSONObject res;
					try {
						res = serviceAdmin.updateService(serviceInfo);
						arr.put(res);
					} catch (UnknownServiceException e) {
						return Response.status(Status.NOT_FOUND).build();
					} catch (WebApplicationException e) {
						errorArray.put(serviceInfo);
					}
					continue;
					// return Response.ok(res).build();
				} else {
					if (logger.isDebugEnabled()) {
						logger.debug("Service with endpointID: "
								+ serviceInfo
										.getString(ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID
												.getAttributeName())
								+ " does not exist or the update message is too old.");
					}
					return Response
							.status(Status.UNAUTHORIZED)
							.entity("Access denied for DN - " + owner
									+ " to update service with the endpointID - "
									+ sendpointID).build();
				}
			}
			if (arr.length() > 0){
				EventDispatcher.notifyRecievers(new Event(
						EventTypes.SERVICE_UPDATE, arr));
			}
			if (errorArray.length() > 0) {
				return Response.status(Status.CONFLICT).entity(errorArray)
						.build();
			}
			return Response.ok(arr).build();
		} catch (Exception e) {
			Log.logException("Error in updating the services", e, logger);
			JSONObject jErr = new JSONObject();
			jErr.put("error", e.getCause());
			throw new WebApplicationException(Response
					.status(Status.INTERNAL_SERVER_ERROR).entity(jErr).build());
		}
	}

	/**
	 * Deleting the service description
	 * 
	 * @param infos
	 *            contains a ..?SERVICE_ENDPOINT_ID=http://serviceurl
	 * @throws JSONException
	 * */
	@DELETE
	public Response deleteService(@Context UriInfo infos)
			throws WebApplicationException, JSONException {
		String sendpointID = null;
		try {
			Client c = (Client) req.getAttribute("client");
			if (!EMIRServer.getServerSecurityProperties().isSslEnabled() && c == null) {
				c = Client.getAnonymousClient();
			}
			String owner = c.getDistinguishedName();
			sendpointID = extractServiceEndpointIDFromUri(infos);
			String messageTime = extractServiceDateFromUri(infos);
			if (EMIRServer.getServerProperties().isGlobalEnabled() &&
						messageTime == "unknown") {
				// New entry and message generation time need, it is come from one DSR
				messageTime = ServiceUtil.toUTCFormat(new Date());
			}
			logger.debug("deleting service by ID: " + sendpointID
					+ ", Owned by: " + owner);
			if (c.getRole().getName().equalsIgnoreCase("admin")) {
				// let the admin delete everything
				serviceAdmin.removeService(sendpointID, messageTime);
			} else if ((owner != null)
					&& (serviceAdmin.checkOwner(owner, sendpointID))
						&& (serviceAdmin.checkMessageGenerationTime(messageTime, sendpointID))) {

				serviceAdmin.removeService(sendpointID, messageTime);

			} else {
				return Response
						.status(Status.UNAUTHORIZED)
						.entity("Access denied for DN - " + owner
								+ " to remove service with the ID - "
								+ sendpointID).build();
			}

		} catch (IllegalArgumentException e) {
			Log.logException("Missing/Invalid query parameter: The delete request must contain a query parameter: /serviceadmin?"
					+ ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID
					.getAttributeName() + " = <SERVICE ENDPOINT ID>", e, logger);
			return Response
					.status(Status.BAD_REQUEST)
					.entity("Missing/Invalid query parameter: The delete request must contain a query parameter: /serviceadmin?"
							+ ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID
									.getAttributeName() + " = <SERVICE ENDPOINT ID>")
					.build();
		} catch (Exception e) {
			Log.logException("Error in deleting the service",e,logger);
			JSONObject jErr = new JSONObject();
			jErr.put("error", e.getCause());
			throw new WebApplicationException(Response
					.status(Status.INTERNAL_SERVER_ERROR).entity(jErr).build());
		}
		return Response.ok().build();
	}
	
	private String extractServiceDateFromUri(UriInfo infos) throws IllegalArgumentException{
		MultivaluedMap<String, String> mm = infos.getQueryParameters();
		String attrName = ServiceBasicAttributeNames.SERVICE_UPDATE_SINCE
				.getAttributeName();
		String key = (mm.containsKey(attrName)) ? attrName : "unknown";
		if (key == "unknown") {
			return "unknown";
		}
		String value = mm
				.getFirst(ServiceBasicAttributeNames.SERVICE_UPDATE_SINCE
						.getAttributeName());
		return value;
	}
	
}
