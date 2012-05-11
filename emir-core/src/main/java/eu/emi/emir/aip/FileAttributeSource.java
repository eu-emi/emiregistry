/*
 * Copyright (c) 2010 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE file for licencing information.
 *
 * Created on 06-09-2010
 * Author: K. Benedyczak <golbi@mat.umk.pl>
 */
package eu.emi.emir.aip;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.x500.X500Principal;

import org.apache.log4j.Logger;

import eu.emi.emir.client.util.Log;
import eu.emi.emir.security.IAttributeSource;
import eu.emi.emir.security.SecurityTokens;
import eu.emi.emir.security.SubjectAttributesHolder;
import eu.emi.emir.security.XACMLAttribute;
import eu.emi.emir.security.XACMLAttribute.Type;

/**
 * Retrieves client's attributes from a file. File format is quite simple:
 * 
 * <pre>
 * <fileAttributeSource>
 *   <entry key="CN=someDN,C=PL">
 *     <attribute name="xlogin">
 *       <value>nobody</value>
 *       <value>somebody</value>
 *     </attribute>
 *     <attribute name="role"><value>user</value></attribute>
 *   </entry>
 * </fileAttributeSource>
 * </pre>
 * 
 * You can add arbitrary number of attributes and attribute values.
 * <p>
 * Configuration of this source consist of two entries:
 * <ul>
 * <li>file - the path of the described above file with attributes
 * <li>matching - strict|regexp In strict mode canonical representation of the
 * key is compared with the canonical representation of the argument. In regexp
 * mode then key is considered a regular expression and argument is matched with
 * it.
 * </ul>
 * <p>
 * Evaluation is simplistic: the first entry matching the client is used
 * (important when you use wildcards).
 * <p>
 * The attributes file is automatically refreshed after any change, before
 * subsequent read. If the syntax is wrong then loud message is logged and old
 * version is used.
 * <p>
 * Some attribute names are special: xlogin, role, group, supplementaryGroups,
 * addOsGroups, queue. Attributes with those names (case insensitive) are
 * handled as those special UNICORE attributes (e.g. xlogin is used to provide
 * available local OS user names for the client).
 * <p>
 * All other attributes are treated as XACML authorization attributes of String
 * type and are passed to the PDP. Such attributes must have at least one value
 * to be processed.
 * 
 * @author golbi
 * 
 */
public class FileAttributeSource implements IAttributeSource {
	private static final Logger logger = Log.getLogger(Log.EMIR_SECURITY,
			FileAttributeSource.class);

	// config options
	private File uudbFile = new File("conf", "simpleuudb");

	private enum MatchingTypes {
		STRICT, REGEXP
	};

	private boolean strictMatching = true;

	private long lastChanged;
	private String name;
	private String status = "OK";
	private Map<String, List<Attribute>> map;
	public static final String SPECIAL_XLOGIN = "xlogin";
	public static final String SPECIAL_ROLE = "role";
	public static final String SPECIAL_GROUP = "group";
	public static final String SPECIAL_SUP_GROUPS = "supplementaryGroups";
	public static final String SPECIAL_ADD_OS_GIDS = "addOsGroups";
	public static final String SPECIAL_QUEUE = "queue";

	@Override
	public void init(String name) throws Exception {
		this.name = name;
		AttributesFileParser parser = new AttributesFileParser(
				new FileInputStream(uudbFile));
		lastChanged = uudbFile.lastModified();
		try {
			map = parser.parse();
			if (strictMatching)
				canonMap();
		} catch (IOException e) {
			status = e.getMessage();
			throw e;
		}

	}

	@Override
	public SubjectAttributesHolder getAttributes(SecurityTokens tokens,
			SubjectAttributesHolder otherAuthoriserInfo) throws IOException {
		parseIfNeeded();
		String subject = tokens.getUserName().getName(
				X500Principal.CANONICAL);
		List<Attribute> attrs = searchFor(subject);
		Map<String, String[]> retAll = new HashMap<String, String[]>();
		Map<String, String[]> retFirst = new HashMap<String, String[]>();
		List<XACMLAttribute> retXACML = new ArrayList<XACMLAttribute>();
		if (attrs != null)
			putAttributes(attrs, retAll, retFirst, retXACML);
		return new SubjectAttributesHolder(retXACML, retFirst, retAll);
	}

	private void putAttributes(List<Attribute> attrs,
			Map<String, String[]> allIncRet, Map<String, String[]> firstIncRet,
			List<XACMLAttribute> authzRet) {
		for (Attribute a : attrs) {
			String name = a.getName();
			boolean isIncarnation = true;
			// if (name.equalsIgnoreCase(SPECIAL_XLOGIN))
			// name = IAttributeSource.ATTRIBUTE_XLOGIN; else
			if (name.equalsIgnoreCase(SPECIAL_ROLE))
				name = IAttributeSource.ATTRIBUTE_ROLE;
			else if (name.equalsIgnoreCase(SPECIAL_GROUP))
				name = IAttributeSource.ATTRIBUTE_GROUP;
			else if (name.equalsIgnoreCase(SPECIAL_SUP_GROUPS))
				name = IAttributeSource.ATTRIBUTE_SUPPLEMENTARY_GROUPS;
			else if (name.equalsIgnoreCase(SPECIAL_ADD_OS_GIDS))
				name = IAttributeSource.ATTRIBUTE_ADD_DEFAULT_GROUPS;
			// else if (name.equalsIgnoreCase(SPECIAL_QUEUE))
			// name = IAttributeSource.ATTRIBUTE_QUEUES;
			else
				isIncarnation = false;

			if (isIncarnation) {
				// defaults: for all we take a first value listed,
				// except of supplementary groups, where we take all.
				if (!name
						.equals(IAttributeSource.ATTRIBUTE_SUPPLEMENTARY_GROUPS)) {
					if (a.getValues().size() > 0)
						firstIncRet.put(name,
								new String[] { a.getValues().get(0) });
					else
						firstIncRet.put(name, new String[] {});
				} else
					firstIncRet.put(
							name,
							a.getValues().toArray(
									new String[a.getValues().size()]));

				allIncRet
						.put(name,
								a.getValues().toArray(
										new String[a.getValues().size()]));
			} else {
				List<String> values = a.getValues();
				for (String value : values)
					authzRet.add(new XACMLAttribute(name, value, Type.STRING));
				if (values.size() == 0)
					logger.info("XACML Authorization attribute '" + name
							+ "' defined without a value, ignoring");
			}
		}
	}

	private List<Attribute> searchFor(String name) {
		if (strictMatching)
			return map.get(name);
		else {
			Iterator<String> keys = map.keySet().iterator();
			while (keys.hasNext()) {
				String pattern = keys.next();
				Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
				Matcher m = p.matcher(name);
				if (m.matches())
					return map.get(pattern);
			}
		}
		return null;
	}

	private void parseIfNeeded() {
		long lastMod = uudbFile.lastModified();
		if (lastMod <= lastChanged)
			return;
		lastChanged = lastMod;
		try {
			AttributesFileParser parser = new AttributesFileParser(
					new FileInputStream(uudbFile));
			map = parser.parse();
			if (strictMatching)
				canonMap();
			logger.info("Updated user attributes were loaded from the file "
					+ uudbFile);
		} catch (IOException e) {
			logger.error("The updated attributes list is INVALID: "
					+ e.getMessage());
		}
	}

	private void canonMap() {
		Map<String, List<Attribute>> map2 = new HashMap<String, List<Attribute>>();
		Iterator<Entry<String, List<Attribute>>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, List<Attribute>> e = it.next();
			String key = e.getKey();
			X500Principal x500 = new X500Principal(key);
			map2.put(x500.getName(X500Principal.CANONICAL), e.getValue());
		}
		map = map2;
	}

	@Override
	public String getStatusDescription() {
		return "File Attribute Source [" + name + "]: " + status
				+ ", using map file " + uudbFile.getAbsolutePath();
	}

	@Override
	public String getName() {
		return name;
	}

	public void setFile(String uudbFile) {
		this.uudbFile = new File(uudbFile);
	}

	public void setMatching(String val) {
		if (val.equalsIgnoreCase(MatchingTypes.STRICT.name()))
			strictMatching = true;
		else if (val.equalsIgnoreCase(MatchingTypes.REGEXP.name()))
			strictMatching = false;
		else
			logger.error("Invalid value of the 'matching' configuration option: "
					+ val + ", using default: " + MatchingTypes.STRICT);
	}

	@Override
	public String[] getAcceptedVOs() {
		return null;
	}
}
