package eu.emi.dsr.security;

import java.security.cert.CertPath;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Enumeration;

import javax.security.auth.x500.X500Principal;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;

import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;

import eu.emi.dsr.DSRServer;
import eu.emi.dsr.core.ServerConstants;
import eu.emi.dsr.security.util.AuthZAttributeStore;
import eu.emi.dsr.security.util.ResourceDescriptor;
import eu.emi.dsr.util.Log;

/**
 * @author a.memon
 * 
 */
public class AccessControlFilter implements ContainerRequestFilter {
	private static Logger logger = Log.getLogger(Log.SECURITY,
			AccessControlFilter.class);
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
	@Override
	public ContainerRequest filter(ContainerRequest request)
			throws WebApplicationException {
//		if (!DSRServer.getConfiguration().getBooleanProperty(
//				ISecurityProperties.REGISTRY_CHECKACCESS)) {
//			return request;
//		} else 	{
//			X509Certificate[] certArr = (X509Certificate[]) httpRequest
//					.getAttribute("javax.servlet.request.X509Certificate");
//			logger.debug("user dn: "
//					+ certArr[0].getSubjectX500Principal().getName(
//							X500Principal.CANONICAL));
			try {
				checkAccess();
			} catch (AuthorisationException e) {
				throw new WebApplicationException(e,
						Status.UNAUTHORIZED.getStatusCode());
			}

//		}

		return request;
	}

	protected void checkAccess()
			throws AuthorisationException {
		SecurityTokens tokens = null;
		Client client = null;
		String action = null;
		ResourceDescriptor resourceDescriptor = null;

		try {
			Boolean b = Boolean.valueOf(DSRServer.getProperty(ISecurityProperties.REGISTRY_SSL_ENABLED,"false"));
			//dealing with the principal
			if (b) {
				X509Certificate[] certArr = (X509Certificate[]) httpRequest
						.getAttribute("javax.servlet.request.X509Certificate");
				tokens = new SecurityTokens();
				CertPath cp = CertificateFactory.getInstance("X.509")
						.generateCertPath(Arrays.asList(certArr));
				tokens.setUser(cp);
				tokens.setUserName(certArr[0].getSubjectX500Principal());					
			} 
			
			client = SecurityManager.createAndAuthoriseClient(tokens);
			httpRequest.setAttribute(ServerConstants.CLIENT, client);
			
			if ("true".equalsIgnoreCase(DSRServer.getProperty(ISecurityProperties.REGISTRY_CHECKACCESS, "false"))) {
				AuthZAttributeStore.setTokens(tokens);
				AuthZAttributeStore.setClient(client);	
				action = httpRequest.getMethod();
				String owner = SecurityManager.getServerIdentity().getName();
				resourceDescriptor = new ResourceDescriptor(uriInfo.getPath(),
						null, owner);
				doCheck(tokens, client, action, resourceDescriptor);
			}
			
			
		} catch (Exception e) {
			Log.logException("Error setting up authorisation check", e, logger);
			AuthZAttributeStore.removeClient();
			AuthZAttributeStore.removeTokens();
			throw new AuthorisationException("Authorisation failed. Reason: "
					+ e.getMessage());
		}
		
	}

	/**
	 * perform the actual check
	 * 
	 * @param securityTokens
	 *            - the security tokens for the current request
	 * @param client
	 *            - the current client
	 * @param action
	 *            - the action
	 * @param rd
	 *            - the resource
	 */
	protected void doCheck(SecurityTokens securityTokens, Client client,
			String action, ResourceDescriptor rd)
			throws WebApplicationException {

		if (logger.isDebugEnabled())
			logger.debug("Checking access on service " + rd);

		// do not check server-scope (internal) use of the resources
		if (SecurityManager.isServer(client)) {
			if (logger.isDebugEnabled())
				logger.debug("Accept server-scope action <" + action + "> on "
						+ rd);
			return;
		}

		// check signature status
		SecurityManager.checkAuthentication(securityTokens, action, rd);

		SecurityManager.checkAuthorisation(client, action, rd);

	}

}
