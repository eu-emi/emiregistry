/**
 * 
 */
package eu.emi.dsr.resource;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import eu.emi.dsr.core.ServiceColManager;
import eu.emi.dsr.util.Log;
import eu.eu_emi.emiregistry.QueryResult;

/**
 * @author a.memon
 * TODO support for glue2 in paged query
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
	@Path("/urls")
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
	public JSONArray getServiceTypes() throws WebApplicationException {
		JSONArray o = null;
		try {
			o = col.getDistinctTypes();
		} catch (Exception e) {
			throw new WebApplicationException(e);
		} 

		return o;
	}


	@GET
	@Path("/query")
	public JSONArray query(@Context UriInfo ui) throws WebApplicationException {
		MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
		Set<String> s = queryParams.keySet();
		Map<String, Object> m = new HashMap<String, Object>();
		for (Iterator<String> iterator = s.iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
			m.put(key, queryParams.getFirst(key));
		}

		JSONArray jArr = null;

		try {
			jArr = col.query(m);
		} catch (Exception e) {
			throw new WebApplicationException(e);
		} 

		return jArr;
	}

	@GET
	@Path("/query.xml")
	@Produces({MediaType.APPLICATION_XML,MediaType.TEXT_XML})
	public QueryResult queryXml(@Context UriInfo ui)
			throws WebApplicationException {
		MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
		Set<String> s = queryParams.keySet();
		Map<String, Object> m = new HashMap<String, Object>();
		for (Iterator<String> iterator = s.iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
			m.put(key, queryParams.getFirst(key));
		}

		QueryResult qr = null;
		try {
			qr = col.queryGlue2(m);
		} catch (Exception e) {
			throw new WebApplicationException(e);
		} 

		
		return qr;
	}

	@GET
	@Path("/pagedquery")
	public JSONObject pagedQuery(@Context UriInfo ui)
			throws WebApplicationException {
		MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
		Set<String> s = queryParams.keySet();
		Map<String, Object> m = new HashMap<String, Object>();
		for (Iterator<String> iterator = s.iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
			m.put(key, queryParams.getFirst(key));
		}

		JSONObject jArr = null;

		try {
			jArr = col.pagedQuery(m);
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}

		return jArr;
	}
}
