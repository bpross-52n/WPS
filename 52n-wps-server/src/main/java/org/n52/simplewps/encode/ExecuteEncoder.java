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
package org.n52.simplewps.encode;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import org.apache.xmlbeans.XmlObject;
import org.n52.iceland.coding.OperationKey;
import org.n52.iceland.coding.encode.AbstractDelegatingEncoder;
import org.n52.iceland.coding.encode.EncoderKey;
import org.n52.iceland.coding.encode.OperationEncoderKey;
import org.n52.iceland.coding.encode.XmlEncoderKey;
import org.n52.iceland.exception.ows.OwsExceptionReport;
import org.n52.iceland.exception.ows.concrete.UnsupportedEncoderInputException;
import org.n52.iceland.ogc.ows.OWSConstants.HelperValues;
import org.n52.iceland.util.http.MediaType;
import org.n52.iceland.util.http.MediaTypes;
import org.n52.simplewps.request.operator.ExecuteResponse;

import com.google.common.collect.Sets;

public class ExecuteEncoder<T extends ExecuteResponse> extends AbstractDelegatingEncoder<XmlObject, ExecuteResponse> {

    @Override
    public XmlObject encode(ExecuteResponse objectToEncode) throws OwsExceptionReport, UnsupportedEncoderInputException {
        return encode(objectToEncode, new EnumMap<>(HelperValues.class));
    }

    @Override
    public XmlObject encode(ExecuteResponse response,
            Map<HelperValues, String> additionalValues) throws OwsExceptionReport, UnsupportedEncoderInputException {
        if (response == null) {
            throw new UnsupportedEncoderInputException(this, response);
        }
        XmlObject xml = (XmlObject) response.getResultObject();
        // try {
        // xml = XmlObject.Factory.parse(response.getResponse());
        // } catch (XmlException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        return xml;
    }

    @Override
    public MediaType getContentType() {
        return MediaTypes.TEXT_XML;
    }

    @Override
    public Set<EncoderKey> getKeys() {
        OperationKey key = new OperationKey("WPS", "1.0.0", "Execute");
        Set<EncoderKey> encoderKeys =
                Sets.newHashSet(new XmlEncoderKey("wps", ExecuteResponse.class), new OperationEncoderKey(key, MediaTypes.TEXT_XML), new OperationEncoderKey(key, MediaTypes.APPLICATION_XML));
        return encoderKeys;
    }

}
