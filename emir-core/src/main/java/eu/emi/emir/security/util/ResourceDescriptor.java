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
 
package eu.emi.emir.security.util;


/**
 * Descriptor for a resource. 
 * This might be a ws resource (service name, uuid)
 * or a plain web service (servicename). The owner of the resource is also given.
 * 
 * @author schuller
 */
public class ResourceDescriptor {

	//the service name, i.e. "AdminService
	final String serviceName;
	
	//the uuid of a WS-Resource (e.g. "default_registry")
	final String resourceID;
	
	//the DN (i.e. X500Principal.getName()) of the resource owner
	final String owner;
	
	public ResourceDescriptor(String serviceName, String resourceID, String owner){
		this.serviceName=serviceName;
		this.resourceID=resourceID;
		this.owner=owner;
	}
	
	public String toString(){
		StringBuilder sb=new StringBuilder();
		sb.append(serviceName);
		if(resourceID!=null)sb.append('[').append(resourceID).append(']');
		if(owner!=null)sb.append("[owner: ").append(owner).append(']');
		return sb.toString();
	}
	
	public String getOwner() {
		return owner;
	}

	public String getResourceID() {
		return resourceID;
	}

	public String getServiceName() {
		return serviceName;
	}
	
}
