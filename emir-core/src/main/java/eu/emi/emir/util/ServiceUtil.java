/**
 * 
 */
package eu.emi.emir.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.LogManager;

import org.apache.log4j.PropertyConfigurator;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import eu.emi.emir.client.ServiceBasicAttributeNames;
import eu.emi.emir.client.util.Log;
import eu.emi.emir.exception.InvalidServiceDescriptionException;
import eu.emi.emir.validator.ValidatorFactory;
import eu.unicore.util.configuration.ConfigurationException;

/**
 * 
 * Utility class to (Un)Marshal the Service information
 * 
 * @author a.memon
 * 
 */
public class ServiceUtil {

	// public static Service fromJSON(JSONObject obj){
	// return null;
	// }
	//
	// public static JSONObject toJSON(Service serviceDescription){
	// return null;
	// }

	public static SimpleDateFormat ServiceDateFormat = new SimpleDateFormat(
			"dd-mm-yyyy,HH:mm");

	public static SimpleDateFormat ISODateFormat = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ssZ");

	public static SimpleDateFormat UTCISODateFormat = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	private static List<String> lstNames;

	/**
	 * Checks the service description being registered contains the mandatory
	 * service attributes
	 * 
	 * @param serviceDesc
	 * @throws InvalidServiceDescriptionException
	 * @throws ParseException 
	 * @throws ConfigurationException 
	 * @throws JSONException
	 */
	public synchronized static boolean isValidServiceInfo(JSONObject jo)
			throws InvalidServiceDescriptionException, ConfigurationException, JSONException, ParseException {
		return ValidatorFactory.getRegistrationValidator().validateInfo(jo);
	}

	/**
	 * Checks the service description being updated contains the service
	 * endpoint ID attributes
	 * 
	 * @param serviceDesc
	 * @throws InvalidServiceDescriptionException 
	 */
	public synchronized static boolean isValidRemovedServiceInfo(JSONObject jo)
			throws InvalidServiceDescriptionException {
		return ValidatorFactory.getRegistrationValidator().validateEndpointIDInfo(jo);
	}

	public synchronized static String toUTCFormat(Date d) {
		SimpleDateFormat formatter = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
		String timestamp = formatter.format(d);
		return timestamp;
	}

	public static Date toUTCFormat(String d) throws ParseException {
		SimpleDateFormat formatter = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
		Date timestamp = formatter.parse(d);
		return timestamp;
	}

	public synchronized static String convertStreamToString(InputStream is)
			throws IOException {
		/*
		 * To convert the InputStream to String we use the Reader.read(char[]
		 * buffer) method. We iterate until the Reader return -1 which means
		 * there's no more data to read. We use the StringWriter class to
		 * produce the string.
		 */
		if (is != null) {
			Writer writer = new StringWriter();

			char[] buffer = new char[1024];
			try {
				Reader reader = new BufferedReader(new InputStreamReader(is,
						"UTF-8"));
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
			} finally {
				is.close();
			}
			return writer.toString();
		} else {
			return "";
		}
	}

	private static void initList() {
		if (lstNames == null) {
			lstNames = new ArrayList<String>();
			for (ServiceBasicAttributeNames s : ServiceBasicAttributeNames
					.values()) {
				lstNames.add(s.getAttributeName());
			}
		}

	}

	public static List<String> getAttributeNames() {
		initList();
		return lstNames;
	}

	/**
	 * Simultaneously initializing the log4j and log4j 
	 * 
	 * @param path
	 */
	public static void initLogger(String path) {
		if (path == null) {
			return;
		}
		//initialize log4j
		PropertyConfigurator.configure(path);
		//initialize java.util.logging
		LogManager l = LogManager.getLogManager();
		try {
			l.readConfiguration(new FileInputStream(new File(path)));
		} catch (SecurityException e) {
			Log.logException("", e);
		} catch (FileNotFoundException e) {
			Log.logException("", e);
		} catch (IOException e) {
			Log.logException("", e);
		}

	}

}
