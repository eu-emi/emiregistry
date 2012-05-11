package eu.emi.emir.security;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlObject;

import eu.emi.emir.client.util.Log;
import eu.emi.emir.util.Utilities;

/**
 * Callback class that handles additional attributes in the User assertion.
 * <p>
 * Currently the following user preferences are recognized: uid, gid, 
 * supplementary_gids and role.
 * 
 * @see AuthInHandler
 * @author schuller
 * @author golbi
 */
public class UserAttributeCallback implements UserAttributeHandler{
	
	private static final Logger logger = Log.getLogger(Log.EMIR_SECURITY,UserAttributeCallback.class);
	public static final String USER_PREFERENCES_KEY = UserAttributeCallback.class.getCanonicalName();
	
	/**
	 * process an attribute defined in the User assertion
	 * 
	 * @param name -  the name of the attribute
	 * @param nameFormat - the NameFormat
	 * @param values - the array of values
	 * @param mainToken - the security tokens
	 */
	public void processUserDefinedAttribute(String name, String nameFormat, XmlObject[]values, SecurityTokens mainToken){
//		if (!nameFormat.equals(ETDClientSettings.SAML_ATTRIBUTE_REQUEST_NAMEFORMAT))
//			logger.debug("Ignoring request for unknown attribute of type <"+nameFormat+">");
		@SuppressWarnings("unchecked")
		Map<String, String[]> preferences = 
			(Map<String, String[]>) mainToken.getContext().get(USER_PREFERENCES_KEY);
		if (preferences == null) {
			preferences = new HashMap<String, String[]>();
			mainToken.getContext().put(USER_PREFERENCES_KEY, preferences);
		}
//		if (genericAttributeHandle(IAttributeSource.ATTRIBUTE_XLOGIN, 
//				preferences, name, values, false)) 
//			return;
		if (genericAttributeHandle(IAttributeSource.ATTRIBUTE_GROUP, 
				preferences, name, values, false)) 
			return;
		if (genericAttributeHandle(IAttributeSource.ATTRIBUTE_ROLE, 
				preferences, name, values, false)) 
			return;
		if (genericAttributeHandle(IAttributeSource.ATTRIBUTE_SUPPLEMENTARY_GROUPS, 
				preferences, name, values, true)) 
			return;
		if (genericAttributeHandle(IAttributeSource.ATTRIBUTE_ADD_DEFAULT_GROUPS, 
				preferences, name, values, false)) 
			return;
		
		logger.debug("Ignoring request for unknown attribute named <"+name+">");
	}
	
	private boolean genericAttributeHandle(String processedName, Map<String, String[]> preferences, 
			String name, XmlObject[]xmlValues, boolean multivalued) {
		if (!processedName.equals(name))
			return false;
		String []values;
		if (!multivalued) {
			values = new String[1];
			values[0] = Utilities.extractElementTextAsString(xmlValues[0]);
		} else {
			values = new String[xmlValues.length];  
			for (int i=0; i<values.length; i++)
				values[i] = Utilities.extractElementTextAsString(xmlValues[i]);
		}
		preferences.put(processedName, values);
		if(logger.isDebugEnabled()){
			logger.debug("Got request for '" + processedName + 
					"' with value <"+Arrays.toString(values)+">");
		}
		return true;
	}
}
