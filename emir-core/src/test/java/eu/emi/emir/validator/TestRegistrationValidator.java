/**
 * 
 */
package eu.emi.emir.validator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import eu.emi.emir.EMIRServer;
import eu.emi.emir.ServerProperties;
import eu.emi.emir.client.ServiceBasicAttributeNames;
import eu.emi.emir.client.TestValueConstants;
import eu.emi.emir.client.util.DateUtil;
import eu.emi.emir.client.util.Log;
import eu.emi.emir.validator.InvalidServiceDescriptionException;
import eu.emi.emir.validator.ValidatorFactory;
import eu.unicore.util.configuration.ConfigurationException;

/**
 * @author a.memon
 *
 */
public class TestRegistrationValidator {
	private static final Logger logger = Log.getLogger("emir.test", TestRegistrationValidator.class);
	EMIRServer e = null;
	@Before
	public void setup(){
		e = new EMIRServer(new Properties());
	}
	
	@Test
	public void testValidateInfo() throws Exception{
		logger.info(TestValueConstants.getJSONWithMandatoryAttributes());
		assertTrue(ValidatorFactory.getRegistrationValidator().validateInfo(TestValueConstants.getJSONWithMandatoryAttributes()));
	}
	
	@Test
	public void testInvalidDateType() throws Exception{
		JSONObject jo = new JSONObject(FileUtils.readFileToString(new File("src/test/resources/json/serviceinfo.json")));
		Calendar c = Calendar.getInstance();
		c.add(Calendar.MONTH,12);
		JSONObject d = new JSONObject();
		d.put("$date", DateUtil.toUTCFormat(c.getTime()));
		jo.put(ServiceBasicAttributeNames.SERVICE_EXPIRE_ON.getAttributeName(), d);
		jo.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_DOWNTIME_START.getAttributeName(), new Date());
		assertFalse(ValidatorFactory.getRegistrationValidator().validateInfo(jo));
	}
	
	@Test (expected = InvalidServiceDescriptionException.class)
	public void testZeroLengthArray() throws JSONException, InvalidServiceDescriptionException, ConfigurationException, ParseException{
		JSONObject jo = TestValueConstants.getJSONWithMandatoryAttributes();
		logger.info(ServiceBasicAttributeNames.SERVICE_ENDPOINT_CAPABILITY.toString());
		jo.remove(ServiceBasicAttributeNames.SERVICE_ENDPOINT_CAPABILITY.toString());
		JSONArray ja = new JSONArray();
		jo.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_CAPABILITY.toString(),ja);
		assertFalse(ValidatorFactory.getRegistrationValidator().validateInfo(jo));
	}
	
	@Test
	public void testFlexibleAttributeCheckingMode() throws JSONException, ConfigurationException, InvalidServiceDescriptionException, ParseException{
		Properties p = new Properties();
		p.setProperty(ServerProperties.PREFIX+ServerProperties.PROP_RECORD_CHECKING_MODE, "flexible");
		e = new EMIRServer(p);
		JSONObject jo = new JSONObject();
		jo.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID.toString(), "1");
		assertTrue(new RegistrationValidator().validateInfo(jo));
	}
	
}
