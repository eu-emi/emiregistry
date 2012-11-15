/**
 * 
 */
package eu.emi.emir.client.util;

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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.LogManager;

import org.apache.log4j.PropertyConfigurator;
import eu.emi.emir.client.ServiceBasicAttributeNames;

/**
 * 
 * Utility class to (Un)Marshal the Service information
 * 
 * @author a.memon
 * 
 */
public class ServiceUtil {

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
		if (DateUtil.lstNames == null) {
			DateUtil.lstNames = new ArrayList<String>();
			for (ServiceBasicAttributeNames s : ServiceBasicAttributeNames
					.values()) {
				DateUtil.lstNames.add(s.getAttributeName());
			}
		}

	}

	public static List<String> getAttributeNames() {
		initList();
		return DateUtil.lstNames;
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
