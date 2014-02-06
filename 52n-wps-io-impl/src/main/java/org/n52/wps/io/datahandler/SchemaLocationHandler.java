/**
 * Copyright (C) 2012
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
 */
package org.n52.wps.io.datahandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.n52.wps.conflation.http.AuthenticatedHttpConnector;
import org.n52.wps.io.SchemaRepository;
import org.n52.wps.io.datahandler.parser.GML3ApplicationSchemaParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class SchemaLocationHandler extends DefaultHandler {

	
	private static final Logger LOGGER = LoggerFactory.getLogger(SchemaLocationHandler.class);
	private static final boolean schemaDownloadEnabled = true;
	private static final String BLACKLISTED = "_BLACK_LISTED";
	
	private String  schemaUrl;
	private String nameSpaceURI;
	private boolean rootVisited = false;
	private Map<String, String> namespaces = new HashMap<String, String>();
	
	private static Properties localSchemaMappings = new Properties();
	private static String localSchemaRepository = "/localSchemaRepository";
	private static File localSchemaRepositoryBaseDirectory;
	private static File mappingsFile;
	private static Set<String> blackListedFiles = new HashSet<String>();
	private static URL emptySchemaDummy;
	
	static {
		try {
			URL root = GML3ApplicationSchemaParser.class.getResource(localSchemaRepository);
			localSchemaRepositoryBaseDirectory = new File(root.toURI().getPath());
			if (localSchemaRepositoryBaseDirectory != null) {
				if (!localSchemaRepositoryBaseDirectory.exists())
					localSchemaRepositoryBaseDirectory .mkdir();
				mappingsFile = new File(localSchemaRepositoryBaseDirectory, "mappings.properties");
				loadProperties();
				File emptyDummy = new File(localSchemaRepositoryBaseDirectory, "emptySchemaDummy.xsd");
				if (emptyDummy != null && emptyDummy.exists()) {
					emptySchemaDummy = emptyDummy.toURI().toURL();
				} else {
					LOGGER.warn("could not find emptySchemaDummy.xsd - blacklisting will NOT work.");
				}
			}
			else {
				LOGGER.warn("Base Directory for local schema repository not available!");
			}
		} catch (Exception e) {
			LOGGER.warn("Base Directory for local schema repository not available!", e);
		}
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException { 
		super.startElement(uri, localName, qName, attributes);
		if(rootVisited) {
			return;
		}
		// check if root is a xml-beans element.
		if(localName.equals("xml-fragment")) {
			return;
		}
		rootVisited = true;
		String schemaLocationAttr = attributes.getValue(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "schemaLocation");
		if(schemaLocationAttr == null) {
			LOGGER.debug("schemaLocation attribute is not set correctly with namespace");
			schemaLocationAttr = attributes.getValue("xsi:schemaLocation");
			if(schemaLocationAttr == null){
				schemaLocationAttr = attributes.getValue("schemaLocation");
			}
		}
		String[] locationStrings = schemaLocationAttr.replace("  ", " ").split(" ");
		if (locationStrings.length % 2 != 0) {
			LOGGER.debug("schemaLocation does not reference locations correctly, odd number of whitespace separated addresses");
			return;
		}
		for (int i = 0; i< locationStrings.length; i++) {
			if(i % 2 == 0 && !locationStrings[i].equals("http://www.opengis.net/wfs") &&
					!locationStrings[i].equals(FeatureTypeSchema.GML_3_NAMESPACE) &&
					!locationStrings[i].equals(FeatureTypeSchema.GML_32_NAMESPACE) && !locationStrings[i].isEmpty()){
				nameSpaceURI = locationStrings[i];
				schemaUrl = locationStrings[i + 1];
				return;
			}
				
		}
	}

	private static void loadProperties() throws FileNotFoundException, IOException {
		if (mappingsFile != null && mappingsFile.exists()) {
			localSchemaMappings.load(new FileInputStream(mappingsFile));
			
			String blacklisted = localSchemaMappings.getProperty(BLACKLISTED);
			if (blacklisted != null) {
				for (String file : blacklisted.trim().split(",")) {
					if (!file.trim().isEmpty())
						blackListedFiles.add(file.trim());
				}
			}
		}		
	}

	private static void saveProperties() throws FileNotFoundException, IOException {
		if (mappingsFile != null) {
			StringBuilder blacklisted = new StringBuilder();
			for (String file : blackListedFiles) {
				blacklisted.append(file);
				blacklisted.append(", ");
			}
			localSchemaMappings.setProperty(BLACKLISTED, blacklisted.toString());
			
			localSchemaMappings.store(new FileOutputStream(mappingsFile), null);
		}		
	}
	
	public String getSchemaUrl(){
		return schemaUrl;
	}

	@Override
	public void startPrefixMapping(String prefix, String uri) throws SAXException {
		super.startPrefixMapping(prefix, uri);
		namespaces.put(prefix, uri);
		
	}
	
	public String getTargetNamespace() {
		return nameSpaceURI;
	}

	public Map<String, String> getNamespaces() {
		return namespaces;
	}
	
	public static FeatureTypeSchema determineFeatureTypeSchema(File file) {
		try {
			SchemaLocationHandler handler = new SchemaLocationHandler();
			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setNamespaceAware(true);

			factory.newSAXParser().parse(new FileInputStream(file), handler); 
			String schemaUrl = handler.getSchemaUrl(); 

			if (schemaUrl == null){
				return null;
			}
			
			String gml = null;
			if (handler.getNamespaces().containsValue(FeatureTypeSchema.GML_32_NAMESPACE)) {
				gml = FeatureTypeSchema.GML_32_NAMESPACE;
			} else if (handler.getNamespaces().containsValue(FeatureTypeSchema.GML_3_NAMESPACE)) {
				gml = FeatureTypeSchema.GML_3_NAMESPACE;
			}
			
			String namespaceURI = handler.getTargetNamespace();
			
//			String local = localSchemaMappings.getProperty(namespaceURI);
//			if (local != null) {
//				schemaUrl = buildLocalSchemaURL(local);
//			}
			SchemaRepository.registerSchemaLocation(namespaceURI, schemaUrl);
			SchemaRepository.registerGMLVersion(namespaceURI, gml);

			return new FeatureTypeSchema(namespaceURI, schemaUrl, gml);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException(e);
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		} catch (SAXException e) {
			throw new IllegalArgumentException(e);
		} catch(ParserConfigurationException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	
	public static class FeatureTypeSchema {
		
		public static final String GML_32_NAMESPACE = "http://www.opengis.net/gml/3.2";
		public static final String GML_3_NAMESPACE = "http://www.opengis.net/gml";

		private String namespace;
		private String schemaLocation;
		private String gmlNamespace;

		public FeatureTypeSchema(String namespaceURI, String schemaURL, String gml) {
			this.namespace = namespaceURI;
			this.schemaLocation = schemaURL;
			this.gmlNamespace = gml;
		}

		public FeatureTypeSchema(String namespaceURI, String schemaURL) {
			this(namespaceURI, schemaURL, null);
		}

		public String getNamespace() {
			return namespace;
		}

		public String getSchemaLocation() {
			return schemaLocation;
		}

		public String getGmlNamespace() {
			return gmlNamespace;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(namespace);
			sb.append(" = ");
			sb.append(schemaLocation);
			if (gmlNamespace != null) {
				sb.append("; GML = ");
				sb.append(gmlNamespace);
			}
			return sb.toString();
		}
		
		
		

	}

	public static boolean isSchemaDownloadEnabled() {
		return schemaDownloadEnabled;
	}

	public static synchronized String resolveFromLocalSchemaRepository(String parent, String child) {
		if (parent == null) return null;
		
		if (blackListedFiles.contains(child)) {
			return emptySchemaDummy.toString();
		}
		
		File result = null;
		
		String localBase = localSchemaMappings.getProperty(parent);
		File localBaseDirectory = null;
		if (localBase == null && isSchemaDownloadEnabled()){
			localBase = lastPortionOfRemoteLocation(parent);
			localBaseDirectory = createLocalSchemaBaseDirectory(localBase);	
			if (localBaseDirectory != null && localBaseDirectory.exists()) {
				localSchemaMappings.setProperty(parent, localBase);
				try {
					saveProperties();
				} catch (FileNotFoundException e) {
					LOGGER.warn(e.getMessage(), e);
				} catch (IOException e) {
					LOGGER.warn(e.getMessage(), e);
				}
			}
		}
		else {
			localBaseDirectory = createLocalSchemaBaseDirectory(localBase);	
		}
		
		if (localBaseDirectory != null) {
			result = new File(localBaseDirectory, child);
			if (!result.exists()) {
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info("Downloading schema "+child+ " from "+parent);
				}
				try {
					downloadRemoteSchema(parent, child, result);
				} catch (IOException e) {
					return null;
				}
			}
		}
		
		URL url = null;
		try {
			url = result.toURI().toURL();
		} catch (MalformedURLException e) {
			LOGGER.warn(e.getMessage(), e);
		}
		return (url == null) ? null : url.toString();
	}

	private static void downloadRemoteSchema(String baseUrl, String childSchemaFile,
			File targetFile) throws IOException {
		URI uri;
		try {
			uri = new URI(baseUrl +"/" + childSchemaFile);
		} catch (URISyntaxException e) {
			throw new IOException(e);
		}
		AuthenticatedHttpConnector.downloadRemoteFile(uri, targetFile);
	}

	private static File createLocalSchemaBaseDirectory(String localBase) {
		if (localSchemaRepositoryBaseDirectory == null || !localSchemaRepositoryBaseDirectory.exists()) return null;
		
		File newBaseDirectory = new File(localSchemaRepositoryBaseDirectory, localBase);
		if (!newBaseDirectory.exists()) {
			newBaseDirectory.mkdir();
		}
		return newBaseDirectory;
	}

	private static String lastPortionOfRemoteLocation(String locationBaseUri) {
		int index = locationBaseUri.lastIndexOf("/");
		if (index > 0) {
			return locationBaseUri.substring(index+1, locationBaseUri.length());
		}
		return UUID.randomUUID().toString();
	}
	
}
