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
package org.n52.wps.io.datahandler.parser;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.geotools.data.DataUtilities;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.GeometryAttributeImpl;
import org.geotools.feature.type.GeometryDescriptorImpl;
import org.geotools.feature.type.GeometryTypeImpl;
import org.geotools.filter.identity.GmlObjectIdImpl;
import org.geotools.gml3.ApplicationSchemaXSD;
import org.geotools.gml3.v3_2.GMLConfiguration;
import org.geotools.xml.Configuration;
import org.geotools.xml.Parser;
import org.n52.wps.io.data.binding.complex.GTVectorDataBindingWithSourceURL;
import org.n52.wps.io.datahandler.ApplicationSchemaXSDWithGMLVersion;
import org.n52.wps.io.datahandler.GML32WFSApplicationSchemaConfiguration;
import org.n52.wps.io.datahandler.SchemaLocationHandler;
import org.n52.wps.io.datahandler.SchemaLocationHandler.FeatureTypeSchema;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.GeometryType;
import org.opengis.filter.identity.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.vividsolutions.jts.geom.Geometry;


/**
 * This parser handles xml files for GML 3.2.1
 *  
 * @author matthes rieke
 */
public class GML32WFSApplicationSchemaIncludingDatasetSourceParser extends AbstractParser {
	
	private static Logger LOGGER = LoggerFactory.getLogger(GML32WFSApplicationSchemaIncludingDatasetSourceParser.class);

	public GML32WFSApplicationSchemaIncludingDatasetSourceParser() {
		super();
		supportedIDataTypes.add(GTVectorDataBindingWithSourceURL.class);
	}
	

	@Override
	public GTVectorDataBindingWithSourceURL parse(InputStream stream, String mimeType, String schema) {

		FileOutputStream fos = null;
		try {
			File tempFile = File.createTempFile("wps", "tmp");
			finalizeFiles.add(tempFile); // mark for final delete
			fos = new FileOutputStream(tempFile);
			int i = stream.read();
			while (i != -1) {
				fos.write(i);
				i = stream.read();
			}
			fos.flush();
			fos.close();

			SchemaLocationHandler.FeatureTypeSchema schematypeTuple = SchemaLocationHandler.determineFeatureTypeSchema(tempFile);
			return parse(new FileInputStream(tempFile), schematypeTuple);
		}
		catch (IOException e) {
			if (fos != null) try { fos.close(); } catch (Exception e1) { }
			throw new IllegalArgumentException("Error while creating tempFile", e);
		}
	}

	public GTVectorDataBindingWithSourceURL parse(InputStream input, SchemaLocationHandler.FeatureTypeSchema schematypeTuple) {
		Configuration configuration = resolveConfiguration(schematypeTuple);

		Parser parser = new Parser(configuration);
		parser.setStrict(true);

		//parse
		FeatureCollection<?, ?> fc = resolveFeatureCollection(parser, input);

		GTVectorDataBindingWithSourceURL data = new GTVectorDataBindingWithSourceURL(fc);

		return data;
	}
	

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private FeatureCollection<?, ?> resolveFeatureCollection(Parser parser, InputStream input) {
		FeatureCollection fc = null;
		try {
			Object parsedData = parser.parse(input);
			if (parsedData instanceof FeatureCollection){
				fc = (FeatureCollection<?, ?>) parsedData;
			} else {
				List<SimpleFeature> featureList = ((ArrayList<SimpleFeature>)((HashMap) parsedData).get("featureMember"));
				if (featureList != null){
					fc = DataUtilities.collection(featureList);
				} else {
					fc = (FeatureCollection<?, ?>) ((Map<?, ?>) parsedData).get("FeatureCollection");
				}
			}

			FeatureIterator<?> featureIterator = fc.features();
			while (featureIterator.hasNext()) {
				SimpleFeature feature = (SimpleFeature) featureIterator.next();
				
				if (feature.getDefaultGeometry() == null) {
					Collection<Property> properties = feature.getProperties();
					for (Property property : properties){
						try {
							Geometry g = (Geometry) property.getValue();
							if (g != null) {
								GeometryAttribute oldGeometryDescriptor = feature.getDefaultGeometryProperty();
								GeometryType type = new GeometryTypeImpl(property.getName(), (Class<?>) oldGeometryDescriptor.getType().getBinding(),
										oldGeometryDescriptor.getType().getCoordinateReferenceSystem(),
										oldGeometryDescriptor.getType().isIdentified(),
										oldGeometryDescriptor.getType().isAbstract(),
										oldGeometryDescriptor.getType().getRestrictions(),
										oldGeometryDescriptor.getType().getSuper()
										,oldGeometryDescriptor.getType().getDescription());

								GeometryDescriptor newGeometryDescriptor = new GeometryDescriptorImpl(type, property.getName(), 0, 1, true, null);
								Identifier identifier = new GmlObjectIdImpl(feature.getID());
								GeometryAttributeImpl geo = new GeometryAttributeImpl((Object) g, newGeometryDescriptor, identifier);
								feature.setDefaultGeometryProperty(geo);
								feature.setDefaultGeometry(g);

							}
						} catch (ClassCastException e){
							//do nothing
						}

					}
				}
			}
		} catch (IOException e) {
			LOGGER.warn(e.getMessage(), e);
			throw new RuntimeException(e);
		} catch (SAXException e) {
			LOGGER.warn(e.getMessage(), e);
			throw new RuntimeException(e);
		} catch (ParserConfigurationException e) {
			LOGGER.warn(e.getMessage(), e);
			throw new RuntimeException(e);
		}
		
		return fc;
	}
	

	private Configuration resolveConfiguration(SchemaLocationHandler.FeatureTypeSchema schematypeTuple) {
		/*
		 * TODO all if-statements are nonsense.. clean up
		 */
		if (schematypeTuple != null) {
			String schemaLocation =  schematypeTuple.getSchemaLocation();
			if (schemaLocation.startsWith(SchemaLocationHandler.FeatureTypeSchema.GML_32_NAMESPACE)){
				return new GMLConfiguration();
			} else {
				if (schemaLocation != null && schematypeTuple.getNamespace()!=null) {
					ApplicationSchemaXSD appSchema = prepareApplicationSchema(schematypeTuple);
					
					if (schematypeTuple.getGmlNamespace() != null) {
						return new GML32WFSApplicationSchemaConfiguration(appSchema, schematypeTuple.getGmlNamespace());
					} else {
						return new GML32WFSApplicationSchemaConfiguration(appSchema, FeatureTypeSchema.GML_3_NAMESPACE);
					}
				}
			}
		}
		
		return new GMLConfiguration();
	}

	private static synchronized ApplicationSchemaXSD prepareApplicationSchema(SchemaLocationHandler.FeatureTypeSchema schematypeTuple) {
		ApplicationSchemaXSDWithGMLVersion schema = new ApplicationSchemaXSDWithGMLVersion(schematypeTuple.getNamespace(),
				schematypeTuple.getSchemaLocation(), schematypeTuple.getGmlNamespace());
		return schema;
	}

}

