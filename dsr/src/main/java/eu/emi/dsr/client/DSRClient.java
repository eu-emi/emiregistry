/**
 * 
 */
package eu.emi.dsr.client;

import org.apache.log4j.Logger;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.ext.ssl.PkixSslContextFactory;
import org.restlet.resource.ClientResource;
import org.restlet.util.Series;

import eu.emi.dsr.util.Log;

/**
 * Helper class to create the client resource instance
 * 
 * @author a.memon
 * 
 */
public class DSRClient {
	/**
	 * 
	 */
	private static Logger logger = Log
			.getLogger(Log.DSRCLIENT, DSRClient.class);
	ClientResource cr = null;
	ClientSecurityProperties sProps = null;

	public DSRClient(String url, ClientSecurityProperties sp) {
		logger.debug("creating ssl client");
		sProps = sp;
		init(url);
	}

	public DSRClient(String url) {
		logger.debug("creating default client");
		cr = new ClientResource(url);
	}

	private void init(String url) {
		PkixSslContextFactory ctxFactory = new PkixSslContextFactory();
		Context ctx = new Context();
		Client client = new Client(ctx, Protocol.HTTPS);
		Series<Parameter> params = ctx.getParameters();
		String contextFactory = PkixSslContextFactory.class.getName();
		params.add("sslContextFactory", contextFactory);
		params.add("truststorePath", sProps.getTrustStorePath());
		params.add("truststorePassword", sProps.getTruststorePassword());
		params.add("truststoreType", sProps.getTruststoreType());

		params.add("keystorePath", sProps.getKeystorePath());
		params.add("keystorePassword", sProps.getKeystorePassword());
		params.add("keystoreType", sProps.getKeystoreType());

		// it is recommended to use keystore and key password same
		params.add("keyPassword", sProps.getKeystorePassword());

		ctxFactory.init(ctx.getParameters());

//		ctxFactory.init(ctx.getParameters());

		cr = new ClientResource(ctx, url);
	}

	
	public ClientResource getClientResource(){
		return cr;
	}
}
