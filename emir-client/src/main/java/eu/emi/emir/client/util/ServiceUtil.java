/**
 * 
 */
package eu.emi.emir.client.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
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

	private static List<String> lstAttributeNames = null;

	private synchronized static void initList() {
		lstAttributeNames = new CopyOnWriteArrayList<String>();
		for (ServiceBasicAttributeNames s : ServiceBasicAttributeNames.values()) {
			lstAttributeNames.add(s.getAttributeName());
		}
	}

	public static List<String> getAttributeNames() {
		if (lstAttributeNames == null) {
			initList();
		}
		return Collections.unmodifiableList(lstAttributeNames);
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
		// initialize log4j
		PropertyConfigurator.configure(path);
		// initialize java.util.logging
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
