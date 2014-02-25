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

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import net.opengis.sampling.x20.impl.SFSamplingFeatureDocumentImpl;

import org.geotools.gml3.v3_2.GMLConfiguration;
import org.geotools.xml.AbstractComplexBinding;
import org.geotools.xml.ElementInstance;
import org.geotools.xml.Node;
import org.geotools.xs.XSConfiguration;
import org.n52.wps.io.datahandler.SchemaLocationHandler.FeatureTypeSchema;
import org.picocontainer.MutablePicoContainer;

public class GML32OMConfiguration extends GMLConfiguration {

	private String gmlNamespace;
	private String srsName = "urn:ogc:def:crs:EPSG::4326";
	private Map<QName, Class<?>> bindings = new HashMap<QName, Class<?>>();
	
	public GML32OMConfiguration() {
		addDependency(new XSConfiguration());
		bindings.put(new QName("http://www.opengis.net/samplingSpatial/2.0", "SF_SpatialSamplingFeature"),
				SFSamplingFeatureDocumentImplBinding.class);
	}

	@Override
	protected void configureContext(MutablePicoContainer container) {
		super.configureContext(container);
		registerBindingImplementations(container);
	}

	private void registerBindingImplementations(MutablePicoContainer container) {
		for (QName qn : bindings.keySet()) {
			container.registerComponentImplementation(qn, bindings.get(qn));	
		}
	}
	
	
	public static class SFSamplingFeatureDocumentImplBinding extends AbstractComplexBinding{
		
		public static final QName NAME = new QName("http://www.opengis.net/samplingSpatial/2.0", "SF_SpatialSamplingFeature");

		@Override
		public QName getTarget() {
			return NAME;
		}

		@Override
		public Class<?> getType() {
			return SFSamplingFeatureDocumentImpl.class;
		}
		
		@Override
		public Object parse(ElementInstance instance, Node node, Object value)
				throws Exception {
			
			Object parsedObject = null;
			
			
			return parsedObject;
			
		}
	}
}
