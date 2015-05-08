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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.n52.wps.commons.WPSConfig;
import org.n52.wps.io.data.GenericFileData;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralDoubleBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.server.AbstractAlgorithm;
import org.n52.wps.server.ExceptionReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FloodImpactAssessment extends AbstractAlgorithm {

	private static Logger LOGGER = LoggerFactory
			.getLogger(FloodImpactAssessment.class);

	public FloodImpactAssessment() {
	}

	@Override
	public Map<String, IData> run(Map<String, List<IData>> inputData)
			throws ExceptionReport {

		List<IData> nonArableLandDataList = inputData.get("nonArableLand");

		double nonArableLandInPercent = ((LiteralDoubleBinding) nonArableLandDataList
				.get(0)).getPayload();

		List<IData> outputParameterDataList = inputData.get("outputParameter");

		String outputParameter = ((LiteralStringBinding) outputParameterDataList
				.get(0)).getPayload();

		InputStream outputFileStream = null;
		InputStream referenceFileStream = null;

		String baseDir = WPSConfig.getInstance().getApplicationBaseDir();

		try {

			if (outputParameter.equals("aggregated-land-allocation")) {
				outputFileStream = new FileInputStream(new File(baseDir
						+ "/static/data/aggregated-land-allocation-50.png"));
				referenceFileStream = new FileInputStream(new File(baseDir
						+ "/static/data/aggregated-land-allocation-0.png"));
			} else if (outputParameter.equals("prices-by-sector")) {
				outputFileStream = new FileInputStream(new File(baseDir
						+ "/static/data/prices-by-sector-50.png"));
				referenceFileStream = new FileInputStream(new File(baseDir
						+ "/static/data/prices-by-sector-0.png"));
			}
		} catch (IOException e) {
			LOGGER.debug(e.getMessage());
		}
		Map<String, IData> results = new HashMap<String, IData>();
		results.put("impactAssessmentDiagram", new GenericFileDataBinding(
				new GenericFileData(outputFileStream, "image/png")));
		results.put("referenceDiagram", new GenericFileDataBinding(
				new GenericFileData(referenceFileStream, "image/png")));

		return results;
	}

	@Override
	public List<String> getErrors() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<?> getInputDataType(String id) {
		switch (id) {
		case "nonArableLand":
			return LiteralDoubleBinding.class;
		case "outputParameter":
			return LiteralStringBinding.class;
		default:
			return LiteralStringBinding.class;
		}
	}

	@Override
	public Class<?> getOutputDataType(String id) {
		return GenericFileDataBinding.class;
	}

}
