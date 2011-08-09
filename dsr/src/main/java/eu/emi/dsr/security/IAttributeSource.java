/*********************************************************************************
 * Copyright (c) 2006-2010 Forschungszentrum Juelich GmbH 
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

package eu.emi.dsr.security;

import java.io.IOException;


/**
 * IAttributeSource provides the interface for UNICORE/X to retrieve authorisation information
 * (attributes) for a particular request from an attribute provider, based on information 
 * such as Client DN, certificate, etc, contained in an instance of {@link SecurityTokens}.<br/>
 * <p>
 * The getAttributes method shall return the incarnation attributes, which are specially handled by 
 * the UNICORE stack. Those attribute names are defined in this interface with constants ATTRIBUTE_*. 
 * Additionally XACML attributes can be returned, those are used for authorization (are fed to
 * the PDP). Note that any attribute which is returned in the first list shouldn't
 * be returned again in XACML version; UNICORE automatically exposes incarnation attributes
 * to the PDP too when needed.
 * 
 * <em>Lifecycle</em>
 * IAttributeSource implementations are created and initialised by the {@link AttributeSourceFactory},
 * which will create the instance using Class.forName(), set additional parameters, and finally call
 * the init() method. The IAuthoriser will be created only once, and will be kept alive during the
 * lifetime of the server.<br/>
 * <p>
 * <em>Parameter injection</em>
 * When creating an IAttributeSource instance, UNICORE/X will set parameters according to the properties
 * defined in the main configuration file (usually <code>uas.config</code>), provided there is a public
 * setter method. For example, if the class has a field <code>setHost(String host)</code>, it
 * will be automatically invoked by UNICORE/X if the configuration has a property<br/> 
 * <code>
 * uas.security.attributes.NAME1.Host
 * </code><br/>
 * Currently parameters can be of type String, boolean, or numerical, for details see {@link AttributeSourceFactory}
 * <p>
 * 
 * 
 * @author schuller
 * @author golbi
 */
public interface IAttributeSource {

	/**
	 * UNICORE role attribute key. Only one may be selected.
	 */
	public static final String ATTRIBUTE_ROLE="role";

	/**
	 * UNIX login attribute key. Only one may be selected.
	 */
	//public static final String ATTRIBUTE_XLOGIN="xlogin";

	/**
	 * UNIX primary group attribute key. Only one may be selected.
	 */
	public static final String ATTRIBUTE_GROUP="group";

	/**
	 * UNIX supplementary groups attribute key.
	 */
	public static final String ATTRIBUTE_SUPPLEMENTARY_GROUPS="supplementaryGroups";

	/**
	 * Add OS default groups.
	 */
	public static final String ATTRIBUTE_ADD_DEFAULT_GROUPS="addDefaultGroups";

	/**
	 * BSS queue attribute key.
	 */
	//public static final String ATTRIBUTE_QUEUES="queue";

	/**
	 * Virtual Organisations  attribute key.
	 */
	public static final String ATTRIBUTE_VOS="virtualOrganisations";


	/**
	 * role attribute value: trusted agent as asserted by a SAML trust delegation assertion 
	 */
	//public static final String ROLE_TRUSTED_AGENT="trusted-agent";

	/**
	 * role attribute value: anonymous
	 */
	public static final String ROLE_ANONYMOUS="anonymous";

	/**
	 * role attribute value: admin
	 */
	public static final String ROLE_ADMIN="admin";

	/**
	 * initialise the source
	 */
	public void init(String name)throws Exception;

	/**
	 * Retrieves a map of attributes based on the supplied SecurityTokens.<br/>
	 * 
	 * Since authorisers can be chained, it might be sometimes useful to see attributes returned by 
	 * authorisers that have run previously. This information is supplied in the "otherAuthoriserInfo" map.<br/>
	 * 
	 * Attribute sources must not make any authorisation decisions. Thus, no exceptions must be thrown
	 * if no attributes are found. Only IOExceptions should be thrown in case of technical problems 
	 * contacting the actual attribute provider. This is to allow upstream code (i.e. the UNICORE/X 
	 * server) to log the error, or to take any other action (like notify an administrator). 
	 * If no attributes are found, an empty map or <code>null</code> must be returned.<br/>
	 * 
	 * @param tokens - security tokens for this request
	 * @param otherAuthoriserInfo - attributes returned by other authorisers, which may be <code>null</code>
	 * @return subject's attributes
	 * @throws IOException in case of technical problems
	 */
	public SubjectAttributesHolder getAttributes(final SecurityTokens tokens, 
			SubjectAttributesHolder otherAuthoriserInfo) throws IOException;	 

	/**
	 * Get a user-friendly description of the attribute source's status.<br/>
	 * This is used for informing the administrator of any problems with configuration, 
	 * connection problems, etc. UNICORE/X will print a log entry describing
	 * the status immediately after startup.<br/>
	 * 
	 * This method should not block for too long, since the server startup would be stalled
	 * in this case. For example, you may use the {@link TimeoutRunner} class to avoid blocking.
	 * 
	 * @return a String describing this attribute source's status.
	 */
	public String getStatusDescription();

	/**
	 * This method should return name of this attribute source, which was passed to the init() method.
	 * @return
	 */
	public String getName();
	
	/**
	 * This method returns the list of VOs that this attribute source supports.
	 * If the attribute source is not aware of virtual organisations (like XUUDB)
	 * then empty list is returned, meaning that it is possible that VO-less users
	 * might be accepted by this attribute source.
	 * @return
	 */
	public String[] getAcceptedVOs();
}
