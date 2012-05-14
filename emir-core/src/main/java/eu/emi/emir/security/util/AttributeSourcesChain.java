/*********************************************************************************
 * Copyright (c) 2010 Forschungszentrum Juelich GmbH 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * (1) Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the disclaimer at the end. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution.
 * 
 * (2) Neither the name of Forschungszentrum Juelich GmbH nor the names of its 
 * contributors may be used to endorse or promote products derived from this 
 * software without specific prior written permission.
 * 
 * DISCLAIMER
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ********************************************************************************/

package eu.emi.emir.security.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.log4j.NDC;

import eu.emi.emir.DSRServer;
import eu.emi.emir.client.util.Log;
import eu.emi.emir.security.AttributeSourceConfigurator;
import eu.emi.emir.security.AttributeSourceFactory;
import eu.emi.emir.security.AuthorisationException;
import eu.emi.emir.security.IAttributeSource;
import eu.emi.emir.security.SecurityTokens;
import eu.emi.emir.security.SubjectAttributesHolder;

/**
 * IAttributeSource implementation that combines the results from a chain of attribute sources using
 * a configurable combining policy:
 * <ul>
 *  <li>FIRST_APPLICABLE: the first source returning any result is used</li>
 *  <li>FIRST_ACCESSIBLE: the first accessible (i.e. not throwing an exception) source is used</li>
 *  <li>MERGE_LAST_OVERRIDES (default): all results are combined, so that the later 
 *      attribute sources in the chain can override earlier ones</li>
 *  <li>MERGE : all results are combined, and valid attribute values of the same attribute are merged.
 *  Note that in case of default values for incarnation attributes (used if user doesn't request
 *  a particular value) merging is not done, but values are overridden. This is as for those
 *  attributes in nearly all cases multiple values doesn't make sense (user can have one uid,
 *  primary gid, job may be submitted only to one queue).</li>  
 * </ul> 
 * 
 * @author schuller
 * @author golbi
 */
public class AttributeSourcesChain implements IAttributeSource{

	private final static Logger logger=Log.getLogger(Log.EMIR_SECURITY, AttributeSourcesChain.class);
	
	private List<IAttributeSource> chain;
	private List<String> names;
	private String name;
	private String orderString;
	private String combinerName;
	private CombiningPolicy combiner;
	private Properties properties = null;
	
	
	/**
	 * will initialise all the authorisers in the chain by calling their init() method
	 */
	@Override
	public void init(String name)throws Exception{
		this.name = name;
		initOrder();
		for(int i=0; i<chain.size(); i++){
			NDC.push(names.get(i));
			chain.get(i).init(names.get(i));
			NDC.pop();
		}
		initCombiningPolicy();
	}
	
	/**
	 * combines results from all configured attribute sources
	 */
	@Override
	public SubjectAttributesHolder getAttributes(SecurityTokens tokens, SubjectAttributesHolder unused)
			throws IOException, AuthorisationException {
		SubjectAttributesHolder resultMap = new SubjectAttributesHolder();
		for (IAttributeSource a: chain){
			NDC.push(a.getName());
			try{
				SubjectAttributesHolder current = a.getAttributes(tokens,resultMap);
				if (logger.isDebugEnabled()) {
					logger.debug("Attribute source " + a.getName() + 
							" returned the following attributes:\n" + current);
				}
				
				if (!combiner.combineAttributes(resultMap,current)) {
					logger.debug("Attributes combiner decided to stop processing of attribute " +
							"sources at " + a.getName() + ".");
					break;
				}
			}
			catch(IOException e){
				Log.logException("Attribute source <"+a.getClass()+"> not available.", e, logger);
			}
			finally{
				NDC.pop();
			}
		}
		return resultMap;
	}

	@Override
	public String getStatusDescription() {
		StringBuilder sb=new StringBuilder();
		String lineSep=System.getProperty("line.separator");
		for(IAttributeSource a: chain){
			sb.append(a.getStatusDescription());
			sb.append(lineSep);
		}
		if(chain.size()>1){
			sb.append("Combining policy: ").append(String.valueOf(combiner));
			sb.append(lineSep);
		}
		return sb.toString();
	}
	
	public List<IAttributeSource>getChain(){
		return Collections.unmodifiableList(chain);
	}
	
	public CombiningPolicy getCombiningPolicy(){
		return combiner;
	}
	
	/**
	 * merge info from "slave" map into master map, overriding info already present
	 */
	void merge(Map<String,String[]>master, Map<String,String[]>slave){
		for(Map.Entry<String,String[]>e: slave.entrySet()){
			master.put(e.getKey(),e.getValue());
		}
	}

	public void setProperties(Properties p) {
		properties = p;
	}

	public void setOrder(String order) {
		orderString = order;
	}

	public void setCombiningPolicy(String name) {
		combinerName = name;
	}
	
	private void initOrder() throws Exception {
		chain = new ArrayList<IAttributeSource>();
		names = new ArrayList<String>();
		if (orderString == null) {			
			String nn = name == null ? "" : "." + name;
			throw new IllegalArgumentException("Configuration inconsistent, " +
					"need to define <" + AttributeSourceFactory.BASE + 
					nn + ".order>");
		}
		String[] authzNames=orderString.split(" +");
		
		if (properties == null)
			properties = DSRServer.getConfiguration().getProperties();
		
		for(String auth: authzNames){
			chain.add(AttributeSourceConfigurator.configureAttributeSource(auth, 
				properties));
			names.add(auth);
		}
	}
	
	private void initCombiningPolicy() {
		if(combinerName==null){
			combinerName=MergeLastOverrides.NAME;
			logger.info("Using default combining policy "+combinerName);
		}
		
		if(MergeLastOverrides.NAME.equalsIgnoreCase(combinerName)){
			combiner=new AttributeSourcesChain.MergeLastOverrides();
		}
		else if(Merge.NAME.equalsIgnoreCase(combinerName)){
			combiner=new AttributeSourcesChain.Merge();
		}
		else if(FirstApplicable.NAME.equalsIgnoreCase(combinerName)){
			combiner=new AttributeSourcesChain.FirstApplicable();
		}
		else if(FirstAccessible.NAME.equalsIgnoreCase(combinerName)){
			combiner=new AttributeSourcesChain.FirstAccessible();
		}
		else{
			try{
				Object c=Class.forName(combinerName).newInstance();
				combiner=(CombiningPolicy)c;
			}
			catch(Exception ex){
				logger.error("Can't create combining policy <"+combinerName+
					">. Fallback to default <"+MergeLastOverrides.NAME+">.",ex);
				combiner=new AttributeSourcesChain.MergeLastOverrides();
			}
		}
	}

	
	/**
	 * defines how attributes should be combined
	 */
	public static interface CombiningPolicy{
		
		/**
		 * combines new attributes with the already existing ones
		 * @param master - the already existing attributes
		 * @param newAttributes - the new attributes
		 * @return true if next attribute sources should be evaluated, false if processing should be stopped.
		 */
		public boolean combineAttributes(SubjectAttributesHolder master, 
				SubjectAttributesHolder newAttributes);
		
	}
	
	/**
	 * first applicable: only the first not empty map of attributes is used
	 */
	public static class FirstApplicable implements CombiningPolicy {
		public static final String NAME = "FIRST_APPLICABLE";
		public boolean combineAttributes(SubjectAttributesHolder master, SubjectAttributesHolder newAttributes){
			//shouldn't happen but check anyway
			if (master.isPresent()) {
				return false;
			} else {
				if (!newAttributes.isPresent())
					return true;
				master.addAllOverwritting(newAttributes);
				return false;
			}
		}
		
		public String toString(){
			return NAME;
		}
	}

	/**
	 * first accessible: the answer from the first accessible attribute source is used. It is
	 * assumed that AttributeSource throws an exception when there is communication error.
	 */
	public static class FirstAccessible implements CombiningPolicy{
		public static final String NAME = "FIRST_ACCESSIBLE"; 
		public boolean combineAttributes(SubjectAttributesHolder master, SubjectAttributesHolder newAttributes){
			//shouldn't happen but check anyway
			if(master.isPresent()) {
				return false;
			} else {
				master.addAllOverwritting(newAttributes);	
				return false;
			}
		}
		
		public String toString(){
			return NAME;
		}
	}

	
	/**
	 * merge_last_overrides:  new attributes overwrite existing ones
	 */
	public static class MergeLastOverrides implements CombiningPolicy{
		public static final String NAME = "MERGE_LAST_OVERRIDES"; 
		public boolean combineAttributes(SubjectAttributesHolder master, SubjectAttributesHolder newAttributes){
			master.addAllOverwritting(newAttributes);
			return true;
		}
		
		public String toString(){
			return NAME;
		}
	}
	
	/**
	 * Merge:  attributes with the same key are combined (values are added).
	 * This is always OK for XACML attributes (only duplicates are not maintained) and
	 * for lists of valid values. However for regular incarnation attributes 
	 * (like xlogin, role, primary gid, queue) only one value make sense. So in case of 
	 * default values obtained from AS of incarnation attributes the same policy is 
	 * used as in MERGE_LAST_OVERRIDS. 
	 */
	public static class Merge implements CombiningPolicy{
		public static final String NAME = "MERGE"; 
			
		public boolean combineAttributes(SubjectAttributesHolder master, SubjectAttributesHolder newAttributes){
			master.addAllMerging(newAttributes);
			return true;
		}
		
		public String toString(){
			return NAME;
		}
	}

	@Override
	public String getName()
	{
		return name == null ? "MainChain" : name;
	}

	@Override
	public String[] getAcceptedVOs()
	{
		Set<String> accepted = new HashSet<String>();
		for (IAttributeSource as: chain)
		{
			String[] asVos = as.getAcceptedVOs();
			if (asVos != null)
				for (String vo: asVos)
					accepted.add(vo);
		}
		
		return accepted.toArray(new String[accepted.size()]);
	}
}
