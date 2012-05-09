/*
 * Copyright (c) 2010 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE file for licencing information.
 *
 * Created on 06-09-2010
 * Author: K. Benedyczak <golbi@mat.umk.pl>
 */
package eu.emi.emir.aip;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;



/**
 * Utility class to parse file with attributes. Format is described in {@link FileAttributeSource}.
 * StAX parser is used.
 * @author golbi
 */
public class AttributesFileParser
{
	private InputStream is;
	private static final String MAIN_EL = "fileAttributeSource";
	private static final String ENTRY_EL = "entry";
	private static final String ATTR_EL = "attribute";
	private static final String VALUE_EL = "value";
	private static final String KEY_EL = "key";
	private static final QName KEY_AT = new QName("key");
	private static final QName NAME_AT = new QName("name");
	
	public AttributesFileParser(InputStream is)
	{
		this.is = is;
	}
	
	public Map<String, List<Attribute>> parse() throws IOException
	{
		Map<String, List<Attribute>> ret = new LinkedHashMap<String, List<Attribute>>();
		XMLInputFactory factory = XMLInputFactory.newInstance();
		XMLEventReader reader = null;
		try
		{
			reader = factory.createXMLEventReader(new BufferedInputStream(is));
		} catch (XMLStreamException e)
		{
			throw new IOException("Can't initialize XML parser: " + e.toString());
		}
		
		StartElement start = getNextElement(reader, MAIN_EL, null);
		if (start == null)
			throw new IOException("File must start with " + MAIN_EL + " element.");

		//entries loop
		while (reader.hasNext())
		{
			StartElement entry = getNextElement(reader, ENTRY_EL, MAIN_EL);
			if (entry == null)
				break;
			
			String key=null;
			//check if key is given as attribute
			javax.xml.stream.events.Attribute a = entry.getAttributeByName(KEY_AT);
			if (a != null)
			{
				key = a.getValue();
			}else
			{
				//must have key as a sub-element
				StartElement keyElement = getNextElement(reader, KEY_EL, ATTR_EL);
				if(keyElement!=null){
					key=readTextContent(reader).trim();
				}
			}

			if(key==null){
				throw new IOException("Got entry without key!");
			}
			
			if (ret.containsKey(key))
				throw new IOException("The same entry key used twice: " + key);
			
			List<Attribute> attributes = new ArrayList<Attribute>();
			
			//attributes loop
			while (reader.hasNext())
			{
				StartElement attribute = getNextElement(reader, ATTR_EL, ENTRY_EL);
				if (attribute == null)
					break;
				javax.xml.stream.events.Attribute a2 = attribute.getAttributeByName(NAME_AT);
				if (a2 == null)
					throw new IOException("Got attribute without name attribute");
				String attributeName = a2.getValue();
				List<String> values = new ArrayList<String>();
				Attribute toAdd = new Attribute(attributeName, values);
				//values loop
				while (reader.hasNext())
				{
					StartElement value = getNextElement(reader, VALUE_EL, ATTR_EL);
					if (value == null)
						break;
					String valueStr = readTextContent(reader);
					values.add(valueStr);
				}
				attributes.add(toAdd);
			}			
			ret.put(key, attributes);
		}
		is.close();
		return ret;
	}
	
	private XMLEvent readNext(XMLEventReader reader) throws IOException
	{
		try
		{
			return reader.nextEvent();
		} catch (XMLStreamException e)
		{
			throw new IOException("Error in XML syntax at " + e.getLocation() + 
					": " + e.getMessage());
		}
	}
	
	private StartElement getNextElement(XMLEventReader reader, String searchedElement, 
			String limitElement) throws IOException
	{
		while (reader.hasNext())
		{
			XMLEvent event = readNext(reader);
			if (event instanceof StartElement)
			{
				StartElement el = (StartElement)event;
				if (!el.getName().getLocalPart().equals(searchedElement))
					throw new IOException("Expected " + searchedElement + 
							" but found " + el.getName());
				return el;
			}
			if (event instanceof EndElement)
			{
				EndElement end = (EndElement) event;
				if (limitElement != null && end.getName().getLocalPart().equals(limitElement))
					return null;
			}
		}
		return null;
	}

	private String readTextContent(XMLEventReader reader) throws IOException
	{
		try
		{
			return reader.getElementText();
		} catch (XMLStreamException e)
		{
			throw new IOException("Error in XML syntax at " + e.getLocation() + 
					": " + e.getMessage());
		}
	}
}
