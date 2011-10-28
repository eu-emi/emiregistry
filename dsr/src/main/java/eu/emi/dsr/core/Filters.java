/**
 * 
 */
package eu.emi.dsr.core;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;

import eu.emi.dsr.resource.ServiceAdminResource;
import eu.emi.dsr.util.Log;

/**
 * @author g.szigeti
 *
 */
public class Filters extends ServerConstants {
	private static Logger logger = Log.getLogger(Log.DSR,
			Filters.class);
	private static HashMap<String, String> filters;
	private static HashMap<String, String> outpufilters;
	private String inputFilterPath;
	private String outputFilterPath;
	
	/**
	 * 
	 */
	public Filters() {
		inputFilterPath = "src/main/resources/conf/inputfilters";
	}

	public JSONArray IncomingFilter(JSONArray serviceInfos) {
		if (filters == null){
			// Initialized the filter object
			// Filled the filters from the input file
			filters = LoadFromFile(inputFilterPath);
		}
		JSONArray filteredArray = new JSONArray();
		//Get Map in Set interface to get key and value
		Set<Entry<String, String>> s=filters.entrySet();

		for (int i=0; i<serviceInfos.length(); i++){
			boolean found = false;
	        //Move next key and value of Map by iterator
	        Iterator<Entry<String, String>> it=s.iterator();
	        while(it.hasNext())
	        {
	            // key=value separator this by Map.Entry to get key and value
	            @SuppressWarnings("rawtypes")
				Map.Entry m =(Map.Entry)it.next();

	            try {
	            	if (serviceInfos.getJSONObject(i).has((String)m.getKey()) &&
	            			serviceInfos.getJSONObject(i).getString((String)m.getKey()).
	            			equals((String)m.getValue())) {
	            		// Match to the filter entry
	            		found = true;
	            		if (logger.isDebugEnabled()) {
	            			logger.debug("Positive filter matching!  "
	            					+ serviceInfos.getJSONObject(i).
	            					    getString("Service_Endpoint_URL")+ ", Name of attribute: "
	            					    + (String)m.getKey() + ", Value: "
	            					    + (String)m.getValue());
	            		}
	            		break;
	            	}
	            } catch (JSONException e) {
	            	// TODO Auto-generated catch block
	            	e.printStackTrace();
	            }
	        }
	        if (!found){
	        	// Add this entry to the output array
	        	try {
	        		filteredArray.put(serviceInfos.getJSONObject(i));
	        	} catch (JSONException e) {
	        		// TODO Auto-generated catch block
	        		e.printStackTrace();
	        	}
	        }
		}
		return filteredArray;
	}

	private HashMap<String, String> LoadFromFile(String path) {
		HashMap<String, String> filters = new HashMap<String, String>();
		try {
			// Open the input filter file
			FileInputStream fstream = new FileInputStream(path);
			
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			
			String strLine;
			String[] temp;
			String delimiter = "=";
			//Read File Line By Line
			while ((strLine = br.readLine()) != null) {
				//#comment
				if(strLine.trim().substring(0, 1).endsWith("#")){
					continue;
				}

				// Replace all space characters with empty string
				temp = strLine.replaceAll(" ","").split(delimiter,2);
				if (temp.length == 2) {
					filters.put(temp[0], temp[1]);
				}
			}
			//Close the input stream
			in.close();
		} catch (FileNotFoundException e) {
			logger.warn("Filter file (" + path + ") not found! Filter turned OFF!");
		} catch (Exception e){
			// TODO Auto-generated catch block
        	e.printStackTrace();
		}
		return filters;
	}

}
