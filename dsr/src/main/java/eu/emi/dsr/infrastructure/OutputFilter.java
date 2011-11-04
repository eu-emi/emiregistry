package eu.emi.dsr.infrastructure;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;

import eu.emi.dsr.util.Log;

/**
 * @author g.szigeti
 * 
 */
public class OutputFilter implements ContainerResponseFilter {
	private Logger logger = Log.getLogger(Log.DSR,
			OutputFilter.class);
	private Filters filter = null;

	@Context
	HttpServletResponse httpResponse;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sun.jersey.spi.container.ContainerResponseFilter#filter(com.sun.jersey
	 * .spi.container.ContainerRequest, com.sun.jersey
	 * .spi.container.ContainerResponse)
	 */
	@Override
	public ContainerResponse filter(ContainerRequest request,
			ContainerResponse response) {
		logger.debug("OUTPUTFILTER called"); // TODO: remove this line after the debugging
		if ( response.getEntity() == null) {
			return response;
		}
		if (filter == null) {
			filter = new Filters();
		}
		if (response.getMediaType() != null && 
				response.getMediaType().toString().equals(MediaType.APPLICATION_JSON)) {
			/* Here filtered not only JSONArray outgoing messages, because 
			 * the simple queries messages contains JSONObject.
			 */
			if ( response.getEntity().getClass().equals(JSONArray.class)) {
				JSONArray entity = filter.outputFilter((JSONArray)response.getEntity());
				response.setEntity(entity);
			}else {
				/* Need fore the serviceadmin's GET method.
				 * It is returned only one JSONObject.
				 */
				JSONArray tmpEntity = new JSONArray();
				tmpEntity.put((JSONObject)response.getEntity());
				JSONArray entity = filter.outputFilter(tmpEntity);
				try {
					response.setEntity(entity.getJSONObject(0));
				} catch (JSONException e) {
					// Response will be include empty content
					response.setEntity(new JSONObject());
				}
			}
		}
		return response;
	}

}
