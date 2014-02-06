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
package org.n52.wps.io.datahandler.generator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.namespace.QName;

import org.geotools.feature.FeatureCollection;
import org.geotools.gml3.GMLConfiguration;
import org.geotools.xml.Configuration;
import org.geotools.xml.Encoder;
import org.n52.wps.io.SchemaRepository;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.datahandler.ApplicationSchemaXSDWithGMLVersion;
import org.n52.wps.io.datahandler.GML3ApplicationSchemaConfiguration;
import org.opengis.feature.type.FeatureType;

public class GML3ApplicationSchemaGenerator extends AbstractGenerator {

	public GML3ApplicationSchemaGenerator() {
		super();
		supportedIDataTypes.add(GTVectorDataBinding.class);
	}
	
	public void writeToStream(IData coll, OutputStream os) {
		FeatureCollection<?, ?> correctFeatureCollection = ((GTVectorDataBinding) coll).getPayload();
		
        FeatureType schema = correctFeatureCollection.getSchema();
        String namespace = null;
        String schemaLocation = null;
        if (schema != null) {
        	namespace = schema.getName().getNamespaceURI();
        	schemaLocation = SchemaRepository.getSchemaLocation(namespace);
        }
       
        Configuration configuration = null;
        Encoder encoder = null;
        QName rootElementQName = null;
        if (schemaLocation == null || namespace == null) {
        	namespace = "http://www.opengis.net/gml";
        	schemaLocation = "http://schemas.opengis.net/gml/3.1.1/base/feature.xsd";
        	configuration = new GMLConfiguration();//new ApplicationSchemaConfiguration(namespace, schemaLocation);
            
            encoder = new Encoder(configuration );
            encoder.setNamespaceAware(true);
            encoder.setSchemaLocation("http://www.opengis.net/gml", "http://schemas.opengis.net/gml/3.1.1/base/feature.xsd");
            rootElementQName = new QName("http://www.opengis.net/gml", "FeatureCollection", "gml");
           
        } else {
        	configuration = new GML3ApplicationSchemaConfiguration(
        			new ApplicationSchemaXSDWithGMLVersion(namespace, schemaLocation, SchemaRepository.getGMLNamespaceForSchema(namespace)));
        	    
            encoder = new Encoder(configuration );
            encoder.setNamespaceAware(true);
            encoder.setSchemaLocation(namespace, schemaLocation);
            encoder.setIndenting(true);
            encoder.setIndentSize(4);
            
            rootElementQName = new QName(SchemaRepository.getGMLNamespaceForSchema(namespace), "FeatureCollection", "gml");
        }
        	
        try{
            encoder.encode(correctFeatureCollection, rootElementQName, os);
        }catch(IOException e){
        	throw new RuntimeException(e);
        }
		
	}


	@Override
	public InputStream generateStream(IData data, String mimeType, String schema)
			throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		writeToStream(data, os);
		
		byte[] array = os.toByteArray();
		os.close();
		return new ByteArrayInputStream(array);
	}
	
}
