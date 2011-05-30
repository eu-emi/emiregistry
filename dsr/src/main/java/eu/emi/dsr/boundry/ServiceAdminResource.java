/**
 * 
 */
package eu.emi.dsr.boundry;

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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.eclipse.jetty.util.ajax.JSON;
import org.json.JSONException;
import org.json.JSONObject;

import eu.emi.dsr.util.Log;
import eu.emi.dsr.util.ServiceUtil;

/**
 * Resource for the service providers (privileged) to manage their services
 * 
 * @author a.memon
 */
@Path("/serviceadmin")
public class ServiceAdminResource {
	private static Logger logger = Log.getLogger(Log.DSR,
			ServiceAdminResource.class);

	@GET
	public JSONObject getServicebyUrl(@Context UriInfo infos)
			throws WebApplicationException {
		String value = getServiceUrlFromUri(infos);
		JSONObject j = null;
		logger.info(String.format("%s = %s", "serviceurl", value));
		Map<String, String> map = new HashMap<String, String>();
		map.put("serviceurl", "http://someurl");
		map.put("servicetype", "sometype");
		j = new JSONObject(map);

		return j;
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

	/**
	 * Probe the id/handle to the given service url. The request is reqpresented
	 * as a url string e.g.
	 * http(s)://hostname:port/serviceadmin/probeid?serviceurl=http://service1
	 * 
	 * @return the url of the service e.g.:
	 *         http(s)://hostname:port/serviceadmin/serviceid
	 * */
	@GET
	@Path("/probeid")
	public String probeId(@Context UriInfo infos)
			throws WebApplicationException {
		MultivaluedMap<String, String> mm = infos.getQueryParameters();
		String key = (mm.containsKey("serviceurl")) ? "serviceurl" : "unknown";
		if (key == "unknown") {
			throw new WebApplicationException(new IllegalArgumentException(
					"invalid param"));
		}
		String value = mm.getFirst("serviceurl");

		logger.info(String.format("%s = %s", key, value));

		return "https://hostname:port/serviceadmin/serviceid";
	}

	@POST
	public JSONObject registerService(String serviceInfo)
			throws WebApplicationException {
		// ServiceUtil.isValid(serviceInfo);
		JSONObject j = new JSONObject();

		try {
			j.append("serviceurl", "http://");
			logger.info("adding service with url: "
					+ new JSONObject(serviceInfo).getString("serviceurl"));
		} catch (JSONException e) {
			throw new WebApplicationException();
		}
		return j;
	}
	
	
	@PUT	
	public JSONObject updateService(String serviceDescription)
			throws WebApplicationException {
		// ServiceUtil.isValid(serviceInfo);
		JSONObject j = new JSONObject();

		try {
			j.append("serviceurl", "http://");
			logger.info("updating the service with url: "
					+ new JSONObject(serviceDescription).getString("serviceurl"));
		} catch (JSONException e) {
			throw new WebApplicationException();
		}
		return j;
	}

	/**
	 * Deleting the service description
	 * @param infos contains a ../serviceurl=http://serviceurl
	 * */
	@DELETE
	public void deleteService(@Context UriInfo infos) {
		String serviceurl = getServiceUrlFromUri(infos);
		logger.info("deleting the service with url: "+serviceurl);
	}

}
