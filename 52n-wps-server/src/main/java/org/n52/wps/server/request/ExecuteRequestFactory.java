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
package org.n52.wps.server.request;

import javax.inject.Inject;

import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.io.GeneratorFactory;
import org.n52.wps.io.ParserFactory;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.RepositoryManager;
import org.n52.wps.server.database.DatabaseFactory;
import org.w3c.dom.Document;

/**
 * Factory class to create ExecuteRequests with necessary injected classes
 * 
 * @author Benjamin Pross
 *
 */
public class ExecuteRequestFactory {

    @Inject
    private RepositoryManager repositoryManager;
    
    @Inject
    private ParserFactory parserFactory;
    
    @Inject
    private GeneratorFactory generatorFactory;
    
    @Inject
    private WPSConfig wpsConfig;
    
    @Inject
    private DatabaseFactory databaseFactory;
    
    public ExecuteRequest createExecuteRequest(Document doc){
        try {
            return new ExecuteRequestV100(doc, repositoryManager, parserFactory, databaseFactory, wpsConfig);
        } catch (ExceptionReport e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
    
    public ExecuteRequest createExecuteRequest(CaseInsensitiveMap ciMap){
        try {
            return new ExecuteRequestV100(ciMap, repositoryManager, parserFactory, databaseFactory, wpsConfig);
        } catch (ExceptionReport e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
        
    }
}
