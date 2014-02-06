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
import org.n52.wps.server.algorithm.conflation.Kinda_Generic_ConflationProcess;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;

import com.vividsolutions.jts.geom.Geometry;

public class Kinda_Generic_ConflationProcessTest extends TestCase {

	public void testCreateNewFeature(){
		
		Kinda_Generic_ConflationProcess process = new Kinda_Generic_ConflationProcess();
		
		InputStream tdsin = this.getClass().getResourceAsStream("tds311.xml");
		
		GML311BasicParser parser = new GML311BasicParser();
		
	    GTVectorDataBinding gtv = parser.parse(tdsin, "text/xml", "http://schemas.opengis.net/gml/3.1.1/base/gml.xsd");
	    
	    FeatureCollection<?, ?> ftc = gtv.getPayload();
	    
		InputStream tnmin = this.getClass().getResourceAsStream("tnm_firestations.xml");		
		
	    GTVectorDataBinding gtv2 = parser.parse(tnmin, "text/xml", "http://schemas.opengis.net/gml/3.1.1/base/gml.xsd");
	    
	    FeatureCollection<?, ?> ftc2 = gtv2.getPayload();	    
	    
	    System.out.println(ftc.size());
	    System.out.println(ftc2.size());
	    
		String rules = "mappings:[address->address;"
			    +"name->geoNameCollection.memberGeoName.fullName;],"
			    +"rules:[target within 0.1 mi->update source;]";
		
		Map<String, String> map = process.createMapping(rules);
		
		FeatureIterator<?> iter1 = ftc2.features();
		
		FeatureType ft = ftc.features().next().getType();
		
		SimpleFeature f = null;

		List<SimpleFeature> oldFeatures = Arrays.asList(ftc.toArray(new SimpleFeature[]{}));
		
		List<SimpleFeature> newFeatures = new ArrayList<SimpleFeature>();
		
		newFeatures.addAll(oldFeatures);
		
		System.out.println(newFeatures.size());
		
		while (iter1.hasNext()) {
			Object o = iter1.next();
			if (o instanceof SimpleFeature) {
				f = (SimpleFeature) o;
				
				Geometry g = null;
				
				if(f.getDefaultGeometry() == null){
					process.tryCreatingGeom(f);
				}else{
					g = (Geometry)f.getDefaultGeometry();
				}
				
//				SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder((SimpleFeatureType)ft);
//				
//				SimpleFeature sft = featureBuilder.buildFeature(f.getIdentifier().getID());
				
				SimpleFeature sft = (SimpleFeature) GTHelper.createFeature(f.getIdentifier().getID(), (Geometry)f.getDefaultGeometry(), (SimpleFeatureType)ft);
				
				process.mapProperties(f, sft, map);
				
				newFeatures.add(sft);
			}

		}
		
		FeatureCollection<?, ?> result = new ListFeatureCollection((SimpleFeatureType)ft, newFeatures);
		
		System.out.println(result.size());
		
		GML3ApplicationSchemaGenerator generator = new GML3ApplicationSchemaGenerator();
		
		try {
			InputStream in = generator.generateStream(new GTVectorDataBinding(result), "", "");
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File("d:/tmp/generatedConflationResult.xml")));
			
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
