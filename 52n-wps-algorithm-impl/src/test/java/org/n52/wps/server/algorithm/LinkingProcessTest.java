package org.n52.wps.server.algorithm;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import net.opengis.om.x20.OMObservationType;

import org.geotools.feature.FeatureCollection;
import org.n52.wps.io.data.GazetteerConflationResultEntry;
import org.n52.wps.io.data.binding.complex.GML32OMWFSDataBinding;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.datahandler.parser.GML32OMWFSGBasicParser;
import org.n52.wps.io.datahandler.parser.GML3ApplicationSchemaParser;
import org.n52.wps.io.datahandler.parser.GML3OMApplicationSchemaParser;
import org.n52.wps.server.algorithm.conflation.LinkingProcess;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;

public class LinkingProcessTest extends TestCase{

	public void testLinking(){
		
		System.setProperty("org.geotools.referencing.forceXY", "true");
		
		LinkingProcess process = new LinkingProcess("c:\\python27", "d:\\chaos-folder\\fuzz.py");
		
//		InputStream tdsin = this.getClass().getResourceAsStream("firestations_conflated.xml");
		InputStream tdsin = this.getClass().getResourceAsStream("firestations_conflated2.xml");
		
		GML3ApplicationSchemaParser parser = new GML3ApplicationSchemaParser();
		
	    GTVectorDataBinding gtv = parser.parse(tdsin, "text/xml", "http://schemas.opengis.net/gml/3.2.1/gml.xsd");
	    
		InputStream tnmin = this.getClass().getResourceAsStream("vgi-wfs-mockup.xml");		

		GML32OMWFSGBasicParser basicParser = new GML32OMWFSGBasicParser();

		GML32OMWFSDataBinding binding = basicParser.parse(tnmin, "", "");
	    
		List<OMObservationType> list = binding.getPayload();
		
	    System.out.println(list.size());
	    
		List<GazetteerConflationResultEntry> finalResults = process.runMatching(gtv, binding, 50d, 15d);
		
		Collections.sort(finalResults);		
		
		for (GazetteerConflationResultEntry gazetteerConflationResultEntry : finalResults) {
			System.out.println(gazetteerConflationResultEntry);
		}
	    
		
	}
	
}
