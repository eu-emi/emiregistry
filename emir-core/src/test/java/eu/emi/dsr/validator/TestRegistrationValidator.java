/**
 * 
 */
package eu.emi.dsr.validator;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import eu.emi.client.ServiceBasicAttributeNames;
import eu.emi.emir.util.DateUtil;
import eu.emi.emir.util.ServiceUtil;
import eu.emi.emir.validator.ValidatorFactory;

/**
 * @author a.memon
 *
 */
public class TestRegistrationValidator {
	@Test
	public void testValidateExpiryInfo() throws JSONException, IOException{
		JSONObject jo = new JSONObject(FileUtils.readFileToString(new File("src/test/resources/json/serviceinfo.json")));
		DateUtil.setExpiryTime(jo, 365);
		assertTrue(ValidatorFactory.getRegistrationValidator().validateInfo(jo));
	}
	
	@Test
	public void testInvalidExpiryInfo() throws JSONException, IOException{
		JSONObject jo = new JSONObject(FileUtils.readFileToString(new File("src/test/resources/json/serviceinfo.json")));
		assertFalse(ValidatorFactory.getRegistrationValidator().validateInfo(jo));
	}
	
	@Test
	public void testInvalidDateType() throws JSONException, IOException{
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
	public void testInvalidArrays() throws JSONException, IOException{
		JSONObject jo = new JSONObject(FileUtils.readFileToString(new File("src/test/resources/json/serviceinfo.json")));
		jo.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_TRUSTEDCA.getAttributeName(), "dn1");
		assertFalse(ValidatorFactory.getRegistrationValidator().validateInfo(jo));
	}
}
