/*
 * Copyright (c) 2010 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE file for licencing information.
 *
 * Created on 02-11-2010
 * Author: K. Benedyczak <golbi@mat.umk.pl>
 */
package eu.emi.dsr.pdp.local;

import java.util.List;

import org.herasaf.xacml.core.policy.Evaluatable;

/**
 * Used to listen about policy changes.
 * @author golbi
 *
 */
public interface PolicyListener
{
	public void updateConfiguration(List<Evaluatable> policies, String algorithm);
}
