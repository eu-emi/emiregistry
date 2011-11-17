package eu.emi.dsr.infrastructure;

import java.io.ByteArrayInputStream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;

import eu.emi.client.util.Log;

/**
 * @author g.szigeti
 * 
 */
public class InputFilter implements ContainerRequestFilter {
	private Logger logger = Log.getLogger(Log.DSR,
			InputFilter.class);
	private Filters filter = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sun.jersey.spi.container.ContainerRequestFilter#filter(com.sun.jersey
	 * .spi.container.ContainerRequest)
	 */
	@Override
	public ContainerRequest filter(ContainerRequest request)
			throws WebApplicationException {
		// Use this filter for only the serviceadmin resource
		if(!request.getPath().equals("serviceadmin")){ 
			// Don't need filtering this message because it is not use a serviceadmin resource.
			return request;
		}
		
		if (logger.isDebugEnabled()){
			logger.debug("INPUTFILTER called");
		}
		
		if (filter == null) {
			filter = new Filters();
		}
		if (request.getMediaType() != null && 
				request.getMediaType().toString().equals(MediaType.APPLICATION_JSON)) {
			JSONArray entity = filter.inputFilter(request.getEntity(JSONArray.class));
			request.setEntityInputStream(new ByteArrayInputStream(entity.toString().getBytes()));
		}
		return request;
	}

}
