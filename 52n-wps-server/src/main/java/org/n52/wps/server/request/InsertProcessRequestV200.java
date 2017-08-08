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
package org.n52.wps.server.request;

import org.apache.xmlbeans.XmlException;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.response.InsertProcessResponseV200;
import org.n52.wps.server.response.Response;
import org.w3c.dom.Document;

import net.opengis.wps.x20.InsertProcessDocument;
import net.opengis.wps.x20.InsertProcessInfoDocument;

public class InsertProcessRequestV200 extends Request {

    private InsertProcessInfoDocument response;

    private InsertProcessDocument request;

    private String processID;

    public InsertProcessRequestV200(Document doc) throws ExceptionReport {
        super(doc);

        if(!validate()){
            throw new ExceptionReport("InsertProcessRequest not valid",
                    ExceptionReport.NO_APPLICABLE_CODE);
        }
        if(request.getInsertProcess().getProcessSpecification().getId() != null){
            processID = request.getInsertProcess().getProcessSpecification().getId();
        }
        if(processID == null || processID.equals("")){
            throw new ExceptionReport("ProcessID cannot be empty",
                    ExceptionReport.INVALID_PARAMETER_VALUE, "processID");
        }
    }

    @Override
    public Object getAttachedResult() {
        return response;
    }

    @Override
    public Response call() throws ExceptionReport {

        response = InsertProcessInfoDocument.Factory.newInstance();

        response.addNewInsertProcessInfo().setProcessID(processID);

        return new InsertProcessResponseV200(this);
    }

    @Override
    public boolean validate() throws ExceptionReport {
        try {
            request = InsertProcessDocument.Factory.parse(doc.getFirstChild());
        } catch (XmlException e) {
            return false;
        }

        return request != null;
    }

}
