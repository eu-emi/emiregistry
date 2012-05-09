/*
 * Copyright (c) 2010 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE file for licencing information.
 *
 * Created on 25-10-2010
 * Author: K. Benedyczak <golbi@mat.umk.pl>
 */
package eu.emi.emir.pdp.local;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.herasaf.xacml.core.SyntaxException;
import org.herasaf.xacml.core.api.PDP;
import org.herasaf.xacml.core.context.RequestCtx;
import org.herasaf.xacml.core.context.ResponseCtx;
import org.herasaf.xacml.core.context.impl.DecisionType;
import org.herasaf.xacml.core.context.impl.MissingAttributeDetailType;
import org.herasaf.xacml.core.context.impl.ResultType;
import org.herasaf.xacml.core.context.impl.StatusDetailType;
import org.herasaf.xacml.core.context.impl.StatusType;
import org.herasaf.xacml.core.converter.URNToPolicyCombiningAlgorithmConverter;
import org.herasaf.xacml.core.policy.Evaluatable;
import org.herasaf.xacml.core.simplePDP.OrderedMapBasedSimplePolicyRepository;
import org.herasaf.xacml.core.simplePDP.SimplePDPConfiguration;
import org.herasaf.xacml.core.simplePDP.SimplePDPFactory;
import org.xml.sax.SAXException;

import eu.emi.client.util.Log;
import eu.emi.emir.pdp.PDPResult;
import eu.emi.emir.pdp.RegistryPDP;
import eu.emi.emir.security.Client;
import eu.emi.emir.security.util.ResourceDescriptor;



/**
 * HerasAF based implementation of a local XACML PDP. 
 * 
 * TODO Write custom PDP replacing SimplePDP. 
 * TODO Write custom PolicyStore. 
 * TODO Use PIP. 
 * @author golbi
 * @author a.memon
 */
public class LocalHerasafPDP implements RegistryPDP, PolicyListener
{
	private static final Logger log = Log.getLogger(Log.SECURITY, LocalHerasafPDP.class);
	private PDP engine;
	private RequestCreator requestMaker;
	private ReadWriteLock lock = new ReentrantReadWriteLock();
	
	public LocalHerasafPDP(String configurationFile) throws IOException, SyntaxException, JAXBException, SAXException
	{
		requestMaker = new RequestCreator();
		new LocalPolicyStore(this, configurationFile);
	}
	
	public void updateConfiguration(List<Evaluatable> policies, String algorithm)
	{
		SimplePDPConfiguration config = new SimplePDPConfiguration();
		URNToPolicyCombiningAlgorithmConverter policyCnv = 
			new URNToPolicyCombiningAlgorithmConverter();
		OrderedMapBasedSimplePolicyRepository repo = 
			new OrderedMapBasedSimplePolicyRepository();
		repo.deploy(policies);
		config.setRootCombiningAlgorithm(policyCnv.unmarshal(algorithm));
		config.setPolicyRetrievalPoint(repo);
		lock.writeLock().lock();
		engine = SimplePDPFactory.getSimplePDP(config);
		lock.writeLock().unlock();
	}
	
	private ResponseCtx authorize(RequestCtx request)
	{
		lock.readLock().lock();
		ResponseCtx resp = engine.evaluate(request);
		lock.readLock().unlock();
		return resp;
	}
	
	public PDPResult checkAuthorisation(Client c, String action,
			ResourceDescriptor d) throws Exception
	{
		RequestCtx request = requestMaker.createRequest(c, action, d);
		if (log.isDebugEnabled())
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			request.marshal(baos);
			log.debug("XACML request:" + baos.toString());
		}
		
		ResponseCtx response = authorize(request);
		if (log.isDebugEnabled())
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			response.marshal(baos);
			log.debug("XACML response:" + baos.toString());
		}

		List<ResultType> results = response.getResponse().getResults();
		if (results.size() != 1)
			throw new Exception("XACML herasAF PDP BUG: got " + results.size() +
				" results after asking about one resource. Should get 1.");
		ResultType result = results.get(0);
		return new PDPResult(getDecision(result), getComment(result));
	}

	private static PDPResult.Decision getDecision(ResultType result)
	{
		if (result.getDecision().equals(DecisionType.DENY))
			return PDPResult.Decision.DENY;
		if (result.getDecision().equals(DecisionType.PERMIT))
			return PDPResult.Decision.PERMIT;
		return PDPResult.Decision.UNCLEAR;
	}
	
	private static String getComment(ResultType result)
	{
		StatusType status = result.getStatus();
		if (status == null)
			return "";
		StringBuilder msg = new StringBuilder();
		if (status.getStatusCode() != null)
		{
			msg.append("Decission status code: [");
			msg.append(status.getStatusCode().getValue() + "]\n");
		}
		String m = status.getStatusMessage();
		if (m != null)
		{
			msg.append("Message: [" + m + "]\n");
		}
		StatusDetailType detail = status.getStatusDetail();
		if (detail != null)
		{
			List<MissingAttributeDetailType> mas = 
				detail.getMissingAttributeDetails();
			if (mas != null)
			{
				msg.append("The following attributes are missing: [");
				for (MissingAttributeDetailType ma: mas)
				{
					msg.append(" " + ma.getAttributeId());
				}
				msg.append(" ]");
			}
		}
		return msg.toString().trim();
	}
}
