/*
 * Copyright (c) 2010 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE file for licencing information.
 *
 * Created on 26-10-2010
 * Author: K. Benedyczak <golbi@mat.umk.pl>
 */
package eu.emi.dsr.pdp.local;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.log4j.Logger;
import org.herasaf.xacml.core.SyntaxException;
import org.herasaf.xacml.core.policy.Evaluatable;
import org.herasaf.xacml.core.policy.PolicyMarshaller;
import org.herasaf.xacml.core.simplePDP.SimplePDPFactory;
import org.herasaf.xacml.core.utils.JAXBMarshallerConfiguration;
import org.xml.sax.SAXException;

import eu.emi.dsr.core.RegistryThreadPool;
import eu.emi.dsr.util.FileWatcher;
import eu.emi.dsr.util.Log;



/**
 * Initially loads policies, also able to reload them.<br>
 * Policies are read from a configurable directory. 
 * Configuration file is monitored for changes,
 * after a change policies from directory are reloaded. <br>
 * Directory contents is set of files, which should be either XACML 2.0 policies
 * or policy sets. Those policies are combined with a configurable combining algorithm.
 * @author golbi
 */
public class LocalPolicyStore
{
	public static final Logger log = Log.getLogger(Log.SECURITY, LocalPolicyStore.class);
	public static final String POLICY_ALG_DENY_OVERRIDES = "urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:deny-overrides";
	public static final String SPOLICY_ALG_DENY_OVERRIDES = "deny-overrides";
	public static final String POLICY_ALG_PERMIT_OVERRIDES = "urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:permit-overrides";
	public static final String SPOLICY_ALG_PERMIT_OVERRIDES = "permit-overrides";
	public static final String POLICY_ALG_FIRST_APPLICABLE = "urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:first-applicable";
	public static final String SPOLICY_ALG_FIRST_APPLICABLE = "first-applicable";
	public static final String POLICY_ALG_ONLY_ONE = "urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:only-one-applicable";
	public static final String SPOLICY_ALG_ONLY_ONE = "only-one-applicable";
	public static final String POLICY_ALG_ORDERED_DENY_OVERRIDES = "urn:oasis:names:tc:xacml:1.1:policy-combining-algorithm:ordered-deny-overrides";
	public static final String SPOLICY_ALG_ORDERED_DENY_OVERRIDES = "ordered-deny-overrides";
	public static final String POLICY_ALG_ORDERED_PERMIT_OVERRIDES = "urn:oasis:names:tc:xacml:1.1:policy-combining-algorithm:ordered-permit-overrides";
	public static final String SPOLICY_ALG_ORDERED_PERMIT_OVERRIDES = "ordered-permit-overrides";

	public static final String DIR_KEY = "localpdp.directory";
	public static final String DIR_DEF = "conf/policies";
	public static final String COMBINING_ALG_KEY = "localpdp.combiningAlg";
	public static final String COMBINING_ALG_DEF = POLICY_ALG_FIRST_APPLICABLE;
	public static final String WILDCARD_KEY = "localpdp.filesWildcard";
	public static final String WILDCARD_DEF = "*.xml";
	private Map<String, String> algKeys2FullNames;
	
	private String configurationFile;
	private PolicyListener pdp;
	
	
	public LocalPolicyStore(PolicyListener pdp, String configurationFile) 
		throws IOException, SyntaxException, JAXBException, SAXException
	{
		this(pdp, configurationFile, 5000);
	}
	
	public LocalPolicyStore(PolicyListener pdp, String configurationFile, int interval) 
		throws IOException, SyntaxException, JAXBException, SAXException
	{
		//uhhh herasf developers are OO gurus...
		SimplePDPFactory.getSimplePDP();
		
		this.configurationFile = configurationFile;
		this.pdp = pdp;
		JAXBContext policyContext = JAXBContext.newInstance("org.herasaf.xacml.core.policy.impl");
		PolicyMarshaller.setJAXBContext(policyContext);
		JAXBMarshallerConfiguration jmc = new JAXBMarshallerConfiguration();
		jmc.setValidateParsing(true);
		//dirty trick to overcome stupid .getClass.getClassloader() in herasf code
		URL url = LocalPolicyStore.class.getResource("/local/xacmlpolicy-2.0.xsd");
		jmc.setSchemaByPath("url:" + url.toExternalForm());
		PolicyMarshaller.setJAXBMarshallerConfiguration(jmc);
		initPolicyNameMap();
		reload();
		startConfigWatcher(interval);
	}
	
	private void reload() throws IOException, SyntaxException
	{
		Properties cfg = new Properties();
		cfg.load(new FileInputStream(configurationFile));
		String directory = cfg.getProperty(DIR_KEY, DIR_DEF);
		File dir = new File(directory);
		if (!dir.isDirectory())
			throw new IOException("Configured XACML policies repository " + 
					directory + " doesn't exist, or is not a directory.");
		String talg = cfg.getProperty(COMBINING_ALG_KEY, COMBINING_ALG_DEF);
		String alg = algKeys2FullNames.get(talg);
		if (alg == null)
			throw new IOException("Configured XACML policy combining algorithm " +
					talg + " is unknown.");

		String wildcard = cfg.getProperty(WILDCARD_KEY, WILDCARD_DEF);
		
		FilenameFilter filter = new WildcardFileFilter(wildcard);
		String []files = dir.list(filter);
		if (files.length == 0)
			throw new IOException("Configured XACML policies repository " + 
					directory + " is empty");

		if (log.isDebugEnabled())
		{
			log.debug("Using policy directory: " + directory + 
				" with files matching " + wildcard +
				" (found " + files.length + " policies)");
			log.debug("Using policy combining algorithm: " + alg);
		}
		
		Arrays.sort(files);
		List<Evaluatable> policies = new ArrayList<Evaluatable>();
		for (String policyF: files)
		{
			File p = new File(dir.getAbsolutePath() + File.separator + policyF);
			try
			{

				Evaluatable policy = PolicyMarshaller.unmarshal(p);
				policies.add(policy);
			} catch(SyntaxException e)
			{
				throw new SyntaxException("Syntax error in file " 
						+ policyF, e);
			}
		}
		
		pdp.updateConfiguration(policies, alg);
	}
	

	private void startConfigWatcher(int interval)
	{
		Runnable r = new Runnable()
		{
			public void run()
			{
				log.info("Local XACML PDP configuration file was modified, re-configuring.");
				try
				{
					reload();
				} catch (IOException e)
				{
					log.error("Error reading XAML PDP configuration (file " + configurationFile + "): "
							+ e.toString(), e);
				} catch (SyntaxException e)
				{
					log.error("Error parsing XAML policies: "
							+ e.toString() + " " + e.getCause().toString(), e);				
				}
			}
		};
		try
		{
			File confFile = new File(configurationFile);
			FileWatcher fw = new FileWatcher(confFile, r);
			RegistryThreadPool.getScheduledExecutorService().scheduleWithFixedDelay(
					fw, interval, interval, TimeUnit.MILLISECONDS);
		}catch(FileNotFoundException fex)
		{
			log.error("XAML PDP configuration file <" + configurationFile + "> not found.");
		}
	}
	
	private void initPolicyNameMap()
	{
		algKeys2FullNames = new HashMap<String, String>();
		algKeys2FullNames.put(POLICY_ALG_DENY_OVERRIDES, POLICY_ALG_DENY_OVERRIDES);
		algKeys2FullNames.put(POLICY_ALG_FIRST_APPLICABLE, POLICY_ALG_FIRST_APPLICABLE);
		algKeys2FullNames.put(POLICY_ALG_ONLY_ONE, POLICY_ALG_ONLY_ONE);
		algKeys2FullNames.put(POLICY_ALG_ORDERED_DENY_OVERRIDES, POLICY_ALG_ORDERED_DENY_OVERRIDES);
		algKeys2FullNames.put(POLICY_ALG_ORDERED_PERMIT_OVERRIDES, POLICY_ALG_ORDERED_PERMIT_OVERRIDES);
		algKeys2FullNames.put(POLICY_ALG_PERMIT_OVERRIDES, POLICY_ALG_PERMIT_OVERRIDES);
		algKeys2FullNames.put(SPOLICY_ALG_DENY_OVERRIDES, POLICY_ALG_DENY_OVERRIDES);
		algKeys2FullNames.put(SPOLICY_ALG_FIRST_APPLICABLE, POLICY_ALG_FIRST_APPLICABLE);
		algKeys2FullNames.put(SPOLICY_ALG_ONLY_ONE, POLICY_ALG_ONLY_ONE);
		algKeys2FullNames.put(SPOLICY_ALG_ORDERED_DENY_OVERRIDES, POLICY_ALG_ORDERED_DENY_OVERRIDES);
		algKeys2FullNames.put(SPOLICY_ALG_ORDERED_PERMIT_OVERRIDES, POLICY_ALG_ORDERED_PERMIT_OVERRIDES);
		algKeys2FullNames.put(SPOLICY_ALG_PERMIT_OVERRIDES, POLICY_ALG_PERMIT_OVERRIDES);
	}
}
