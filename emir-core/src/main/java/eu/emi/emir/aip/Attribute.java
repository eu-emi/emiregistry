/*
 * Copyright (c) 2010 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE file for licencing information.
 *
 * Created on 06-09-2010
 * Author: K. Benedyczak <golbi@mat.umk.pl>
 */
package eu.emi.emir.aip;

import java.util.List;

/**
 * Represents one attribute read form the attributes files i.e. name and value(s).
 * @author golbi
 */
public class Attribute
{
	private String name;
	private List<String> values;
	
	public Attribute(String name, List<String> values)
	{
		this.name = name;
		this.values = values;
	}
	
	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
	}
	public List<String> getValues()
	{
		return values;
	}
	public void setValues(List<String> values)
	{
		this.values = values;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((values == null) ? 0 : values.hashCode());
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
		Attribute other = (Attribute) obj;
		if (name == null)
		{
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (values == null)
		{
			if (other.values != null)
				return false;
		} else if (!values.equals(other.values))
			return false;
		return true;
	}
}
