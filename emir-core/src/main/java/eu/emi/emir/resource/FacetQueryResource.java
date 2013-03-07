/**
 * 
 */
package eu.emi.emir.resource;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.List;

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
import org.codehaus.jettison.json.JSONException;

import eu.emi.emir.client.ServiceBasicAttributeNames;
import eu.emi.emir.client.util.ExtentendedMultiValuedMap;
import eu.emi.emir.client.util.Log;
import eu.emi.emir.db.ServiceDatabase;
import eu.emi.emir.db.mongodb.MongoDBServiceDatabase;

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
		
		
//		List<String> facetNames = queryParams.get(PARAM_FACET_NAMES);
//
//		if (facetNames == null || facetNames.isEmpty()) {
//			setNames = DEFAULT_NAMES;
//		} else {
//			setNames = new HashSet<String>();
//			setNames.addAll(facetNames);
//		}

		
		
		try {

//			ja = sd.facetedQuery(setNames);
			ja = sd.facetedQuery(map);

		} catch (JSONException e) {
			Log.logException("Error in executing faceted query", e, logger);
			throw new WebApplicationException(e);
		}
		return Response.ok(ja).build();
	}

}
