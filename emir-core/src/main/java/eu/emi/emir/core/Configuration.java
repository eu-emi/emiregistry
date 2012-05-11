/**
 * 
 */
package eu.emi.emir.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import eu.emi.emir.client.util.Log;

/**
 * @author a.memon
 * @author g.szigeti
 * 
 */
public class Configuration implements Cloneable{
	private String path;
	private Properties props;
	/**
	 * 
	 */
	public Configuration(final String pathparam) {
		if (pathparam != null) {
			path = pathparam;
		} else {
			try {
				throw new Exception("path cannot be empty");
			} catch (Exception e) {
				Log.logException("", e);
			}
		}
		
		props = new Properties();
		if (props.isEmpty()) {
			try {
				props.load(new FileInputStream(new File(path)));
			} catch (FileNotFoundException e) {
				Log.logException("", e);
			} catch (IOException e) {
				Log.logException("", e);
			}
		}

	}
	
	public Properties getProperties(){
		return this.props;
	}
	
	public Configuration(Properties properties) {
		this.props = properties;		
	}

	public void setProperty(String key, String value) {
		props.setProperty(key, value);
	}

	public String getProperty(String key) {
		String retval = "";
		try {
			retval = props.getProperty(key);
		} catch (NullPointerException e) {
			return null;
		}
		return retval;
	}

	public Integer getIntegerProperty(String key) {
		return Integer.valueOf(props.getProperty(key));
	}
	
	public Boolean getBooleanProperty(String key, String defaultValue) {
		return Boolean.valueOf(props.getProperty(key,defaultValue));
	}
	
	public Boolean getBooleanProperty(String key) {
		return Boolean.valueOf(props.getProperty(key));
	}
	
	public Boolean getBooleanProperty(String key, Boolean defaultValue) {
		return Boolean.valueOf(props.getProperty(key,"false"));
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
