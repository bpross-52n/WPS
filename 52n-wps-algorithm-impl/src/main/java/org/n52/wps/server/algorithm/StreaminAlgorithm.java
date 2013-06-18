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
package org.n52.wps.server.algorithm;

import java.io.InputStream;

import org.apache.log4j.Logger;
import org.n52.wps.algorithm.annotation.Algorithm;
import org.n52.wps.algorithm.annotation.ComplexDataInput;
import org.n52.wps.algorithm.annotation.ComplexDataOutput;
import org.n52.wps.algorithm.annotation.Execute;
import org.n52.wps.io.data.binding.complex.StreamDataBinding;
import org.n52.wps.server.AbstractAnnotatedAlgorithm;

@Algorithm(version = "1.0.0")
public class StreaminAlgorithm extends AbstractAnnotatedAlgorithm {
	
	private static Logger LOGGER = Logger.getLogger(StreaminAlgorithm.class);

	InputStream stream;
	
	InputStream output;
	
	@ComplexDataInput(identifier="theStream", binding=StreamDataBinding.class)
	public void setTheStream(InputStream theStream){
		this.stream = theStream;
	}
	
	@ComplexDataOutput(identifier="output", binding=StreamDataBinding.class)
	public InputStream getOutput(){
		return this.output;
	}
	
	@Execute
	public void runStream(){
		
		output = stream;
		
		
//	    Configuration configuration = new GMLConfiguration();
//	    StreamingParser parser;
//		try {
//			parser = new StreamingParser( configuration, stream, Feature.class );
//			
//		    Feature f = null;
//		    while ( ( f = (Feature) parser.parse() ) != null ) {
//		      this.update("Parsed another one: " + f.getIdentifier());
//		    }
//		} catch (Exception e) {
//			LOGGER.error("Exception while parsing features", e);
//		}
		
		
		
	}
	
}
