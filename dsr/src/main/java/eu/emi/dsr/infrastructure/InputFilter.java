package eu.emi.dsr.infrastructure;

import java.io.ByteArrayInputStream;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;

import eu.emi.dsr.util.Log;

/**
 * @author g.szigeti
 * 
 */
public class InputFilter implements ContainerRequestFilter {
	private Logger logger = Log.getLogger(Log.DSR,
			InputFilter.class);
	private Filters filter = null;

	@Context
	HttpServletRequest httpRequest;

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
		//why the filter is shared variable?
		if (filter == null) {
			filter = new Filters();
		}
		
		if (request.getHeaderValue("Accept").equalsIgnoreCase(MediaType.APPLICATION_JSON)) {
			JSONArray entity = filter.inputFilter(request.getEntity(JSONArray.class));
			request.setEntityInputStream(new ByteArrayInputStream(entity.toString().getBytes()));
		}
		return request;
	}

}
