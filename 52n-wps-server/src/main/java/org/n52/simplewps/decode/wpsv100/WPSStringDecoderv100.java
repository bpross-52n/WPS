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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.iceland.coding.CodingRepository;
import org.n52.iceland.coding.decode.Decoder;
import org.n52.iceland.coding.decode.DecoderKey;
import org.n52.iceland.coding.decode.OperationDecoderKey;
import org.n52.iceland.coding.decode.XmlNamespaceDecoderKey;
import org.n52.iceland.exception.ows.NoApplicableCodeException;
import org.n52.iceland.exception.ows.OwsExceptionReport;
import org.n52.iceland.exception.ows.concrete.NoDecoderForKeyException;
import org.n52.iceland.exception.ows.concrete.UnsupportedDecoderInputException;
import org.n52.iceland.ogc.wps.Wps1Constants;
import org.n52.iceland.ogc.wps.WpsConstants;
import org.n52.iceland.service.AbstractServiceCommunicationObject;
import org.n52.iceland.util.CollectionHelper;
import org.n52.iceland.util.http.MediaTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import com.google.common.base.Joiner;

/**
 * String request {@link Decoder} for SOS 1.0.0 requests
 *
 * @author <a href="mailto:c.hollmann@52north.org">Carsten Hollmann</a>
 * @since 5.0.0
 *
 */
public class WPSStringDecoderv100 implements Decoder<AbstractServiceCommunicationObject, String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(WPSStringDecoderv100.class);

    private static final Set<DecoderKey> DECODER_KEYS = CollectionHelper.union(xmlDecoderKeysForOperation(WpsConstants.WPS,
            Wps1Constants.SERVICEVERSION, WpsConstants.Operations.GetCapabilities,
            WpsConstants.Operations.DescribeProcess, WpsConstants.Operations.Execute), xmlDecoderKeysForOperation(WpsConstants.WPS,
                    null, WpsConstants.Operations.GetCapabilities));

    public WPSStringDecoderv100() {
        LOGGER.debug("Decoder for the following keys initialized successfully: {}!", Joiner.on(", ")
                .join(DECODER_KEYS));
    }

    public Set<DecoderKey> getKeys() {
        return Collections.unmodifiableSet(DECODER_KEYS);
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

    @Override
    public AbstractServiceCommunicationObject decode(String objectToDecode) throws OwsExceptionReport, UnsupportedDecoderInputException {
        return (AbstractServiceCommunicationObject) decodeXmlObject(objectToDecode);
    }

    public static Object decodeXmlObject(final String xmlString) throws OwsExceptionReport {
        try {
            return decodeXmlObject(XmlObject.Factory.parse(xmlString));
        } catch (final XmlException e) {
            throw new NoApplicableCodeException();
        }
    }

    public static Object decodeXmlObject(final XmlObject xbObject) throws OwsExceptionReport {
        final DecoderKey key = getDecoderKey(xbObject);
        final Decoder<?, XmlObject> decoder = CodingRepository.getInstance().getDecoder(key);
        if (decoder == null) {
            throw new NoDecoderForKeyException(key);
        }
        return decoder.decode(xbObject);
    }
    
    public static DecoderKey getDecoderKey(final XmlObject doc) {
        return new XmlNamespaceDecoderKey(getNamespace(doc), doc.getClass());
    }

    public static String getNamespace(final XmlObject doc) {
        Node domNode = doc.getDomNode();
        String namespaceURI = domNode.getNamespaceURI();
        if (namespaceURI == null && domNode.getFirstChild() != null) {
            namespaceURI = domNode.getFirstChild().getNamespaceURI();
        }
        /*
         * if document starts with a comment, get next sibling (and ignore
         * initial comment)
         */
        if (namespaceURI == null &&
            domNode.getFirstChild() != null &&
            domNode.getFirstChild().getNextSibling() != null) {
            namespaceURI = domNode.getFirstChild().getNextSibling().getNamespaceURI();
        }
        // check with schemaType namespace, necessary for anyType elements
        final String schemaTypeNamespace = getSchemaTypeNamespace(doc);
        if (schemaTypeNamespace == null) {
            return namespaceURI;
        } else {
            if (schemaTypeNamespace.equals(namespaceURI)) {
                return namespaceURI;
            } else {
                return schemaTypeNamespace;
            }
        }

    }

    private static String getSchemaTypeNamespace(final XmlObject doc) {
        QName name = null;
        if (doc.schemaType().isAttributeType()) {
            name = doc.schemaType().getAttributeTypeAttributeName();
        } else {
            // TODO check else/if for ...schemaType().isDocumentType ?
            name = doc.schemaType().getName();
        }
        if (name != null) {
            return name.getNamespaceURI();
        }
        return null;
    }
}
