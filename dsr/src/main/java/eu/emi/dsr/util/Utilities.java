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


package eu.emi.dsr.util;

import java.net.URI;
import java.util.UUID;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlCursor.TokenType;

import eu.emi.dsr.DSRServer;
import eu.emi.dsr.core.ServerConstants;
import eu.emi.dsr.security.ISecurityProperties;


/**
 * tools and utilities
 * 
 * @author schuller
 * @author demuth
 */
public class Utilities {

	protected Utilities() {
	}

	/**
	 * returns a new globally unique identifier
	 * @return
	 */
	public static String newUniqueID(){
		return UUID.randomUUID().toString();
	}

	public static String extractServiceName(String url){
		try{
			URI u = new URI(url);
			String[] path = u.getPath().split("/");
			return path[path.length-1];
		}catch(Exception e){return null;}
	}

	/**
	 * validate that the given String value (interpreted as an Integer) 
	 * is in the supplied range
	 * 
	 * @param value - String to be verified
	 * @param minValue - minimum
	 * @param maxValue - maximum
	 * @return true if the value is within the range, false otherwise
	 */
	public static boolean validateIntegerRange(String value, int minValue, int maxValue){
		try{
			if(value==null)return false;
			Integer i=Integer.parseInt(value);
			if(i<minValue || i > maxValue){
				return false;
			}
		}catch(Exception e){
			return false;
		}
		return true;
	}


	/**
	 * return the physical server address
	 * @return a URL of the form scheme://host:port where 'scheme' is http or https
	 */
	public static String getPhysicalServerAddress(){
		String host=DSRServer.getProperty(ServerConstants.REGISTRY_HOSTNAME).toString();
		String port=DSRServer.getProperty(ServerConstants.REGISTRY_PORT).toString();
		String proto="http";
		if("true".equals(DSRServer.getProperty(ISecurityProperties.REGISTRY_SSL_ENABLED))){
			proto="https";
		};
		return proto+"://"+host+":"+port;
	}


	
	
	/**
	 * extract the text content of an XML element 
	 * 
	 * @param source the xml element
	 * 
	 * @return the text content, or "" if element has no content
	 */
	public static String extractElementTextAsString(XmlObject source){
		XmlCursor c=null;
		try{
			c=source.newCursor();
			while(c.hasNextToken()){
				if(c.toNextToken().equals(TokenType.TEXT)){
					return c.getChars();	
				}
			}
			return "";
		}finally{
			try{
				c.dispose();
			}catch(Exception e){}
		}
	}

}
