/***************************************************************
 This implementation provides a framework to publish processes to the
web through the  OGC Web Processing Service interface. The framework 
is extensible in terms of processes and data handlers. It is compliant 
to the WPS version 0.4.0 (OGC 05-007r4). 

 Copyright (C) 2006 by con terra GmbH

 Authors: 
	 Bastian Schaeffer, IfGI; Matthias Mueller, TU Dresden

 Contact: Albert Remke, con terra GmbH, Martin-Luther-King-Weg 24,
 48155 Muenster, Germany, 52n@conterra.de

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 version 2 as published by the Free Software Foundation.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program (see gnu-gpl v2.txt); if not, write to
 the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 Boston, MA  02111-1307, USA or visit the web page of the Free
 Software Foundation, http://www.fsf.org.

 Created on: 13.06.2006
 ***************************************************************/
package org.n52.wps.io.datahandler.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import net.opengis.gml.x32.FeaturePropertyType;
import net.opengis.om.x20.OMObservationDocument;
import net.opengis.om.x20.OMObservationType;
import net.opengis.om.x20.impl.OMObservationDocumentImpl;
import net.opengis.samplingSpatial.x20.SFSpatialSamplingFeatureDocument;
import net.opengis.samplingSpatial.x20.SFSpatialSamplingFeatureType;
import net.opengis.samplingSpatial.x20.impl.SFSpatialSamplingFeatureTypeImpl;

import org.apache.xerces.parsers.DOMParser;
import org.apache.xmlbeans.XmlException;
import org.n52.wps.io.data.binding.complex.GML32OMWFSDataBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * This parser handles xml files compliant to gmlpacket.xsd 
 * @author schaeffer
 *
 */
public class GML32OMWFSGBasicParser extends AbstractParser {
	
	private static Logger LOGGER = LoggerFactory.getLogger(GML32OMWFSGBasicParser.class);
	
	public GML32OMWFSGBasicParser() {
		super();
		supportedIDataTypes.add(GML32OMWFSDataBinding.class);
	}
	
	@Override
	public GML32OMWFSDataBinding parse(InputStream stream, String mimeType, String schema) {

		ArrayList<OMObservationType> observationList = new ArrayList<OMObservationType>();
		
		GML32OMWFSDataBinding result = new GML32OMWFSDataBinding(observationList);
		
		try {
			parseObservations(stream, observationList);
		} catch (SAXException e) {
			LOGGER.error(e.getMessage());
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
		}
		
		return result;
	}
	
	public void parseObservations(InputStream stream, ArrayList<OMObservationType> observationList) throws SAXException, IOException{
		
		DOMParser parser = new DOMParser();		
		
		InputSource inputSource = new InputSource(stream);
		
		parser.parse(inputSource);
		
		Document d = parser.getDocument();
		
		NodeList features = d.getElementsByTagNameNS("http://www.opengis.net/wfs/2.0", "member");
	
        for(int i=0;i<features.getLength();i++){
        	
            Node node = features.item(i);
            
            if(node.getChildNodes().getLength() > 0){
            	try { 
            		OMObservationDocumentImpl observationDocumentImpl = (OMObservationDocumentImpl) OMObservationDocument.Factory.parse(node.getChildNodes().item(1));
					
            		OMObservationType observationType = observationDocumentImpl.getOMObservation();
            		
					FeaturePropertyType featureOfInterest = observationDocumentImpl.getOMObservation().getFeatureOfInterest();
					
					SFSpatialSamplingFeatureType featureTypeImpl = SFSpatialSamplingFeatureType.Factory.newInstance();
					
					if(featureOfInterest instanceof SFSpatialSamplingFeatureTypeImpl){
						featureTypeImpl =  (SFSpatialSamplingFeatureTypeImpl)featureOfInterest;
					}else{
						SFSpatialSamplingFeatureDocument spatialSamplingFeatureType = SFSpatialSamplingFeatureDocument.Factory.parse(featureOfInterest.getDomNode().getChildNodes().item(1));
						
						featureTypeImpl = spatialSamplingFeatureType.getSFSpatialSamplingFeature();
					}
					observationType.getFeatureOfInterest().setAbstractFeature(featureTypeImpl);
					
					observationList.add(observationType);
					
				} catch (XmlException e) {
					LOGGER.error(e.getMessage());
				}
            }
        }
		
	}

}
