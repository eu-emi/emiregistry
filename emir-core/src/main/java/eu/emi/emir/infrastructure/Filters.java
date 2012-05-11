/**
 * 
 */
package eu.emi.emir.infrastructure;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import eu.emi.client.util.Log;
import eu.emi.emir.DSRServer;
import eu.emi.emir.core.ServerConstants;

/**
 * @author g.szigeti
 *
 */
public class Filters extends ServerConstants {
	private static Logger logger = Log.getLogger(Log.EMIR_CORE, Filters.class);
	private HashMap<String, List<String>> inputfilters;
	private HashMap<String, List<String>> outputfilters;
	private String inputFilterPath;
	private long lastModificationInput;
	
	private String outputFilterPath;
	private long lastModificationOutput;
	
	/**
	 * Constructor
	 */
	public Filters() {
		inputFilterPath = DSRServer.getProperty(REGISTRY_FILTERS_INPUTFILEPATH);
		outputFilterPath = DSRServer.getProperty(REGISTRY_FILTERS_OUTPUTFILEPATH);
	}

	/**
	 * @param Array of incoming messages
	 * @return Filtered messages
	 */
	public JSONArray inputFilter(JSONArray serviceInfos) {
		if (inputFilterPath == null) {
			logger.warn("registry.filters.input file path is empty in the configuration! Input filter turned OFF!");
			return serviceInfos;
		}
		return filter(serviceInfos, inputFilterPath, inputfilters);
	}

	/**
	 * @param Array of outgoing messages
	 * @return Filtered messages
	 */
	public JSONArray outputFilter(JSONArray serviceInfos) {
		if (outputFilterPath == null) {
			logger.warn("registry.filters.output file path is empty in the configuration! Output filter turned OFF!");
			return serviceInfos;
		}
		return filter(serviceInfos, outputFilterPath, outputfilters);
	}
	
	/**
	 * General function for filtering the incoming and outgoing message
	 * @param Array of incoming/outgoing messages
	 * @param Path of the filter file
	 * @param filter object
	 * @return Filtered messages
	 */
	private JSONArray filter(JSONArray serviceInfos, String path, HashMap<String, List<String>> filters) {
		long lastModification = new File(path).lastModified();
		if (filters == null){
			// Initialized the filter object
			// Filled the filters from the input file
			filters = loadFromFile(path);
			// Set the date of the last modification
			if (path.equals(inputFilterPath)) {
				inputfilters = filters;
				lastModificationInput = lastModification;
			} else {
				outputfilters = filters;
				lastModificationOutput = lastModification;
			}
		}
		
		// Changed the file since the last usage
		if ( path.equals(inputFilterPath) && lastModificationInput < lastModification ) {
			filters.clear();
			// Filled the filters from the input file
			filters = inputfilters =loadFromFile(path);
			lastModificationInput = lastModification;
			logger.debug("Input filters updated!");
		}
		if ( path.equals(outputFilterPath) && lastModificationOutput < lastModification ) {
			filters.clear();
			// Filled the filters from the input file
			filters = outputfilters = loadFromFile(path);
			lastModificationOutput = lastModification;
			logger.debug("Output filters updated!");

		}
		
		JSONArray filteredArray = new JSONArray();
		//Get Map in Set interface to get key and value
		Set<Entry<String, List<String>>> s=filters.entrySet();

		for (int i=0; i<serviceInfos.length(); i++){
			boolean found = false;
	        //Move next key and value of Map by iterator
	        Iterator<Entry<String, List<String>>> it=s.iterator();
	        while(it.hasNext())
	        {
	            // key=value separator this by Map.Entry to get key and value
	            @SuppressWarnings("rawtypes")
				Map.Entry m =(Map.Entry)it.next();

	            try {
	            	if (serviceInfos.getJSONObject(i).has((String)m.getKey()) &&
	            			valueMatch(serviceInfos.getJSONObject(i), m)) {
	            		// Match to the filter entry
	            		found = true;
	            		if (logger.isDebugEnabled()) {
	            			logger.debug("Positive "+ ((path.equals(inputFilterPath)) ? "input" : "output") + " filter matching!  "
	            					+ "Service_Endpoint_URL: " + serviceInfos.getJSONObject(i).
	            					    getString("Service_Endpoint_URL")+ ", Name of attribute: "
	            					    + (String)m.getKey() + ", Value: "
	            					    + serviceInfos.getJSONObject(i).getString((String)m.getKey()));
	            		}
	            		break;
	            	}
	            } catch (JSONException e) {
	            	Log.logException("", e);
	            }
	        }
	        if (!found){
	        	// Add this entry to the output array
	        	try {
	        		filteredArray.put(serviceInfos.getJSONObject(i));
	        	} catch (JSONException e) {
	        		Log.logException("", e);
	        	}
	        }
		}
		return filteredArray;
	}

	/**
	 * Parsing the filter file
	 * @param Path of the filters
	 * @return Map of the parsed filters
	 */
	private HashMap<String, List<String>> loadFromFile(String path) {
		HashMap<String, List<String>> filters = new HashMap<String, List<String>>();
		try {
			// Open the input filter file
			FileInputStream fstream = new FileInputStream(path);
			
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			
			String strLine;
			String[] temp;
			String delimiter = "=";
			// Read File Line By Line
			while ((strLine = br.readLine()) != null) {
				// #comment
				if(strLine.trim().substring(0, 1).endsWith("#")){
					continue;
				}

				// Replace all space characters with empty string
				temp = strLine.replaceAll(" ","").split(delimiter,2);
				if (temp.length == 2) {
					List<String> values = new ArrayList<String>();
					// key and value check
					if (filters.containsKey(temp[0])) {
						values = filters.get(temp[0]);
					}
					
					// unique value condition
					if (!values.contains(temp[1])) {
						values.add(temp[1]);
						// put into the Map
						filters.put(temp[0], values);
					}
				}
			}
			// Close the input stream
			in.close();
		} catch (FileNotFoundException e) {
			logger.warn("Filter file (" + path + ") not found! "
					+ ((path.equals(inputFilterPath)) ? "Input" : "Output") + " filter turned OFF!");
		} catch (Exception e){
			Log.logException("", e);
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Parsed filters: " + filters.toString());
		}
		return filters;
	}

	/**
	 * Matching the filter's value with the entry value
	 * @param filter value
	 * @param entry value(s)
	 * @return Map of the parsed filters
	 */
	@SuppressWarnings("unchecked")
	private boolean valueMatch(JSONObject entry, @SuppressWarnings("rawtypes") Map.Entry m) {
		// simple value
		try {
			if (((List<String>)m.getValue()).
					contains(entry.getString((String)m.getKey()))){
				return true;
			}
		} catch (JSONException e) {
			if (logger.isDebugEnabled()){
				logger.warn("entry.getString trow exception in the valueMatch function: " + e);
			}
		}

		// JSONArray with JSONObject value type
		try {
			JSONArray entryValues =  (JSONArray)entry.get((String)m.getKey());
			for (int i=0; i< entryValues.length(); i++) {
				if (((List<String>)m.getValue()).
						contains(entryValues.getJSONObject(i).toString())){
					return true;
				}
			}
		} catch (JSONException e) {
			if (logger.isDebugEnabled()){
				logger.debug("This key (" + (String)m.getKey() + ") not exist in this entry or the values of JSONArray are not JSONObject.");
			}
		} catch (ClassCastException e) {
			if (logger.isDebugEnabled()){
				logger.debug("The value (" + (String)m.getKey() +") was not JSONArray.");
			}
		}


		// JSONArray with String value type
		try {
			JSONArray entryValues =  (JSONArray)entry.get((String)m.getKey());
			for (int i=0; i< entryValues.length(); i++) {
				if (((List<String>)m.getValue()).
						contains(entryValues.get(i).toString())){
					return true;
				}
			}
		} catch (JSONException e) {
			if (logger.isDebugEnabled()){
				logger.debug("This key(" + (String)m.getKey() + ") not exist in this entry.");
			}
		} catch (ClassCastException e) {
			if (logger.isDebugEnabled()){
				logger.debug("The value (" + (String)m.getKey() +") was not simple Array.");
			}
		}
		return false;
	}

}
