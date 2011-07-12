/**
 * 
 */
package eu.emi.dsr.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * @author a.memon
 * 
 */
public class Configuration implements Cloneable{
	private String path;
	private Properties props;
	/**
	 * 
	 */
	public Configuration(final String path) {
		if (path != null) {
			this.path = path;
		} else {
			try {
				throw new Exception("path cannot be empty");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		props = new Properties();
		if (props.isEmpty()) {
			try {
				props.load(new FileInputStream(new File(path)));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
	
	public Configuration(Properties properties) {
		this.props = properties;		
	}

	public void setProperty(String key, String value) {
		props.setProperty(key, value);
	}

	public String getProperty(String key) {
		return props.getProperty(key);
	}

	public Integer getIntegerProperty(String key) {
		return Integer.valueOf(props.getProperty(key));
	}
	
	public Boolean getBooleanProperty(String key) {
		return Boolean.valueOf(props.getProperty(key));
	}

	public Long getLongProperty(String key) {
		return Long.valueOf(props.getProperty(key));
	}

	public Double getDoubleProperty(String key) {
		return Double.valueOf(props.getProperty(key));
	}

	public Float getFloatProperty(String key) {
		return Float.valueOf(props.getProperty(key));
	}

	public String getProperty(String key, String defaultValue) {
		return props.getProperty(key, defaultValue);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Configuration clone() throws CloneNotSupportedException {
		return (Configuration) super.clone();
	}
}
