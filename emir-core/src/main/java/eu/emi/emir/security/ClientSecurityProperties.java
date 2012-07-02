/**
 * 
 */
package eu.emi.emir.security;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import eu.emi.emir.ServerProperties;
import eu.unicore.security.canl.IAuthnAndTrustConfiguration;
import eu.unicore.util.configuration.ConfigurationException;
import eu.unicore.util.configuration.DocumentationReferencePrefix;
import eu.unicore.util.configuration.PropertyMD;
import eu.unicore.util.httpclient.ClientProperties;

/**
 * @author a.memon
 *
 */
public class ClientSecurityProperties extends ClientProperties{
	@DocumentationReferencePrefix
	public static final String PREFIX = ServerProperties.PREFIX+ClientProperties.DEFAULT_PREFIX;
	
	public final static Map<String, PropertyMD> META = new HashMap<String, PropertyMD>();

	
	static 
	{
		META.put(PROP_MESSAGE_SIGNING_ENABLED, new PropertyMD("false").
				setDescription("Controls whether signing of key web service requests should be performed."));
		META.putAll(ClientProperties.META);
		META.remove(PROP_SSL_ENABLED);
	}
	
	/**
	 * 
	 */
	public ClientSecurityProperties(Properties p, String trustPrefix,String credPrefix, String clientPrefix) {
		super(p, trustPrefix, credPrefix, clientPrefix);
	}
	
	public ClientSecurityProperties(Properties p, IAuthnAndTrustConfiguration authAndTrust) {
		super(p, authAndTrust);
	}
	
	public ClientSecurityProperties(Properties p, String clientPrefix, IAuthnAndTrustConfiguration authAndTrust) {
		super(p, clientPrefix, authAndTrust);
	}
	
	public ClientSecurityProperties(Properties p, IServerSecurityConfiguration baseSettings)
			throws ConfigurationException {
		super(createClientProperties(p, baseSettings), 
				PREFIX, baseSettings);
	}
	
	private static Properties createClientProperties(Properties p, IServerSecurityConfiguration baseSettings) {
		p.setProperty(PREFIX+ClientProperties.PROP_SSL_ENABLED,	baseSettings.isSslEnabled()+"");
		return p;
	}
}
