/*
 * Copyright (c) 2008 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE file for licencing information.
 *
 * Created on May 31, 2008
 * Author: K. Benedyczak <golbi@mat.umk.pl>
 */

package eu.emi.dsr.security;

/**
 * Enum with status of the signature verification.
 * @author K. Benedyczak
 */
public enum SignatureStatus 
{
	/**
	 * Signature wasn't checked for this request. It is not known
	 * if it is present or not.
	 */
	UNCHECKED,
	/**
	 * Request is unsigned.
	 */
	UNSIGNED, 
	/**
	 * Request is correctly signed.
	 */
	OK, 
	/**
	 * Request is signed but incorrectly.
	 */
	WRONG, 
	/**
	 * Request is correctly signed, but not all elements required by policy
	 * were signed. Can be treated as UNSIGNED.
	 */
	OK_BUT_NOT_IN_POLICY
}