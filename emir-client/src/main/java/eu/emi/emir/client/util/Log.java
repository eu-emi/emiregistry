package eu.emi.emir.client.util;

/**
 * Log utility class to set emir loggers
 * @author a.memon
 *
 */
public class Log extends eu.unicore.util.Log{

	/**
	 * logger prefix for core services
	 */
	public static final String EMIR_CORE="emir.core";
	
	/**
	 * logger prefix for client stack
	 */
	public static final String EMIR_CLIENT="emir.client";
	
	/**
	 * logger prefix for db stack
	 */
	public static final String EMIR_DB="emir.db";

	/**
	 * logger prefix for security stack
	 */
	public static final String EMIR_SECURITY = "emir.security";
	/**
	 * logger prefix for http(s) stack
	 */
	public static final String EMIR_HTTPSERVER = "emir.httpserver";
	
	
}
