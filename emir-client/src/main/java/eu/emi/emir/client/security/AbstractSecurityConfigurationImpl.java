/*
 * Copyright (c) 2008 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE file for licencing information.
 *
 * Created on May 28, 2008
 * Author: K. Benedyczak <golbi@mat.umk.pl>
 */

package eu.emi.emir.client.security;

import javax.net.ssl.SSLContext;

/**
 * Do nothing implementation of security configuration. Useful for subclassing.
 * @author K. Benedyczak
 */
public abstract class AbstractSecurityConfigurationImpl implements IAuthenticationConfiguration
{
	public boolean doHttpAuthn()
	{
		return false;
	}

	public boolean doSSLAuthn()
	{
		return false;
	}

	public String getHttpPassword()
	{
		return null;
	}

	public String getHttpUser()
	{
		return null;
	}

	public String getKeystore()
	{
		return null;
	}

	public String getKeystoreAlias()
	{
		return null;
	}

	public String getKeystoreKeyPassword()
	{
		return null;
	}

	public String getKeystorePassword()
	{
		return null;
	}

	public String getKeystoreType()
	{
		return null;
	}

	public String getTruststore()
	{
		return null;
	}

	public String getTruststorePassword()
	{
		return null;
	}

	public String getTruststoreType()
	{
		return null;
	}
	
	public SSLContext getSSLContext()
	{
		return null;
	}
	
	public abstract IAuthenticationConfiguration clone();
}
