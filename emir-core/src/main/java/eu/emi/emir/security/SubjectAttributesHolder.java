/*
 * Copyright (c) 2011 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE file for licencing information.
 *
 * Created on 25-01-2011
 * Author: K. Benedyczak <golbi@mat.umk.pl>
 */
package eu.emi.emir.security;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Holds subject's attributes as collected by one or more attribute sources.
 * There are two principal sets of attributes here: incarnation attributes and 
 * extra XACML attributes which are used for authorisation only. For incarnation
 * attributes two structures are stored: the actual attributes that shall be used,
 * and all permitted values. The latter are used when user specify the attribute by 
 * herself.  
 *  
 * @author golbi
 */
public class SubjectAttributesHolder implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	private Map<String, List<XACMLAttribute>> xacmlAttributes;
	private Map<String, String[]> defaultIncarnationAttributes;
	private Map<String, String[]> validIncarnationAttributes;

	/**
	 * All structures are initialized to be empty.
	 */
	public SubjectAttributesHolder()
	{
		xacmlAttributes = new HashMap<String, List<XACMLAttribute>>();
		defaultIncarnationAttributes = new HashMap<String, String[]>();
		validIncarnationAttributes = new HashMap<String, String[]>();
	}

	
	/**
	 * No XACML attributes, valid == default
	 * @param incarnationAttributes
	 */
	public SubjectAttributesHolder(Map<String, String[]> incarnationAttributes)
	{
		this(null, incarnationAttributes, incarnationAttributes);
	}
	
	public SubjectAttributesHolder(Map<String, String[]> defaultIncarnationAttributes,
			Map<String, String[]> validIncarnationAttributes)
	{
		this(null, defaultIncarnationAttributes, validIncarnationAttributes);
	}
	
	public SubjectAttributesHolder(List<XACMLAttribute> xacmlAttributes,
			Map<String, String[]> defaultIncarnationAttributes,
			Map<String, String[]> validIncarnationAttributes)
	{
		setXacmlAttributes(xacmlAttributes);
		setAllIncarnationAttributes(defaultIncarnationAttributes, validIncarnationAttributes);
	}

	/**
	 * Adds all attributes from the argument object. Existing attributes are overwritten:
	 * incarnation attributes with same names are simply replaced. 
	 * In case of XACML attributes all existing with the names contained in the argument
	 * list are removed first. 
	 * 
	 * @param from
	 */
	public void addAllOverwritting(SubjectAttributesHolder from)
	{
		if (from.getDefaultIncarnationAttributes() != null)
			defaultIncarnationAttributes.putAll(from.getDefaultIncarnationAttributes());
		if (from.getValidIncarnationAttributes() != null)
			validIncarnationAttributes.putAll(from.getValidIncarnationAttributes());
		if (from.getXacmlAttributes() != null)
		{
			for (XACMLAttribute xacmlAttribute: from.getXacmlAttributes())
				xacmlAttributes.remove(xacmlAttribute.getName());
			for (XACMLAttribute xacmlAttribute: from.getXacmlAttributes())
				addToXACMLList(xacmlAttribute);
		}
	}

	/**
	 * Adds all attributes from the argument object. Existing attributes are merged whenever 
	 * this makes sense: valid values and XACML attributes are merged, defaults for incarnation 
	 * are overriden.
	 * 
	 * @param from
	 */
	public void addAllMerging(SubjectAttributesHolder from)
	{
		//this one same as in case overriding
		if (from.getDefaultIncarnationAttributes() != null)
			defaultIncarnationAttributes.putAll(from.getDefaultIncarnationAttributes());
		if (from.getValidIncarnationAttributes() != null)
		{
			for(Map.Entry<String, String[]>e: from.getValidIncarnationAttributes().entrySet())
			{
				String key=e.getKey();
				String[]existing=validIncarnationAttributes.get(key);
				String[]newAttr=e.getValue();
				
				if(existing!=null)
				{
					Set<String> result = new LinkedHashSet<String>();
					for (String existingA: existing)
						result.add(existingA);
					for (String newA: newAttr)
						result.add(newA);
					validIncarnationAttributes.put(key, result.toArray(new String[result.size()]));
				} else
				{
					validIncarnationAttributes.put(key, newAttr);
				}
			}	
		}
		if (from.getXacmlAttributes() != null)
		{
			for (XACMLAttribute xacmlAttribute: from.getXacmlAttributes())
				addToXACMLList(xacmlAttribute);
		}
	}
	
	private void addToXACMLList(XACMLAttribute a)
	{
		List<XACMLAttribute> current = this.xacmlAttributes.get(a.getName());
		if (current == null)
		{
			current = new ArrayList<XACMLAttribute>();
			this.xacmlAttributes.put(a.getName(), current);
		}
		if (!current.contains(a))
			current.add(a);		
	}
	
	public List<XACMLAttribute> getXacmlAttributes()
	{
		List<XACMLAttribute> ret = new ArrayList<XACMLAttribute>();
		Collection<List<XACMLAttribute>> vals = xacmlAttributes.values();
		for (List<XACMLAttribute> val: vals)
			ret.addAll(val);
		return ret;
	}
	
	public void setXacmlAttributes(List<XACMLAttribute> xacmlAttributes)
	{
		this.xacmlAttributes = new HashMap<String, List<XACMLAttribute>>();
		if (xacmlAttributes != null)
		{
			for (XACMLAttribute a: xacmlAttributes)
				addToXACMLList(a);
		}
	}
	
	public Map<String, String[]> getDefaultIncarnationAttributes()
	{
		return defaultIncarnationAttributes;
	}

	public Map<String, String[]> getValidIncarnationAttributes()
	{
		return validIncarnationAttributes;
	}
	
	/**
	 * Sets incarnation attributes. Valid incarnation attributes must be a superset of default incarnation
	 * attributes.  
	 * @param defaultIncarnationAttributes
	 * @param validIncarnationAttributes
	 */
	public void setAllIncarnationAttributes(Map<String, String[]> defaultIncarnationAttributes, 
			Map<String, String[]> validIncarnationAttributes)
	{
		if (defaultIncarnationAttributes == null || validIncarnationAttributes == null)
			throw new IllegalArgumentException("Arguments can not be null");
		Iterator<Map.Entry<String, String[]>> it = defaultIncarnationAttributes.entrySet().iterator();
		while (it.hasNext()) 
		{
			Map.Entry<String, String[]> defA = it.next();
			if (validIncarnationAttributes.containsKey(defA.getKey()))
			{
				String[] validVals = validIncarnationAttributes.get(defA.getKey());
				String[] defaultVals = defA.getValue();
				for (String defaultVal: defaultVals)
				{
					boolean found = false;
					for (String validVal: validVals)
						if (validVal.equals(defaultVal))
						{
							found = true;
							break;
						}
					if (!found)
						throw new IllegalArgumentException("The default incarnation attribute >" + 
								defA.getKey() + "< value >" + defaultVal + 
								"< is not present among valid incarnation attributes.");
				}
			} else
			{
				throw new IllegalArgumentException("The default incarnation attribute " + 
						defA.getKey() + " is not present among valid incarnation attributes.");
			}
		}
		this.defaultIncarnationAttributes = new HashMap<String, String[]>();
		this.defaultIncarnationAttributes.putAll(defaultIncarnationAttributes);
		this.validIncarnationAttributes = new HashMap<String, String[]>();
		this.validIncarnationAttributes.putAll(validIncarnationAttributes);
	}
	
	public boolean isPresent()
	{
		if (defaultIncarnationAttributes.size() > 0)
			return true;
		if (xacmlAttributes.size() > 0)
			return true;
		return false;
	}
	
	private static void outputAttrsMap(StringBuilder sb, Map<String, String[]> attrs)
	{
		Iterator<Entry<String, String[]>> valid = 
			attrs.entrySet().iterator();
		while (valid.hasNext())
		{
			Entry<String, String[]> validE = valid.next();
			sb.append(validE.getKey());
			sb.append(": ");
			sb.append(Arrays.toString(validE.getValue()));
			if (valid.hasNext())
				sb.append("; ");
		}
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder(1024);
		boolean needEnter = false;
		if (validIncarnationAttributes.size() != 0)
		{
			sb.append("Valid attribute values: ");
			outputAttrsMap(sb, validIncarnationAttributes);
			needEnter = true;
		}
		if (defaultIncarnationAttributes.size() != 0)
		{
			if (needEnter)
				sb.append("\n");
			sb.append("Default attribute values: ");
			outputAttrsMap(sb, defaultIncarnationAttributes);
			needEnter = true;
		}
		if (xacmlAttributes.size() > 0) 
		{
			if (needEnter)
				sb.append("\n");
			sb.append("XACML authorization attributes: ");
			Iterator<Entry<String, List<XACMLAttribute>>> xacml = 
				xacmlAttributes.entrySet().iterator();
			while (xacml.hasNext())
			{
				Entry<String, List<XACMLAttribute>> validE = xacml.next();
				sb.append(validE.getValue());
			}
		}
		return sb.toString();
	}
}
