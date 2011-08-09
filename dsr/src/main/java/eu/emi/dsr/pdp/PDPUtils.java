/*
 * Copyright (c) 2010 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE file for licencing information.
 *
 * Created on 2010-11-11
 * Author: K. Benedyczak <golbi@mat.umk.pl>
 */
package eu.emi.dsr.pdp;

import java.util.HashSet;
import java.util.Set;

import eu.emi.dsr.security.Client;



/**
 * Utilities shared by various PDPs. 
 * @author golbi
 */
public class PDPUtils
{
	public static final String LOCAL_SUBJECT_ROLE_ATTR = "role";
	public static final String LOCAL_SUBJECT_CONSIGNOR_ATTR = "consignor";
	public static final String LOCAL_SUBJECT_VO_ATTR = "vo";
	public static final String LOCAL_OWNER_ATTR = "owner";
	public static final String LOCAL_WSR_ATTR = "urn:unicore:wsresource";

	private static Set<String> reservedAttrs;
	static {
		reservedAttrs = new HashSet<String>();
		reservedAttrs.add(LOCAL_OWNER_ATTR);
		reservedAttrs.add(LOCAL_SUBJECT_CONSIGNOR_ATTR);
		reservedAttrs.add(LOCAL_SUBJECT_ROLE_ATTR);
		reservedAttrs.add(LOCAL_SUBJECT_VO_ATTR);
		reservedAttrs.add(LOCAL_WSR_ATTR);
	}
	
	public static void validateClient(Client c) throws IllegalArgumentException
	{
		if (c.getDistinguishedName() == null)
			throw new IllegalArgumentException("Subject DN is not available in authZ subsystem");
		if (c.getRole() == null)
			throw new IllegalArgumentException("Subject's role is not available in authZ subsystem");
		if (c.getRole().getName() == null)
			throw new IllegalArgumentException("Subject's role name is not available in authZ subsystem");
	}
	
	public static boolean checkGenericAttr(String name)
	{
		return !reservedAttrs.contains(name);
	}
}
