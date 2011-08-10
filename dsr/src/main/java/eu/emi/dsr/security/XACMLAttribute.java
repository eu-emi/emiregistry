/*
 * Copyright (c) 2010 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE file for licencing information.
 *
 * Created on 08-11-2010
 * Author: K. Benedyczak <golbi@mat.umk.pl>
 */
package eu.emi.dsr.security;

import java.io.Serializable;

/**
 * This class is used to define an attribute. It is not bound to any XACML library API
 * but the definition should be complaint (and easily translatable) to any XACML attribute
 * object. 
 * @author golbi
 */
public class XACMLAttribute implements Serializable
{
	private static final long serialVersionUID = 1L;

	public enum Name {
		XACML_SUBJECT_ID_ATTR("urn:oasis:names:tc:xacml:1.0:subject:subject-id"), 
		XACML_RESOURCE_ID_ATTR("urn:oasis:names:tc:xacml:1.0:resource:resource-id"),
		XACML_ACTION_ID_ATTR("urn:oasis:names:tc:xacml:1.0:action:action-id");
		
		private String name;
		private Name(String name)
		{
			this.name = name;
		}
		
		public String toString()
		{
			return name;
		}
	}
	
	
	public enum Type {
		STRING("http://www.w3.org/2001/XMLSchema#string"), 
		BOOL("http://www.w3.org/2001/XMLSchema#boolean"),
		INTEGER("http://www.w3.org/2001/XMLSchema#integer"),
		DOUBLE("http://www.w3.org/2001/XMLSchema#double"),
		TIME("http://www.w3.org/2001/XMLSchema#time"),
		DATETIME("http://www.w3.org/2001/XMLSchema#dateTime"),
		ANYURI("http://www.w3.org/2001/XMLSchema#anyURI"),
		HEXBINARY("http://www.w3.org/2001/XMLSchema#hexBinary"),
		BASE64BINARY("http://www.w3.org/2001/XMLSchema#base64Binary"),
		X500NAME("urn:oasis:names:tc:xacml:1.0:data-type:x500Name"),
		RFC822NAME("urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name"),
		IP("urn:oasis:names:tc:xacml:2.0:data-type:ipAddress"),
		DNS("urn:oasis:names:tc:xacml:2.0:data-type:dnsName");
		
		private String name;
		private Type(String name)
		{
			this.name = name;
		}
		
		public String toString()
		{
			return name;
		}
	}
	
	private String name;
	private String value;
	private Type type;
	
	/**
	 * 
	 * @param name
	 * @param value
	 * @param type XACML 2.0 attribute type.
	 */
	public XACMLAttribute(String name, String value, Type type)
	{
		this.name = name;
		this.value = value;
		this.type = type;
	}
	
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}
	
	public String getValue()
	{
		return value;
	}
	
	public void setValue(String value)
	{
		this.value = value;
	}
	
	/**
	 * Sets XACML 2.0 type
	 * @return
	 */
	public Type getType()
	{
		return type;
	}
	
	/**
	 * Sets XACML 2.0 type.
	 * @param type
	 */
	public void setType(Type type)
	{
		this.type = type;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((type == null) ? 0 : type.hashCode());
		result = prime * result
				+ ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		XACMLAttribute other = (XACMLAttribute) obj;
		if (name == null)
		{
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (type != other.type)
			return false;
		if (value == null)
		{
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder(128);
		sb.append(name);
		sb.append(": ");
		sb.append(value);
		sb.append(" [");
		sb.append(type);
		sb.append("]");
		return sb.toString();
	}
}
