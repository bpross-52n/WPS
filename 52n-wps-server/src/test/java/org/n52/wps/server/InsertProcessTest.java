/*
 * Copyright (C) 2007-2017 52°North Initiative for Geospatial Open Source
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
package org.n52.wps.server;

import java.io.IOException;
import java.util.UUID;

import org.apache.xmlbeans.XmlException;
import org.junit.Test;
import org.n52.wps.commons.XMLBeansHelper;
import org.n52.wps.server.util.InsertProcessUtils;

import net.opengis.wps.x20.InsertProcessDocument;
import net.opengis.wps.x20.InsertProcessInfoDocument;

public class InsertProcessTest {

    @Test
    public void testInsertProcess(){

        InsertProcessInfoDocument response = InsertProcessInfoDocument.Factory.newInstance();

        response.addNewInsertProcessInfo();

        XMLBeansHelper.addSchemaLocationToXMLObject(response, "http://www.opengis.net/wps/2.0 localhost:8080/wps/static/schemas/wpsInsertProcess.xsd");

        response.getInsertProcessInfo().setProcessID("test");

        System.out.println(response.xmlText(XMLBeansHelper.getXmlOptions()));

    }

    @Test
    public void testGetProcessSpecification() throws XmlException, IOException{

        InsertProcessDocument insertProcessDocument = InsertProcessDocument.Factory.parse(getClass().getResourceAsStream("insertprocess-request.xml"));

        System.out.println(InsertProcessUtils.getInstance().getProcessSpecification(insertProcessDocument));

    }

    @Test
    public void testCreateMultipartContent() throws XmlException, IOException{

        InsertProcessDocument insertProcessDocument = InsertProcessDocument.Factory.parse(getClass().getResourceAsStream("insertprocess-request.xml"));

        Object o = InsertProcessUtils.getInstance().getProcessSpecification(insertProcessDocument);

        String processSpecification = "";

        if(o instanceof String){
            processSpecification = (String)o;
        }

        String boundary = UUID.randomUUID().toString();

        String multipartContent = InsertProcessUtils.getInstance().createMultipartContent(processSpecification, "test-deployment", "test.bpmn", boundary);

        System.out.println(multipartContent);

    }

}
