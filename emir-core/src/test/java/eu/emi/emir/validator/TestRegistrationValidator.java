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
import eu.emi.emir.TestValueConstants;
import eu.emi.emir.client.ServiceBasicAttributeNames;
import eu.emi.emir.client.util.Log;
import eu.emi.emir.exception.InvalidServiceDescriptionException;
import eu.emi.emir.util.DateUtil;
import eu.emi.emir.util.ServiceUtil;
import eu.unicore.util.configuration.ConfigurationException;

/**
 * @author a.memon
 *
 */
public class TestRegistrationValidator {
	private static final Logger logger = Log.getLogger("emir.test", TestRegistrationValidator.class);
	
	@Before
	public void setup(){
		EMIRServer s = new EMIRServer(new Properties());
	}
	
	@Test
	public void testValidateInfo() throws Exception{
		logger.info(TestValueConstants.getJSONWithMandatoryAttributes());
		assertTrue(ValidatorFactory.getRegistrationValidator().validateInfo(TestValueConstants.getJSONWithMandatoryAttributes()));
	}
	
	@Test (expected = InvalidServiceDescriptionException.class)
	public void testInvalidExpiryInfo_ExceedsDefault() throws Exception{
		JSONObject jo = TestValueConstants.getJSONWithMandatoryAttributes();
		DateUtil.setExpiryTime(jo, EMIRServer.getServerProperties().getIntValue(ServerProperties.PROP_RECORD_EXPIRY_MAXIMUM)+10);
		logger.info(ValidatorFactory.getRegistrationValidator().validateInfo(jo));
		assertFalse(ValidatorFactory.getRegistrationValidator().validateInfo(jo));
	}
	
	@Test (expected = InvalidServiceDescriptionException.class)
	public void testInvalidExpiryInfo_NegativeValue() throws Exception{
		JSONObject jo = TestValueConstants.getJSONWithMandatoryAttributes();
		DateUtil.setExpiryTime(jo, -EMIRServer.getServerProperties().getIntValue(ServerProperties.PROP_RECORD_EXPIRY_MAXIMUM));
		logger.info(ValidatorFactory.getRegistrationValidator().validateInfo(jo));
		assertFalse(ValidatorFactory.getRegistrationValidator().validateInfo(jo));
	}
	
	@Test
	public void testInvalidDateType() throws Exception{
		JSONObject jo = new JSONObject(FileUtils.readFileToString(new File("src/test/resources/json/serviceinfo.json")));
		Calendar c = Calendar.getInstance();
		c.add(Calendar.MONTH,12);
		JSONObject d = new JSONObject();
		d.put("$date", ServiceUtil.toUTCFormat(c.getTime()));
		jo.put(ServiceBasicAttributeNames.SERVICE_EXPIRE_ON.getAttributeName(), d);
		jo.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_DOWNTIME_START.getAttributeName(), new Date());
		assertFalse(ValidatorFactory.getRegistrationValidator().validateInfo(jo));
	}
	
	@Test
	public void testZeroLengthArray() throws JSONException, InvalidServiceDescriptionException, ConfigurationException, ParseException{
		JSONObject jo = TestValueConstants.getJSONWithMandatoryAttributes();
		logger.info(ServiceBasicAttributeNames.SERVICE_ENDPOINT_CAPABILITY.toString());
		jo.remove(ServiceBasicAttributeNames.SERVICE_ENDPOINT_CAPABILITY.toString());
		JSONArray ja = new JSONArray();
		jo.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_CAPABILITY.toString(),ja);
		assertFalse(ValidatorFactory.getRegistrationValidator().validateInfo(jo));
	}
	
}
