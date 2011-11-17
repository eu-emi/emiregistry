package eu.emi.dsr.security;

import org.apache.log4j.Logger;

import eu.emi.dsr.security.util.ResourceDescriptor;
import eu.emi.client.util.Log;


/**
 * if the requested action requires it, check whether we have
 * a valid signature 
 */
public class DSignAuthNCheck implements AuthNCheckingStrategy {

	private static final Logger logger=Log.getLogger(Log.SECURITY,DSignAuthNCheck.class);

	public DSignAuthNCheck(){
		logger.debug("Initialise AuthN check for digital signature.");
	}
	
	public void checkAuthentication(SecurityTokens tokens, String action,
			ResourceDescriptor d) throws AuthenticationException {
		String soapAction=(String)tokens.getContext().get(SecurityTokens.CTX_SOAP_ACTION);
		if(soapAction==null){
			logger.fatal("SOAP handler pipeline is not set up correctly.");
			throw new AuthenticationException("Internal server error. Please contact the system administrator.");
		}
		logger.debug("Check authentication for <"+soapAction+">");
		
		if(!SecurityManager.needSignature(soapAction)) return;
		
		
		//OK now check if we have a signature and it is OK
		
		if(SignatureStatus.OK.equals(tokens.getMessageSignatureStatus())){
			return;
		}

		String msg="Non repudiation/integrity check failed on <"+d.toString()+">: signature is required for <"+action+">";
		logger.info(msg);
		throw new AuthenticationException(msg);
	}

}
