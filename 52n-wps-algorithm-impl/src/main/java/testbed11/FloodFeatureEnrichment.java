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
package testbed11;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.xmlbeans.XmlObject;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.algorithm.annotation.Algorithm;
import org.n52.wps.algorithm.annotation.ComplexDataInput;
import org.n52.wps.algorithm.annotation.ComplexDataOutput;
import org.n52.wps.algorithm.annotation.Execute;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.io.data.binding.complex.GenericXMLDataBinding;
import org.n52.wps.server.AbstractAnnotatedAlgorithm;
import org.n52.wps.server.LocalAlgorithmRepository;
import org.n52.wps.server.grass.configurationmodule.GrassProcessRepositoryCM;
import org.n52.wps.webapp.api.ConfigurationCategory;
import org.n52.wps.webapp.api.ConfigurationModule;
import org.n52.wps.webapp.api.types.ConfigurationEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Algorithm(version="0.0.1")
public class FloodFeatureEnrichment extends AbstractAnnotatedAlgorithm {

	private static Logger LOGGER = LoggerFactory.getLogger(FloodFeatureEnrichment.class);
	
    private XmlObject complexInput;

    private XmlObject complexOutput;
    
    private final String userNamePropertyName = "testbed-11-FME-user";
    private final String passwordPropertyName = "testbed-11-FME-pwd";
    private final String urlPropertyName = "testbed-11-FME-url";
    private final String protocolPropertyName = "testbed-11-FME-protocol";
    
    private String userName = "tb11user";
    private String password = "flood11";
    private String url = "testbed11-dean.fmecloud.com/fmedatastreaming/UCR/FloodFeatureService.fmw";
    private String protocol = "https://";
    
    public FloodFeatureEnrichment(){
    	
		ConfigurationModule localAlgorithmConfigModule = WPSConfig.getInstance().getConfigurationModuleForClass(LocalAlgorithmRepository.class.getName(), ConfigurationCategory.REPOSITORY);
    	
		List<? extends ConfigurationEntry<?>> propertyArray = localAlgorithmConfigModule.getConfigurationEntries();
		
		/*
		 * get properties of process from repository config module
		 *
		 */
		
		for (ConfigurationEntry<?> property : propertyArray) {			
    		if(property.getKey().equals(userNamePropertyName)){
    			userName = property.getValue().toString();
    		}else if(property.getKey().equals(passwordPropertyName)){
    			password = property.getValue().toString();
    		}else if(property.getKey().equals(urlPropertyName)){
    			url = property.getValue().toString();
    		}else if(property.getKey().equals(protocolPropertyName)){
    			protocol = property.getValue().toString();
    		}
		}
    	
    }
    
    @Execute
	public void callFMEServer() throws Exception {

		HttpPost post = new HttpPost(protocol + userName + ":" + password + "@" + url);

		if (!post.containsHeader("Content-Type")) {
			post.addHeader("Content-Type", "application/xml");
		}

		post.setEntity(createEntity(complexInput.xmlText().getBytes("UTF-8")));

		if (!post.containsHeader("Accept-Encoding")) {
			post.addHeader("Accept-Encoding", "gzip");
		}

		HttpResponse response = execute(post);

		InputStream is = response.getEntity().getContent();

		complexOutput = XmlObject.Factory.parse(is);
	}

	public HttpEntity createEntity(byte[] data) throws IOException {
        AbstractHttpEntity entity;
        if (data.length < 8192) {
            entity = new ByteArrayEntity(data);
        } else {
        	LOGGER.info("Zipping input.");
            ByteArrayOutputStream arr = new ByteArrayOutputStream();
            OutputStream zipper = new GZIPOutputStream(arr);
            zipper.write(data);
            zipper.close();
            entity = new ByteArrayEntity(arr.toByteArray());
            entity.setContentEncoding("gzip");
        }
        return entity;
	}
    
	public HttpResponse execute(HttpUriRequest request) throws IOException {
		HttpClient client = new DefaultHttpClient();
		try {
			HttpResponse  result = client.execute(request);
			return result;
		} catch (ClientProtocolException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		}
	}

    @ComplexDataOutput(identifier = "floodRiskResult", title = "floodRiskResult", abstrakt="GML enriched with flood risk information.", binding = GenericXMLDataBinding.class)
    public XmlObject getComplexOutput() {
        return complexOutput;
    }

    @ComplexDataInput(binding = GenericXMLDataBinding.class, abstrakt="GML that should be enriched with flood risk information.", title = "floodRiskSource", identifier = "floodRiskSource", minOccurs = 0, maxOccurs = 1)
    public void setComplexInput(XmlObject complexInput) {
        this.complexInput = complexInput;
    }
	
	
}
