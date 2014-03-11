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

import junit.framework.TestCase;

import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.n52.wps.io.GTHelper;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.complex.GTVectorDataBindingWithSourceURL;
import org.n52.wps.io.datahandler.generator.GML3ApplicationSchemaGenerator;
import org.n52.wps.io.datahandler.parser.GML311BasicParser;
import org.n52.wps.io.datahandler.parser.GML32WFSApplicationSchemaIncludingDatasetSourceParser;
import org.n52.wps.server.algorithm.conflation.Kinda_Generic_ConflationProcess;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;

import com.vividsolutions.jts.geom.Geometry;

public class Kinda_Generic_ConflationProcessTest extends TestCase {

	private String OWS_10_SC_USGS_FEATURE = "ows10:SC_USGS_Feature_";
	private String OWS_10_SC_NGA_FEATURE = "ows10:SC_NGA_Feature_";
	private String F2N_CONFLATED_MAP_FEATURE = "f2n:ConflatedMap_Feature_";
	private String OWS_10_SC_USGS_FEATURE_TYPE = "ows10:SC_USGS_FeatureType";
	private String OWS_10_SC_NGA_FEATURE_TYPE = "ows10:SC_NGA_FeatureType";
	private String OWS_10_SC_NGA = "ows10:SC_NGA_FeatureType";
	
	public void testCreateNewFeature(){
		
		Kinda_Generic_ConflationProcess process = new Kinda_Generic_ConflationProcess();
		
		InputStream tdsin = this.getClass().getResourceAsStream("tds_firestationsWFS200GML32.xml");
		
//		GML3ApplicationSchemaParser parser = new GML3ApplicationSchemaParser();
		GML32WFSApplicationSchemaIncludingDatasetSourceParser parser = new GML32WFSApplicationSchemaIncludingDatasetSourceParser();
		
	    GTVectorDataBindingWithSourceURL gtv = parser.parse(tdsin, "text/xml", "http://schemas.opengis.net/gml/3.2.1/gml.xsd");
	    
	    FeatureCollection<?, ?> ftc = gtv.getPayload();
	    
	    System.out.println(ftc.size());
	    
		InputStream tnmin = this.getClass().getResourceAsStream("tnm_firestations.xml");		
//		InputStream tnmin = this.getClass().getResourceAsStream("dncshp.zip");		
		
		GML311BasicParser parser2 = new GML311BasicParser();
//		GTBinZippedSHPParser parser2 = new GTBinZippedSHPParser();
		
	    GTVectorDataBinding gtv2 = parser2.parse(tnmin, "text/xml", "http://schemas.opengis.net/gml/3.1.1/base/gml.xsd");
//	    GTVectorDataBinding gtv2 = parser2.parse(tnmin, "application/x-zipped-shp", null);
	    
	    FeatureCollection<?, ?> ftc2 = gtv2.getPayload();
	    
		String rules = "{ \"mappings\": { \"address\":\"address\", \"name\":\"geoNameCollection.memberGeoName.fullName\" }, \"fixedAttributeValues\": { \"featureFunction-1\":\"firefighting\" } }";
		
		process.createRules(rules);
		
		FeatureIterator<?> iter1 = ftc2.features();
		
		FeatureType ft = ftc.features().next().getType();
		
		SimpleFeature f = null;

		List<SimpleFeature> oldFeatures = Arrays.asList(ftc.toArray(new SimpleFeature[]{}));
		
		List<SimpleFeature> newFeatures = new ArrayList<SimpleFeature>();
		
		newFeatures.addAll(oldFeatures);
		
		List<SimpleFeature> onlyNewFeatures = new ArrayList<SimpleFeature>();
		
		while (iter1.hasNext()) {
			Object o = iter1.next();
			if (o instanceof SimpleFeature) {
				f = (SimpleFeature) o;
				
//				SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder((SimpleFeatureType)ft);
//				
//				SimpleFeature sft = featureBuilder.buildFeature(f.getIdentifier().getID());
				
				SimpleFeature sft = (SimpleFeature) GTHelper.createFeature2(f.getIdentifier().getID(), (Geometry)f.getDefaultGeometry(), (SimpleFeatureType)ft);
				
				process.mapProperties(f, sft);
				
				process.addfixedAttributeValues(sft);
				
				process.addDefaultValues(sft);

				newFeatures.add(sft);
				onlyNewFeatures.add(sft);
			}

		}
		
		StringBuilder featureTypeStatementBuilder = new StringBuilder(); 
		StringBuilder memberStatementBuilder = new StringBuilder(); 
		StringBuilder generatedByStatementBuilder = new StringBuilder(); 
		StringBuilder originStatementBuilder = new StringBuilder(); 
		
		for (SimpleFeature simpleFeature : newFeatures) {
			
			String featureProvenance1 = F2N_CONFLATED_MAP_FEATURE + simpleFeature.getID() + " a " + OWS_10_SC_NGA_FEATURE_TYPE;
			
			featureTypeStatementBuilder.append(featureProvenance1);
			
			String featureProvenance2 = F2N_CONFLATED_MAP_FEATURE + simpleFeature.getID() + " a " + OWS_10_SC_NGA_FEATURE_TYPE;

			
		}
		
		FeatureCollection<?, ?> result = new ListFeatureCollection((SimpleFeatureType)ft, newFeatures);	 
		
		assertTrue(result.size() == (ftc2.size() + ftc.size()));
		
		GML3ApplicationSchemaGenerator generator = new GML3ApplicationSchemaGenerator();
		
		try {
			InputStream in = generator.generateStream(new GTVectorDataBinding(result), "", "");
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File("d:/tmp/generatedConflationResult32WFS.xml")));
			
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
