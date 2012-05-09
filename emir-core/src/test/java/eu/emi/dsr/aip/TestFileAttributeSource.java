/*
 * Copyright (c) 2010 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE file for licencing information.
 *
 * Created on 06-09-2010
 * Author: K. Benedyczak <golbi@mat.umk.pl>
 */
package eu.emi.dsr.aip;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.security.auth.x500.X500Principal;

import org.junit.Test;

import eu.emi.emir.aip.FileAttributeSource;
import eu.emi.emir.security.SecurityTokens;
import eu.emi.emir.security.SubjectAttributesHolder;
import static org.junit.Assert.*;

/**
 * @author golbi
 * @author a.memon
 */
public class TestFileAttributeSource
{
	public static final String NAME = "TST1";

	
	private FileAttributeSource init(String file, String matching)
	{
		FileAttributeSource src = new FileAttributeSource();
		src.setFile(file);
		src.setMatching(matching);
		try
		{
			src.init(NAME);
		} catch (Exception e)
		{
			e.printStackTrace();
			fail("Can't init AttributeSource: " + e);
		}
		
		assertTrue(NAME.equals(src.getName()));
		assertTrue(src.getStatusDescription().contains("OK"));
		return src;
	}
	
	@Test
	public void testStrict()
	{
		FileAttributeSource src = init("src/test/resources/conf/users/testUdb-strict.xml", "strict");
		
		SecurityTokens tokens = new SecurityTokens();
		tokens.setUserName(new X500Principal("EMAILADDRESS=emiregistry@user.eu, CN=EMIRegistry-Demo-User, OU=JSC, O=Forschungszentrum Juelich GmbH, L=Juelich, C=DE"));
		
		try
		{
			SubjectAttributesHolder holder = src.getAttributes(tokens, null);
			Map<String, String[]> valid = holder.getValidIncarnationAttributes();
			assertTrue(valid.size() == 1);
			assertTrue(valid.get("role")[0].equalsIgnoreCase("serviceowner"));
		} catch (IOException e)
		{
			e.printStackTrace();
			fail("Can't get attributes: " + e);
		}
	}

	@Test
	public void testRegExp()
	{
		FileAttributeSource src = init("src/test/resources/conf/users/testUdb-regexp.xml", "regexp");
		
		SecurityTokens tokens = new SecurityTokens();
		tokens.setConsignorTrusted(true);
		SubjectAttributesHolder map;
		
		try
		{
			tokens.setUserName(new X500Principal("CN=Stanisław Lem, O=ICM, C=PL"));
			map = src.getAttributes(tokens, null);
			assertTrue(map.getValidIncarnationAttributes().size() == 1);

			tokens.setUserName(new X500Principal("CN=Stanisław Lem, O=ACK, C=PL"));
			map = src.getAttributes(tokens, null);
			assertTrue(map.getValidIncarnationAttributes().size() == 1);

			tokens.setUserName(new X500Principal("CN=Stanisław Lem, O=I, C=PL"));
			map = src.getAttributes(tokens, null);
			assertTrue(map.getValidIncarnationAttributes().size() == 1);
						
			tokens.setUserName(new X500Principal("CN=Dead Man, C=US"));
			map = src.getAttributes(tokens, null);
			assertTrue(map.getValidIncarnationAttributes().size() == 1);
		} catch (IOException e)
		{
			e.printStackTrace();
			fail("Can't get attributes: " + e);
		}
	}


	
	
	public void testRefresh() throws InterruptedException
	{
		String srcF = "src/test/resources/conf/users/testUdb-strict.xml";
		String dst = "target/test-classes/testUdb-copy.xml";
		try
		{
			File f = new File(dst);
			f.delete();
			copyFile(srcF, dst);

			FileAttributeSource src = init(dst, "regexp");

			SecurityTokens tokens = new SecurityTokens();
			SubjectAttributesHolder holder;
			Map<String, String[]> def;

			tokens.setUserName(new X500Principal("EMAILADDRESS=emiregistry@user.eu, CN=EMIRegistry-Demo-User, OU=JSC, O=Forschungszentrum Juelich GmbH, L=Juelich, C=DE"));
			
			holder = src.getAttributes(tokens, null);
			def = holder.getDefaultIncarnationAttributes();
			
			System.out.println(def.size());
			assertTrue(def.size() == 1);
			assertTrue(def.get("role") != null && def.get("role").length == 1
					&& def.get("role")[0].equals("serviceowner"));
			Thread.sleep(1000);
			copyFile("src/test/resources/conf/users/testUdb-regexp.xml", 
				dst);
			
			holder = src.getAttributes(tokens, null);
			def = holder.getDefaultIncarnationAttributes();
			assertTrue(def.size() == 1);
			assertTrue(def.get("role") != null && def.get("role").length == 1
					&& def.get("role")[0].equals("banned"));

		} catch (IOException e)
		{
			e.printStackTrace();
			fail("Can't get attributes: " + e);
		}
	}




	private static void copyFile(String from, String to) throws IOException
	{
		BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(to));
		InputStream fis = new BufferedInputStream(new FileInputStream(from));
		int b;
		while ((b = fis.read()) != -1)
			os.write(b);
		os.close();
		fis.close();
	}



}
