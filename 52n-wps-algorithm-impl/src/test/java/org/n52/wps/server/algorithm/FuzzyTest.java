package org.n52.wps.server.algorithm;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.datahandler.parser.GML32WFSGBasicParser;
import org.n52.wps.io.datahandler.parser.GML3WFSGBasicParser;
import org.n52.wps.server.algorithm.conflation.GazetteerConflationResultEntry;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class FuzzyTest {

	private static Logger LOGGER = LoggerFactory.getLogger(FuzzyTest.class);

	private String pythonName = "python.exe";

	private String command;

	private String pythonHome;

	private char fileSeparator = File.separatorChar;

	private String fuzzyHome;
	
	private final String lineSeparator = System.getProperty("line.separator");
	public static final String OS_Name = System.getProperty("os.name");

	private MathTransform tx;
	
	private double kmInMilesFactor = 1.609347219;
	
	private String alternativeGeographicIdentifierName = "alternativeGeographicIdentifier";
	private String geographicIdentifierName = "geographicIdentifier";
	
	public FuzzyTest() {

		pythonHome = "c:\\Python27";
		fuzzyHome = "d:\\tmp";
		
		if (!OS_Name.startsWith("Windows")) {
			pythonName = "python";
		}

		try {
			
			CoordinateReferenceSystem wgs84 = CRS.decode("EPSG:4326");
			CoordinateReferenceSystem nad83 = CRS.decode("EPSG:2953");
			
			System.out.println(wgs84.toString());
			System.out.println(nad83.toString());
			
			
			tx = CRS.findMathTransform(wgs84, nad83, false);
			LOGGER.info(tx.toString());
		} catch (Exception e) {
			LOGGER.error("Exception while trying to find transformation between WGS84 and NAD83.", e);
		} 
		
		boolean treu = true;
		
		if(treu){
			return;
		}
		
		String finalTargetGazetteerRequest = "http://ows-svc1.compusult.net/nbgaz/services/?service=WFS&version=2.0.0&request=GetFeature&typeName=SI_LocationInstance&count=10";
		
		GML32WFSGBasicParser gml32Parser = new GML32WFSGBasicParser();
		
		URL finalTargetGazetteerRequestURL;
		
		GTVectorDataBinding targetGazetteerFeatures = null;
		
		try {
			finalTargetGazetteerRequestURL = new URL(finalTargetGazetteerRequest);
			
			targetGazetteerFeatures = gml32Parser.parse(finalTargetGazetteerRequestURL.openStream(), "text/xml", "http://schemas.opengis.net/gml/3.1.1/base/gml.xsd");
						
		} catch (MalformedURLException e) {
			throw new RuntimeException("Malformed target gazeeteer request url: " + finalTargetGazetteerRequest, e);
		} catch (IOException e) {
			throw new RuntimeException("Could not connect to target gazetteer: " + finalTargetGazetteerRequest, e);
		}
		
		if(targetGazetteerFeatures == null){
			throw new RuntimeException("No target gazetteer features found.");
		}
		
		String finalSourceGazetteerRequest = "http://services.interactive-instruments.de/xsprojects/ows10/service/gazetteer-simple/wfs?service=WFS&version=1.1.0&request=GetFeature&namespace=xmlns%28iso19112=http://www.isotc211.org/19112%29&typename=iso19112:SI_LocationInstance&BBOX=45.27273,-67.32279,45.77162,-66.35665,EPSG:4326";
				
		/*
		 * load response stream into feature collection
		 */
		
		URL finalSourceGazetteerRequestURL;
		
		GTVectorDataBinding sourceGazetteerFeatures = null;
		
		GML3WFSGBasicParser gml3Parser = new GML3WFSGBasicParser();
		
		try {
			finalSourceGazetteerRequestURL = new URL(finalSourceGazetteerRequest);
			
			sourceGazetteerFeatures = gml3Parser.parse(finalSourceGazetteerRequestURL.openStream(), "text/xml", "http://schemas.opengis.net/gml/3.1.1/base/gml.xsd");
						
		} catch (MalformedURLException e) {
			throw new RuntimeException("Malformed source gazeeteer request url: " + finalSourceGazetteerRequest, e);
		} catch (IOException e) {
			throw new RuntimeException("Could not connect to source gazetteer: " + finalSourceGazetteerRequest, e);
		}
		
		if(sourceGazetteerFeatures == null){
			throw new RuntimeException("No source gazetteer features found.");
		}
		
		/*
		 * loop over features
		 */
		FeatureIterator<?> featureIterator1 = sourceGazetteerFeatures.getPayload().features();
//		
//		FeatureIterator<?> featureIterator2 = targetGazetteerFeatures.getPayload().features();
//
//		SimpleFeature sourceFeature = (SimpleFeature)featureIterator1.next();
//		
//		Point sourcePointInNad83 = transformSourceFeature(sourceFeature);
//		
//		/*
//		 * iterate over target features
//		 */		
//		while(featureIterator2.hasNext()){
//			
//			SimpleFeature targetFeature = (SimpleFeature)featureIterator2.next();
//			
//			double isInRange = isInRange2(sourcePointInNad83, targetFeature, 100);
//			
//			System.out.println(isInRange);
//			
//			if(isInRange != -1){
//				
//				/*
//				 * check names with FuzzyWuzzy
//				 * first get source name
//				 * we have to do this for each alternativeGeogrId
//				 * save the combination with the highest fw score, if tied, use distance 
//				 */
//				
//				List<String> sourceNameList = getAllAlternativeGeographicIdentifier(sourceFeature);
//				String targetName = getAlternativeGeographicIdentifier(targetFeature);
//				
//				for (String sourceName : sourceNameList) {
//					int fwScore = getFuzzyWuzzyScore(sourceName, targetName);
//					System.out.println(fwScore + " " + isInRange + " " + getGeographicIdentifier(sourceFeature) + " " + getGeographicIdentifier(targetFeature) + " " + sourceName + " " + targetName);
//				}
//				
//				/*
//				 * if above fuzzywuzzy threshold:
//				 * save fw score, distance, geographicId_NGA, geographicId_NB, alternativeGeographicIdentifier_NGA, alternativeGeographicIdentifier_NB
//				 */
//			}	
//		}
		
		double distanceThreshold = 100;
		double fwThreshold = 0;
		
		List<GazetteerConflationResultEntry> finalResults = new ArrayList<GazetteerConflationResultEntry>();
		
//		while(featureIterator1.hasNext()){
//			
			SimpleFeature sourceFeature = (SimpleFeature) featureIterator1.next();
			
			String sourceFeatureGeogrName = getGeographicIdentifier(sourceFeature);
			
			List<String> sourceNameList = getAllAlternativeGeographicIdentifier(sourceFeature);

			Map<SimpleFeature, Double> targetFeaturesInRange = getFeatureInRange(sourceFeature, targetGazetteerFeatures.getPayload(), distanceThreshold);
			
			List<GazetteerConflationResultEntry> tmpResults = new ArrayList<GazetteerConflationResultEntry>();
			
//			for (String sourceName : sourceNameList) {				
//		
//				/*
//				 * iterate over target features
//				 */		
//				
//				Iterator<SimpleFeature> targetFeatureIterator = targetFeaturesInRange.keySet().iterator();
//				
//				while(targetFeatureIterator.hasNext()){
//					
//					SimpleFeature targetFeature = targetFeatureIterator.next();
//						
//						/*
//						 * check names with FuzzyWuzzy
//						 * first get source name
//						 * we have to do this for each alternativeGeogrId
//						 * save the combination with the highest fw score, if tied, use distance 
//						 */
//						
//						String targetName = getAlternativeGeographicIdentifier(targetFeature);
//
//						int fwScore = getFuzzyWuzzyScore(sourceName, targetName);
//						
//						System.out.println(fwScore + " " + targetFeaturesInRange.get(targetFeature) + " " + sourceFeatureGeogrName + " " + getGeographicIdentifier(targetFeature) + " " + sourceName + " " + targetName);
//						
//						/*
//						 * if above fuzzywuzzy threshold:
//						 * save fw score, distance, geographicId_NGA, geographicId_NB, alternativeGeographicIdentifier_NGA, alternativeGeographicIdentifier_NB
//						 */
//						if(fwScore >= fwThreshold){
//							tmpResults.add(new GazetteerConflationResultEntry(fwScore, targetFeaturesInRange.get(targetFeature), sourceFeatureGeogrName, getGeographicIdentifier(targetFeature), sourceName, targetName));
//						}
//					
//				}
//				
//			}
//			
//			Collections.sort(tmpResults);
//			
//			System.out.println(tmpResults.get(0));
//			
//			finalResults.add(tmpResults.get(0));
//			
//			
//		}
//		Collections.sort(finalResults);
//		
//		for (GazetteerConflationResultEntry gazetteerConflationResultEntry : finalResults) {
//			System.out.println(gazetteerConflationResultEntry);
//		}
		
//		getFuzzyWuzzyScore(name1, name2);
	}
	
	private Map<SimpleFeature, Double> getFeatureInRange(SimpleFeature sourceFeature, FeatureCollection<?, ?> candidateFeatures, double distanceThreshold){
		
		Map<SimpleFeature, Double> featuresInRange = new HashMap<SimpleFeature, Double>();
		
		FeatureIterator<?> targetFeatureIterator = candidateFeatures.features();
		
		LOGGER.info(getAlternativeGeographicIdentifier(sourceFeature));
		
		LOGGER.info("" + sourceFeature.getDefaultGeometry());
		
		Point sourceFeaturePointInNad83 = transformSourceFeature(sourceFeature);
		
		LOGGER.info("" + sourceFeaturePointInNad83);
		
		while (targetFeatureIterator.hasNext()) {
			SimpleFeature candidateFeature = (SimpleFeature) targetFeatureIterator.next();
			
			double range = isInRange2(sourceFeaturePointInNad83, candidateFeature, distanceThreshold);
			
			if(range != -1){
				featuresInRange.put(candidateFeature, range);
			}
		}
		
		return featuresInRange;
	}
	
	private String getGeographicIdentifier(SimpleFeature feature){
		
		String geographicIdentifier = "";
		
		Collection<Property> properties = feature.getProperties();
		
		for (Property property : properties) {
			String propName = property.getName().toString();
			if(propName.contains(geographicIdentifierName) && !propName.contains(alternativeGeographicIdentifierName)){
				geographicIdentifier = property.getValue().toString();
				break;
			}
		}
		
		return geographicIdentifier;
		
	}
	
	private List<String> getAllAlternativeGeographicIdentifier(SimpleFeature feature){
		
		List<String> alternativeGeographicIdentifierList = new ArrayList<String>();
		
		Collection<Property> properties = feature.getProperties();
		
		for (Property property : properties) {
			if(property.getName().toString().contains(alternativeGeographicIdentifierName)){
				alternativeGeographicIdentifierList.add(property.getValue().toString());
				break;
			}
		}
		
		return alternativeGeographicIdentifierList;
		
	}
	
	private String getAlternativeGeographicIdentifier(SimpleFeature feature){
		
		String alternativeGeographicIdentifier = "";
		
		Collection<Property> properties = feature.getProperties();
		
		for (Property property : properties) {
			if(property.getName().toString().contains(alternativeGeographicIdentifierName)){
				alternativeGeographicIdentifier = property.getValue().toString();
				break;
			}
		}
		
		return alternativeGeographicIdentifier;
		
	}
	
	private Point transformSourceFeature(SimpleFeature sourceFeature) {
		Point p1Nad83 = null;

		if (sourceFeature.getDefaultGeometry() instanceof Point) {

			Point p1 = (Point) sourceFeature.getDefaultGeometry();

			p1Nad83 = transformWGS84ToNAD83(p1);
		}

		return p1Nad83;
	}

	private Point transformWGS84ToNAD83(Point wgs84Point) {
		Point p1Nad83 = null;
		try {
			p1Nad83 = (Point) JTS.transform(wgs84Point, tx);
		} catch (Exception e) {
			LOGGER.error("Exception while trying to transform WGS84 to NAD83.",
					e);
			return null;
		}

		return p1Nad83;
	}

	private double isInRange2(Point sourceFeaturePoint, SimpleFeature f2,
			double distanceThreshold) {

		if (f2.getDefaultGeometry() instanceof Point) {

			/*
			 * TODO: coordinates from second WFS-G seem to be in a different order
			 * right now, we just revert the order of the coordinates
			 * additionally we could check, whether they are not inside the bounding box of the target
			 *  
			 */
			Point p2 = (Point) f2.getDefaultGeometry();
			
			Coordinate reversedP2Coordinate = new Coordinate(p2.getY(), p2.getX());
			
			Point p2Reversed = new GeometryFactory().createPoint(reversedP2Coordinate);
			
			/*
			 * transform to NAD83 projected coordinate system
			 */			
			Point p2Nad83 = transformWGS84ToNAD83(p2Reversed);
			
			//get the distance in meter
			double tmpDistance = sourceFeaturePoint
					.distance(p2Nad83);
			System.out.println(getAlternativeGeographicIdentifier(f2));
			System.out.println(tmpDistance/1000 + " " + (distanceThreshold * kmInMilesFactor));
			
			//check against threshold, convert both values to kilometer
			if ((tmpDistance/1000) < (distanceThreshold * kmInMilesFactor)) {
				//return distance in miles
				return (tmpDistance/1000) * (1/kmInMilesFactor);
			}
		}
		return -1;
	}
	
	private String getCommand(String name1, String name2) {

		command = getPythonHome() + fileSeparator + pythonName + " "
				+ fuzzyHome + fileSeparator + "fuzz.py " + name1 + " " + name2;

		return command;
	}

	private String getPythonHome() {
		return pythonHome;
	}

	private int getFuzzyWuzzyScore(String name1, String name2){			

		try {

//			LOGGER.info("Executing FuzzyWuzzy with " + name1 + " and " + name2);

			Runtime rt = Runtime.getRuntime();

			Process proc = rt.exec(getCommand(name1, name2));

			PipedOutputStream pipedOut = new PipedOutputStream();

			PipedInputStream pipedIn = new PipedInputStream(pipedOut);
			
			PipedOutputStream pipedOut1 = new PipedOutputStream();
			
			PipedInputStream pipedIn1 = new PipedInputStream(pipedOut1);

			// any error message?
			StreamGobbler errorGobbler = new StreamGobbler(
					proc.getErrorStream(), "ERROR", pipedOut);

			// any output?
			StreamGobbler outputGobbler = new StreamGobbler(
					proc.getInputStream(), "OUTPUT", pipedOut1);

			// kick them off
			errorGobbler.start();
			outputGobbler.start();

			// fetch errors if there are any
			BufferedReader errorReader = new BufferedReader(
					new InputStreamReader(pipedIn));
			
			// fetch errors if there are any
			BufferedReader outputReader = new BufferedReader(
					new InputStreamReader(pipedIn1));

			String line = errorReader.readLine();

			String errors = "";

			while (line != null) {

				errors = errors.concat(line + lineSeparator);

				line = errorReader.readLine();
			}
			
			line = outputReader.readLine();

			String output = "";

			while (line != null) {

				output = output.concat(line + lineSeparator);

				line = outputReader.readLine();
			}

			System.out.println(output);
			
			try {
				proc.waitFor();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			} finally {
				proc.destroy();
			}

			return Integer.parseInt(output.trim());
			
		} catch (IOException e) {
			LOGGER.error(
					"An error occured while executing the FuzzyWuzzy process.",
					e);
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		new FuzzyTest();

	}

}
