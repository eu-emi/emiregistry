/*
 * Copyright (c) 2010 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE file for licencing information.
 *
 * Created on 25-10-2010
 * Author: K. Benedyczak <golbi@mat.umk.pl>
 */
package eu.emi.emir.pdp.local;

import java.security.cert.X509Certificate;
import java.util.List;

import javax.security.auth.x500.X500Principal;

import org.apache.log4j.Logger;
import org.herasaf.xacml.core.context.RequestCtx;
import org.herasaf.xacml.core.context.impl.ActionType;
import org.herasaf.xacml.core.context.impl.AttributeType;
import org.herasaf.xacml.core.context.impl.AttributeValueType;
import org.herasaf.xacml.core.context.impl.EnvironmentType;
import org.herasaf.xacml.core.context.impl.RequestType;
import org.herasaf.xacml.core.context.impl.ResourceType;
import org.herasaf.xacml.core.context.impl.SubjectType;
import org.herasaf.xacml.core.converter.URNToDataTypeConverter;
import org.herasaf.xacml.core.dataTypeAttribute.DataTypeAttribute;
import org.herasaf.xacml.core.dataTypeAttribute.impl.AnyURIDataTypeAttribute;
import org.herasaf.xacml.core.dataTypeAttribute.impl.StringDataTypeAttribute;
import org.herasaf.xacml.core.dataTypeAttribute.impl.X500DataTypeAttribute;


import eu.emi.emir.client.util.Log;
import eu.emi.emir.pdp.PDPUtils;
import eu.emi.emir.security.Client;
import eu.emi.emir.security.XACMLAttribute;
import eu.emi.emir.security.util.ResourceDescriptor;

/**
 * Creates an XACML request.
 * 
 *  
 * @author golbi
 */
public class RequestCreator
{
	private static final Logger log = Log.getLogger(Log.EMIR_SECURITY, RequestCreator.class);
	
	public RequestCtx createRequest(Client c, String action,
			ResourceDescriptor des)
	{
		RequestType req = new RequestType();
		
		
		//1 - subject attributes
		List<SubjectType> subjects = req.getSubjects();
		SubjectType subject = new SubjectType();
		subjects.add(subject);
		List<AttributeType> subjectAttrs = subject.getAttributes();

		PDPUtils.validateClient(c);
		
		AttributeType idAttribute = getAttribute(XACMLAttribute.Name.XACML_SUBJECT_ID_ATTR.toString(), 
				new X500DataTypeAttribute(), 
				new X500Principal(c.getDistinguishedName()).getName());
		subjectAttrs.add(idAttribute);
		
		AttributeType roleAttribute = getAttribute(PDPUtils.LOCAL_SUBJECT_ROLE_ATTR,
				new StringDataTypeAttribute(),
				c.getRole().getName());
		subjectAttrs.add(roleAttribute);
		
		//TODO support vos later
//		String[] vos = c.getVos();
//		for (String vo: vos)
//		{
//			AttributeType voAttribute = getAttribute(PDPUtils.LOCAL_SUBJECT_VO_ATTR,
//				new StringDataTypeAttribute(), vo);
//			subjectAttrs.add(voAttribute);
//		}
		
		if (c.getSecurityTokens() != null)
		{
			X509Certificate consignorCert = c.getSecurityTokens().getConsignorCertificate();
			if(consignorCert != null){
				AttributeType consignorAttribute = getAttribute(PDPUtils.LOCAL_SUBJECT_CONSIGNOR_ATTR,
						new X500DataTypeAttribute(),
						consignorCert.getSubjectX500Principal().getName());
				subjectAttrs.add(consignorAttribute);
			}
		}
		
		addAttributesFromAIPs(c, subjectAttrs);
		
		//2 - resource attributes
		List<ResourceType> resources = req.getResources();
		ResourceType resource = new ResourceType();
		resources.add(resource);
		
		List<AttributeType> resourceAttrs = resource.getAttributes();
		AttributeType resourceIdAttr = getAttribute(XACMLAttribute.Name.XACML_RESOURCE_ID_ATTR.toString(),
				new AnyURIDataTypeAttribute(),
				des.getServiceName());
		resourceAttrs.add(resourceIdAttr);
		
		if (des.getResourceID() != null)
		{
			AttributeType wsrResourceIdAttr = getAttribute(PDPUtils.LOCAL_WSR_ATTR,
					new StringDataTypeAttribute(),
					des.getResourceID());
			resourceAttrs.add(wsrResourceIdAttr);
		}
		
		if (des.getOwner() != null)
		{
			AttributeType ownerAttribute = getAttribute(PDPUtils.LOCAL_OWNER_ATTR,
					new X500DataTypeAttribute(),
					new X500Principal(des.getOwner()).getName());
			resourceAttrs.add(ownerAttribute);
		}
		
		//3 - action
		if (action != null)
		{
			ActionType xacmlAction = new ActionType();
			List<AttributeType> actionAttrs = xacmlAction.getAttributes();
			AttributeType actionIdAttr = getAttribute(XACMLAttribute.Name.XACML_ACTION_ID_ATTR.toString(),
					new StringDataTypeAttribute(),
					action);
			actionAttrs.add(actionIdAttr);
			
			req.setAction(xacmlAction);
		}
		
		//4 - environment (hereasf throws NPE if not set)
		EnvironmentType env = new EnvironmentType();
		req.setEnvironment(env);
		
		RequestCtx ret = new RequestCtx(req);
		return ret;
	}

	private static AttributeValueType getStringAV(String val)
	{
		AttributeValueType ret = new AttributeValueType();
		ret.getContent().add(val);
		return ret;
	}
	
	private static AttributeType getAttribute(String id, DataTypeAttribute<?> type, String value)
	{
		AttributeType attribute = new AttributeType();
		attribute.setAttributeId(id);
		attribute.setDataType(type);
		attribute.getAttributeValues().add(getStringAV(value));
		return attribute; 
	}
	
	private void addAttributesFromAIPs(Client client, List<AttributeType> subjectAttrs)
	{
		URNToDataTypeConverter converter = new URNToDataTypeConverter();
		for (XACMLAttribute attr: client.getSubjectAttributes().getXacmlAttributes())
		{
			if (!PDPUtils.checkGenericAttr(attr.getName()))
			{
				log.warn("Among clients GENERIC XACML attributes retrieved from the " +
						"configured attribute sources, the special attribute " + 
						attr.getName() + " was found. Ignoring it.");
				continue;
			}
			AttributeType herasfAttribute = getAttribute(attr.getName(),
				converter.unmarshal(attr.getType().toString()),
				attr.getValue());
			subjectAttrs.add(herasfAttribute);
		}
	}
}
