/**
 * 
 */
package eu.emi.emir.security;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.security.auth.x500.X500Principal;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;

import eu.emi.emir.EMIRServer;
import eu.emi.emir.client.util.Log;
import eu.emi.emir.util.FileWatcher;

/**
 * It checks incoming requests, trying to access the <b>serviceadmin<b> resource
 * for the valid/authorized DNs
 * 
 * @author a.memon
 * 
 */
public class ACLFilter implements ContainerRequestFilter {
	private static Logger logger = Log.getLogger(Log.EMIR_SECURITY, ACLFilter.class);
	private File aclFile = null;
	private FileWatcher watchDog;
	@SuppressWarnings("unused")
	private boolean active;
	// private final Set<String> acceptedDNs = new HashSet<String>();
	private final Map<String, String> acceptedDNs = new HashMap<String, String>();
	private static Set<String> roles = new HashSet<String>();

	static {
		roles.add("serviceowner");
		roles.add("admin");
	}

	@Context
	HttpServletRequest httpRequest;
	@SuppressWarnings("unused")
	private File aclFile2;

	/**
	 * @throws IOException 
	 * 
	 */
	public ACLFilter() throws IOException {
			this(new File(EMIRServer.getServerSecurityProperties().getACLConfigurationFile()));	
	}

	/**
	 * @param file
	 * @throws IOException
	 */
	public ACLFilter(File aclFile) {
		this.aclFile = aclFile;
		if (!aclFile.exists()) {
			logger.warn("ACL not active: file <" + aclFile + "> does not exist");
			active = false;
			watchDog = null;
			return;
		} else {
			active = true;
			logger.info("EMIR using ACL file " + aclFile);
			readACL();
			try {
				watchDog = new FileWatcher(aclFile, new Runnable() {
					public void run() {
						readACL();
					}
				});
				watchDog.schedule(3000, TimeUnit.MILLISECONDS);
			} catch (FileNotFoundException e) {
				Log.logException("Invalid file path: "+aclFile, e, logger);
			}
			
		}
	}

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
		Client client = null;
		Role role = new Role();
		Boolean b = EMIRServer.getServerSecurityProperties().isSslEnabled();
		String path = request.getPath();
		//double check if the acl is enabled
		if (b && EMIRServer.getServerSecurityProperties().isACLAccessControlEnabled()) {
			X509Certificate[] certArr = (X509Certificate[]) httpRequest
					.getAttribute("javax.servlet.request.X509Certificate");
			String userName = certArr[0].getSubjectX500Principal()
					.getName();
			if (path.equalsIgnoreCase("serviceadmin")) {
				
				// setting the client to send it to the serviceadmin
				// resource
				client = checkAccess(userName);
			} else {
				//setting the dn regardless of the resource 
				client = new Client();
				client.setDistinguishedName(userName);
			}
		} 

		if (logger.isDebugEnabled()) {
			if (!request.getPath().equalsIgnoreCase("favicon.ico")) {
				logger.debug("Accessing resource: '" + request.getPath()
						+ "' with DN: " + client.getDistinguishedName());	
			}
			
		}

		httpRequest.setAttribute(SecurityManager.CLIENT, client);

		return request;
	}

	protected Client checkAccess(String userName)
			throws WebApplicationException {
		Client client = null;
		String msg = "Admin access denied!\n\nTo allow access for this "
				+ "certificate, the distinguished name \n" + userName
				+ "\nneeds to be entered into the ACL file."
				+ "\nPlease check the EMIR's ACL file!\n\n";
		synchronized (acceptedDNs) {
			// if (!acceptedDNs.get(userName)) {
			if (!(acceptedDNs.containsKey(userName))) {
				logger.info(msg);
				throw new WebApplicationException(Response
						.status(Status.UNAUTHORIZED).entity(msg).build());
			} else {
				client = new Client();
				client.setDistinguishedName(userName);
				String roleName = acceptedDNs.get(userName);
				logger.debug(roles.contains(roleName));
				if ((roleName != null) && roles.contains(roleName)) {
					client.setRole(new Role(roleName, ""));
				} else {
					// if the role is not present hence throw the exception
					throw new WebApplicationException(Response
							.status(Status.UNAUTHORIZED).entity(msg).build());
				}
			}

		}
		return client;
	}

	protected void readACL() {
		synchronized (acceptedDNs) {
			BufferedReader br = null;
			try {
				br = new BufferedReader(new FileReader(aclFile));
				String theLine;
				acceptedDNs.clear();
				while (true) {
					theLine = br.readLine();
					if (theLine == null)
						break;
					String line = theLine.trim();
					if (line.startsWith("#"))
						continue;
					if (!line.trim().equals("")) {
						try {
							String[] pair = line.split("::");
							X500Principal p = new X500Principal(pair[0].trim());
							acceptedDNs.put(p.getName(), pair[1].trim());
							logger.info("Allowing admin access for <" + line
									+ ">");
						} catch (Exception ex) {
							logger.warn("Invalid entry <" + line + ">", ex);
						}
					}
				}
			} catch (Exception ex) {
				logger.fatal("ACL file read error!", ex);
			} finally {
				try {
					if (br != null)
						br.close();
				} catch (IOException ioex) {
				}
			}
		}
	}

}
