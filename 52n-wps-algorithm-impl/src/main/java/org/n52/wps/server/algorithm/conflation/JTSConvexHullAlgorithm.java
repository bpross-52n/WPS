/**
 * Copyright (C) 2013
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 * 
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 * 
 * This program is free software; you can redistribute and/or modify it under 
 * the terms of the GNU General Public License version 2 as published by the 
 * Free Software Foundation.
 * 
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
 * 
 */
package org.n52.wps.server.algorithm.conflation;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;

import org.n52.wps.algorithm.annotation.Algorithm;
import org.n52.wps.algorithm.annotation.ComplexDataInput;
import org.n52.wps.algorithm.annotation.ComplexDataOutput;
import org.n52.wps.algorithm.annotation.Execute;
import org.n52.wps.io.data.binding.complex.JTSGeometryBinding;
import org.n52.wps.server.AbstractAnnotatedAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;

/**
 * This algorithm creates a convex hull of a JTS geometry using build the in method.
 * @author BenjaminPross
 *
 */
@Algorithm(version = "1.1.0")
public class JTSConvexHullAlgorithm extends AbstractAnnotatedAlgorithm {

    private static Logger LOGGER = LoggerFactory.getLogger(JTSConvexHullAlgorithm.class);
	private Properties properties;

    public JTSConvexHullAlgorithm() {
        super();
		properties = new Properties();
		
		try {
	    	String domain;
			try {
				domain = new File(getClass().getResource("/").toURI()).toString();
			} catch (URISyntaxException e) {
				throw new RuntimeException(e);
			}
	        int index = domain.indexOf("WEB-INF");
	        if (index > 0) {
	            String substring = domain.substring(0, index);
	            LOGGER.info(substring);	            
	        }
//			properties.load(getClass().getResourceAsStream("featureNamespaceProvenanceMappings.properties"));
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
    }
    
    private Geometry result;
    private Geometry data;

    @ComplexDataOutput(identifier = "result", binding = JTSGeometryBinding.class)
    public Geometry getResult() {
        return result;
    }

    @ComplexDataInput(identifier = "data", binding = JTSGeometryBinding.class)
    public void setData(Geometry data) {
        this.data = data;
    }

    @Execute
    public void runAlgorithm() {
    	result = data.convexHull();
    }
}