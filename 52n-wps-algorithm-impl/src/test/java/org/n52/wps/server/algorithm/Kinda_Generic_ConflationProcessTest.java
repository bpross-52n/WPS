package org.n52.wps.server.algorithm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.n52.wps.io.GTHelper;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.datahandler.GML3ApplicationSchemaConfiguration;
import org.n52.wps.io.datahandler.generator.GML311BasicGenerator;
import org.n52.wps.io.datahandler.generator.GML3ApplicationSchemaGenerator;
import org.n52.wps.io.datahandler.parser.GML311BasicParser;
import org.n52.wps.io.datahandler.parser.GML3ApplicationSchemaParser;
import org.n52.wps.io.datahandler.parser.GTBinZippedSHPParser;
import org.n52.wps.server.algorithm.conflation.Kinda_Generic_ConflationProcess;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;

import com.vividsolutions.jts.geom.Geometry;

public class Kinda_Generic_ConflationProcessTest extends TestCase {

	public void testCreateNewFeature(){
		
		Kinda_Generic_ConflationProcess process = new Kinda_Generic_ConflationProcess();
		
		InputStream tdsin = this.getClass().getResourceAsStream("tds_firestations.xml");
		
		GML3ApplicationSchemaParser parser = new GML3ApplicationSchemaParser();
		
	    GTVectorDataBinding gtv = parser.parse(tdsin, "text/xml", "http://schemas.opengis.net/gml/3.2.1/gml.xsd");
	    
	    FeatureCollection<?, ?> ftc = gtv.getPayload();
	    
//		InputStream tnmin = this.getClass().getResourceAsStream("tnm_firestations.xml");		
		InputStream tnmin = this.getClass().getResourceAsStream("dncshp.zip");		
		
//		GML311BasicParser parser2 = new GML311BasicParser();
		GTBinZippedSHPParser parser2 = new GTBinZippedSHPParser();
		
//	    GTVectorDataBinding gtv2 = parser2.parse(tnmin, "text/xml", "http://schemas.opengis.net/gml/3.1.1/base/gml.xsd");
	    GTVectorDataBinding gtv2 = parser2.parse(tnmin, "application/x-zipped-shp", null);
	    
	    FeatureCollection<?, ?> ftc2 = gtv2.getPayload();
	    
		String rules = "mappings:[bfc_descri->geoNameCollection.memberGeoName.fullName;]";
		
		process.createRules(rules);
		
		FeatureIterator<?> iter1 = ftc2.features();
		
		FeatureType ft = ftc.features().next().getType();
		
		SimpleFeature f = null;

		List<SimpleFeature> oldFeatures = Arrays.asList(ftc.toArray(new SimpleFeature[]{}));
		
		List<SimpleFeature> newFeatures = new ArrayList<SimpleFeature>();
		
		newFeatures.addAll(oldFeatures);
		
		while (iter1.hasNext()) {
			Object o = iter1.next();
			if (o instanceof SimpleFeature) {
				f = (SimpleFeature) o;
				
//				SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder((SimpleFeatureType)ft);
//				
//				SimpleFeature sft = featureBuilder.buildFeature(f.getIdentifier().getID());
				
				SimpleFeature sft = (SimpleFeature) GTHelper.createFeature(f.getIdentifier().getID(), (Geometry)f.getDefaultGeometry(), (SimpleFeatureType)ft);
				
				process.mapProperties(f, sft);
				
				process.addfixedAttributeValues(sft);
				
				process.addDefaultValues(sft);

				newFeatures.add(sft);
			}

		}
		
		FeatureCollection<?, ?> result = new ListFeatureCollection((SimpleFeatureType)ft, newFeatures);	 
		
		assertTrue(result.size() == (ftc2.size() + ftc.size()));
		
		GML3ApplicationSchemaGenerator generator = new GML3ApplicationSchemaGenerator();
		
		try {
			InputStream in = generator.generateStream(new GTVectorDataBinding(result), "", "");
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File("d:/tmp/generatedConflationResult3.xml")));
			
			String line = "";
			
			while((line = reader.readLine()) != null){
				writer.write(line + "\n");
			}
			writer.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
