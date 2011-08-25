/*********************************************************************************
 * Copyright (c) 2006 Forschungszentrum Juelich GmbH 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * (1) Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the disclaimer at the end. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution.
 * 
 * (2) Neither the name of Forschungszentrum Juelich GmbH nor the names of its 
 * contributors may be used to endorse or promote products derived from this 
 * software without specific prior written permission.
 * 
 * DISCLAIMER
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ********************************************************************************/
/*
 * Copyright (c) 2008 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE file for licencing information.
 *
 * Created on May 12, 2008
 * Author: K. Benedyczak <golbi@mat.umk.pl>
 */

package eu.emi.dsr.security;

import java.io.Serializable;
import java.security.cert.CertPath;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.x500.X500Principal;

//import eu.unicore.security.etd.TrustDelegation;

/**
 * A set of security tokens with authentication information: Unicore
 * consignor and user are hold here. Also trust delegation tokens 
 * and digital signature status are kept here. The additional data  
 * can be stored in a <i>context</i> map. Some keys of objects that can 
 * be found in the context are defined here too 
 * (e.g. HTTP BASIC Auth login and password).  
 * @author K. Benedyczak
 * @author Bernd Schuller
 */
public class SecurityTokens implements Serializable
{
	private static final long serialVersionUID = 1L;

	/**
	 * Key for storing the security tokens in the message context.
	 */
	public static final String KEY = SecurityTokens.class.getName() + ".key";

	
	/**
	 * Context key of the token (as found in Unicore6Tokens) with HTTP BASIC login data.
	 * The value corresponding to this key is either null or {@link HTTPAuthNTokens}.
	 */
	public static final String CTX_LOGIN_HTTP = HTTPAuthNTokens.class.getName() + ".key"; 

	/**
	 * Context key of the SOAP action being invoked. The value corresponding to 
	 * this key is either null or a String. 
	 */
	public static final String CTX_SOAP_ACTION = "REQUEST.soapAction";

	/**
	 * Context key of String which denotes if the request is done as normal WS request.
	 */
	public static final String CTX_SCOPE_KEY = SecurityTokens.class.getName() + ".scope";

	/**
	 * Standard value of SCOPE_KEY.
	 */
	public static final String SCOPE_REQUEST = "request";

	private CertPath user;
	private CertPath consignor;
	private SignatureStatus signatureStatus = SignatureStatus.UNCHECKED;
	private Map<String, Object> context;
	private X500Principal userName;
	/**
	 * If true then tdTokens confirmed that the User allowed the Consignor to act 
	 * on her behalf or Consignor is equal to User or this is a local call.
	 */
	private boolean consignorTrusted;
	/**
	 * If true then tdTokens contains a valid TD which was issued by the User.
	 */
	private boolean trustDelegationValidated;
//	private List<TrustDelegation> tdTokens;

	public SecurityTokens()
	{
		context = new HashMap<String, Object>();
	}


	/**
	 * Sets a consignor. It should be a VALIDATED identity.
	 * @param consignor
	 */
	public void setConsignor(CertPath consignor)
	{
		this.consignor = consignor;
	}

	/**
	 * Retrieves the stored consignor as a certificate path.
	 * @return
	 */
	public CertPath getConsignor()
	{
		return consignor;
	}

	/**
	 * Retrieves stored consignor as X509 certificate.
	 * @return
	 */
	public X509Certificate getConsignorCertificate()
	{
		if (consignor != null)
			return (X509Certificate) consignor.getCertificates().get(0);
		return null;
	}
	

	/**
	 * Sets user identity in terms of certificates. It is an identity of a user on 
	 * whose behalf consignor wishes to execute the request. 
	 * It has not to be verified, i.e. it can be just a requested user. 
	 * @param user
	 */
	public void setUser(CertPath user)
	{
		this.user = user;
		this.userName = ((X509Certificate) user.getCertificates().get(0))
			.getSubjectX500Principal();
	}

	/**
	 * Returns user's certificates path. Note that it <b>may not represent a 
	 * valid user</b>, i.e. there might 
	 * be no trust delegation chain from the returned user to the actual consignor.
	 * @return
	 */
	public CertPath getUser()
	{
		return user;
	}

	/**
	 * Sets user identity in terms of DN. It is identity of user on 
	 * whose behalf consignor wishes to execute the request. 
	 * It has not to be verified, i.e. it can be just a requested user.
	 * <p>
	 * Note that calling this method will clear user's certificate if it was stored
	 * before with setUser()!
	 *  
	 * @param userName
	 */
	public void setUserName(X500Principal userName)
	{
		this.userName = userName;
		user = null;
	}

	/**
	 * Returns a user's X509 certificate. Note that it <b>may not represent a 
	 * valid user</b>, i.e. there might 
	 * be no trust delegation chain from the returned user to the actual consignor.
	 * @return
	 */
	public X509Certificate getUserCertificate()
	{
		if (user != null)
			return (X509Certificate) user.getCertificates().get(0);
		else
			return null;
	}

	/**
	 * Returns a user's DN. Note that it <b>may not represent a 
	 * valid user</b>, i.e. there may 
	 * not be trust delegation chain from the returned user to the actual consignor.
	 * @return
	 */
	public X500Principal getUserName()
	{
		if (userName != null)
			return userName;
		else if (user != null)
			return ((X509Certificate) user.getCertificates().get(0))
					.getSubjectX500Principal();
		else
			return null;
	}

	/**
	 * Returns effective user certificate, i.e. the <i>user</i> (if there is a valid 
	 * trust delegation
	 * from the user to the consignor) or the <i>consignor</i> (in other cases). 
	 * This method will return null when the consignor is unknown.
	 */
//	public X509Certificate getEffectiveUserCertificate()
//	{
//		if (user != null && consignorTrusted)
//			return getUserCertificate();
//		return getConsignorCertificate();
//	}

	/**
	 * Returns effective user DN, i.e. the <i>user</i> (if there is a valid 
	 * trust delegation
	 * from the user to the consignor) or the <i>consignor</i> (in other cases).
	 * This method will return null when the consignor is unknown.
	 */
//	public X500Principal getEffectiveUserName()
//	{
//		if (userName != null && consignorTrusted)
//			return getUserName();
//		X509Certificate cc = getConsignorCertificate();
//		if (cc == null)
//			return null;
//		return cc.getSubjectX500Principal();
//	}


        private static final String lineSep=System.getProperty("line.separator");

	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		if (userName != null)
		    sb.append("User name: ").append(userName.getName()).append(lineSep);
		if (user != null)
		    sb.append("(have user cert)").append(lineSep);
		if (consignor != null)
		{
		    X509Certificate cc = (X509Certificate)consignor.getCertificates().get(0); 
		    sb.append("Consignor DN: ").append(cc.getSubjectX500Principal().getName());
		}
		if (signatureStatus != null)
		{
		    sb.append(lineSep+"Message signature status: ").append(signatureStatus.toString());
		}
		String res = sb.toString();
		if (res.length() == 0)
			return super.toString() + " [no details available]";
		return res;
	}

	/**
	 * Returns a map with additional security related settings. This can be used 
	 * by handlers to pass additional data. 
	 */
	public Map<String, Object> getContext()
	{
		return context;
	}


	/**
	 * Returns the status of the request's signature.
	 * @return
	 */
	public SignatureStatus getMessageSignatureStatus()
	{
		return signatureStatus;
	}

	/**
	 * Sets a status of the request's signature.
	 * @param status
	 */
	public void setMessageSignatureStatus(SignatureStatus status)
	{
		signatureStatus = status;
	}

	/**
	 * Returns true if the Consignor is anyhow allowed to work on
	 * User's behalf, as set by the setConsignorTrusted method. 
	 * @return
	 */
	public boolean isConsignorTrusted()
	{
		return consignorTrusted;
	}

	/**
	 * Sets the key value telling if the Consignor is allowed to work on 
	 * the Users behalf. 
	 */
	public void setConsignorTrusted(boolean consignorTrusted)
	{
		this.consignorTrusted = consignorTrusted;
	}

	/**
	 * Returns true iff the trust delegation attached is valid and 
	 * issued by the User. This does not mean that the trust is delegated to 
	 * the consignor, use isValidConsig
	 * @return
	 */
	public boolean isTrustDelegationValidated()
	{
		return trustDelegationValidated;
	}

	/**
	 * Sets the attached trust delegation general validation status.
	 */
	public void setTrustDelegationValidated(boolean validTrustDelegation)
	{
		this.trustDelegationValidated = validTrustDelegation;
	}

//	/**
//	 * Sets trust delegation tokens. Note that those need not to be anyhow verified.
//	 * @param tdTokens
//	 */
//	public void setTrustDelegationTokens(List<TrustDelegation> tdTokens)
//	{
//		this.tdTokens = tdTokens;
//	}
//
//	/**
//	 * Gets trust delegation tokens. Note that those need not to be anyhow verified.
//	 */
//	public List<TrustDelegation> getTrustDelegationTokens()
//	{
//		return tdTokens;
//	}

	/**
	 * Two sets of tokes are considered equal if their effective user names, 
	 * consignor certs, delegation statuses and signature status are equal.
	 */
	public boolean equals(Object otherO)
	{
		if (otherO == null || !(otherO instanceof SecurityTokens))
		{
			return false;
		}
		SecurityTokens other = (SecurityTokens) otherO;
		
//		if (!other.getMessageSignatureStatus().equals(getMessageSignatureStatus()))
//			return false;
//		if (other.isConsignorTrusted() != isConsignorTrusted())
//			return false;
//		if (other.isTrustDelegationValidated() != isTrustDelegationValidated())
//			return false;
		
//		if (other.getConsignorCertificate() == null)
//		{
//			if (getConsignorCertificate() != null)
//				return false;
//		} else if (!other.getConsignorCertificate().equals(getConsignorCertificate()))
//			return false;
		
//		if (other.getEffectiveUserName() == null)
//		{
//			if (getEffectiveUserName() != null)
//				return false;
//		} else if (!other.getEffectiveUserName().equals(getEffectiveUserName()))
//			return false;
		return true;
	}

//	public int hashCode(){
//		return getMessageSignatureStatus().hashCode()
//				^ (isConsignorTrusted()?0x1:0x0)
//				^ (isTrustDelegationValidated()?0x2:0x0)
//				^ getConsignorCertificate().hashCode()
//				^ getEffectiveUserName().hashCode();
//	}
}












