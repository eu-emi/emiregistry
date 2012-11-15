package eu.emi.emir.p2p;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Calendar;
import java.util.Properties;
import java.util.Scanner;

import javax.ws.rs.core.MediaType;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.ClientResponse.Status;

import eu.emi.emir.client.EMIRClient;
import eu.emi.emir.security.ClientSecurityProperties;
import eu.unicore.security.canl.AuthnAndTrustProperties;
import eu.unicore.security.canl.CredentialProperties;
import eu.unicore.security.canl.TruststoreProperties;

public class TestMaximalRegistrations {
	private static String serverUrl = "https://localhost:54321";
	private static int numberOfEntries = 40000;
	private static int numberOfEntriesPerMessage = 100;
	private static boolean stepByStep = true;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Scanner sc = new Scanner(System.in);
		System.out
				.print("Use deafult values (40000 entries, step by step)? Y/N ");
		String answer = sc.nextLine();
		if (answer.toUpperCase().equals("N")) {
			System.out.print("Number of registered entries: ");
			answer = sc.nextLine();
			numberOfEntries = Integer.valueOf(answer);

			System.out.print("Number of entries per message: ");
			answer = sc.nextLine();
			numberOfEntriesPerMessage = Integer.valueOf(answer);

			System.out.print("Would you like to stop after sent message? Y/N ");
			answer = sc.nextLine();
			if (answer.toUpperCase().equals("N")) {
				stepByStep = false;
			}

		}

		// Delete all elements
		System.out.print("Would you like to delete all entries? Y/N ");
		answer = sc.nextLine();
		if (answer.toUpperCase().equals("Y")) {
			for (int i = 1; i < numberOfEntries + 1; i++) {
				String url = "http://" + i;
				try {
					getChildClient("/serviceadmin?Service_Endpoint_ID=" + url)
							.delete();
					System.out.println("clean up: " + url);
				} catch (UniformInterfaceException e) {
					System.out.println("DB clean (" + url + ")");
				} catch (ClientHandlerException e) {
					System.out.println("No route to host (" + serverUrl + ")!");
					fail();
				}
			}
		}
	}

	@Test
	public void testServiceadminPOST() throws JSONException, IOException,
			InterruptedException {
		System.out.println("/serviceadmin POST test");
		JSONArray jos = new JSONArray();
		Calendar now = Calendar.getInstance();
		long start = now.getTimeInMillis();
		for (int i = 1; i < numberOfEntries + 1; i++) {
			String url = "http://" + i;

			// one registration to the child server
			JSONObject jo = new JSONObject(
					"{\"Service_ID\": \"urn:ogf:Service:piff.hep.lu.se:aris,GLUE2GroupID=resource,o=glue\","
							+ "\"Service_Endpoint_URL\" : \"urn:ogf:Endpoint:piff.hep.lu.se:ldapglue2:2135,GLUE2ServiceID=urn:ogf:Service:piff.hep.lu.se:aris,GLUE2GroupID=resource,o=glue\","
							+ "\"Service_Endpoint_ID\" : \"" + url + "\"} ");

			jos.put(jo);

			if ((i % numberOfEntriesPerMessage) == 0) {
				System.out.println("	send " + i);
				// send message
				ClientResponse res = getChildClient("/serviceadmin").accept(
						MediaType.APPLICATION_JSON_TYPE).post(
						ClientResponse.class, jos);
				assertTrue(res.getStatus() == Status.OK.getStatusCode());

				JSONArray rjo = res.getEntity(JSONArray.class);
				// System.out.println("	"+rjo.toString());
				assertFalse(rjo.length() == 0);

				// waiting
				if (stepByStep
						&& (i < 1000 || i == 1000 || i == 2000 || i == 3000
								|| i == 4000 || i == 5000 || i == 6000
								|| i == 7000 || i == 8000 || i == 9000
								|| i == 10000 || i == 15000 || i == 20000
								|| i == 25000 || i == 30000 || i == 35000 || i == 40000)) {
					Scanner sc = new Scanner(System.in);
					System.out.print("Press Enter to continue...");
					@SuppressWarnings("unused")
					String answer = sc.nextLine();
				}
				// clean the message container
				jos = new JSONArray();
			}

		}
		Calendar nowe = Calendar.getInstance();
		long end = nowe.getTimeInMillis();
		System.out.println("	" + "Registration OK with " + numberOfEntries
				+ " entries.");
		System.out.println("Running time: " + (end - start) + " ms");
	}

	protected static WebResource getChildClient(String path) {
		EMIRClient c = new EMIRClient(serverUrl + path);

		if (serverUrl.substring(0, 5).equals("https")) {
			Properties p = new Properties();
			// keystore setting
			p.setProperty(ClientSecurityProperties.PREFIX
					+ CredentialProperties.DEFAULT_PREFIX
					+ CredentialProperties.PROP_PASSWORD, "emi");
			p.setProperty(ClientSecurityProperties.PREFIX
					+ CredentialProperties.DEFAULT_PREFIX
					+ CredentialProperties.PROP_FORMAT,
					CredentialProperties.CredentialFormat.pkcs12.toString());
			p.setProperty(ClientSecurityProperties.PREFIX
					+ CredentialProperties.DEFAULT_PREFIX
					+ CredentialProperties.PROP_LOCATION,
					"src/test/resources/certs/demo-user.p12");

			p.setProperty(ClientSecurityProperties.PREFIX
					+ TruststoreProperties.DEFAULT_PREFIX
					+ TruststoreProperties.PROP_TYPE,
					TruststoreProperties.TruststoreType.keystore.toString());
			p.setProperty(ClientSecurityProperties.PREFIX
					+ TruststoreProperties.DEFAULT_PREFIX
					+ TruststoreProperties.PROP_KS_PATH,
					"src/test/resources/certs/demo-server.jks");
			p.setProperty(ClientSecurityProperties.PREFIX
					+ TruststoreProperties.DEFAULT_PREFIX
					+ TruststoreProperties.PROP_KS_TYPE, "JKS");
			p.setProperty(ClientSecurityProperties.PREFIX
					+ TruststoreProperties.DEFAULT_PREFIX
					+ TruststoreProperties.PROP_KS_PASSWORD, "emi");

			AuthnAndTrustProperties authn = new AuthnAndTrustProperties(p,
					ClientSecurityProperties.PREFIX
							+ TruststoreProperties.DEFAULT_PREFIX,
					ClientSecurityProperties.PREFIX
							+ CredentialProperties.DEFAULT_PREFIX);

			ClientSecurityProperties csp = new ClientSecurityProperties(p,
					authn);
			c = new EMIRClient(serverUrl + path, csp);
		}
		return c.getClientResource();
	}

}
