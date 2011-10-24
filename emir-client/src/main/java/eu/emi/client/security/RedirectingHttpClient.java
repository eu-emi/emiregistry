/*
 * Copyright (c) 2011 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE file for licencing information.
 *
 * Created on 09-01-2011
 * Author: K. Benedyczak <golbi@mat.umk.pl>
 */
package eu.emi.client.security;

import java.io.IOException;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpState;

/**
 * Extension of {@link HttpClient} which handles redirects. Useful as
 * Jakarta Commons HttpClient doesn't support redirects in case of POSTs.
 *  
 * @author schuller
 * @author golbi
 */
public class RedirectingHttpClient extends HttpClient
{
	private boolean closeConnection;
	private int maxRedirects;
	public static final int DEFAULT_MAX_REDIRECTS = 3;
	private int redirectsCount;

	public RedirectingHttpClient()
	{
		this(true, DEFAULT_MAX_REDIRECTS);
	}
	
	public RedirectingHttpClient(boolean disableConnectionClose)
	{
		this(disableConnectionClose, DEFAULT_MAX_REDIRECTS);
	}
	
	public RedirectingHttpClient(boolean closeConnection, int maxRedirects)
	{
		super();
		this.maxRedirects = maxRedirects;
		this.closeConnection = closeConnection;
		redirectsCount = 0;
	}
	
	@Override
	public int executeMethod(HostConfiguration hostConfig, HttpMethod method, HttpState httpState) throws IOException, HttpException {
		if(closeConnection){
			method.setRequestHeader("Connection", "close");
		}
		int i=super.executeMethod(hostConfig, method, httpState);
		String redirectLocation;
		Header locationHeader = method.getResponseHeader("location");
		if (locationHeader != null && redirectsCount < maxRedirects) {
			redirectLocation = locationHeader.getValue();				
			method.setURI(new org.apache.commons.httpclient.URI(redirectLocation,false));
			redirectsCount++;
			return executeMethod(hostConfig, method, httpState);
		}
		redirectsCount = 0;
		return i;
	}
}
