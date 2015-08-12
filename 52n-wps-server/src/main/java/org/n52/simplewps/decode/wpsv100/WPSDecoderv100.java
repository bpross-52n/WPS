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
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */
package org.n52.simplewps.decode.wpsv100;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.opengis.wps.x100.DescribeProcessDocument;
import net.opengis.wps.x100.DescribeProcessDocument.DescribeProcess;
import net.opengis.wps.x100.ExecuteDocument;
import net.opengis.wps.x100.GetCapabilitiesDocument;
import net.opengis.wps.x100.GetCapabilitiesDocument.GetCapabilities;

import org.apache.xmlbeans.XmlObject;
import org.n52.iceland.coding.decode.Decoder;
import org.n52.iceland.coding.decode.DecoderKey;
import org.n52.iceland.coding.decode.OperationDecoderKey;
import org.n52.iceland.coding.decode.XmlNamespaceDecoderKey;
import org.n52.iceland.exception.ows.OwsExceptionReport;
import org.n52.iceland.exception.ows.concrete.UnsupportedDecoderInputException;
import org.n52.iceland.ogc.wps.Wps1Constants;
import org.n52.iceland.ogc.wps.WpsConstants;
import org.n52.iceland.request.GetCapabilitiesRequest;
import org.n52.iceland.service.AbstractServiceCommunicationObject;
import org.n52.iceland.util.CollectionHelper;
import org.n52.iceland.util.http.MediaTypes;
import org.n52.simplewps.request.operator.DescribeProcessRequest;
import org.n52.simplewps.request.operator.ExecuteRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WPSDecoderv100 implements Decoder<AbstractServiceCommunicationObject, XmlObject> {

    private static final Logger LOGGER = LoggerFactory.getLogger(WPSDecoderv100.class);

    private static final Set<DecoderKey> DECODER_KEYS = CollectionHelper.union(decoderKeysForElements(Wps1Constants.NS_WPS, GetCapabilitiesDocument.class, DescribeProcessDocument.class,
            ExecuteDocument.class), xmlDecoderKeysForOperation(WpsConstants.WPS, Wps1Constants.SERVICEVERSION, WpsConstants.Operations.GetCapabilities,
            WpsConstants.Operations.DescribeProcess, WpsConstants.Operations.Execute), xmlDecoderKeysForOperation(WpsConstants.WPS, null, WpsConstants.Operations.GetCapabilities));

    @Override
    public Set<DecoderKey> getKeys() {
        return Collections.unmodifiableSet(DECODER_KEYS);
    }

    @Override
    public AbstractServiceCommunicationObject decode(XmlObject xmlObject) throws OwsExceptionReport, UnsupportedDecoderInputException {

        AbstractServiceCommunicationObject request = null;
        LOGGER.debug("REQUESTTYPE:" + xmlObject.getClass());
        // validate document
//        XmlHelper.validateDocument(xmlObject);

        // GetCapabilities request
        if (xmlObject instanceof GetCapabilitiesDocument) {
            GetCapabilitiesDocument getCapsDoc = (GetCapabilitiesDocument) xmlObject;
            request = parseGetCapabilities(getCapsDoc);
        } else // DescribeProcess request
        if (xmlObject instanceof DescribeProcessDocument) {
            DescribeProcessDocument describeProcessDoc = (DescribeProcessDocument) xmlObject;
            request = parseDescribeProcess(describeProcessDoc);
        } else // Execute request
        if (xmlObject instanceof ExecuteDocument) {
            ExecuteDocument executeDoc = (ExecuteDocument) xmlObject;
            request = parseExecute(executeDoc);
        }
        else {
//            throw new UnsupportedDecoderXmlInputException(this, xmlObject);
        }
        return request;
    }

    private AbstractServiceCommunicationObject parseExecute(ExecuteDocument executeDoc) {
        
        ExecuteRequest request = new ExecuteRequest();
        
        request.setService(executeDoc.getExecute().getService());
        request.setVersion(executeDoc.getExecute().getVersion());
        request.setExecute(executeDoc);
        
        return request;
    }

    private AbstractServiceCommunicationObject parseDescribeProcess(DescribeProcessDocument describeProcessDoc) {
        DescribeProcessRequest request = new DescribeProcessRequest();
        
        DescribeProcess describeProcess = describeProcessDoc.getDescribeProcess();
        
        request.setService(describeProcess.getService());
        request.setVersion(describeProcess.getVersion());
        request.setProcessIdentifier(describeProcess.getIdentifierArray()[0].getStringValue());
        return request;
    }

    private AbstractServiceCommunicationObject parseGetCapabilities(GetCapabilitiesDocument getCapsDoc) {
        
        GetCapabilities getCaps = getCapsDoc.getGetCapabilities();
        GetCapabilitiesRequest request = new GetCapabilitiesRequest(getCaps.getService());

        if (getCaps.getAcceptVersions() != null && getCaps.getAcceptVersions().sizeOfVersionArray() != 0) {
            request.setAcceptVersions(Arrays.asList(getCaps.getAcceptVersions().getVersionArray()));
        }

        return request;
    }

    public static Set<DecoderKey> xmlDecoderKeysForOperation(String service, String version, Enum<?>... operations) {
        final HashSet<DecoderKey> set = new HashSet<DecoderKey>(operations.length);
        for (final Enum<?> o : operations) {
            set.add(new OperationDecoderKey(service, version, o.name(), MediaTypes.TEXT_XML));
            set.add(new OperationDecoderKey(service, version, o.name(), MediaTypes.APPLICATION_XML));
        }
        return set;
    }

    public static Set<DecoderKey> decoderKeysForElements(final String namespace, final Class<?>... elements) {
        final HashSet<DecoderKey> keys = new HashSet<DecoderKey>(elements.length);
        for (final Class<?> x : elements) {
            keys.add(new XmlNamespaceDecoderKey(namespace, x));
        }
        return keys;
    }

}
