package eu.emi.emir.p2p;

import static org.junit.Assert.*;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
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

import eu.emi.emir.client.ClientSecurityProperties;
import eu.emi.emir.client.DSRClient;
import eu.emi.emir.client.security.ISecurityProperties;

public class TestMaximalRegistrations {
	private static String serverUrl = "https://localhost:54321";
	private static int numberOfEntries = 40000;
	private static int numberOfEntriesPerMessage = 100;
	private static boolean stepByStep = true;

	@BeforeClass
    public static void setUpBeforeClass() throws Exception {
        Scanner sc = new Scanner(System.in);
        System.out.print("Use deafult values (40000 entries, step by step)? Y/N ");
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


		//Delete all elements
        System.out.print("Would you like to delete all entries? Y/N ");
        answer = sc.nextLine();
        if (answer.toUpperCase().equals("Y")) {
			for (int i=1; i<numberOfEntries+1; i++) {
				String url = "http://"+i;
				try{
					getChildClient("/serviceadmin?Service_Endpoint_URL=" + url).delete();	
					System.out.println("clean up: " + url);
				} catch (UniformInterfaceException e){
					System.out.println("DB clean ("+ url +")");
				} catch (ClientHandlerException e){
					System.out.println("No route to host ("+serverUrl+")!");
					fail();
				}
			}
        }
    }

	@Test
	public void testServiceadminPOST() throws JSONException, IOException, InterruptedException{
		System.out.println("/serviceadmin POST test");
		JSONArray jos = new JSONArray();
		Calendar now = Calendar.getInstance();
		long start = now.getTimeInMillis();
		for (int i=1; i<numberOfEntries+1; i++) {
			String url = "http://"+i;

			// one registration to the child server
			JSONObject jo = new JSONObject("{\"Service_ID\": \"urn:ogf:Service:piff.hep.lu.se:aris,GLUE2GroupID=resource,o=glue\","+
	    "\"Service_Endpoint_ID\" : \"urn:ogf:Endpoint:piff.hep.lu.se:ldapglue2:2135,GLUE2ServiceID=urn:ogf:Service:piff.hep.lu.se:aris,GLUE2GroupID=resource,o=glue\","+
	    "\"Service_Endpoint_URL\" : \""+url+"\"} ");
	
			jos.put(jo);
	
			if ( (i % numberOfEntriesPerMessage) == 0 ) {
				System.out.println("	send "+i);
				//send message
				ClientResponse res = getChildClient("/serviceadmin").accept(MediaType.APPLICATION_JSON_TYPE)
						.post(ClientResponse.class, jos);
				assertTrue(res.getStatus() == Status.OK.getStatusCode());
	
				JSONArray rjo = res.getEntity(JSONArray.class);
				//System.out.println("	"+rjo.toString());
		        assertFalse(rjo.length()==0);
		        
		        //waiting
				if ( stepByStep && ( i<1000 
						|| i==1000 || i==2000 || i==3000 || i==4000 || i==5000 || i==6000 || i==7000 || i==8000 || i==9000 
						|| i==10000 || i==15000 || i==20000 || i==25000 || i==30000 || i==35000 || i==40000)){
			        Scanner sc = new Scanner(System.in);
			        System.out.print("Press Enter to continue...");
			        @SuppressWarnings("unused")
					String answer = sc.nextLine();
				}
				//clean the message container
				jos = new JSONArray();
			}
		
		}
		Calendar nowe = Calendar.getInstance();
		long end = nowe.getTimeInMillis();
		System.out.println("	"+"Registration OK with "+ numberOfEntries +" entries.");
		System.out.println("Running time: " + (end-start)+ " ms");
	}
	


	protected static WebResource getChildClient(String path) {
		DSRClient c = new DSRClient(serverUrl + path);
		if (serverUrl.substring(0, 5).equals("https")) {
			Properties p = new Properties();

			p.put(ISecurityProperties.REGISTRY_SSL_CLIENTAUTH, "true");
			p.put(ISecurityProperties.REGISTRY_SSL_KEYPASS, "emi");
			p.put(ISecurityProperties.REGISTRY_SSL_KEYTYPE, "pkcs12");
			p.put(ISecurityProperties.REGISTRY_SSL_KEYSTORE,
					"src/test/resources/certs/demo-user.p12");
//		"src/test/resources/certs/niif-gsr.p12");
			p.put(ISecurityProperties.REGISTRY_SSL_TRUSTPASS, "emi");
			p.put(ISecurityProperties.REGISTRY_SSL_TRUSTSTORE,
					"src/test/resources/certs/demo-server.jks");
			p.put(ISecurityProperties.REGISTRY_SSL_TRUSTTYPE, "jks");
			ClientSecurityProperties csp = null;
			try {
				csp = new ClientSecurityProperties(p);
			} catch (UnrecoverableKeyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (KeyStoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (CertificateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			c = new DSRClient(serverUrl + path, csp);
		}
		return c.getClientResource();
	}

}
