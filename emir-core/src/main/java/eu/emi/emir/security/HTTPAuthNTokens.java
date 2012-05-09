package eu.emi.emir.security;

import java.io.Serializable;


/**
 * Holds authentication data in simple form.
 * @author K. Benedyczak
 */
public class HTTPAuthNTokens implements Serializable
{
	private static final long serialVersionUID = 3425680289291775268L;
	private String userName;
	private transient String passwd;
	
	public HTTPAuthNTokens(String userName, String passwd)
	{
		super();
		this.userName = userName;
		this.passwd = passwd;
	}

	public String getPasswd()
	{
		return passwd;
	}
	public String getUserName()
	{
		return userName;
	}
}
