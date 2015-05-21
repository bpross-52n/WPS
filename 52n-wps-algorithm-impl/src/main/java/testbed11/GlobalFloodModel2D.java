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

import org.n52.wps.algorithm.annotation.Algorithm;
import org.n52.wps.algorithm.annotation.Execute;
import org.n52.wps.algorithm.annotation.LiteralDataInput;
import org.n52.wps.algorithm.annotation.LiteralDataOutput;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.server.AbstractAnnotatedAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Algorithm(version="0.1.0")
public class GlobalFloodModel2D extends AbstractAnnotatedAlgorithm {
	
	private static Logger LOGGER = LoggerFactory
			.getLogger(GlobalFloodModel2D.class);

	private String returnPeriod;
	private String layerID;
    
    public GlobalFloodModel2D(){}
    
    @Execute
	public void call2DModel() throws Exception {
    	
    	Thread.sleep(5000);    	
    	
    	String dummyString = returnPeriod;
    	
    	LOGGER.debug(dummyString);
    	
    	layerID = "testbed11:fluvial_undefended_1in1000_tile_1";
	}

    @LiteralDataOutput(identifier="layerID", binding=LiteralStringBinding.class)
	public String getLayerID() {
		return layerID;
	}

    @LiteralDataInput(identifier="returnPeriod", binding=LiteralStringBinding.class, allowedValues={"1:10", "1:100", "1:1000"})
	public void setReturnPeriod(String returnPeriod) {
		this.returnPeriod = returnPeriod;
	}
	
}