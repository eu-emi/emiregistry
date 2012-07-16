package eu.emi.emir.security;

import java.security.cert.CertPath;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;

import eu.emi.emir.EMIRServer;
import eu.emi.emir.client.util.Log;
import eu.emi.emir.security.util.AuthZAttributeStore;
import eu.emi.emir.security.util.ResourceDescriptor;

/**
 * This filter intercept the incoming requests and authorize the subject's DN
 * using XACML policies
 * 
 * @author a.memon
 * 
 */
public class AccessControlFilter implements ContainerRequestFilter {
	private static Logger logger = Log.getLogger(Log.EMIR_SECURITY,
			AccessControlFilter.class);
	@Context
	UriInfo uriInfo;
	@Context
	HttpServletRequest httpRequest;

	@SuppressWarnings("unused")
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
		try {
			checkAccess();
		} catch (AuthorisationException e) {
			throw new WebApplicationException(e, Response
					.status(Status.UNAUTHORIZED)
					.entity("Error performing access control: \n" + e).build());
		}

		return request;
	}

	protected void checkAccess() throws AuthorisationException {
		SecurityTokens tokens = null;
		Client client = null;
		String action = null;
		ResourceDescriptor resourceDescriptor = null;

		try {
//			Boolean b = Boolean.valueOf(DSRServer.getProperty(
//					ISecurityProperties.REGISTRY_SSL_ENABLED, "false"));
			Boolean b = Boolean.valueOf(EMIRServer.getServerSecurityProperties().isSslEnabled());
			// dealing with the principal
			if (b) {
				X509Certificate[] certArr = (X509Certificate[]) httpRequest
						.getAttribute("javax.servlet.request.X509Certificate");
				tokens = new SecurityTokens();
				CertPath cp = CertificateFactory.getInstance("X.509")
						.generateCertPath(Arrays.asList(certArr));
				tokens.setUser(cp);
				tokens.setUserName(certArr[0].getSubjectX500Principal());
			
			client = SecurityManager.createAndAuthoriseClient(tokens);
			httpRequest.setAttribute(SecurityManager.CLIENT, client);

			if (EMIRServer.getServerSecurityProperties().isXACMLAccessControlEnabled()) {
				AuthZAttributeStore.setTokens(tokens);
				AuthZAttributeStore.setClient(client);
				action = httpRequest.getMethod();
				String owner = SecurityManager.getServerIdentity().getName();
				resourceDescriptor = new ResourceDescriptor(uriInfo.getPath(),
						null, owner);
				doCheck(tokens, client, action, resourceDescriptor);
			}
			
			}

		} catch (Exception e) {
			Log.logException("Error setting up authorisation check", e, logger);
			AuthZAttributeStore.removeClient();
			AuthZAttributeStore.removeTokens();
			AuthorisationException ae = new AuthorisationException("Authorisation failed. Reason: "
					+ e.getMessage(), e);
			Response res = Response
			.status(Status.UNAUTHORIZED)
			.entity("Error performing access control or Unauthorised access: \n" + e).build();
			throw new WebApplicationException(ae, res);
			
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
