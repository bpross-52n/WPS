/**
 * ﻿Copyright (C) 2007 - 2014 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *       • Apache License, version 2.0
 *       • Apache Software License, version 1.0
 *       • GNU Lesser General Public License, version 3
 *       • Mozilla Public License, versions 1.0, 1.1 and 2.0
 *       • Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * License version 2 and the aforementioned licenses.
 *
 * As an exception to the terms of the GPL, you may copy, modify,
 * propagate, and distribute a work formed by combining 52°North WPS
 * GeoTools Modules with the Eclipse Libraries, or a work derivative of
 * such a combination, even if such copying, modification, propagation, or
 * distribution would otherwise violate the terms of the GPL. Nothing in
 * this exception exempts you from complying with the GPL in all respects
 * for all of the code used other than the Eclipse Libraries. You may
 * include this exception and its grant of permissions when you distribute
 * 52°North WPS GeoTools Modules. Inclusion of this notice with such a
 * distribution constitutes a grant of such permissions. If you do not wish
 * to grant these permissions, remove this paragraph from your
 * distribution. "52°North WPS GeoTools Modules" means the 52°North WPS
 * modules using GeoTools functionality - software licensed under version 2
 * or any later version of the GPL, or a work based on such software and
 * licensed under the GPL. "Eclipse Libraries" means Eclipse Modeling
 * Framework Project and XML Schema Definition software distributed by the
 * Eclipse Foundation and licensed under the Eclipse Public License Version
 * 1.0 ("EPL"), or a work based on such software and licensed under the EPL.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */
package org.n52.wps.io.datahandler.parser;

import java.io.InputStream;

import net.opengis.om.x20.OMObservationDocument;
import net.opengis.sos.x20.GetObservationByIdResponseDocument;
import net.opengis.sos.x20.GetObservationByIdResponseType;
import net.opengis.sos.x20.GetObservationResponseDocument;
import net.opengis.sos.x20.GetObservationResponseType;

import org.apache.xmlbeans.XmlObject;
import org.n52.iceland.exception.ows.OwsExceptionReport;
import org.n52.sos.decode.OmDecoderv20;
import org.n52.sos.decode.OmWmlResultDecoderv20;
import org.n52.sos.ogc.om.OmObservation;
import org.n52.sos.ogc.wml.WaterMLConstants;
import org.n52.sos.util.SosConfiguration;
import org.n52.wps.io.data.binding.complex.GenericXMLDataBinding;
import org.n52.wps.io.data.binding.complex.OMObservationBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This parser parses OM_Observations from XML streams. Currently, *Document
 * is expected. Uses {@link GenericXMLDataParser}. 
 * 
 * TODO remove return null and throw exceptions
 * 
 * @since 4.0.0
 * 
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 * 
 * @see GenericXMLDataParser
 * 
 */
public class WaterMLParser extends AbstractParser {

	private static Logger LOGGER = LoggerFactory.getLogger(WaterMLParser.class);

	public WaterMLParser(){
		super();
		supportedIDataTypes.add(OMObservationBinding.class);
	}
	
        @Override
        public OMObservationBinding parse(InputStream stream, String mimeType, String schema) { 
                if (!validateInput(mimeType,schema,stream) ){
                        return null;
                }
                GenericXMLDataBinding xmlData = new GenericXMLDataParser().parse(stream, mimeType, schema);
                
                if (xmlData == null || xmlData.getPayload() == null) {
                        LOGGER.error("Input stream could not be parsed to generic XML.");
                        return null;
                }
                
                final XmlObject payload = xmlData.getPayload();
                if (payload instanceof OMObservationDocument) {
                        try {
                                OMObservationDocument xmlOmObservation = (OMObservationDocument) payload;
                                SosConfiguration.init();
                                Object parsedObject = new OmDecoderv20().decode(xmlOmObservation.getOMObservation());
                                if (parsedObject instanceof OmObservation) {
                                    
//                                      List<OmObservation> observationList = Arrays.asList(new OmObservation[]{(OmObservation)parsedObject});
//                                  
//                                      OMObservationCollection omObservationCollection = new OMObservationCollection(observationList);
                                    
                                        return new OMObservationBinding((OmObservation)parsedObject);
                                }
                                LOGGER.error("O&M decoder output not supported. Type received: '{}'.", parsedObject.getClass().getName());
                                
                        } catch (OwsExceptionReport e) {
                                LOGGER.error("O&M data could not be parsed. Exception thrown!", e);
                                return null;
                        }
                }else if(payload instanceof GetObservationResponseDocument){
                    try {
                        GetObservationResponseDocument xmlGetOmObservationResponse = (GetObservationResponseDocument) payload;
                        SosConfiguration.init();
                        
                        GetObservationResponseType getObservationResponse = xmlGetOmObservationResponse.getGetObservationResponse();
                        
//                        int length = getObservationResponse.getObservationDataArray().length;
//                        
//                        List<OmObservation> observationList = new ArrayList<>();
//                        
//                        for (int i = 0; i < length; i++) {
//                            ObservationData observationData = getObservationResponse.getObservationDataArray(i);
//                            Object parsedObject = new OmDecoderv20().decode(observationData.getOMObservation());
//                            if (parsedObject instanceof OmObservation) {
//                                observationList.add((OmObservation) parsedObject);
//                            }
//                        }
//                        OMObservationCollection omObservationCollection = new OMObservationCollection(observationList);
                      
                        Object parsedObject = new OmWmlResultDecoderv20().decode(getObservationResponse.getObservationDataArray(0).getOMObservation());
                        if (parsedObject instanceof OmObservation) {
                            return new OMObservationBinding((OmObservation)parsedObject);                            
                        }                                      
                        LOGGER.error("O&M decoder output not supported. Type received: '{}'.", parsedObject.getClass().getName());
                } catch (OwsExceptionReport e) {
                        LOGGER.error("O&M data could not be parsed. Exception thrown!", e);
                        return null;
                }
                    
                }else if(payload instanceof GetObservationByIdResponseDocument){
                    try {
                        GetObservationByIdResponseDocument xmlGetOmObservationByIdResponse = (GetObservationByIdResponseDocument) payload;
                        SosConfiguration.init();
                        
                        GetObservationByIdResponseType getObservationResponse = xmlGetOmObservationByIdResponse.getGetObservationByIdResponse();
                        
//                        int length = getObservationResponse.getObservationArray().length;
//                        
//                        List<OmObservation> observationList = new ArrayList<>();
//                        
//                        for (int i = 0; i < length; i++) {
//                            Observation observation = getObservationResponse.getObservationArray(i);
//                            Object parsedObject = new OmDecoderv20().decode(observation.getOMObservation());
//                            if (parsedObject instanceof OmObservation) {
//                                observationList.add((OmObservation) parsedObject);
//                            }
//                        }
//                        OMObservationCollection omObservationCollection = new OMObservationCollection(observationList);
//                        
//                        return new OMObservationBinding(omObservationCollection);
                        Object parsedObject = new OmWmlResultDecoderv20().decode(getObservationResponse.getObservationArray(0).getOMObservation());
                        if (parsedObject instanceof OmObservation) {
                            return new OMObservationBinding((OmObservation)parsedObject);                            
                        }    
                        LOGGER.error("O&M decoder output not supported. Type received: '{}'.", parsedObject.getClass().getName());
                        
                } catch (OwsExceptionReport e) {
                        LOGGER.error("O&M data could not be parsed. Exception thrown!", e);
                        return null;
                }
                    
                }
                LOGGER.error("XML document type not supported: '{}'.", payload.getClass().getName());
                
                return null;
        }

	private boolean validateInput(String mimeType, String schema, InputStream stream) {
		if (mimeType != null && 
				!mimeType.isEmpty() &&
				mimeType.equals(WaterMLConstants.WML_CONTENT_TYPE.toString()) &&
				schema != null &&
				!schema.isEmpty() && 
				schema.equals(WaterMLConstants.NS_WML_20) &&
				stream != null) {
			return true;
		}
		LOGGER.debug("Input not valid: Mimetype: '{}', Schema: '{}'",
				mimeType, schema);
		return false;
	}
	
}
