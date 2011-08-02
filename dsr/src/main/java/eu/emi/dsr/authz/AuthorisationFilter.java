/**
 * 
 */
package eu.emi.dsr.authz;

import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

import javax.security.cert.Certificate;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;

import eu.emi.dsr.DSRServer;
import eu.emi.dsr.core.ServerConstants;
import eu.emi.dsr.util.Log;

/**
 * Authorisation filter to intercept the inbound requests
 * @author a.memon
 * 
 */
public class AuthorisationFilter implements ContainerRequestFilter {
	private static Logger logger = Log.getLogger(Log.DSR, AuthorisationFilter.class);
	@Context
	UriInfo uriInfo;
	@Context
    HttpServletRequest httpRequest;
	private static final String REALM = "HTTPS authentication";

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sun.jersey.spi.container.ContainerRequestFilter#filter(com.sun.jersey
	 * .spi.container.ContainerRequest)
	 */
	public ContainerRequest filter(ContainerRequest request) {
		if (!DSRServer.getConfiguration().getBooleanProperty(ServerConstants.REGISTRY_ACCESSCONTROL)) {
			return request;
		}
		Enumeration a = httpRequest.getAttributeNames();
		
		while (a.hasMoreElements()) {
			String object = (String) a.nextElement();
			logger.debug(object);
			
		}
		X509Certificate[] certArr = (X509Certificate[]) httpRequest.getAttribute("javax.servlet.request.X509Certificate");
		if (certArr != null) {
			logger.debug("user dn: "+certArr[0].getSubjectDN());
			logger.debug(request.getMethod());
			logger.debug(request.getPath());	
		}
		
		
		return request;
	}

	/**
	 * @param request
	 */
	private void authorize(ContainerRequest request) {
		request.setSecurityContext(new Authorizer(null));
		
	}

	public class Authorizer implements SecurityContext {
		
		/**
		 * 
		 */
		public Authorizer(Certificate certificate) {
			// TODO Auto-generated constructor stub
		}
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.ws.rs.core.SecurityContext#getAuthenticationScheme()
		 */
		public String getAuthenticationScheme() {
			return SecurityContext.CLIENT_CERT_AUTH;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.ws.rs.core.SecurityContext#getUserPrincipal()
		 */
		public Principal getUserPrincipal() {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.ws.rs.core.SecurityContext#isSecure()
		 */
		public boolean isSecure() {
			return "https".equals(uriInfo.getRequestUri().getScheme());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.ws.rs.core.SecurityContext#isUserInRole(java.lang.String)
		 */
		public boolean isUserInRole(String arg0) {
			// TODO do authorisation checks
			return false;
		}

	}

}
