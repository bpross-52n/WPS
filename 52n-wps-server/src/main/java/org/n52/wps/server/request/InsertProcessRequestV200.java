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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.UUID;

import org.apache.xmlbeans.XmlException;
import org.n52.wps.algorithm.util.JavaProcessCompiler;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.commons.XMLBeansHelper;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.RepositoryManagerSingletonWrapper;
import org.n52.wps.server.modules.UploadedAlgorithmRepositoryCM;
import org.n52.wps.server.response.InsertProcessResponseV200;
import org.n52.wps.server.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import net.opengis.wps.x20.InsertProcessDocument;
import net.opengis.wps.x20.InsertProcessInfoDocument;

public class InsertProcessRequestV200 extends Request {

    private static Logger LOGGER = LoggerFactory.getLogger(InsertProcessRequestV200.class);

    private InsertProcessInfoDocument response;

    private InsertProcessDocument request;

    private String processID;

    private String pathToProcessArtifacts = "/workflow/";

    private String pathToUploadDirectory = "";

    private String workflowProcessName = "ConflationWorkflow";

    private String charset = "UTF-8";

    private String lineSeparator = System.getProperty("line.separator");

    public InsertProcessRequestV200(Document doc) throws ExceptionReport {
        super(doc);

        String domain = WPSConfig.class.getProtectionDomain().getCodeSource().getLocation().getFile();

        try {
            domain = URLDecoder.decode(domain, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("Could not decode domain: " + domain, e);
        }

        int startIndex = domain.indexOf("lib");

        domain = domain.substring(0, startIndex);
        String baseDirLocation = domain;

        pathToUploadDirectory = baseDirLocation + "/classes/uploaded/testbed13/dsi/";

        File folder = new File(pathToUploadDirectory);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        if (!validate()) {
            throw new ExceptionReport("InsertProcessRequest not valid", ExceptionReport.NO_APPLICABLE_CODE);
        }
        if (request.getInsertProcess().getProcessSpecification().getId() != null) {
            processID = request.getInsertProcess().getProcessSpecification().getId();
        }
        if (processID == null || processID.equals("")) {
            throw new ExceptionReport("ProcessID cannot be empty", ExceptionReport.INVALID_PARAMETER_VALUE,
                    "processID");
        }

        createWorkflow(processID);

    }

    @Override
    public Object getAttachedResult() {
        return response;
    }

    @Override
    public Response call() throws ExceptionReport {

        response = InsertProcessInfoDocument.Factory.newInstance();

        response.addNewInsertProcessInfo();

        XMLBeansHelper.addSchemaLocationToXMLObject(response,
                "http://www.opengis.net/wps/2.0 http://tb12.dev.52north.org/schemas/wpsInsertProcess.xsd");

        response.getInsertProcessInfo().setProcessID(processID);

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

    private String createWorkflow(String originalProcessID) {

        processID = originalProcessID + "_" + UUID.randomUUID().toString().substring(0, 5);

        String workflowJavaClassContent = readArtifactAndReplaceID(getClass().getResourceAsStream(pathToProcessArtifacts + workflowProcessName + ".java"), processID);

        String workflowXMLProcessDescriptionContent = readArtifactAndReplaceID(getClass().getResourceAsStream(pathToProcessArtifacts + workflowProcessName + ".xml"), processID);

        String javaClassFilename = pathToUploadDirectory + processID + ".java";

        writeArtifact(workflowJavaClassContent, new File(javaClassFilename));

        writeArtifact(workflowXMLProcessDescriptionContent, new File(pathToUploadDirectory + processID + ".xml"));

        JavaProcessCompiler.compile(javaClassFilename);

        WPSConfig.getInstance().getConfigurationManager().getConfigurationServices().addAlgorithmEntry(UploadedAlgorithmRepositoryCM.class.getName(), "testbed13.dsi." + processID);

        RepositoryManagerSingletonWrapper.getInstance().addAlgorithm("testbed13.dsi." + processID);

        return processID;

    }

    private String readArtifactAndReplaceID(InputStream artifactInputstream,
            String newProcessID) {

        String content = "";

        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(artifactInputstream, charset));

            String line = null;

            while ((line = bufferedReader.readLine()) != null) {

                if (line.contains(workflowProcessName)) {
                    line = line.replace(workflowProcessName, newProcessID);
                }

                content = content.concat(line + lineSeparator);
            }

        } catch (UnsupportedEncodingException e) {
            LOGGER.error("Unsupported encoding.", e);
        } catch (IOException e) {
            LOGGER.error("IOException while trying to read artifact.", e);
        }

        return content;
    }

    private void writeArtifact(String arctifactContent,
            File outputFile) {

        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile));

            bufferedWriter.write(arctifactContent);

            bufferedWriter.close();

        } catch (IOException e) {
            LOGGER.error("IOException while trying to write artifact to file: " + outputFile.getAbsolutePath(), e);
        }

    }

}
