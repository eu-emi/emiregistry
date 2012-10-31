/**
 * 
 */
package eu.emi.emir.jetty;

import java.util.Map;
import java.util.Properties;

import eu.emi.emir.ServerProperties;
import eu.unicore.util.configuration.DocumentationReferenceMeta;
import eu.unicore.util.configuration.DocumentationReferencePrefix;
import eu.unicore.util.configuration.PropertyMD;
import eu.unicore.util.jetty.HttpServerProperties;

/**
 * @author a.memon
 *
 */
public class EMIRJettyProperties extends HttpServerProperties {
		public static final String JETTY_PREFIX = "jetty.";
	
		@DocumentationReferencePrefix
		public static final String PREFIX = ServerProperties.PREFIX + JETTY_PREFIX;
		
		@DocumentationReferenceMeta
		protected final static Map<String, PropertyMD> defaults=HttpServerProperties.defaults;
		
		public EMIRJettyProperties(Properties source) {
			super(source, PREFIX, defaults);
		}	
		
}
