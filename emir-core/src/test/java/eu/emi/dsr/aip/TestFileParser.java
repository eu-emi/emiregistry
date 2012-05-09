/*
 * Copyright (c) 2010 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE file for licencing information.
 *
 * Created on 06-09-2010
 * Author: K. Benedyczak <golbi@mat.umk.pl>
 */
package eu.emi.dsr.aip;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import eu.emi.emir.aip.Attribute;
import eu.emi.emir.aip.AttributesFileParser;
import static org.junit.Assert.*;

/**
 * @author golbi
 * @author a.memon
 */
public class TestFileParser
{
	private static final String WRONG[] = {
		"", 
		"<dsdfsdf/>",
		"<fileAttributeSource><aaa/></fileAttributeSource>",
		"<fileAttributeSource><entry key=\"\"><aaa/></entry></fileAttributeSource>",
		"<fileAttributeSource><entry></entry></fileAttributeSource>",
		"<fileAttributeSource><entry key=\"\"><attribute name=\"\"><aaa/>" +
			"</attribute></entry></fileAttributeSource>",
		"<fileAttributeSource><entry key=\"\">" +
			"<attribute><aaa/></attribute></entry></fileAttributeSource>"};
	
	private static final String OK = 
			"<?xml version=\"1.0\" encoding=\"UTF-16\"?><fileAttributeSource>" +
			"   <entry key=\"CN=Stanisław Lem, C=PL\">" +
			"      <attribute name=\"role\"><value>user</value></attribute>" +
			"      <attribute name=\"empty\"/>" +
			"      <attribute name=\"xlogin\">" +
			"         <value>somebody</value>" +
			"         <value>nobody</value>" +
			"      </attribute>" +			
			"   </entry>" +
			"   <entry key=\"CN=Dead Man, C=US\">" +
			"      <attribute name=\"role\"><value>user</value></attribute>" +
			"   </entry>" +
			"</fileAttributeSource>";
	private static final String OK2 = 
		"<fileAttributeSource/>";
		
	private static final String OK3 = 
			"<?xml version=\"1.0\" encoding=\"UTF-16\"?><fileAttributeSource>" +
			"   <entry>"+
		    "      <key>CN=Stanislaw Lem, C=PL</key>" +
			"      <attribute name=\"role\"><value>user</value></attribute>" +
			"      <attribute name=\"empty\"/>" +
			"      <attribute name=\"xlogin\">" +
			"         <value>somebody</value>" +
			"         <value>nobody</value>" +
			"      </attribute>" +			
			"   </entry>" +
			"   <entry key=\"CN=Dead Man, C=US\">" +
			"      <attribute name=\"role\"><value>user</value></attribute>" +
			"   </entry>" +
			"</fileAttributeSource>";
	@Test
	public void testWrong() throws UnsupportedEncodingException
	{
		for (String toTest: WRONG)
		{
			AttributesFileParser parser = new AttributesFileParser(
					new ByteArrayInputStream(toTest.getBytes("UTF-16")));
			try
			{
				parser.parse();
				fail("Invalid XML was parsed correctly: " + toTest);
			} catch (IOException e)
			{
			}
		}
	}
	@Test
	public void testOK2() throws UnsupportedEncodingException
	{
		AttributesFileParser parser = new AttributesFileParser(
				new ByteArrayInputStream(OK2.getBytes("UTF-16")));
		try
		{
			Map<String, List<Attribute>> map = parser.parse();
			assertTrue(map.isEmpty());
		} catch (IOException e)
		{
			fail("Can't parse correct empty file");
		}
	}
	@Test
	public void testOK() throws UnsupportedEncodingException
	{
		AttributesFileParser parser = new AttributesFileParser(
				new ByteArrayInputStream(OK.getBytes("UTF-16")));
		try
		{
			Map<String, List<Attribute>> map = parser.parse();
			assertTrue(map.size() == 2);
			List<Attribute> attrs = map.get("CN=Stanisław Lem, C=PL");
			assertTrue(attrs != null && attrs.size() == 3);
			assertTrue(attrs.get(0).getName().equals("role") &&
					attrs.get(0).getValues().size() == 1 &&
					attrs.get(0).getValues().get(0).equals("user"));
			assertTrue(attrs.get(1).getName().equals("empty") &&
					attrs.get(1).getValues().size() == 0);
			assertTrue(attrs.get(2).getName().equals("xlogin") &&
					attrs.get(2).getValues().size() == 2 &&
					attrs.get(2).getValues().get(0).equals("somebody") &&
					attrs.get(2).getValues().get(1).equals("nobody"));
			
			attrs = map.get("CN=Dead Man, C=US");
			assertTrue(attrs != null && attrs.size() == 1);
			assertTrue(attrs.get(0).getName().equals("role") &&
					attrs.get(0).getValues().size() == 1 &&
					attrs.get(0).getValues().get(0).equals("user"));
		} catch (IOException e)
		{
			fail("Can't parse correct data");
		}
	}
	@Test
	public void testOK3() throws UnsupportedEncodingException
	{
		AttributesFileParser parser = new AttributesFileParser(
				new ByteArrayInputStream(OK3.getBytes("UTF-16")));
		try
		{
			Map<String, List<Attribute>> map = parser.parse();
			List<Attribute> attrs = map.get("CN=Stanislaw Lem, C=PL");
			assertTrue(attrs != null && attrs.size() == 3);
		} catch (IOException e)
		{
			e.printStackTrace();
			fail("Can't parse correct file");
		}
	}
}
