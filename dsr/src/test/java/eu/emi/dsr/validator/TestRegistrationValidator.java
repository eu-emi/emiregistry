/**
 * 
 */
package eu.emi.dsr.validator;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import eu.emi.dsr.core.ServiceBasicAttributeNames;
import eu.emi.dsr.util.ServiceUtil;

/**
 * @author a.memon
 *
 */
public class TestRegistrationValidator {
	@Test
	public void testValidateExpiryInfo() throws JSONException, IOException{
		JSONObject jo = new JSONObject(ServiceUtil.convertFileToString("src/test/resources/serviceinfo.json"));
		Calendar c = Calendar.getInstance();
		c.add(12, c.MONTH);
		JSONObject d = new JSONObject();
		d.put("$date", ServiceUtil.toUTCFormat(c.getTime()));
		jo.put(ServiceBasicAttributeNames.SERVICE_EXPIRE_ON.getAttributeName(), d);
		assertTrue(ValidatorFactory.getRegistrationValidator().validateInfo(jo));
	}
	
	@Test
	public void testInvalidExpiryInfo() throws JSONException, IOException{
		JSONObject jo = new JSONObject(ServiceUtil.convertFileToString("src/test/resources/serviceinfo.json"));
		assertFalse(ValidatorFactory.getRegistrationValidator().validateInfo(jo));
	}
	
	@Test
	public void testInvalidDateType() throws JSONException, IOException{
		JSONObject jo = new JSONObject(ServiceUtil.convertFileToString("src/test/resources/serviceinfo.json"));
		Calendar c = Calendar.getInstance();
		c.add(12, c.MONTH);
		JSONObject d = new JSONObject();
		d.put("$date", ServiceUtil.toUTCFormat(c.getTime()));
		jo.put(ServiceBasicAttributeNames.SERVICE_EXPIRE_ON.getAttributeName(), d);
		jo.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_DOWNTIME_START.getAttributeName(), new Date());
		assertFalse(ValidatorFactory.getRegistrationValidator().validateInfo(jo));
	}
	@Test
	public void testInvalidArrays() throws JSONException, IOException{
		JSONObject jo = new JSONObject(ServiceUtil.convertFileToString("src/test/resources/serviceinfo.json"));
		jo.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_TRUSTEDCA.getAttributeName(), "dn1");
		assertFalse(ValidatorFactory.getRegistrationValidator().validateInfo(jo));
	}
}
