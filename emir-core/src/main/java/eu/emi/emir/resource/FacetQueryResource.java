/**
 * 
 */
package eu.emi.emir.resource;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;

import eu.emi.emir.client.ServiceBasicAttributeNames;
import eu.emi.emir.client.util.Log;
import eu.emi.emir.db.ServiceDatabase;
import eu.emi.emir.db.mongodb.MongoDBServiceDatabase;
import eu.emi.emir.util.ExceptionUtil;

/**
 * @author a.memon
 * 
 */
@Path("/services/facet")
public class FacetQueryResource {
	private static final Logger logger = Log.getLogger(Log.EMIR_CORE,
			FacetQueryResource.class);

	public static final Set<String> DEFAULT_NAMES;

	public static final String PARAM_FACET_NAMES = "names";

	static {
		HashSet<String> names = new HashSet<String>();
		names.add(ServiceBasicAttributeNames.SERVICE_ENDPOINT_CAPABILITY
				.toString());
		names.add(ServiceBasicAttributeNames.SERVICE_TYPE.toString());
		DEFAULT_NAMES = Collections.unmodifiableSet(names);
	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getFacets(@Context UriInfo ui)
			throws WebApplicationException {
		ServiceDatabase sd = new MongoDBServiceDatabase();
		
		JSONArray ja = new JSONArray();
		
		
		
		MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
		
		Set<String> setNames = queryParams.keySet();
		
		Map<String, String> map = new HashMap<String,String>();
		
		for(String k : setNames){
			map.put(k,queryParams.getFirst(k));	
		}
		
		
		try {

			ja = sd.facetedQuery(map);

		} catch (Exception e) {
			Log.logException("Error in executing faceted query", e, logger);
			ja.put(ExceptionUtil.toJson(e));
			Response resp = Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
			throw new WebApplicationException(e,resp);
		}
		return Response.ok(ja).build();
	}

}
