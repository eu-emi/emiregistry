package eu.emi.dsr.security;

import org.apache.xmlbeans.XmlObject;


/**
 * callback for handling Attributes defined in a UserAssertion 
 * @author schuller
 */
public interface UserAttributeHandler {
	
	/**
	 * process a SAML attribute defined in the User assertion
	 * 
	 * @param name -  the name of the attribute
	 * @param nameFormat - the NameFormat
	 * @param values - the array of values
	 * @param tokens - the security tokens
	 */
	public void processUserDefinedAttribute(String name, String nameFormat, XmlObject[]values, SecurityTokens tokens);
	
}
