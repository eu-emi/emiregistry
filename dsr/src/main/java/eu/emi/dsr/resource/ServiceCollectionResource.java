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
import javax.ws.rs.PathParam;
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
import eu.emi.dsr.db.PersistentStoreFailureException;
import eu.emi.dsr.db.QueryException;
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
		} catch (JSONException e) {
			throw new WebApplicationException(e);
		} catch (QueryException e) {
			throw new WebApplicationException(e);
		} catch (PersistentStoreFailureException e) {
			throw new WebApplicationException(e);
		}

		return o;
	}

	/** query method */
	@GET
	@Path("/type/{servicetype:.+}")
	public JSONArray getServiceByType(@PathParam("servicetype") String type)
			throws WebApplicationException {
		JSONArray o = null;
		try {
			o = col.getServicesByType(type);
		} catch (JSONException e) {
			throw new WebApplicationException(e);
		} catch (QueryException e) {
			throw new WebApplicationException(e);
		} catch (PersistentStoreFailureException e) {
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
		for (Iterator iterator = s.iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
			m.put(key, queryParams.getFirst(key));
		}

		JSONArray jArr = null;

		try {
			jArr = col.query(m);
		} catch (QueryException e) {
			new WebApplicationException(e);
		} catch (PersistentStoreFailureException e) {
			new WebApplicationException(e);
		}

		return jArr;
	}

	@GET
	@Path("/pagedquery")
	public JSONObject pagedQuery(@Context UriInfo ui)
			throws WebApplicationException {
		MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
		Set<String> s = queryParams.keySet();
		Map<String, Object> m = new HashMap<String, Object>();
		for (Iterator iterator = s.iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
			m.put(key, queryParams.getFirst(key));
		}

		JSONObject jArr = null;

		try {
			jArr = col.pagedQuery(m);
		} catch (JSONException e) {
			new WebApplicationException(e);
		}

		return jArr;
	}
}
