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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.xml.namespace.QName;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.gml2.SrsSyntax;
import org.geotools.gml3.ApplicationSchemaXSD;
import org.geotools.gml3.bindings.AbstractFeatureCollectionTypeBinding;
import org.geotools.gml3.bindings.LineStringTypeBinding;
import org.geotools.gml3.bindings.PointTypeBinding;
import org.geotools.gml3.v3_2.GML;
import org.geotools.gml3.v3_2.bindings.EnvelopeTypeBinding;
import org.geotools.wfs.v2_0.WFSConfiguration;
import org.geotools.xml.AbstractComplexBinding;
import org.geotools.xml.Configuration;
import org.geotools.xml.Encoder;
import org.geotools.xs.XSConfiguration;
import org.n52.wps.io.datahandler.SchemaLocationHandler.FeatureTypeSchema;
import org.n52.wps.provenance.ISOProvenanceEncoding;
import org.n52.wps.provenance.ProvenanceEncoding;
import org.n52.wps.provenance.ProvenanceFeature;
import org.picocontainer.MutablePicoContainer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.helpers.NamespaceSupport;

import com.vividsolutions.jts.geom.CoordinateSequenceFactory;
import com.vividsolutions.jts.geom.GeometryFactory;

public class GML3ApplicationSchemaConfiguration extends Configuration {

	private String gmlNamespace;
	private String srsName = "urn:ogc:def:crs:EPSG::4326";
	private Map<QName, Class<?>> bindings = new HashMap<QName, Class<?>>();

	public GML3ApplicationSchemaConfiguration(ApplicationSchemaXSDWithGMLVersion schema) {
		this(schema, schema.getGmlNamespace());
	}

	public GML3ApplicationSchemaConfiguration(ApplicationSchemaXSD schema, String gmlNamespace) {
		super(schema);
		addDependency(new XSConfiguration());

		if (gmlNamespace.equals(FeatureTypeSchema.GML_32_NAMESPACE)) {
			Configuration gmlConfiguration = new WFSConfiguration();
			gmlConfiguration.getProperties().add(
					org.geotools.gml3.GMLConfiguration.NO_FEATURE_BOUNDS);
			gmlConfiguration.getProperties().add(
					org.geotools.gml3.GMLConfiguration.ENCODE_FEATURE_MEMBER);
			addDependency(gmlConfiguration);
		} else {
			Configuration gmlConfiguration = new WFSConfiguration();
			gmlConfiguration.getProperties().add(
					org.geotools.gml3.GMLConfiguration.NO_FEATURE_BOUNDS);
			gmlConfiguration.getProperties().add(
					org.geotools.gml3.GMLConfiguration.ENCODE_FEATURE_MEMBER);
			addDependency(gmlConfiguration);
		}
		this.gmlNamespace = gmlNamespace;
		
		bindings.put(GML.AbstractFeatureCollectionType,
				AbstractFeatureCollectionTypeBindingGML3Fix.class);
		bindings.put(GML.LineStringType, LineStringWithIdFix.class);
		bindings.put(GML.PointType, PointWithIdFix.class);
		bindings.put(GML.EnvelopeType, EnvelopeWithSrsNameFix.class);
//		if (gmlNamespace.equals(FeatureTypeSchema.GML_32_NAMESPACE)) {
//			bindings.put(MetaDataPropertyBinding.NAME,
//					MetaDataPropertyBinding.class);
//		}
	}



	public void setSrsName(String srsName) {
		this.srsName = srsName;
	}

	public String getSrsName() {
		return srsName;
	}

	@Override
	protected void registerBindings(MutablePicoContainer container) {
		registerBindingImplementations(container);
	}

	private void registerBindingImplementations(MutablePicoContainer container) {
		for (QName qn : bindings.keySet()) {
			container.registerComponentImplementation(qn, bindings.get(qn));	
		}
	}

	@Override
	protected void configureEncoder(Encoder encoder) {
		super.configureEncoder(encoder);
		encoder.getNamespaces().declarePrefix("gml", this.gmlNamespace);
		declareBindingNamespaces(encoder.getNamespaces());
		declareSchemaLocations(encoder);
	}
	

	private void declareSchemaLocations(Encoder encoder) {
		for (QName qn : bindings.keySet()) {
			Class<?> clazz = bindings.get(qn);
			BindingSchemaLocation anno = clazz.getAnnotation(BindingSchemaLocation.class);
			if (anno != null) {
				String[] locations = anno.declaredNamespaceSchemaLocations();
				String[] namespaces = anno.declaredNamespaceURIs();
				for (int i = 0; i < namespaces.length; i++) {
					encoder.setSchemaLocation(namespaces[i], locations[i]);
				}
			}
		}			
	}

	private void declareBindingNamespaces(NamespaceSupport namespaces) {
		for (QName qn : bindings.keySet()) {
			Class<?> clazz = bindings.get(qn);
			BindingNamespaces anno = clazz.getAnnotation(BindingNamespaces.class);
			if (anno != null) {
				declareNamespacesPrefixes(setupNamespaces(anno), namespaces);
			}
		}		
	}

	private void declareNamespacesPrefixes(Map<String, String> uriToPrefix,
			NamespaceSupport namespaces) {
		for (String uri : uriToPrefix.keySet()) {
			namespaces.declarePrefix(uriToPrefix.get(uri), uri);
		}
	}

	private static Map<String, String> setupNamespaces(BindingNamespaces bn) {
		Map<String, String> namespacePrefixes = new HashMap<String, String>();
		
		if (bn  == null ||
				(bn.declaredNamespacePrefixes().length != bn.declaredNamespaceURIs().length)) {
			return namespacePrefixes;
		}
		
		for (int i = 0; i < bn.declaredNamespacePrefixes().length; i++) {
			namespacePrefixes.put(bn.declaredNamespaceURIs()[i],
					bn.declaredNamespacePrefixes()[i]);
		}
		
		return namespacePrefixes;
	}
	
	public static class AbstractFeatureCollectionTypeBindingGML3Fix extends
			AbstractFeatureCollectionTypeBinding {

		@Override
		public Object getProperty(Object object, QName name) {
			if (GML.featureMembers.equals(name)) {
				return ((SimpleFeatureCollection) object);
			} else if (GML.boundedBy.equals(name)) {
				SimpleFeatureCollection featureCollection = (SimpleFeatureCollection) object;

				ReferencedEnvelope env = featureCollection.getBounds();

				if (env != null) {
					return !(env.isNull() || env.isEmpty()) ? env : null;
				}

			}
			return super.getProperty(object, name);
		}

	}

	public class LineStringWithIdFix extends LineStringTypeBinding {

		public LineStringWithIdFix(GeometryFactory gFactory,
				CoordinateSequenceFactory csFactory) {
			super(gFactory, csFactory);
		}

		@Override
		public Object getProperty(Object object, QName name) throws Exception {
			if ("id".equals(name.getLocalPart())) {
				return "uuid."+ UUID.randomUUID().toString();
			}
			else if ("srsName".equals(name.getLocalPart())) {
				return GML3ApplicationSchemaConfiguration.this.getSrsName();
			}

			return super.getProperty(object, name);
		}

	}

	public class PointWithIdFix extends PointTypeBinding {

		public PointWithIdFix(GeometryFactory gFactory) {
			super(gFactory);
		}

		@Override
		public Object getProperty(Object object, QName name) {
			if ("id".equals(name.getLocalPart())) {
				return "uuid."+ UUID.randomUUID().toString();
			}

			return super.getProperty(object, name);
		}

	}

	public class EnvelopeWithSrsNameFix extends EnvelopeTypeBinding {

		public EnvelopeWithSrsNameFix(Configuration config, SrsSyntax srsSyntax) {
			super(config, srsSyntax);
		}

		@Override
		public Object getProperty(Object object, QName name) {
			if ("srsName".equals(name.getLocalPart())) {
				return GML3ApplicationSchemaConfiguration.this.getSrsName();
			}

			return super.getProperty(object, name);
		}

	}

	@BindingNamespaces(declaredNamespaceURIs = {"http://www.isotc211.org/2005/gmd",
			"http://www.isotc211.org/2005/gco", "http://www.w3.org/1999/xlink",
			"http://www.opengis.net/ows-9/cci/conflation"},
			declaredNamespacePrefixes = {"gmd", "gco", "xlink", "con"})
	@BindingSchemaLocation(declaredNamespaceSchemaLocations = {"http://test.schemas.opengis.net/ows-9/cci/conflation/conflationMetadataExtension.xsd"},
			declaredNamespaceURIs = {"http://www.opengis.net/ows-9/cci/conflation"})
	public static class MetaDataPropertyBinding extends AbstractComplexBinding {

		public static final String ELEMENT_NAME = "metaDataProperty";
		public static final QName NAME = new QName(FeatureTypeSchema.GML_32_NAMESPACE, ELEMENT_NAME);
		private Map<String,String> namespacePrefixes;
		private ProvenanceEncoding encoder;

		public MetaDataPropertyBinding() {
			setupNamespacePrefixes();
			encoder = new ISOProvenanceEncoding();
		}
		
		private void setupNamespacePrefixes() {
			BindingNamespaces ns = getClass().getAnnotation(BindingNamespaces.class);
			namespacePrefixes = setupNamespaces(ns);
		}

		@Override
		public QName getTarget() {
			return NAME;
		}

		@Override
		public Class<?> getType() {
			return ProvenanceFeature.class;
		}

		public Element encode(Object object, Document document, Element value)
		throws Exception {
			ProvenanceFeature provFeature = (ProvenanceFeature) object;
			return encoder.encode(provFeature, value, document, value.getNamespaceURI(), namespacePrefixes);
		}

	}
	
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.TYPE})
	private @interface BindingNamespaces {
		
		public String[] declaredNamespacePrefixes();
		public String[] declaredNamespaceURIs();
		
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.TYPE})
	private @interface BindingSchemaLocation {
		
		public String[] declaredNamespaceSchemaLocations();
		public String[] declaredNamespaceURIs();
		
	}
}
