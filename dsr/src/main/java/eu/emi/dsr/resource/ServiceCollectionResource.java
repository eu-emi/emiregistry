/**
 * 
 */
package eu.emi.dsr.resource;

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

import eu.emi.dsr.core.ServiceColManager;
import eu.emi.dsr.util.Log;
import eu.eu_emi.emiregistry.QueryResult;

/**
 * @author a.memon TODO support for glue2 in paged query
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
	public Response getServiceEndPoints() throws WebApplicationException {
		JSONObject o = null;
		try {
			o = col.getServiceReferences();
		} catch (JSONException e) {
			throw new WebApplicationException(e);
		}
		if (o.length() == 0) {
			return Response.ok(o).status(Status.NO_CONTENT).build();
		}

		return Response.ok(o).build();
	}

	/** query method */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/types")
	public Response getServiceTypes() throws WebApplicationException {
		JSONArray jArr = null;
		try {
			jArr = col.getDistinctTypes();
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
		if (jArr.length() == 0) {
			return Response.ok(jArr).status(Status.NO_CONTENT).build();
		}

		return Response.ok(jArr).build();

	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response queryWithParams(@Context UriInfo ui) throws WebApplicationException {

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

		if (jArr.length() == 0) {
			return Response.ok(jArr).status(Status.NO_CONTENT).build();
		}

		return Response.ok(jArr).build();
	}

	/**
	 * Query using the advanced MongoDB queries
	 * @param queryDocument
	 *            the JSON document defining the query according to the MongoDB
	 *            Syntax
	 * */
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response queryWithJSON(JSONObject queryDocument)
			throws WebApplicationException {

		JSONArray jArr = null;
		try {
			jArr = col.query(queryDocument);
		} catch (Exception e) {
			throw new WebApplicationException(e);
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
	public Response queryXMLWithJSON(JSONObject queryDocument)
			throws WebApplicationException {

		QueryResult jArr = null;
		try {
			jArr = col.queryGlue2(queryDocument);
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}

		if (jArr.getCount() == BigInteger.ZERO) {
			return Response.ok(jArr).status(Status.NO_CONTENT).build();
		} else if (jArr == null) {
			return Response.ok().status(Status.NO_CONTENT).build();
		}

		return Response.ok(jArr).build();
	}

	/**
	 * Invoked only if the MIME type is defineds as application/xml
	 * 
	 * */

	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.TEXT_XML })
	public Response queryXMLWithParams(@Context UriInfo ui)
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
		if (qr.getCount() == BigInteger.ZERO) {
			return Response.ok(qr).status(Status.NO_CONTENT).build();

		}

		return Response.ok(qr).build();
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
			qr = col.queryGlue2(m);
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}

		if (qr.getCount() == BigInteger.ZERO) {
			return Response.ok(qr).status(Status.NO_CONTENT).build();

		}

		return Response.ok(qr).build();
	}

	@GET
	@Path("/pagedquery")
	public Response pagedQuery(@Context UriInfo ui)
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
		if (jArr.length() == 0) {
			return Response.ok(jArr).status(Status.NO_CONTENT).build();
		}
		return Response.ok(jArr).build();
	}

}
