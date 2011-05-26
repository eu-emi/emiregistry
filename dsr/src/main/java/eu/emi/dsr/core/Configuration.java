/**
 * 
 */
package eu.emi.dsr.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import javax.naming.InitialContext;

/**
 * @author a.memon
 * 
 */
public class Configuration {
	private static Properties properties;
	private String path;

	/**
	 * 
	 */
	public Configuration(final String path) {
		if (path != null) {
			this.path = path;
			init();
		} else {
			try {
				throw new Exception("path cannot be empty");
			} catch (Exception e) {
				e.printStackTrace();
			}
			//TODO set defaults
		}
		
	}
	
	/**
	 * 
	 */
	public Configuration() {
		init();
	}
	
	private void init(){
		properties = new Properties();
	}

	/**
	 * 
	 */
	public void bootstrapProperties() {
		if (properties.isEmpty()) {
			try {
				properties.load(new FileInputStream(new File(path)));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void setProperty(String key, String value){
		properties.setProperty(key, value);
	}

	public static String getProperty(String key) {
		return properties.getProperty(key);
	}

	public static Integer getIntegerProperty(String key) {
		return Integer.valueOf(properties.getProperty(key));
	}

	public static Long getLongProperty(String key) {
		return Long.valueOf(properties.getProperty(key));
	}

	public static Double getDoubleProperty(String key) {
		return Double.valueOf(properties.getProperty(key));
	}

	public static Float getFloatProperty(String key) {
		return Float.valueOf(properties.getProperty(key));
	}

	public static String getProperty(String key, String value) {
		return properties.getProperty(key);
	}
}
