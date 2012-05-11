package eu.emi.emir.client.security;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpURL;
import org.apache.commons.httpclient.protocol.Protocol;

/**
 * Code from Apache Commons HTTPClient "contrib" <br/>
 * 
 * A kind of HostConfiguration that gets its Host from a factory. This is useful
 * for integrating a specialized Protocol or SocketFactory; for example, a
 * SecureSocketFactory that authenticates via SSL. Use
 * HttpClient.setHostConfiguration to install a HostConfigurationWithHostFactory
 * that contains the specialized HostFactory, Protocol or SocketFactory.
 * <p>
 * An alternative is to use Protocol.registerProtocol to register a specialized
 * Protocol. But that has drawbacks: it makes it hard to integrate modules (e.g.
 * web applications in a servlet container) with different strategies, because
 * they share the specialized Protocol (Protocol.PROTOCOLS is static). And it
 * can't support different Protocols for different hosts or ports (since the
 * host and port aren't parameters to Protocol.getProtocol).
 * 
 * @author John Kristian
 */
public class CustomHostConfiguration extends HostConfiguration implements Cloneable{

	public CustomHostConfiguration(HttpHostFactory factory)
	{
		this.factory = factory;
	}

	private CustomHostConfiguration(CustomHostConfiguration that)
	{
		super(that);
		this.factory = that.factory;
	}

	private final HttpHostFactory factory;

	public Object clone()
	{
		return new CustomHostConfiguration(this);
	}

	private static final String DEFAULT_SCHEME = new String(HttpURL.DEFAULT_SCHEME);

	public void setHost(String host)
	{
		setHost(host, Protocol.getProtocol(DEFAULT_SCHEME).getDefaultPort());
	}

	public void setHost(final String host, int port)
	{
		setHost(host, port, DEFAULT_SCHEME);
	}

	public synchronized void setHost(String host, int port, String scheme)
	{
		setHost(factory.getHost(this, scheme, host, port));
	}

}

