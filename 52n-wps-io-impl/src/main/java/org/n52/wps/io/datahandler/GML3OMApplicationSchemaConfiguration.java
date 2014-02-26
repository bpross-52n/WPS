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
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.namespace.QName;

import net.opengis.gml.x32.StringOrRefType;
import net.opengis.om.x20.OMObservationType;
import net.opengis.om.x20.impl.OMObservationDocumentImpl;
import net.opengis.om.x20.impl.OMObservationTypeImpl;
import net.opengis.sampling.x20.SFSamplingFeatureDocument;
import net.opengis.sampling.x20.SFSamplingFeatureType;
import net.opengis.sampling.x20.impl.SFSamplingFeatureDocumentImpl;
import net.opengis.sampling.x20.impl.SFSamplingFeatureTypeImpl;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.gml2.SrsSyntax;
import org.geotools.gml3.ApplicationSchemaXSD;
import org.geotools.gml3.bindings.AbstractFeatureCollectionTypeBinding;
import org.geotools.gml3.bindings.LineStringTypeBinding;
import org.geotools.gml3.v3_2.GML;
import org.geotools.gml3.v3_2.GMLConfiguration;
import org.geotools.gml3.v3_2.bindings.EnvelopeTypeBinding;
import org.geotools.xml.AbstractComplexBinding;
import org.geotools.xml.Configuration;
import org.geotools.xml.DocumentFactory;
import org.geotools.xml.DocumentWriter;
import org.geotools.xml.ElementInstance;
import org.geotools.xml.Encoder;
import org.geotools.xml.Node;
import org.geotools.xml.impl.NodeImpl;
import org.geotools.xs.XSConfiguration;
import org.n52.wps.io.datahandler.GML32OMConfiguration.SFSamplingFeatureDocumentImplBinding;
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

public class GML3OMApplicationSchemaConfiguration extends Configuration {

	private String gmlNamespace;
	private String srsName = "urn:ogc:def:crs:EPSG::4326";
	private Map<QName, Class<?>> bindings = new HashMap<QName, Class<?>>();

	public GML3OMApplicationSchemaConfiguration(ApplicationSchemaXSDWithGMLVersion schema) {
		this(schema, schema.getGmlNamespace());
	}

	public GML3OMApplicationSchemaConfiguration(ApplicationSchemaXSD schema, String gmlNamespace) {
		super(schema);
		addDependency(new XSConfiguration());

		if (gmlNamespace.equals(FeatureTypeSchema.GML_32_NAMESPACE)) {
			addDependency(new GMLConfiguration());
		} else {
			addDependency(new org.geotools.gml3.GMLConfiguration());
		}
		this.gmlNamespace = gmlNamespace;
		bindings.put(new QName("http://www.opengis.net/samplingSpatial/2.0", "SF_SpatialSamplingFeature"),
				SFSamplingFeatureDocumentImplBinding.class);
		bindings.put(new QName("http://www.opengis.net/om/2.0", "OM_Observation"),
				OMObservationTypeImplBinding.class);
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
	
	public static class SFSamplingFeatureDocumentImplBinding extends AbstractComplexBinding{
		
		public static final QName NAME = new QName("http://www.opengis.net/samplingSpatial/2.0", "SF_SpatialSamplingFeature");

		@Override
		public QName getTarget() {
			return NAME;
		}

		@Override
		public Class<?> getType() {
			return SFSamplingFeatureTypeImpl.class;
		}
		
		@Override
		public Object parse(ElementInstance instance, Node node, Object value)
				throws Exception {
			
			List<?> attributes = node.getChildren();
			
			for (Object object : attributes) {
				
				if(object instanceof NodeImpl){
					NodeImpl nodeimpl = (NodeImpl)object;
					
					String name = nodeimpl.getComponent().getName();
					
					Object o = null;
					
					if(nodeimpl.getChildren() != null && nodeimpl.getChildren().size() > 0){
						
						try {
							o = ((org.geotools.xml.impl.NodeImpl)nodeimpl.getChildren().get(0)).getValue();
						} catch (Exception e) {
							// TODO: handle exception
						}	
					}else{
						o = nodeimpl.getValue();
					}
				}
				
				System.out.println(object.getClass());
			}
			
			SFSamplingFeatureTypeImpl parsedObject = (SFSamplingFeatureTypeImpl) SFSamplingFeatureType.Factory.newInstance();
	

			value = parsedObject;
			return value;
			
		}
		
	}


	public static class OMObservationTypeImplBinding extends AbstractComplexBinding{
		
		public static final QName NAME = new QName("http://www.opengis.net/om/2.0", "OM_Observation");

		@Override
		public QName getTarget() {
			return NAME;
		}

		@Override
		public Class<?> getType() {
			return OMObservationDocumentImpl.class;
		}
		
		@Override
		public Object getProperty(Object object, QName name) throws Exception {
			
			if(name.equals(NAME)){
				return (OMObservationTypeImpl) object;
			}
			
			return super.getProperty(object, name);
		}
		
		@Override
		public Object parse(ElementInstance instance, Node node, Object value)
				throws Exception {
			
			
			List<?> attributes = node.getChildren();
			
			for (Object object : attributes) {
				System.out.println(object.getClass());
			}
			
			OMObservationTypeImpl parsedObject = (OMObservationTypeImpl) OMObservationType.Factory.newInstance();
	
			value = parsedObject;
			return value;
			
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
