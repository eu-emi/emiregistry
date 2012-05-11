/*
 * Copyright (c) 2009 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE file for licencing information.
 *
 * Created on 2010-04-16
 * Author: K. Benedyczak <golbi@mat.umk.pl>
 */

package eu.emi.emir.security;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import eu.emi.client.util.Log;
import eu.emi.emir.util.PropertyHelper;


/**
 * Utility class used to configure attribute source
 * 
 * @author schuller
 * @author golbi
 */
public class AttributeSourceConfigurator {
	private static final Logger logger=Log.getLogger(Log.EMIR_SECURITY,AttributeSourceConfigurator.class);
	
	public static IAttributeSource configureAttributeSource(String name, Properties properties)
			throws Exception {
		String dotName = AttributeSourceFactory.BASE+"." + name + ".";
		String clazz = properties.getProperty(dotName + "class");
		if (clazz==null)
			throw new IllegalArgumentException("Inconsistent AuthZ chain definition: expected settings for <"+name+">");
		logger.debug("Creating attribute source " + name + " served by class <"+clazz+">");
		IAttributeSource auth=(IAttributeSource)(Class.forName(clazz).newInstance());
		//find parameters for this attribute source
		Map<String,String>params=new PropertyHelper(properties, 
			new String[]{dotName}).getFilteredMap();
		params.remove(dotName+"class");
		mapParams(auth,params);
		
		//if attribute source provides setProperties method, also pass all properties. Useful 
		//for attribute chains
		Method propsSetter = findSetter(auth.getClass(), "properties");
		if (propsSetter != null && propsSetter.getParameterTypes()[0].
			isAssignableFrom(Properties.class))
			propsSetter.invoke(auth, new Object[]{properties});
		
		return auth;
	}
	
	private static void mapParams(Object obj, Map<String,String>params){
		Class<?> clazz=obj.getClass();
		for(Map.Entry<String,String> en: params.entrySet()){
			String s=en.getKey();
			String paramName=s.substring(s.lastIndexOf(".")+1);
			Method m=findSetter(clazz, paramName);
			if(m==null){
				logger.warn("Can't map parameter <"+s+">");
				continue;
			}
			try{
				setParam(obj,m,en.getValue());
			}
			catch(Exception ex){
				logger.warn("Can't set value <"+en.getValue()+"> for parameter <"+s+">");
			}
		}
	}
	
	private static Method findSetter(Class<?> clazz, String paramName){
		for(Method m: clazz.getMethods()){
			if(m.getName().equalsIgnoreCase("set"+paramName) &&
				m.getParameterTypes().length > 0)return m;
		}
		return null;
	}
	
	private static void setParam(Object obj, Method m, String valueString)throws Exception{
		Object arg=valueString;
		if(m.getParameterTypes()[0].isAssignableFrom(int.class)){
			arg=Integer.parseInt(valueString);
		}
		else if(m.getParameterTypes()[0].isAssignableFrom(Integer.class)){
			arg=Integer.parseInt(valueString);
		}
		else if(m.getParameterTypes()[0].isAssignableFrom(long.class)){
			arg=Long.parseLong(valueString);
		}
		else if(m.getParameterTypes()[0].isAssignableFrom(Long.class)){
			arg=Long.parseLong(valueString);
		}
		else if(m.getParameterTypes()[0].isAssignableFrom(boolean.class)){
			arg=Boolean.valueOf(valueString);
		}
		else if(m.getParameterTypes()[0].isAssignableFrom(Boolean.class)){
			arg=Boolean.valueOf(valueString);
		}
		m.invoke(obj, new Object[]{arg});
	}
}
