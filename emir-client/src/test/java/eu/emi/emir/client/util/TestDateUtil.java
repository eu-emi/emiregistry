/**
 * 
 */
package eu.emi.emir.client.util;

import java.util.Calendar;

import junit.framework.Assert;

import org.junit.Test;

/**
 * @author a.memon
 *
 */
public class TestDateUtil {
	@Test
	public void testDuration(){
		Calendar fromDate = Calendar.getInstance();
		fromDate.set(2012, 05, 31);
		Calendar toDate = Calendar.getInstance();
		toDate.set(2014, 01, 02, 23, 15);
		System.out.println(DateUtil.duration(fromDate, toDate));
		Assert.assertEquals("1 days 7 months 1 years", DateUtil.duration(fromDate, toDate).toString());
	}
}
