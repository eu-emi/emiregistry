/**
 * 
 */
package eu.emi.emir.resource;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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

import eu.emi.emir.client.util.Log;
import eu.emi.emir.core.ServiceColManager;
import eu.emi.emir.util.ExceptionUtil;
import eu.eu_emi.emiregistry.QueryResult;

/**
 * @author a.memon TODO support for glue2 in paged query
 */
@Path("/services")
public class ServiceCollectionResource {
	Logger logger = Log.getLogger(Log.EMIR_CORE, ServiceCollectionResource.class);
	private ServiceColManager col;

	/**
	 * 
	 */
	public ServiceCollectionResource() {
			col = new ServiceColManager();
	}

	/** query method 
	 * @throws JSONException */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/urls")
	public Response getServiceEndPoints() throws WebApplicationException, JSONException {
		JSONArray jArr = null;
		try {
			jArr = col.getServiceReferences();
		} catch (Exception e) {
			Log.logException("Error in doing query for service urls in JSON format", e, logger);
			JSONArray err = new JSONArray();
			err.put(ExceptionUtil.toJson(e));
			throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).entity(err).build());
		}
		if (jArr.length() == 0) {
			return Response.ok(jArr).status(Status.NO_CONTENT).build();
		}

		return Response.ok(jArr).build();
	}

	/** query method 
	 * @throws JSONException */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/types")
	public Response getServiceTypes() throws WebApplicationException, JSONException {
		JSONArray jArr = null;
		try {
			jArr = col.getDistinctTypes();
		} catch (Exception e) {
			Log.logException("Error in doing query for service types in JSON format", e, logger);
			JSONArray err = new JSONArray();
			err.put(ExceptionUtil.toJson(e));
			throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).entity(err).build());
		}
		if (jArr.length() == 0) {
			return Response.ok(jArr).status(Status.NO_CONTENT).build();
		}

		return Response.ok(jArr).build();

	}

	/**
	 * Plain query method, invoked only if the MIME type is defined as application/json
	 * 
	 * */
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response queryWithParamsForJSON(@Context UriInfo ui) throws WebApplicationException, JSONException {

		MultivaluedMap<String, String> queryParams = ui.getQueryParameters();

		Set<String> s = queryParams.keySet();
		Map<String, Object> m = new HashMap<String, Object>();
		for (Iterator<String> iterator = s.iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
			m.put(key, queryParams.getFirst(key));
		}

		JSONArray jArr = null;
		
		try {
			jArr = col.queryForJSON(m);
			
		} catch (Exception e) {
			Log.logException("Error in doing query for services in JSON format", e, logger);
			JSONArray err = new JSONArray();
			err.put(ExceptionUtil.toJson(e));
			throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).entity(err).build());
		}

		if (jArr.length() == 0) {
			return Response.ok(jArr).status(Status.NO_CONTENT).build();
		}
		return Response.ok(jArr).build();
		
	}	
	/**
	 * Plain query method, invoked only if the MIME type is defined as application/xml
	 * 
	 * */

	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.TEXT_XML })
	public Response queryWithParamsForXML(@Context UriInfo ui)
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
			qr = col.queryForXML(m);
		} catch (Exception e) {
			Log.logException("Error in doing query for services in XML format", e, logger);
			throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).entity("<error>"+e.getCause().toString()+"</error>").build());
		}
		if (qr.getCount() == BigInteger.ZERO) {
			return Response.ok(qr).status(Status.NO_CONTENT).build();

		}

		return Response.ok(qr).build();
	}

	/**
	 * Query using the advanced MongoDB queries
	 * @param queryDocument
	 *            the JSON document defining the query according to the MongoDB
	 *            Syntax
	 * @throws JSONException 
	 * */
	@POST
	@Produces({MediaType.APPLICATION_JSON})
	@Consumes(MediaType.APPLICATION_JSON)
	public Response richQueryForJSON(JSONObject queryDocument, @Context UriInfo ui)
			throws WebApplicationException, JSONException {
		MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
		Set<String> s = queryParams.keySet();
		Map<String, Object> m = new HashMap<String, Object>();
		for (Iterator<String> iterator = s.iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
			m.put(key, queryParams.getFirst(key));
		}
		JSONArray jArr = null;
		try {
			jArr = col.queryForJSON(queryDocument, m);
		} catch (Exception e) {
			Log.logException("Error in doing query for services in JSON format", e, logger);
			JSONArray err = new JSONArray();
			err.put(ExceptionUtil.toJson(e));
			throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).entity(err).build());
		}

		if (jArr.length() == 0) {
			return Response.ok(jArr).status(Status.NO_CONTENT).build();
		}

		return Response.ok(jArr).build();
	}
	
	/**
	 * @param queryDocument
	 *            the JSON document defining the query according to the MongoDB
	 *            Syntax
	 * */
	@POST
	@Produces(MediaType.APPLICATION_XML)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response richQueryForXML(JSONObject queryDocument, @Context UriInfo ui)
			throws WebApplicationException {
		MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
		Set<String> s = queryParams.keySet();
		Map<String, Object> m = new HashMap<String, Object>();
		for (Iterator<String> iterator = s.iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
			m.put(key, queryParams.getFirst(key));
		}
		QueryResult jArr = null;
		try {
			jArr = col.queryForXML(queryDocument, m);
		} catch (Exception e) {
			Log.logException("Error in doing query for services in XML format", e, logger);
			throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).entity("<error>"+e.getCause().toString()+"</error>").build());
		}

		if (jArr == null) {
			return Response.ok().status(Status.NO_CONTENT).build();
		} else if (jArr.getCount() == BigInteger.ZERO) {
			return Response.ok(jArr).status(Status.NO_CONTENT).build();
		}

		return Response.ok(jArr).build();
	}
	

	@Deprecated
	@GET
	@Path("/query.xml")
	@Produces({ MediaType.APPLICATION_XML, MediaType.TEXT_XML })
	public Response queryXml(@Context UriInfo ui)
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
			qr = col.queryForXML(m);
		} catch (Exception e) {
			Log.logException("Error executing query for services in XML format", e, logger);
			throw new WebApplicationException(e);
		}

		if (qr.getCount() == BigInteger.ZERO) {
			return Response.ok(qr).status(Status.NO_CONTENT).build();

		}

		return Response.ok(qr).build();
	}

	@Deprecated
	@GET
	@Path("/pagedquery")
	@Produces(MediaType.APPLICATION_JSON)
	public Response pagedQuery(@Context UriInfo ui)
			throws WebApplicationException, JSONException {
		MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
		Set<String> s = queryParams.keySet();
		Map<String, Object> m = new HashMap<String, Object>();
		for (Iterator<String> iterator = s.iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
			m.put(key, queryParams.getFirst(key));
		}

		JSONArray jArr = null;

		try {
			jArr = col.pagedQueryForJSON(m);
		} catch (Exception e) {
			Log.logException("Error executing paged query", e, logger);
			JSONObject jErr = new JSONObject();
			jErr.put("error", e.getCause());
			throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).entity(jErr).build());
		}
		if (jArr.length() == 0) {
			return Response.ok(jArr).status(Status.NO_CONTENT).build();
		}
		return Response.ok(jArr).build();
	}
	
	@Deprecated
	@GET
	@Path("/pagedquery")
	@Produces(MediaType.APPLICATION_XML)
	public Response pagedQueryGlue2(@Context UriInfo ui)
			throws WebApplicationException, JSONException {
		MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
		Set<String> s = queryParams.keySet();
		Map<String, Object> m = new HashMap<String, Object>();
		for (Iterator<String> iterator = s.iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
			m.put(key, queryParams.getFirst(key));
		}

		QueryResult qr = null;

		try {
			qr = col.queryForXML(m);
		} catch (Exception e) {
			Log.logException("Error executing paged query in XML format", e, logger);
			JSONObject jErr = new JSONObject();
			jErr.put("error", e.getCause());
			throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).entity(jErr).build());
		}
		if (qr.getCount() == BigInteger.ZERO) {
			return Response.ok(qr).status(Status.NO_CONTENT).build();

		}

		return Response.ok(qr).build();
	}

}
