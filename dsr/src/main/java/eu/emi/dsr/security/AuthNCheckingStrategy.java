package eu.emi.dsr.security;

import eu.emi.dsr.security.util.ResourceDescriptor;


/**
 * a check for authentication
 */
public interface AuthNCheckingStrategy {

	/**
	 * 
	 * @param tokens - security tokens from the message
	 * @param action - the SOAP action that is about to be invoked 
	 * @param d - the resource that is about to be accessed
	 * @throws AuthenticationException - an unchecked exception that signifies AuthN failure
	 */
	public void checkAuthentication(SecurityTokens tokens, String action, ResourceDescriptor d) throws AuthenticationException;
	
}
