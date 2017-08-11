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
package org.n52.wps.server.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DecompressingHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.n52.wps.commons.XMLUtil;
import org.n52.wps.server.request.strategy.ReferenceInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.opengis.wps.x20.InsertProcessDocument;
import net.opengis.wps.x20.ProcessSpecificationType;

public class InsertProcessUtils {

    private static Logger LOGGER = LoggerFactory.getLogger(InsertProcessUtils.class);

    private static String lineSeparator = System.getProperty("line.separator");

    private StringWriter stringWriter;

    private JsonFactory f;

    private JsonGenerator g;

    private ObjectMapper m;

    private static InsertProcessUtils instance;

    private InsertProcessUtils(){

        stringWriter = new StringWriter();

        f = new JsonFactory();
        try {
            g = f.createGenerator(stringWriter);
        } catch (IOException e) {
           LOGGER.error("Could not create JsonGenerator in constructor.", e);
        }

        m = new ObjectMapper();

    }

    public static InsertProcessUtils getInstance(){
        if(instance == null){
            instance = new InsertProcessUtils();
        }
        return instance;
    }

    public Object getProcessSpecification(InsertProcessDocument request){

        Object processSpecification = null;

        ProcessSpecificationType processSpecificationType = request.getInsertProcess().getProcessSpecification();

        if(processSpecificationType.isSetProcessSpecificationAsValue()){
            try {
                Node parentNode = processSpecificationType.getProcessSpecificationAsValue().getDomNode();
                Node node = null;
                if(parentNode.hasChildNodes() && parentNode.getChildNodes().getLength() > 1){
                    node = parentNode.getChildNodes().item(1);
                }else if(parentNode.hasChildNodes()){
                    node = parentNode.getFirstChild();
                }
                processSpecification = XMLUtil.nodeToString(node);
            } catch (TransformerFactoryConfigurationError | TransformerException e) {
                LOGGER.error("Could not transform Node to String.", e);
            }
        }else if(processSpecificationType.isSetProcessSpecificationAsReference()){
            String href = processSpecificationType.getProcessSpecificationAsReference().getHref();
            try {
                return new URL(href).openStream();
            } catch (MalformedURLException e) {
                LOGGER.error("Malformed URL: " + href, e);
            } catch (IOException e) {
                LOGGER.error("IOException while trying to access URL: " + href, e);
            }
        }

        return processSpecification;
    }

    public String uploadProcessToCamunda(String processSpecification, String deploymentName, String bpmnFileName, String camundaRestEndpoint){

        String camundaProcessID = "";

        String boundary = UUID.randomUUID().toString();

        String multipartContent = createMultipartContent(processSpecification, deploymentName, bpmnFileName, boundary);

        String mimeType = "multipart/form-data; boundary=" + boundary;

        //upload to Camunda

        try {
            InputStream inputstream = httpPost(camundaRestEndpoint, multipartContent, mimeType);

            JsonNode rootNode = m.readTree(inputstream);

        } catch (IOException e) {
            LOGGER.error("Could not upload BPMN.", e);
        }

        return camundaProcessID;

    }

    /**
     * Make a POST request using mimeType and href
     *
     * TODO: add support for autoretry, proxy
     */
    private ReferenceInputStream httpPost(final String dataURLString, final String body, final String mimeType) throws IOException {
        HttpClient backend = new DefaultHttpClient();

        DecompressingHttpClient httpclient = new DecompressingHttpClient(backend);

        HttpPost httppost = new HttpPost(dataURLString);

        if (mimeType != null){
            httppost.addHeader(new BasicHeader("Content-type", mimeType));
        }

        // set body entity
        HttpEntity postEntity = new StringEntity(body);
        httppost.setEntity(postEntity);

        return processResponse(httpclient.execute(httppost));
    }

    private ReferenceInputStream processResponse(HttpResponse response) throws IOException {

        HttpEntity entity = response.getEntity();
        Header header;

        header = entity.getContentType();
        String mimeType = header == null ? null : header.getValue();

        header = entity.getContentEncoding();
        String encoding = header == null ? null : header.getValue();

        return new ReferenceInputStream(entity.getContent(), mimeType, encoding);
    }

    public String createMultipartContent(String processSpecification, String deploymentName, String bpmnFileName, String boundary){

        //create multipart content

        StringBuilder multiPartContentBuilder = new StringBuilder();

        addBoundary(boundary, multiPartContentBuilder, false);

        Map<String, String> parameterMap = new HashMap<>();

        parameterMap.put("name", "deployment-name");

        addContentDisposition(parameterMap, multiPartContentBuilder);

        multiPartContentBuilder.append(deploymentName + lineSeparator);

        addBoundary(boundary, multiPartContentBuilder, false);

        parameterMap = new HashMap<>();

        parameterMap.put("name", "enable-duplicate-filtering");

        addContentDisposition(parameterMap, multiPartContentBuilder);

        multiPartContentBuilder.append(true + lineSeparator);

        addBoundary(boundary, multiPartContentBuilder, false);

        parameterMap = new HashMap<>();

        parameterMap.put("name", "data");
        parameterMap.put("filename", bpmnFileName);

        addContentDisposition(parameterMap, multiPartContentBuilder);

        multiPartContentBuilder.append(processSpecification + lineSeparator);

        addBoundary(boundary, multiPartContentBuilder, true);

        return multiPartContentBuilder.toString();
    }

    private static void addBoundary(String boundary, StringBuilder builder, boolean endOfContent){

        String multipartBoundary = "--" + boundary + (endOfContent ? "--" : lineSeparator);

        builder.append(multipartBoundary);

    }

    private static void addContentDisposition(Map<String, String> parameterMap, StringBuilder builder){

        String parameterString = "";

        Iterator<String> parameterKeyIterator = parameterMap.keySet().iterator();

        while (parameterKeyIterator.hasNext()) {
            String key = (String) parameterKeyIterator.next();

            parameterString = parameterString.concat(key + "=\"" + parameterMap.get(key) + "\"");
            if(parameterKeyIterator.hasNext()){
                parameterString = parameterString.concat("; ");
            }
        }

        builder.append("Content-Disposition: form-data; " + parameterString + lineSeparator);
        builder.append(lineSeparator);

    }

}
