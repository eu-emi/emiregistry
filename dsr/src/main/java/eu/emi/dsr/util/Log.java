package eu.emi.dsr.util;

import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

/**
 * 
 * @author a.memon
 *
 */
public class Log {

	protected Log(){}

	/**
	 * logger prefix for services
	 */
	public static final String DSR="emiregistry.dsr";
	
	/**
	 * logger prefix for client stack
	 */
	public static final String DSRCLIENT="emiregistry.dsr.client";
	
	/**
	 * logger prefix for client stack
	 */
	public static final String DSRDB="emiregistry.dsr.db";

	public static final String SECURITY = "emiregistry.dsr.security";

	public static final String CLIENT = "emiregistry.dsr.client";
	
	/**
	 * returns a logger name, using the given prefix and the simple name
	 * of the given class
	 * 
	 * @param prefix - the prefix to use
	 * @param clazz - the class
	 * @return logger name
	 */
	public static String getLoggerName(String prefix, Class<?>clazz){
		return prefix+"."+clazz.getSimpleName();
	}
	
	/**
	 * returns a logger, using the given prefix and the simple name
	 * of the given class
	 * 
	 * @param prefix - the prefix to use
	 * @param clazz - the class
	 * @return logger
	 */
	public static Logger getLogger(String prefix, Class<?>clazz){
		return Logger.getLogger(prefix+"."+clazz.getSimpleName());
	}
	
	/** 
	 * log an error message to the default logger ("unicore.wsrflite")
	 * A human-friendly message is constructed and logged at "INFO" level.
	 * The stack trace is logged at "DEBUG" level.
	 * 
	 * @param message - the error message
	 * @param cause - the cause of the error
	 *
	 */
	public static void logException(String message, Throwable cause){
		logException(message,cause,Logger.getLogger(DSR));
	}
	
	
	
	/**
	 * log an error message to the specified logger.
	 * A human-friendly message is constructed and logged at "ERROR" level.
	 * The stack trace is logged at "DEBUG" level.
	 * 
	 * @param message - the error message
	 * @param cause - the cause of the error
	 * @param logger - the logger to use
	 */
	public static void logException(String message, Throwable cause, Logger logger){
		logger.error(message);
		if(cause!=null){
			logger.error("The root error was: "+getDetailMessage(cause));
			if(logger.isDebugEnabled())logger.debug("Stack trace",cause);
			else{
				logger.error("To see the full error stack trace, set log4j.logger."+logger.getName()+"=DEBUG");
			}
		}
	}
	
	
	public static void logException(Throwable cause){
		logException("", cause);
	}
	
	/**
	 * construct a (hopefully) useful error message from the root cause of an 
	 * exception
	 * 
	 * @param throwable - the exception
	 * @return datailed error message
	 */
	private static String getDetailMessage(Throwable throwable){
		StringBuilder sb=new StringBuilder();
		Throwable cause=throwable;
		String message=null;
		String type=null;type=cause.getClass().getName();
		do{
			type=cause.getClass().getName();
			message=cause.getMessage();
			cause=cause.getCause();
		}
		while(cause!=null);
		
		if(message!=null)sb.append(type).append(": ").append(message);
		else sb.append(type).append(" (no further message available)");
		return sb.toString();
	}
	
	/**
	 * construct a user-friendly error message 
	 * 
	 * @param message
	 * @param cause
	 * @return
	 */
	public static String createFaultMessage(String message, Throwable cause){
		return message+": "+getDetailMessage(cause);
	}
	
	public static void cleanLogContext(){
		MDC.remove("clientName");
	}
}