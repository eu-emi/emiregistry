/*********************************************************************************
 * Copyright (c) 2010 Forschungszentrum Juelich GmbH 
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

import java.util.Properties;

import org.apache.log4j.Logger;

import eu.emi.dsr.security.util.AttributeSourcesChain;
import eu.emi.client.util.Log;


/**
 * Creates the main attribute source chain and configures it.
 * 
 * @author schuller
 * @author golbi
 */
public class AttributeSourceFactory {

	private static final Logger logger=Log.getLogger(Log.SECURITY,AttributeSourceFactory.class);

	/**
	 * base for property names
	 */
	public static final String BASE="registry.security.attributes";
	/**
	 * attribute sources order property
	 */
	public static final String ORDER=AttributeSourceFactory.BASE + ".order";

	/**
	 * property for defining the combining policy if multiple sources are used
	 */
	public static final String COMBINING_POLICY=AttributeSourceFactory.BASE + ".combiningPolicy";



	private final Properties properties;

	public AttributeSourceFactory(Properties properties){
		this.properties=properties;
	}

	public IAttributeSource makeAttributeSource()throws Exception{
		logger.debug("Creating main attribute sources chain");
		IAttributeSource result;
		String order=properties.getProperty(ORDER);
		if(order!=null){
			AttributeSourcesChain ret = new AttributeSourcesChain();
			ret.setCombiningPolicy(properties.getProperty(COMBINING_POLICY));
			ret.setOrder(order);
			ret.setProperties(properties);
			ret.init(null);
			result=ret;
		}
		else {
			logger.info("No attribute source configured. " +
					"Set the property <"+ORDER+"> to configure attribute sources.");
			result=new SecurityManager.NullAuthoriser();
		}
		return result;
		
	}
}
