/*
 * Copyright (c) 2010 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE file for licencing information.
 *
 * Created on 25-10-2010
 * Author: K. Benedyczak <golbi@mat.umk.pl>
 */
package eu.emi.emir.pdp;

/**
 * Wraps a decision which was produced by PDP. UNCLEAR status means DENY,
 * but additionally some extra logging may be performed.  
 * @author golbi
 */
public class PDPResult
{
	public enum Decision {PERMIT, DENY, UNCLEAR};
	
	private Decision decision;
	private String message;
	
	public PDPResult(Decision decision, String message)
	{
		super();
		this.decision = decision;
		this.message = message;
	}
	
	public Decision getDecision()
	{
		return decision;
	}
	
	public String getMessage()
	{
		return message;
	}
}
