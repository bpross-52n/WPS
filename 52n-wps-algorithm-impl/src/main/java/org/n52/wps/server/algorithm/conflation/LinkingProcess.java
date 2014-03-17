package org.n52.wps.server.algorithm.conflation;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.opengis.gml.x32.AbstractFeatureType;
import net.opengis.gml.x32.AbstractGeometryType;
import net.opengis.gml.x32.FeaturePropertyType;
import net.opengis.gml.x32.PointType;
import net.opengis.gml.x32.StringOrRefType;
import net.opengis.om.x20.OMObservationType;
import net.opengis.samplingSpatial.x20.SFSpatialSamplingFeatureType;
import net.opengis.samplingSpatial.x20.ShapeType;
import net.opengis.samplingSpatial.x20.impl.SFSpatialSamplingFeatureTypeImpl;

import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.io.data.GazetteerConflationResultEntry;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.bbox.GTReferenceEnvelope;
import org.n52.wps.io.data.binding.complex.GML32OMWFSDataBinding;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.complex.GazetteerRelationalOutputDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralDoubleBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.server.AbstractAlgorithm;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.LocalAlgorithmRepository;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class LinkingProcess extends AbstractAlgorithm {

	private static Logger LOGGER = LoggerFactory.getLogger(LinkingProcess.class);
	
	private final String sourceWFS = "Source_Features";
	private final String targetWFS = "Target_Features";
	private final String targetGazetteerDescriptionFilter = "Target_WFS_Description_Filter";
	private final String boundingBoxFilter = "Bounding_Box_Filter";
	private final String searchDistance = "Search_Distance";
	private final String fuzzyWuzzyThreshold = "FuzzyWuzzy_Threshold";
	private final String targetWFSMaxFeatures = "Target_WFS_Max_Features";
	private final String outputFile = "Matched_Features";
	
	private MathTransform tx;
	
	private double kmInMilesFactor = 1.609347219;
	
	private String description = "description";
	private String fullName = "geoNameCollection.memberGeoName.fullName";
	
	private String pythonName = "python.exe";

	private String pythonHome;

	private char fileSeparator = File.separatorChar;
	
	private final String OS_Name = System.getProperty("os.name");
	
	private final String lineSeparator = System.getProperty("line.separator");
	
	private String fuzzyName;
	
	private ExecutorService executor = Executors.newFixedThreadPool(10);
	
	public LinkingProcess(){
		if (WPSConfig.getInstance().isRepositoryActive(
				LocalAlgorithmRepository.class.getCanonicalName())) {
			
			org.n52.wps.PropertyDocument.Property[] propertyArray = WPSConfig.getInstance()
					.getPropertiesForRepositoryClass(
							LocalAlgorithmRepository.class.getCanonicalName());
			
			/*
			 * get properties of Repository
			 */			
			for (org.n52.wps.PropertyDocument.Property property : propertyArray) {
				if (property.getName().equalsIgnoreCase(
						"pythonHome")) {
					pythonHome = property.getStringValue();
				}else if (property.getName().equalsIgnoreCase(
						"fuzzyName")) {
					fuzzyName = property.getStringValue();
				}
			}
		}
		if (!OS_Name.startsWith("Windows")) {
			pythonName = "python";
		}
		
		try {
			
			CoordinateReferenceSystem wgs84 = CRS.decode("EPSG:4326");
			CoordinateReferenceSystem nad83 = CRS.decode("EPSG:26944");
			
			tx = CRS.findMathTransform(wgs84, nad83, false);
		} catch (Exception e) {
			LOGGER.error("Exception while trying to find transformation between WGS84 and NAD83 / California zone 4.", e);
		} 
	}
	
	public LinkingProcess(String pythonHome, String fuzzyName){
		
		this.pythonHome = pythonHome;
		this.fuzzyName = fuzzyName;
		
		if (!OS_Name.startsWith("Windows")) {
			pythonName = "python";
		}

		try {
			
			CoordinateReferenceSystem wgs84 = CRS.decode("EPSG:4326");
			CoordinateReferenceSystem nad83 = CRS.decode("EPSG:26944");
			
			tx = CRS.findMathTransform(wgs84, nad83, false);
		} catch (Exception e) {
			LOGGER.error("Exception while trying to find transformation between WGS84 and NAD83 / California zone 4.", e);
		} 
	}
	
	@Override
	public Map<String, IData> run(Map<String, List<IData>> inputData)
			throws ExceptionReport {
		
		/*
		 * get inputs
		 * 
		 * request source gazetteer with bbox and filter
		 * whats the result?
		 * 
		 * request target gazetteer with filter
		 * 
		 * source gazetteer features are processed (sequentially, one at a time)
		 * 	get coordinates
		 * 	filter gathered target features by search distance 
		 * 	- couldn't we just request the target gazetteer over again with spatial and description filter?!
		 *  match all found features against source feature - filter results by fuzzywuzzy threshold 
		 *  also calculate distance
		 *  
		 * 
		 */
		
		/*
		 * get distance threshold
		 */
		
		List<IData> searchDistanceInputs = inputData.get(searchDistance);
		
		double searchDistance = -1;
		
		try {
			searchDistance = ((LiteralDoubleBinding)searchDistanceInputs.get(0)).getPayload();			
		} catch (ClassCastException e) {
			throw new RuntimeException(this.searchDistance + " input value is not an double.");
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new RuntimeException("No value for input " + this.searchDistance + " provided.");
		}
		
		/*
		 * get FuzzyWuzzy threshold
		 */
		
		List<IData> fuzzyWuzzyThresholdInputs = inputData.get(fuzzyWuzzyThreshold);
		
		double fuzzyWuzzyThreshold = -1;
		
		try {
			fuzzyWuzzyThreshold = ((LiteralDoubleBinding)fuzzyWuzzyThresholdInputs.get(0)).getPayload();			
		} catch (ClassCastException e) {
			throw new RuntimeException(this.fuzzyWuzzyThreshold + " input value is not an double.");
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new RuntimeException("No value for input " + this.fuzzyWuzzyThreshold + " provided.");
		}
		
		GML32OMWFSDataBinding targetGazetteerFeatures = null;
		
		/*
		 * get target features
		 */
		List<IData> targetWFSInputs = inputData.get(targetWFS);
		
		try {
			targetGazetteerFeatures = (GML32OMWFSDataBinding)targetWFSInputs.get(0);		
		} catch (ClassCastException e) {
			throw new RuntimeException(targetWFS + " input value is not an GML32OMWFSDataBinding.");
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new RuntimeException("No value for input " + targetWFS + " provided.");
		}
		
		if(targetGazetteerFeatures == null){
			throw new RuntimeException("No source gazetteer features found.");
		}
		
		LOGGER.info("Found " + targetGazetteerFeatures.getPayload().size() + " target features.");
		
		GTVectorDataBinding sourceGazetteerFeatures = null;
		
		List<IData> sourceFeaturesIDataList = inputData.get(sourceWFS);
		
		IData sourceFeaturesIData = sourceFeaturesIDataList.get(0);
		
		if(sourceFeaturesIData instanceof GTVectorDataBinding){
			sourceGazetteerFeatures = (GTVectorDataBinding)sourceFeaturesIData;
		}
		
		if(sourceGazetteerFeatures == null){
			throw new RuntimeException("No source gazetteer features found.");
		}
		
		LOGGER.info("Found " + sourceGazetteerFeatures.getPayload().size() + " source features.");
		
		List<GazetteerConflationResultEntry> finalResults = runMatching(sourceGazetteerFeatures, targetGazetteerFeatures, fuzzyWuzzyThreshold, searchDistance);
				
		Collections.sort(finalResults);		
		
		LOGGER.debug("Final result count: " + finalResults.size());
		
		Map<String, IData> result = new HashMap<String, IData>();
		
		GazetteerRelationalOutputDataBinding resultBinding = new GazetteerRelationalOutputDataBinding(finalResults);
		
		resultBinding.setGazetteerMatching(false);
		
		result.put(outputFile, resultBinding);
		
		return result;
	}	
	
	public List<GazetteerConflationResultEntry> runMatching(
			GTVectorDataBinding sourceGazetteerFeatures,
			GML32OMWFSDataBinding targetGazetteerFeatures,
			double fuzzyWuzzyThreshold, double searchDistance) {

		/*
		 * loop over features
		 */
		FeatureIterator<?> sourceFeatureIterator = sourceGazetteerFeatures
				.getPayload().features();

		List<GazetteerConflationResultEntry> finalResults = new ArrayList<GazetteerConflationResultEntry>();

		while (sourceFeatureIterator.hasNext()) {

			SimpleFeature sourceFeature = (SimpleFeature) sourceFeatureIterator
					.next();

			String sourceFeatureGeogrName = sourceFeature.getID();

			String sourceName = getMatchingAttributeName(sourceFeature);

			if(sourceName == null|| sourceName.equals("-") || sourceName.equals("No Information") || sourceName.equals("noInformation")){
				continue;
			}
			
			Map<OMObservationType, Double> targetFeaturesInRange = getFeatureInRange(
					sourceFeature, targetGazetteerFeatures.getPayload(),
					searchDistance);

			if (targetFeaturesInRange.size() == 0) {
				LOGGER.info("No features in range for feature with id "
						+ sourceFeature.getID());
				continue;
			}

			List<GazetteerConflationResultEntry> tmpResults = new ArrayList<GazetteerConflationResultEntry>();

			/*
			 * iterate over target features
			 */

			Iterator<OMObservationType> targetFeatureIterator = targetFeaturesInRange
					.keySet().iterator();

			while (targetFeatureIterator.hasNext()) {

				OMObservationType targetFeature = targetFeatureIterator.next();

				/*
				 * check names with FuzzyWuzzy first get source name we have to
				 * do this for each alternativeGeogrId save the combination with
				 * the highest fw score, if tied, use distance
				 */

				String descriptionString = getDescription(targetFeature);

				if(descriptionString == null || descriptionString.equals("")){
					continue;
				}
				
				
				/*
				 * tags in the description field are mostly separated by comma
				 */
				String[] descriptionArray = descriptionString.split(" ");

				String[] sourceNameParts = sourceName.split(" ");

				List<Integer> scoreList = new ArrayList<Integer>();

				for (String sourceNamePart : sourceNameParts) {

					int maxScore = 0;

					for (String targetNamePart : descriptionArray) {
						int fwScore = getFuzzyWuzzyScore(sourceNamePart,
								targetNamePart);
						if (fwScore > maxScore) {
							maxScore = fwScore;
						}
					}
					scoreList.add(maxScore);
				}

				int summedUpScores = 0;

				for (Integer integer : scoreList) {
					summedUpScores = summedUpScores + integer;
				}

				
				int averageScore = 0;				
				
				if(scoreList.size() != 0){
					averageScore = summedUpScores / scoreList.size();
				}

				// int fwScore = getFuzzyWuzzyScore(sourceName, string);

				LOGGER.debug(averageScore + " "
						+ targetFeaturesInRange.get(targetFeature) + " "
						+ sourceFeatureGeogrName + " " + targetFeature.getId()
						+ " " + sourceName + " " + descriptionString);

				/*
				 * if above fuzzywuzzy threshold: save fw score, distance,
				 * geographicId_NGA, geographicId_NB,
				 * alternativeGeographicIdentifier_NGA,
				 * alternativeGeographicIdentifier_NB
				 */
				if (averageScore >= fuzzyWuzzyThreshold) {
					tmpResults.add(new GazetteerConflationResultEntry(
							averageScore, targetFeaturesInRange
									.get(targetFeature),
							sourceFeatureGeogrName, targetFeature.getId(),
							sourceName, descriptionString));
					break;
				}

			}

			if (tmpResults.size() > 0) {
				Collections.sort(tmpResults);

				finalResults.add(tmpResults.get(0));
			}

		}
		return finalResults;
	}

	private String getDescription(OMObservationType targetFeature) {
		
		StringOrRefType descriptionType = targetFeature.getDescription();
		
		String descriptionString = "";
		
		if(descriptionType != null){
			descriptionString = descriptionType.getStringValue();			
		}	
		
		return descriptionString;
	}

	private Map<OMObservationType, Double> getFeatureInRange(SimpleFeature sourceFeature, List<OMObservationType> list, double distanceThreshold){
		
		Map<OMObservationType, Double> featuresInRange = new HashMap<OMObservationType, Double>();
		
		Iterator<OMObservationType> targetFeatureIterator = list.iterator();
		
		LOGGER.info(getMatchingAttributeName(sourceFeature));
		
		LOGGER.info("" + sourceFeature.getDefaultGeometry());
		
		Point sourceFeaturePointInNad83 = transformSourceFeature(sourceFeature);
		
		LOGGER.info("" + sourceFeaturePointInNad83);
		
		while (targetFeatureIterator.hasNext()) {
			OMObservationType candidateFeature = targetFeatureIterator.next();
			
			double range = isInRange(sourceFeaturePointInNad83, candidateFeature, distanceThreshold);
			
			if(range != -1){
				featuresInRange.put(candidateFeature, range);
			}
		}
		
		return featuresInRange;
	}
	
	private String getMatchingAttributeName(SimpleFeature feature){
		
		String geographicIdentifier = "";
		
		Collection<Property> properties = feature.getProperties();
		
		for (Property property : properties) {
			String propName = property.getName().toString();
			if(propName.contains(fullName) && !propName.contains(description)){
				geographicIdentifier = property.getValue().toString();
				break;
			}
		}
		
		return geographicIdentifier;
		
	}
	
	private Point transformSourceFeature(SimpleFeature sourceFeature) {
		Point p1Nad83 = null;

		if (sourceFeature.getDefaultGeometry() instanceof Point) {

			Point p1 = (Point) sourceFeature.getDefaultGeometry();

			Coordinate reversedP1Coordinate = new Coordinate(p1.getY(), p1.getX());
			
			Point p1Reversed = new GeometryFactory().createPoint(reversedP1Coordinate);
			
			p1Nad83 = transformWGS84ToNAD83(p1Reversed);
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

	private double isInRange(Point sourceFeaturePoint, OMObservationType candidateFeature,
			double distanceThreshold) {
		
		FeaturePropertyType featureOfInterest = candidateFeature
				.getFeatureOfInterest();

		AbstractFeatureType abstractFeature = featureOfInterest.getAbstractFeature();

		if (abstractFeature instanceof SFSpatialSamplingFeatureTypeImpl) {

			SFSpatialSamplingFeatureType featureTypeImpl = (SFSpatialSamplingFeatureType)abstractFeature ;
			
			ShapeType shapeType = featureTypeImpl.getShape();

			AbstractGeometryType abstractGeometryType = shapeType
					.getAbstractGeometry();

			if (abstractGeometryType instanceof PointType) {
				
				PointType pointType = (PointType) abstractGeometryType;

				String [] coordinates = pointType.getPos().getStringValue().split(" ");
				/*
				 * right now, we just revert the order of the coordinates
				 * additionally we could check, whether they are not inside the bounding box of the target
				 *  
				 */
				Point p2 = new GeometryFactory().createPoint(new Coordinate(Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1])));
				
				/*
				 * transform to NAD83 projected coordinate system
				 */			
				Point p2Nad83 = transformWGS84ToNAD83(p2);
				
				//get the distance in meter
				double tmpDistance = sourceFeaturePoint
						.distance(p2Nad83);
				LOGGER.info(candidateFeature.getId());
				LOGGER.info(tmpDistance/1000 + " " + (distanceThreshold * kmInMilesFactor));
				
				//check against threshold, convert both values to kilometer
				if ((tmpDistance/1000) < (distanceThreshold * kmInMilesFactor)) {
					//return distance in miles
					return (tmpDistance/1000) * (1/kmInMilesFactor);
				}
			}
		}
		return -1;
	}
	
	private String getCommand(String name1, String name2) {

		return pythonHome + fileSeparator + pythonName + " " + fuzzyName + " " + name1 + " " + name2;
	}

	private int getFuzzyWuzzyScore(String name1, String name2){			

		try {

			LOGGER.info("Executing FuzzyWuzzy with " + name1 + " and " + name2  + " using all upper case letters.");

			Runtime rt = Runtime.getRuntime();

			Process proc = rt.exec(getCommand(name1.toUpperCase(), name2.toUpperCase()));

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
			executor.execute(errorGobbler);
			executor.execute(outputGobbler);

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
			
			try {
				proc.waitFor();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			} finally {
				proc.destroy();
			}
			
			try {
				
				return Integer.parseInt(output.trim());
				
			} catch (Exception e) {				
				
				if(errors != null && !errors.equals("")){
					LOGGER.error(
							"An error occured while executing the FuzzyWuzzy process:" + errors,
							e);
					throw new RuntimeException(errors);
				}
			}
			
		} catch (IOException e) {
			LOGGER.error(
					"An error occured while executing the FuzzyWuzzy process.",
					e);
			throw new RuntimeException(e);
		}
		return -1;
	}
	
	@Override
	public List<String> getErrors() {
		return null;
	}

	@Override
	public Class<?> getInputDataType(String id) {
		
		if(id.endsWith(sourceWFS)){
			return GTVectorDataBinding.class;
		}else if(id.endsWith(targetWFS)){
			return GML32OMWFSDataBinding.class;			
		}else if(id.endsWith(targetGazetteerDescriptionFilter)){
			return LiteralStringBinding.class;
		}else if(id.endsWith(boundingBoxFilter)){
			return GTReferenceEnvelope.class;
		}else if(id.endsWith(searchDistance)){
			return LiteralDoubleBinding.class;
		}else if(id.endsWith(fuzzyWuzzyThreshold)){
			return LiteralDoubleBinding.class;
		}else if(id.endsWith(targetWFSMaxFeatures)){
			return LiteralIntBinding.class;
		}
		
		return null;
	}

	@Override
	public Class<GazetteerRelationalOutputDataBinding> getOutputDataType(String id) {		
		return GazetteerRelationalOutputDataBinding.class;
	}

}
