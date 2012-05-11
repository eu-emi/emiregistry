package eu.emi.emir.client.security;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpHost;
import org.apache.commons.httpclient.protocol.Protocol;

/**
 * Code from Apache Commons HTTPClient "contrib" <br/>
 */
public class HttpHostFactory {

	    public HttpHostFactory(Protocol httpProtocol, Protocol httpsProtocol)
	    {
	        this.httpProtocol = httpProtocol;
	        this.httpsProtocol = httpsProtocol;
	    }

	    protected final Protocol httpProtocol;

	    protected final Protocol httpsProtocol;

	    /** Get a host for the given parameters. This method need not be thread-safe. */
	    public HttpHost getHost(HostConfiguration old, String scheme, String host, int port)
	    {
	        return new HttpHost(host, port, getProtocol(old, scheme, host, port));
	    }

	    /**
	     * Get a Protocol for the given parameters. The default implementation
	     * selects a protocol based only on the scheme. Subclasses can do fancier
	     * things, such as select SSL parameters based on the host or port. This
	     * method must not return null.
	     */
	    protected Protocol getProtocol(HostConfiguration old, String scheme, String host, int port)
	    {
	        final Protocol oldProtocol = old.getProtocol();
	        if (oldProtocol != null) {
	            final String oldScheme = oldProtocol.getScheme();
	            if (oldScheme == scheme || (oldScheme != null && oldScheme.equalsIgnoreCase(scheme))) {
	                // The old protocol has the desired scheme.
	                return oldProtocol; // Retain it.
	            }
	        }
	        Protocol newProtocol = (scheme != null && scheme.toLowerCase().endsWith("s")) ? httpsProtocol
	                : httpProtocol;
	        if (newProtocol == null) {
	            newProtocol = Protocol.getProtocol(scheme);
	        }
	        return newProtocol;
	    }


}
