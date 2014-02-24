package org.n52.wps.server.algorithm;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.geotools.feature.FeatureCollection;
import org.n52.wps.io.data.GazetteerConflationResultEntry;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.complex.GazetteerRelationalOutputDataBinding;
import org.n52.wps.io.datahandler.parser.GML32WFSGBasicParser;
import org.n52.wps.io.datahandler.parser.GML3ApplicationSchemaParser;
import org.n52.wps.server.algorithm.conflation.LinkingProcess;

public class LinkingProcessTest extends TestCase{

	public void testLinking(){
		
		System.setProperty("org.geotools.referencing.forceXY", "true");
		
		LinkingProcess process = new LinkingProcess();
		
//		InputStream tdsin = this.getClass().getResourceAsStream("firestations_conflated.xml");
		InputStream tdsin = this.getClass().getResourceAsStream("tds_firestations.xml");
		
		GML3ApplicationSchemaParser parser = new GML3ApplicationSchemaParser();
		
	    GTVectorDataBinding gtv = parser.parse(tdsin, "text/xml", "http://schemas.opengis.net/gml/3.2.1/gml.xsd");
	    
	    FeatureCollection<?, ?> ftc = gtv.getPayload();
	    
		InputStream tnmin = this.getClass().getResourceAsStream("vgi-wfs-mockup.xml");		

		GML32WFSGBasicParser parser2 = new GML32WFSGBasicParser();
		
	    GTVectorDataBinding gtv2 = parser2.parse(tnmin, "text/xml", "http://schemas.opengis.net/gml/3.2.1/base/gml.xsd");
	    
	    FeatureCollection<?, ?> ftc2 = gtv2.getPayload();
	    
	    System.out.println(ftc2.size());
	    
		List<GazetteerConflationResultEntry> finalResults = process.runMatching(gtv, gtv2, 50d, 15d);
		
		Collections.sort(finalResults);		
		
		for (GazetteerConflationResultEntry gazetteerConflationResultEntry : finalResults) {
			System.out.println(gazetteerConflationResultEntry);
		}
	    
		
	}
	
}
