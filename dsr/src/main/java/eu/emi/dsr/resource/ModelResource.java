/**
 * 
 */
package eu.emi.dsr.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import eu.emi.dsr.core.ServiceBasicAttributeNames;

/**
 * The web resource to view the registry model
 * 
 * @author a.memon
 * 
 */
@Path("/model")
public class ModelResource {
	private static JSONObject jo;
	{
		InputStream is = ModelResource.class.getResourceAsStream("servicemodel.json");
		StringWriter writer = new StringWriter();
		try {
			IOUtils.copy(is, writer);
			jo = new JSONObject(writer.toString());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	@Produces(MediaType.TEXT_HTML)
	@GET
	public Response getModel() throws WebApplicationException {
		StringBuilder b = new StringBuilder();
		b.append("<html>");
		b.append("<head>");
		b.append("</head>");
		b.append("<h1>EMI Registry Service Model</h1>");
		b.append("<body>");
		b.append("<table border = \"1\">");
		// heading
		b.append("<tr>");
		b.append("<td>");
		b.append("<b>Attribute Name</b>");
		b.append("</td>");		
		b.append("<td>");
		b.append("<b>Description</b>");
		b.append("</td>");
		b.append("</tr>");
		// values
		ServiceBasicAttributeNames[] sn = ServiceBasicAttributeNames.values();
		for (int i = 0; i < sn.length; i++) {
			b.append("<tr>");
			b.append("<td>").append(sn[i].getAttributeName()).append("</td>");
			b.append("<td>").append(sn[i].getAttributeDesc()).append("</td>");
			b.append("</tr>");
			b.append("\n");
		}
		b.append("</table>");
		b.append("\n\n");
		b.append("<p>Raw JSON document to register and update the services</p>");
		b.append("<textarea name=\"comments\" rows=\"30\" cols=\"80\">");
		
		try {
			b.append(jo.toString(2));
		} catch (JSONException e) {			
			e.printStackTrace();
		}
		b.append("</textarea>");		
		b.append("</body></html>");
		return Response.ok(b.toString()).build();
	}

}
