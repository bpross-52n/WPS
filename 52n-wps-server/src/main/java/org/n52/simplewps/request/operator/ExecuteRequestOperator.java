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
package org.n52.simplewps.request.operator;

import java.util.Collections;
import java.util.Set;

import javax.inject.Inject;

import org.n52.iceland.exception.ows.OwsExceptionReport;
import org.n52.iceland.ogc.ows.OwsOperation;
import org.n52.iceland.ogc.wps.Wps1Constants;
import org.n52.iceland.ogc.wps.WpsConstants;
import org.n52.iceland.request.AbstractServiceRequest;
import org.n52.iceland.request.operator.RequestOperator;
import org.n52.iceland.request.operator.RequestOperatorKey;
import org.n52.iceland.response.AbstractServiceResponse;
import org.n52.simplewps.handler.ExecuteHandler;
import org.n52.wps.server.RepositoryManager;
import org.w3c.dom.Document;

public class ExecuteRequestOperator implements RequestOperator {

	@Inject
	private RepositoryManager repositoryManager;

	@Override
	public Set<RequestOperatorKey> getKeys() {
		RequestOperatorKey requestOperatorKey = new RequestOperatorKey(WpsConstants.WPS, Wps1Constants.SERVICEVERSION, WpsConstants.Operations.Execute.name(), true);
		return Collections.singleton(requestOperatorKey);
	}

	@Override
	public AbstractServiceResponse receiveRequest(
			AbstractServiceRequest<?> request) throws OwsExceptionReport {		
		return new ExecuteHandler(repositoryManager).getExecuteResponse(request.getVersion(), (Document)((ExecuteRequest)request).getExecute());
	}

	@Override
	public OwsOperation getOperationMetadata(String service, String version)
			throws OwsExceptionReport {		
		return new ExecuteHandler().getOperationsMetadata(service, version);
	}

	@Override
	public RequestOperatorKey getRequestOperatorKeyType() {
		
		return null;
	}

}
