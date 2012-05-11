package eu.emi.client.security;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.HttpsURL;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.log4j.Logger;

import eu.emi.client.util.Log;



/**
 * Contains helper code to create HttpClient instances. The following settings are always set
 * (depending on configuration passed in {@link Properties} object:
 * <ul>
 *  <li> maximum redirects which are automatically taken,
 *  <li> whether to set Connection: close HTTP header
 *  <li> {@link MultiThreadedHttpConnectionManager} is used with a preconfigured default 
 *  values of max connection attempts. 
 *  <li> user agent is set to Mozilla/4.0.
 * </ul>
 * <p>
 * Additionally one can use additional methods of this class to:
 * <ul>
 *  <li> configure connection's SSL
 *  <li> add support for HTTP proxy
 * </ul>
 * The returned client can be configured further by using standard {@link HttpClient}
 * preferences API. Note that for convenience some of the HttpClientParams are recognized
 * too and can be set through Properties argument in one shot.  
 * <p>
 * Contains some code from XFire's {@link CommonsHttpMessageSender}
 * 
 * @author schuller
 * @author golbi
 * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
 * @author <a href="mailto:tsztelak@gmail.com">Tomasz Sztelak</a>
 */
public class HttpUtils {

	private static final Logger logger=Log.getLogger(Log.EMIR_CLIENT, HttpUtils.class);

	//prevent instantiation 
	private HttpUtils(){}
	public static final String USER_AGENT =  
		"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0; XFire Client +http://xfire.codehaus.org)";

	/** If true then connection will be closed immediately after serving the request */
	public static final String CONNECTION_CLOSE = "http.connection-close";
	/** Maximum number of redirects to take. Set to a non positive value to disable automatic redirects. */
	public static final String HTTP_MAX_REDIRECTS = "http.maxRedirects";
	/** Space delimited list of hosts for which HTTP proxy shouldn't be used */
	public static final String HTTP_NON_PROXY_HOSTS = "http.nonProxyHosts";
	/** HTTP proxy host */
	public static final String HTTP_PROXY_HOST = "http.proxyHost";
	/** HTTP proxy port */
	public static final String HTTP_PROXY_PORT = "http.proxyPort";
	/** HTTP proxy user name */
	public static final String HTTP_PROXY_USER = "http.proxy.user";
	/** HTTP proxy password */
	public static final String HTTP_PROXY_PASS = "http.proxy.password";

	private static final int DEFAULT_MAX_HOST_CONNECTIONS = 6;
	private static final int DEFAULT_MAX_TOTAL_CONNECTIONS = MultiThreadedHttpConnectionManager.DEFAULT_MAX_TOTAL_CONNECTIONS;
	private static final int DEFAULT_SO_TIMEOUT = 20000;
	private static final int DEFAULT_CONNECT_TIMEOUT = 15000;

	//the following standard Jakarta HttpClient properties are set from plain properties:
	public static final String MAX_HOST_CONNECTIONS = HttpConnectionManagerParams.MAX_HOST_CONNECTIONS;
	public static final String MAX_TOTAL_CONNECTIONS = HttpConnectionManagerParams.MAX_TOTAL_CONNECTIONS;
	/** socket read timeout for HTTP */
	public static final String SO_TIMEOUT = HttpClientParams.SO_TIMEOUT;
	/** timeout for creating new HTTP connections */
	public static final String CONNECT_TIMEOUT = HttpConnectionManagerParams.CONNECTION_TIMEOUT;
	

	/**
	 * Convenience method for getting a {@link HttpClient} configured 
	 * with HTTP proxy support and SSL setup. 
	 * @param uri -  URI to connect to
	 * @param security - security (i.e. SSL) properties
	 * @param properties - additional properties used to customize the client
	 * @return a preconfigured http client
	 */
	public static synchronized HttpClient createClient(String uri, 
			IAuthenticationConfiguration security, Properties properties)
	{
		HttpClient client = createClient(properties);
		configureSSL(client, security);
		configureProxy(client,uri, properties);
		return client;
	}

	/**
	 * create a HTTP client (code is based on XFire's {@link CommonsHttpMessageSender}
	 */
	public static synchronized HttpClient createClient(Properties properties)
	{
		boolean connClose = Boolean.parseBoolean(properties.getProperty(
				CONNECTION_CLOSE));
		int maxRedirects = getIntValue(HTTP_MAX_REDIRECTS, properties, 
				RedirectingHttpClient.DEFAULT_MAX_REDIRECTS);
		HttpClient client = new RedirectingHttpClient(connClose, maxRedirects);

		MultiThreadedHttpConnectionManager manager = new MultiThreadedHttpConnectionManager();
		HttpConnectionManagerParams conParams = new HttpConnectionManagerParams (); 
		manager.setParams(conParams);
		int maxConnPerHost = getIntValue(MAX_HOST_CONNECTIONS, properties, DEFAULT_MAX_HOST_CONNECTIONS);
		conParams.setDefaultMaxConnectionsPerHost(maxConnPerHost);
		int maxTotalConn  = getIntValue(MAX_TOTAL_CONNECTIONS, properties, DEFAULT_MAX_TOTAL_CONNECTIONS);
		conParams.setMaxTotalConnections(maxTotalConn);
		client.setHttpConnectionManager(manager);

		int socketTimeout = getIntValue(SO_TIMEOUT, properties, DEFAULT_SO_TIMEOUT);
		int connectTimeout = getIntValue(CONNECT_TIMEOUT, properties, DEFAULT_CONNECT_TIMEOUT);
		setConnectionTimeout(client, socketTimeout, connectTimeout);
		
		HttpClientParams  params = client.getParams();
		params.setParameter("http.useragent", USER_AGENT);
		
		params.setVersion(HttpVersion.HTTP_1_1);
		return client;
	}

	public static void configureSSL(HttpClient client, IAuthenticationConfiguration security)
	{
		client.setHostConfiguration(getHostConfiguration(security));
	}
	
	/**
	 * configure the HTTP proxy settings on the given client
	 * 
	 * @param client - the HttpClient instance
	 * @param uri - the URI to connect to
	 */
	public static void configureProxy(HttpClient client, String uri, Properties properties){
		if (isNonProxyHost(uri, properties)) 
			return;

		// Setup the proxy settings
		String proxyHost = (String) properties.getProperty(HTTP_PROXY_HOST);
		if (proxyHost == null)
		{
			proxyHost = System.getProperty(HTTP_PROXY_HOST);
		}

		if (proxyHost != null && proxyHost.trim().length()>0)
		{ 
			String portS = (String) properties.getProperty(HTTP_PROXY_PORT);
			if (portS == null)
			{
				portS = System.getProperty(HTTP_PROXY_PORT);
			}
			int port = 80;
			if (portS != null)
				port = Integer.parseInt(portS);

			client.getHostConfiguration().setProxy(proxyHost, port);

			String proxyUser = (String) properties.getProperty(HTTP_PROXY_USER);
			String proxyPass = (String) properties.getProperty(HTTP_PROXY_PASS);
			if( proxyUser != null && proxyPass != null )
				client.getState().setProxyCredentials(AuthScope.ANY,getCredentials(client,proxyUser, proxyPass));
		}

	}

	/**
	 * get a HostConfiguration using the specified security settings
	 * @param sec - security (SSL) settings
	 * @return {@link HostConfiguration}
	 */
	private static HostConfiguration getHostConfiguration(final IAuthenticationConfiguration sec) {
		AuthSSLProtocolSocketFactory protocolFact=new AuthSSLProtocolSocketFactory(sec);
		Protocol p=new Protocol(new String(HttpsURL.DEFAULT_SCHEME),
				(ProtocolSocketFactory)protocolFact,
				HttpsURL.DEFAULT_PORT);
		HttpHostFactory fact=new HttpHostFactory(null,p);

		HostConfiguration c=new CustomHostConfiguration(fact);
		return c;
	}

	private static boolean isNonProxyHost(String uri, Properties properties){
		String nonProxyHosts=properties.getProperty(HTTP_NON_PROXY_HOSTS);
		if(nonProxyHosts==null)return false;
		try{
			URI u=new URI(uri);
			String host=u.getHost();
			String[] npHosts=nonProxyHosts.split(" ");
			for(String npHost: npHosts){
				if(host.contains(npHost))return true;
			}
		}catch(URISyntaxException e){
			logger.error("Can't resolve URI from "+uri, e);
		}	

		return false;
	}

	private static Credentials getCredentials(HttpClient client, String username, String password){
		client.getParams().setAuthenticationPreemptive(true);
		int domainIndex = username.indexOf('\\');
		if (domainIndex > 0 && username.length() > domainIndex + 1) {
			return new NTCredentials(
					username.substring(0, domainIndex), 
					password, 
					"localhost", 
					username.substring(domainIndex+1));
		} 
		return  new UsernamePasswordCredentials(username,password);
	}

	private static int getIntValue(String key, Properties properties, int defaultValue) {
		int result = defaultValue;
		String str = properties.getProperty(key);
		if( str != null )
		{
			result = Integer.parseInt(str);
		}
		return result;
	}

	/**
	 * Helper method: sets the connection timeout for the HTTP client and the socket timeout.
	 * @param client - the HTTPClient
	 * @param socketTimeout socket timeout in milliseconds
	 * @param connectTimeout connection timeout in milliseconds
	 */
	public static void setConnectionTimeout(HttpClient client, int socketTimeout, int connectTimeout){
		client.getParams().setSoTimeout(socketTimeout);
		client.getParams().setConnectionManagerTimeout(connectTimeout);
	}
}
