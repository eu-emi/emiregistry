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
 *********************************************************************************/
 

package eu.emi.dsr.security;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Describes the user that is consuming resources<br/>
 *  
 * @author schuller
 */
public class Client implements Serializable {
	
	private static final long serialVersionUID=1L;
	
	//for some use cases, credentials are stored in the client object
	public static final String ATTRIBUTE_CREDENTIALS_USERNAME="creds.username";
	public static final String ATTRIBUTE_CREDENTIALS_PASSWORD="creds.password";
	
	//proxy cert : TODO
//	public static final String ATTRIBUTE_CREDENTIALS_PROXY="creds.proxy";
//	public static final String ATTRIBUTE_CREDENTIALS_PROXYFORMAT="creds.proxyformat";
	
	//for storing the email address in the attributes
	public static final String ATTRIBUTE_USER_EMAIL="user.email";
	
	
	//the role of the client
	private Role role;
	//the token by which a client is identified
	private SecurityTokens secTokens;
	
	//who this client really is...
	private String distinguishedName; 
	
//	//the (set of) possible unix login name(s) and groups optionally with the preferred one
//	private Xlogin xlogin;
	
	//list of VOs the user is a member of
	private String[] vos;
	
//	private Queue queue;
	
	
	//all attributes that were established by attribute sources.
	private SubjectAttributesHolder subjectAttributes;
	
	//additional attributes may contain things relevant on the target system 
	//such as licence keys, ... In most cases subjectAttributes are what you need.
	private final Map<String,Serializable> extraAttributes;
	
	/**
	 * constructs an anonymous Client
	 */
	public Client(){
		extraAttributes=new HashMap<String,Serializable>();
		setSubjectAttributes(new SubjectAttributesHolder());		
//		vos = new String[0];
//		queue = new Queue();
	}
	
	public String toString() {
		StringBuilder cInfo = new StringBuilder();
		
		cInfo.append("Name: ");
		cInfo.append(distinguishedName);
//		cInfo.append("\nXlogin: ");
//		cInfo.append(xlogin);
		cInfo.append("\nRole: ");
		cInfo.append(role);
//		if (queue.getValidQueues().length > 0) {
//			cInfo.append("\nQueues: ");
//			cInfo.append(queue);
//		}
//		if (vos.length > 0) {
//			cInfo.append("\nVOs: ");
//			cInfo.append(Arrays.toString(vos));
//		}
		if (secTokens != null)
		{
			cInfo.append("\nSecurity tokens: ");
			cInfo.append(secTokens);
		}	
		return cInfo.toString(); 
	}
	
	/**
	 * @return Returns the {@link SecurityTokens}
	 */
	public SecurityTokens getSecurityTokens() {
		return secTokens;
	}
	/**
	 * @param secTokens the security tokens used to authenticate this client
	 */
	public void setSecurityTokens(SecurityTokens secTokens) {
		this.secTokens = secTokens;
	}
	/**
	 * @return Returns the role.
	 */
	public Role getRole() {
		return role;
	}
	/**
	 * @param role The role to set.
	 */
	public void setRole(Role role) {
		this.role = role;
	}
	/**
	 * @return Returns the distinguishedName.
	 */
	public String getDistinguishedName() {
		return distinguishedName;
	}
	/**
	 * @param distinguishedName The distinguishedName to set.
	 */
	public void setDistinguishedName(String distinguishedName) {
		this.distinguishedName = distinguishedName;
	}

	public Map<String, Serializable> getExtraAttributes() {
		return extraAttributes;
	}
	
	/**
	 * convenience method for getting the user's xlogin
	 * @return an {@link Xlogin}
	 */
//	public Xlogin getXlogin(){
//		return xlogin;
//	}
//	
//	/**
//	 * convenience method for setting the user's xlogin
//	 * @return an {@link Xlogin}
//	 */
//	public void setXlogin(Xlogin xlogin){
//		if (xlogin == null)
//			throw new IllegalArgumentException("Setting null xlogin is prohibited.");
//		this.xlogin=xlogin;
//	}
//
//	public String getUserName() {
//		return xlogin.getUserName();
//	}
//
//	public void setUserName(String userName) {
//		xlogin.setSelectedLogin(userName);
//	}
	
	public String getUserEmail(){
		return (String)extraAttributes.get(ATTRIBUTE_USER_EMAIL);
	}
	
	public void setUserEmail(String email){
		if(email==null)extraAttributes.remove(ATTRIBUTE_USER_EMAIL);
		extraAttributes.put(ATTRIBUTE_USER_EMAIL,email);
	}

	public void setSubjectAttributes(SubjectAttributesHolder subjectAttributes) {
		this.subjectAttributes = subjectAttributes;
	}

	public SubjectAttributesHolder getSubjectAttributes() {
		return subjectAttributes;
	}

	public String[] getVos() {
		return vos;
	}
//
//	public void setVos(String[] vos) {
//		if (vos == null)
//			throw new IllegalArgumentException("Can not set null VOs array, use empty array instead");
//		this.vos = vos;
//	}
//
//	public Queue getQueue() {
//		return queue;
//	}
//
//	public void setQueue(Queue queue) {
//		if (queue == null)
//			throw new IllegalArgumentException("Can not set null Queue object, use empty Queue instead");
//		this.queue = queue;
//	}
}
