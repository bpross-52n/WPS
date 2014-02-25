package org.n52.wps.server.algorithm;

import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.geotools.feature.FeatureCollection;
import org.n52.wps.io.data.GazetteerConflationResultEntry;
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
		
//		LinkingProcess process = new LinkingProcess();
//		
////		InputStream tdsin = this.getClass().getResourceAsStream("firestations_conflated.xml");
//		InputStream tdsin = this.getClass().getResourceAsStream("tds_firestations.xml");
//		
//		GML3ApplicationSchemaParser parser = new GML3ApplicationSchemaParser();
//		
//	    GTVectorDataBinding gtv = parser.parse(tdsin, "text/xml", "http://schemas.opengis.net/gml/3.2.1/gml.xsd");
//	    
//	    FeatureCollection<?, ?> ftc = gtv.getPayload();
	    
		InputStream tnmin = this.getClass().getResourceAsStream("vgi-wfs-mockup.xml");		

		GML3OMApplicationSchemaParser parser2 = new GML3OMApplicationSchemaParser();
		
	    GTVectorDataBinding gtv2 = parser2.parse(tnmin, "text/xml", "http://schemas.opengis.net/gml/3.2.1/base/gml.xsd");
	    
	    FeatureCollection<?, ?> ftc2 = gtv2.getPayload();
	    
	    System.out.println(ftc2.size());
	    
	    Collection<Property> p = ((SimpleFeature)ftc2.features().next()).getProperties();
	    
	    for (Property property : p) {
			System.out.println(property.getName().getLocalPart());
		}
	    
//		List<GazetteerConflationResultEntry> finalResults = process.runMatching(gtv, gtv2, 50d, 15d);
//		
//		Collections.sort(finalResults);		
//		
//		for (GazetteerConflationResultEntry gazetteerConflationResultEntry : finalResults) {
//			System.out.println(gazetteerConflationResultEntry);
//		}
	    
		
	}
	
}
