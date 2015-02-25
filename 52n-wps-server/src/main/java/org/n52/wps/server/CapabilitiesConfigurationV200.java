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

package org.n52.wps.server;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.concurrent.locks.ReentrantLock;

import net.opengis.ows.x20.AddressType;
import net.opengis.ows.x20.CodeType;
import net.opengis.ows.x20.ContactType;
import net.opengis.ows.x20.DCPDocument.DCP;
import net.opengis.ows.x20.HTTPDocument.HTTP;
import net.opengis.ows.x20.LanguageStringType;
import net.opengis.ows.x20.OperationDocument.Operation;
import net.opengis.ows.x20.OperationsMetadataDocument.OperationsMetadata;
import net.opengis.ows.x20.RequestMethodType;
import net.opengis.ows.x20.ResponsiblePartySubsetType;
import net.opengis.ows.x20.ServiceIdentificationDocument.ServiceIdentification;
import net.opengis.ows.x20.ServiceProviderDocument.ServiceProvider;
import net.opengis.wps.x200.CapabilitiesDocument;
import net.opengis.wps.x200.ContentsDocument.Contents;
import net.opengis.wps.x200.ProcessDescriptionType;
import net.opengis.wps.x200.ProcessOfferingDocument.ProcessOffering;
import net.opengis.wps.x200.ProcessSummaryType;
import net.opengis.wps.x200.WPSCapabilitiesType;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.webapp.api.ConfigurationManager;
import org.n52.wps.webapp.entities.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * Encapsulation of the WPS Capabilities document. This class has to be initialized with either a
 * {@linkplain #getInstance(java.io.File) file}, {@linkplain #getInstance(java.net.URL) URL},
 * {@linkplain #getInstance(java.lang.String) path} or
 * {@linkplain #getInstance(net.opengis.wps.x200.CapabilitiesDocument) instance}.
 * 
 * @author foerster
 * @author Christian Autermann
 * @author Benjamin Pross
 * 
 */
public class CapabilitiesConfigurationV200 {
    private static final Logger LOG = LoggerFactory.getLogger(CapabilitiesConfigurationV200.class);

    private static final ReentrantLock lock = new ReentrantLock();

    private static CapabilitiesDocument capabilitiesDocumentObj;

    private static CapabilitiesSkeletonLoadingStrategy loadingStrategy;

    private static ConfigurationManager configurationManager;	
    private static Server serverConfigurationModule;	

    private CapabilitiesConfigurationV200() {
        /* nothing here */
    }

    /**
     * Gets the WPS Capabilities using the specified file to obtain the skeleton. All future calls to
     * {@link #getInstance()} and {@link #getInstance(boolean)
     * } will use this file to obtain the skeleton.
     * 
     * @param filePath
     *        the File pointing to a skeleton
     * 
     * @return the capabilities document
     * 
     * @throws XmlException
     *         if the Capabilities skeleton is not valid
     * @throws IOException
     *         if an IO error occurs
     */
    public static CapabilitiesDocument getInstance(String filePath) throws XmlException, IOException {
        return getInstance(new FileLoadingStrategy(filePath));
    }

    /**
     * Gets the WPS Capabilities using the specified file to obtain the skeleton. All future calls to
     * {@link #getInstance()} and {@link #getInstance(boolean)
     * } will use this file to obtain the skeleton.
     * 
     * @param file
     *        the File pointing to a skeleton
     * 
     * @return the capabilities document
     * 
     * @throws XmlException
     *         if the Capabilities skeleton is not valid
     * @throws IOException
     *         if an IO error occurs
     */
    public static CapabilitiesDocument getInstance(File file) throws XmlException, IOException {
        return getInstance(new FileLoadingStrategy(file));
    }

    /**
     * Gets the WPS Capabilities using the specified URL to obtain the skeleton. All future calls to
     * {@link #getInstance()} and {@link #getInstance(boolean)
     * } will use this URL to obtain the skeleton.
     * 
     * @param url
     *        the URL pointing to a skeleton
     * 
     * @return the capabilities document
     * 
     * @throws XmlException
     *         if the Capabilities skeleton is not valid
     * @throws IOException
     *         if an IO error occurs
     */
    public static CapabilitiesDocument getInstance(URL url) throws XmlException, IOException {
        return getInstance(new URLLoadingStrategy(url));
    }

    /**
     * Gets the WPS Capabilities using the specified skeleton. All future calls to {@link #getInstance()} and
     * {@link #getInstance(boolean) } will use this skeleton.
     * 
     * @param skel
     *        the skeleton
     * 
     * @return the capabilities document
     * 
     * @throws XmlException
     *         if the Capabilities skeleton is not valid
     * @throws IOException
     *         if an IO error occurs
     */
    public static CapabilitiesDocument getInstance(CapabilitiesDocument skel) throws XmlException, IOException {
        return getInstance(new InstanceStrategy(skel));
    }

    /**
     * Gets the WPS Capabilities using the specified strategy. All future calls to {@link #getInstance()} and
     * {@link #getInstance(boolean) } will use this strategy.
     * 
     * @param strategy
     *        the strategy to load the skeleton
     * 
     * @return the capabilities document
     * 
     * @throws XmlException
     *         if the Capabilities skeleton is not valid
     * @throws IOException
     *         if an IO error occurs
     */
    private static CapabilitiesDocument getInstance(CapabilitiesSkeletonLoadingStrategy strategy) throws XmlException,
            IOException {
        Preconditions.checkNotNull(strategy);
        lock.lock();
        try {
            if (strategy.equals(loadingStrategy)) {
                return getInstance(false);
            }
            loadingStrategy = strategy;
            return getInstance(true);
        }
        finally {
            lock.unlock();
        }
    }

    /**
     * Get the WPS Capabilities for this service. The capabilities are reloaded if caching is not enabled in
     * the WPS configuration.
     * 
     * @return the capabilities document
     * 
     * @throws XmlException
     *         if the Capabilities skeleton is not valid
     * @throws IOException
     *         if an IO error occurs
     */
    public static CapabilitiesDocument getInstance() throws XmlException, IOException {
        boolean cached = WPSConfig.getInstance().getWPSConfig().getServer().getCacheCapabilites();
        return getInstance( !cached);
    }

    /**
     * Get the WPS Capabilities for this service and optionally force a reload.
     * 
     * @param reload
     *        if the capabilities should be reloaded
     * 
     * @return the capabilities document
     * 
     * @throws XmlException
     *         if the Capabilities skeleton is not valid
     * @throws IOException
     *         if an IO error occurs
     */
    public static CapabilitiesDocument getInstance(boolean reload) throws XmlException, IOException {
        lock.lock();
        try {
            if (capabilitiesDocumentObj == null || reload) {
            	
            	if(loadingStrategy == null){
            		loadingStrategy = new CreateInstanceStrategy();
            	}
            	
                capabilitiesDocumentObj = loadingStrategy.loadSkeleton();
                initSkeleton(capabilitiesDocumentObj);
            }
            return capabilitiesDocumentObj;
        }
        finally {
            lock.unlock();
        }
    }

    /**
     * Enriches a capabilities skeleton by adding the endpoint URL and creating the process offerings.
     * 
     * @param skel
     *        the skeleton to enrich
     * 
     * @throws UnknownHostException
     *         if the local host name can not be obtained
     */
    private static void initSkeleton(CapabilitiesDocument skel) throws UnknownHostException {
        String endpoint = getEndpointURL();
        if (skel.getCapabilities() == null) {
            skel.addNewCapabilities();
        }
        initOperationsMetadata(skel, endpoint);
        initProcessOfferings(skel);
    }

    /**
     * Enriches the capabilities skeleton by creating the process offerings.
     * 
     * @param skel
     *        the skeleton to enrich
     */
    private static void initProcessOfferings(CapabilitiesDocument skel) {
        Contents contents = skel.getCapabilities()
                .addNewContents();
        for (String algorithmName : RepositoryManager.getInstance()
                .getAlgorithms()) {
        	try {
        		ProcessOffering offering = (ProcessOffering) RepositoryManager
                        .getInstance().getProcessDescription(algorithmName).getProcessDescriptionType(WPSConfig.VERSION_200);
        		
        		ProcessDescriptionType description = offering.getProcess();
        		
                if (description != null) {
                    ProcessSummaryType process = contents.addNewProcessSummary();
                    CodeType ct = process.addNewIdentifier();
                    ct.setStringValue(algorithmName);
                    LanguageStringType title = description.getTitleArray(0);
                    String processVersion = offering.getProcessVersion();
                    process.setProcessVersion(processVersion);
                    
                    process.setJobControlOptions(offering.getJobControlOptions());
                    
                    process.setOutputTransmission(offering.getOutputTransmission());
                    
                    if(description.getTitleArray().length < 1){
                    	description.addNewTitle();
                    	description.getTitleArray()[0] = title;
                    }else{
                    	description.getTitleArray()[0] = title;
                    }
                    
                    LOG.trace("Added algorithm to process offerings: {}\n\t\t{}", algorithmName, process);
                }	
        	}
        	catch (RuntimeException e) {
        		LOG.warn("Exception during instantiation of process {}", algorithmName, e);
        	}
        }
    }

    /**
     * Enriches a capabilities skeleton by adding the endpoint URL to the operations meta data.
     * 
     * @param skel
     *        the skeleton to enrich
     * @param endpointUrl
     *        the endpoint URL of the service
     * 
     */
    private static void initOperationsMetadata(CapabilitiesDocument skel, String endpointUrl) {
        if (skel.getCapabilities().getOperationsMetadata() != null) {
            String endpointUrlGet = endpointUrl + "?";
            for (Operation op : skel.getCapabilities().getOperationsMetadata().getOperationArray()) {
                for (DCP dcp : op.getDCPArray()) {
                    for (RequestMethodType get : dcp.getHTTP().getGetArray()) {

                        get.setHref(endpointUrlGet);
                    }
                    for (RequestMethodType post : dcp.getHTTP().getPostArray()) {
                        post.setHref(endpointUrl);
                    }
                }
            }
        }
    }

    /**
     * Gets the endpoint URL of this service by checking the configuration file and the local host name.
     * 
     * @return the endpoint URL
     * 
     * @throws UnknownHostException
     *         if the local host name could not be resolved into an address
     */
    private static String getEndpointURL() throws UnknownHostException {
        return WPSConfig.getInstance().getServiceEndpoint();
    }

    /**
     * Force a reload of the capabilities skeleton.
     * 
     * @throws XmlException
     *         if the Capabilities skeleton is not valid
     * @throws IOException
     *         if an IO error occurs
     */
    public static void reloadSkeleton() throws XmlException, IOException {
        getInstance(true);
    }

    /**
     * Checks if the capabilities document is loaded.
     * 
     * @return if the capabilities are ready.
     */
    public static boolean ready() {
        lock.lock();
        try {
            return capabilitiesDocumentObj != null;
        }
        finally {
            lock.unlock();
        }
    }
    
	public static Server getServerConfigurationModule() {

		if (serverConfigurationModule == null) {

			if (configurationManager == null) {
				configurationManager = WPSConfig
						.getInstance().getConfigurationManager();
			}if(configurationManager != null){
			    serverConfigurationModule = (Server) configurationManager
					    .getConfigurationServices().getConfigurationModule(
							    Server.class.getName());
			}
		}
		return serverConfigurationModule;
	}

    /**
     * Strategy to load a capabilities skeleton from a URL.
     */
    private static class URLLoadingStrategy implements CapabilitiesSkeletonLoadingStrategy {
        private final URL url;

        /**
         * Creates a new strategy using the specified URL.
         * 
         * @param file
         *        the file
         */
        URLLoadingStrategy(URL url) {
            this.url = Preconditions.checkNotNull(url);
        }

        @Override
        public CapabilitiesDocument loadSkeleton() throws XmlException, IOException {
            XmlOptions options = new XmlOptions().setLoadStripComments();
            return CapabilitiesDocument.Factory.parse(getUrl(), options);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof URLLoadingStrategy) {
                URLLoadingStrategy that = (URLLoadingStrategy) obj;
                return this.getUrl().equals(that.getUrl());
            }
            return false;
        }

        @Override
        public int hashCode() {
            return getUrl().hashCode();
        }

        /**
         * Gets the URL of this strategy.
         * 
         * @return the URL;
         */
        public URL getUrl() {
            return url;
        }
    }

    /**
     * Strategy to load a capabilities skeleton from a file.
     */
    private static class FileLoadingStrategy extends URLLoadingStrategy {

        /**
         * Creates a new strategy using the specified file.
         * 
         * @param file
         *        the file
         */
        FileLoadingStrategy(File file) throws MalformedURLException {
            super(file.toURI().toURL());
        }

        /**
         * Creates a new strategy using the specified file.
         * 
         * @param file
         *        the path to the file
         */
        FileLoadingStrategy(String file) throws MalformedURLException {
            this(new File(Preconditions.checkNotNull(file)));
        }

    }

    /**
     * Strategy to obtain the capabilities skeleton from an existing instance.
     */
    private static class InstanceStrategy implements CapabilitiesSkeletonLoadingStrategy {
        private final CapabilitiesDocument instance;

        /**
         * Creates a new strategy using the specified instance.
         * 
         * @param instance
         *        the instance
         */
        InstanceStrategy(CapabilitiesDocument instance) {
            this.instance = Preconditions.checkNotNull(instance);
        }

        @Override
        public CapabilitiesDocument loadSkeleton() {
            return (CapabilitiesDocument) getInstance().copy();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof InstanceStrategy) {
                InstanceStrategy that = (InstanceStrategy) obj;
                return this.getInstance().equals(that.getInstance());
            }
            return false;
        }

        @Override
        public int hashCode() {
            return getInstance().hashCode();
        }

        /**
         * Gets the instance of this strategy.
         * 
         * @return the instance
         */
        public CapabilitiesDocument getInstance() {
            return instance;
        }
    }

    /**
     * Strategy to obtain the capabilities skeleton from an existing instance.
     */
    private static class CreateInstanceStrategy implements CapabilitiesSkeletonLoadingStrategy {
        private final CapabilitiesDocument instance;

        /**
         * Creates a new strategy using the specified instance.
         * 
         * @param instance
         *        the instance
         */
        CreateInstanceStrategy() {
            this.instance = CapabilitiesDocument.Factory.newInstance();
            
//    		XmlCursor c = instance.newCursor();
//    		c.toFirstChild();
//    		c.toLastAttribute();
//    		c.setAttributeText(new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "schemaLocation"), "http://www.opengis.net/wps/2.0.0 http://schemas.opengis.net/wps/2.0.0/wpsGetCapabilities.xsd");
//            
            /*
             * TODO get necessary input from config
             */
            WPSCapabilitiesType wpsCapabilities = instance.addNewCapabilities();
            
            wpsCapabilities.addNewService().setStringValue("WPS"); //TODO: put in WPSConfig or so
            
            wpsCapabilities.setVersion(WPSConfig.VERSION_200);
            
            ServiceProvider serviceProvider = wpsCapabilities.addNewServiceProvider();
            
            serviceProvider.setProviderName("52°North GmbH");
            
            serviceProvider.addNewProviderSite().setHref("http://52north.org");
                        
            ResponsiblePartySubsetType responsiblePartySubsetType = serviceProvider.addNewServiceContact();
            
            responsiblePartySubsetType.setIndividualName("Benjamin Pross");
            
            ContactType contactType = responsiblePartySubsetType.addNewContactInfo();
            
            AddressType addressType = contactType.addNewAddress();
            
            addressType.addElectronicMailAddress("b.pross@52north.org");
            
            ServiceIdentification serviceIdentification = wpsCapabilities.addNewServiceIdentification();
            
            serviceIdentification.addNewTitle().setStringValue("52°North WPS");
            
            serviceIdentification.addAccessConstraints("NONE");
            
            serviceIdentification.addNewAbstract().setStringValue("52°North WPS");
            
            serviceIdentification.addNewServiceType().setStringValue("WPS");
            
            serviceIdentification.addNewServiceTypeVersion().setStringValue(WPSConfig.VERSION_100);
            
            serviceIdentification.addNewServiceTypeVersion().setStringValue(WPSConfig.VERSION_200);
            
            serviceIdentification.setFees("NONE");
            
            serviceIdentification.addNewKeywords().addNewKeyword().setStringValue("WPS");
            
            OperationsMetadata operationsMetadata = wpsCapabilities.addNewOperationsMetadata();
            
            String wpsWebappPath = WPSConfig.getInstance().getServiceBaseUrl();
            
            String getHREF = wpsWebappPath.endsWith("/") ? wpsWebappPath.substring(0, wpsWebappPath.length()-1) : wpsWebappPath;
            
            getHREF = getHREF.concat("?");
            
            String postHREF = wpsWebappPath;
            
            addOperation(operationsMetadata, "GetCapabilities", getHREF, postHREF);
            addOperation(operationsMetadata, "DescribeProcess", getHREF, postHREF);
            addOperation(operationsMetadata, "Execute", "", postHREF);
            addOperation(operationsMetadata, "GetStatus", getHREF, postHREF);
            addOperation(operationsMetadata, "GetResult", getHREF, postHREF);
        }

        @Override
        public CapabilitiesDocument loadSkeleton() {
            return (CapabilitiesDocument) getInstance().copy();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof InstanceStrategy) {
                InstanceStrategy that = (InstanceStrategy) obj;
                return this.getInstance().equals(that.getInstance());
            }
            return false;
        }

        @Override
        public int hashCode() {
            return getInstance().hashCode();
        }

        /**
         * Gets the instance of this strategy.
         * 
         * @return the instance
         */
        public CapabilitiesDocument getInstance() {
            return instance;
        }
        
        private void addOperation(OperationsMetadata operationsMetadata, String operationName, String getHREF, String postHREF){
                 
            Operation operation = operationsMetadata.addNewOperation();
            
            operation.setName(operationName);
            
            HTTP http = operation.addNewDCP().addNewHTTP();
            
            if(getHREF != null && !getHREF.equals("")){
            	http.addNewGet().setHref(getHREF);            	            	
            }
            if(postHREF != null && !postHREF.equals("")){
            	http.addNewPost().setHref(postHREF);            	            	
            }
        	
        }
    }
    
    /**
     * Strategy to load a capabilities skeleton.
     */
    private interface CapabilitiesSkeletonLoadingStrategy {
        /**
         * Loads a CapabilitiesDocument skeleton. Every call to this method should return another instance.
         * 
         * @return the capabilities skeleton
         * 
         * @throws XmlException
         *         if the Capabilities skeleton is not valid
         * @throws IOException
         *         if an IO error occurs
         */
        CapabilitiesDocument loadSkeleton() throws XmlException, IOException;
    }
}
