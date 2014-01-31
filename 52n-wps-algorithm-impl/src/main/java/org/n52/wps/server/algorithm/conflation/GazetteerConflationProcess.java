package org.n52.wps.server.algorithm.conflation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.io.data.GenericFileData;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.bbox.GTReferenceEnvelope;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralAnyURIBinding;
import org.n52.wps.io.data.binding.literal.LiteralDoubleBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.io.datahandler.parser.GML32WFSGBasicParser;
import org.n52.wps.io.datahandler.parser.GML3WFSGBasicParser;
import org.n52.wps.server.AbstractAlgorithm;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.LocalAlgorithmRepository;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class GazetteerConflationProcess extends AbstractAlgorithm {

	private static Logger LOGGER = LoggerFactory.getLogger(GazetteerConflationProcess.class);
	
	private final String sourceGazetteer = "Source_Gazetteer";
	private final String targetGazetteer = "Target_Gazetteer";
	private final String sourceGazetteerDescriptionFilter = "Source_Gazetteer_Description_Filter";
	private final String targetGazetteerDescriptionFilter = "Target_Gazetteer_Description_Filter";
	private final String boundingBoxFilter = "Bounding_Box_Filter";
	private final String searchDistance = "Search_Distance";
	private final String fuzzyWuzzyThreshold = "FuzzyWuzzy_Threshold";
	private final String ngaGazetteerMaxFeatures = "NGA_Gazetteer_Max_Features";
	private final String newBrunswickGazetteerMaxFeatures = "New_Brunswick_Gazetteer_Max_Features";
	private final String outputFile = "Output_File";
	
	private MathTransform tx;
	
	private double kmInMilesFactor = 1.609347219;
	
	private String alternativeGeographicIdentifierName = "alternativeGeographicIdentifier";
	private String geographicIdentifierName = "geographicIdentifier";
	
	private String pythonName = "python.exe";

	private String pythonHome;

	private char fileSeparator = File.separatorChar;
	
	private final String OS_Name = System.getProperty("os.name");
	
	private final String lineSeparator = System.getProperty("line.separator");
	
	private String fuzzyName;
	
	int maxFeaturesNGAInt = 2500;
	int maxFeaturesNewBrunswickInt = 2500;
	
	private ExecutorService executor = Executors.newFixedThreadPool(10);
	
	public GazetteerConflationProcess(){
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
			CoordinateReferenceSystem nad83 = CRS.decode("EPSG:2953");
			
			tx = CRS.findMathTransform(wgs84, nad83, false);
		} catch (Exception e) {
			LOGGER.error("Exception while trying to find transformation between WGS84 and NAD83.", e);
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
		
		/*
		 * append bbox
		 */
		
		List<IData> bboxFilterInputs = inputData.get(boundingBoxFilter);
		
		Envelope bboxFilterReferenceEnvelope = null;
		
		try {
			bboxFilterReferenceEnvelope = ((GTReferenceEnvelope)bboxFilterInputs.get(0)).getPayload();			
		} catch (ClassCastException e) {
			throw new RuntimeException(boundingBoxFilter + " input value can not be cast to GTReferenceEnvelope.");
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new RuntimeException("No value for input " + boundingBoxFilter + " provided.");
		}
		
		double[] lowerCorner = bboxFilterReferenceEnvelope.getLowerCorner().getCoordinate();
		double[] upperCorner = bboxFilterReferenceEnvelope.getUpperCorner().getCoordinate();
		
		List<IData> sourceGazInputs = inputData.get(sourceGazetteer);
		
		URI sourceGazURI = null;
		
		try {
			sourceGazURI = ((LiteralAnyURIBinding)sourceGazInputs.get(0)).getPayload();			
		} catch (ClassCastException e) {
			throw new RuntimeException(sourceGazetteer + " input value is not an URI.");
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new RuntimeException("No value for input " + sourceGazetteer + " provided.");
		}
		
		/*
		 * get target features
		 */
		List<IData> targetGazInputs = inputData.get(targetGazetteer);
		
		URI targetGazURI = null;
		
		try {
			targetGazURI = ((LiteralAnyURIBinding)targetGazInputs.get(0)).getPayload();			
		} catch (ClassCastException e) {
			throw new RuntimeException(targetGazetteer + " input value is not an URI.");
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new RuntimeException("No value for input " + targetGazetteer + " provided.");
		}
		
		/*
		 * get source description filter 
		 */
		List<IData> sourceDescriptionFilter = inputData.get(sourceGazetteerDescriptionFilter);
		
		String[] sourceDescriptionFilterLiterals = null;
		
		try {
			String sourceDescriptionFilterLiteralString = ((LiteralStringBinding)sourceDescriptionFilter.get(0)).getPayload();
			sourceDescriptionFilterLiterals = sourceDescriptionFilterLiteralString.split(",");
		} catch (ClassCastException e) {
			throw new RuntimeException(sourceGazetteerDescriptionFilter + " input value is not a String.");
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new RuntimeException("No value for input " + sourceGazetteerDescriptionFilter + " provided.");
		}
		
		/*
		 * get  description filter 
		 */
		List<IData> targetDescriptionFilter = inputData.get(targetGazetteerDescriptionFilter);
		
		String[] targetDescriptionFilterLiterals = null;
		
		try {
			String targetDescriptionFilterLiteralString = ((LiteralStringBinding)targetDescriptionFilter.get(0)).getPayload();
			targetDescriptionFilterLiterals = targetDescriptionFilterLiteralString.split(",");
		} catch (ClassCastException e) {
			throw new RuntimeException(targetGazetteerDescriptionFilter + " input value is not a String.");
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new RuntimeException("No value for input " + targetGazetteerDescriptionFilter + " provided.");
		}
		
		/*
		 * get  maxFeatures for new brunswick gazetteer (target)
		 */
		List<IData> maxFeaturesNewBrunswick = inputData.get(newBrunswickGazetteerMaxFeatures);
		
		try {
			if(maxFeaturesNewBrunswick != null && maxFeaturesNewBrunswick.size()>0){
				maxFeaturesNewBrunswickInt = ((LiteralIntBinding)maxFeaturesNewBrunswick.get(0)).getPayload();
			}else{
				LOGGER.info("No maxFeatures value for New Brunswick gazetteer provided, using default value: " + maxFeaturesNewBrunswickInt);
			}
		} catch (ClassCastException e) {
			throw new RuntimeException(newBrunswickGazetteerMaxFeatures + " input value is not an integer.");
		}
		
		String finalTargetGazetteerRequest = targetGazURI.toString();
		
		GML3WFSGBasicParser gml32Parser = new GML3WFSGBasicParser();
		
		GTVectorDataBinding targetGazetteerFeatures = null;
		
		try {
			
			String request = buildNewBrunswickRequest(WFSGRequestStringConstants.WFS100_NEW_BRUNSWICK_GET_FEATURE_WITH_QUERY_REQUEST, "iso19112:locationType//iso19112:name", targetDescriptionFilterLiterals, maxFeaturesNewBrunswickInt);
			
			targetGazetteerFeatures = gml32Parser.parse(PostClient.sendRequestForInputStream(finalTargetGazetteerRequest, request), "text/xml", "http://schemas.opengis.net/gml/3.1.1/base/gml.xsd");
						
		} catch (IOException e) {
			throw new RuntimeException("Could not connect to target gazetteer: " + finalTargetGazetteerRequest, e);
		}
		
		if(targetGazetteerFeatures == null){
			throw new RuntimeException("No source gazetteer features found.");
		}
		
		LOGGER.info("Found " + targetGazetteerFeatures.getPayload().size() + " target features.");
		
		/*
		 * get  maxFeatures for nga gazetteer (source)
		 */
		List<IData> maxFeaturesNGA = inputData.get(ngaGazetteerMaxFeatures);
		
		try {
			if(maxFeaturesNGA != null && maxFeaturesNGA.size()>0){
				maxFeaturesNGAInt = ((LiteralIntBinding)maxFeaturesNGA.get(0)).getPayload();
			}else{
				LOGGER.info("No maxFeatures value for NGA gazetteer provided, using default value: " + maxFeaturesNGAInt);
			}
		} catch (ClassCastException e) {
			throw new RuntimeException(ngaGazetteerMaxFeatures + " input value is not an integer.");
		} 
		
		String finalSourceGazetteerRequest = sourceGazURI.toString();
				
		/*
		 * load response stream into feature collection
		 */
		GML3WFSGBasicParser gml3Parser = new GML3WFSGBasicParser();
		
		GTVectorDataBinding sourceGazetteerFeatures = null;
		
		try {
			
			String request = buildNGARequest(WFSGRequestStringConstants.WFS100_GET_FEATURE_WITH_QUERY_REQUEST, lowerCorner, upperCorner, "iso19112:locationType/iso19112:SI_LocationType/iso19112:identification", sourceDescriptionFilterLiterals, maxFeaturesNGAInt);
			
			sourceGazetteerFeatures = gml3Parser.parse(PostClient.sendRequestForInputStream(finalSourceGazetteerRequest, request), "text/xml", "http://schemas.opengis.net/gml/3.1.1/base/gml.xsd");
					
		} catch (IOException e) {
			throw new RuntimeException("Could not connect to source gazetteer: " + finalSourceGazetteerRequest, e);
		}
		
		if(sourceGazetteerFeatures == null){
			throw new RuntimeException("No source gazetteer features found.");
		}
		
		LOGGER.info("Found " + sourceGazetteerFeatures.getPayload().size() + " source features.");
		
		/*
		 * loop over features
		 */
		FeatureIterator<?> sourceFeatureIterator = sourceGazetteerFeatures.getPayload().features();
		
		List<GazetteerConflationResultEntry> finalResults = new ArrayList<GazetteerConflationResultEntry>();
		
		while(sourceFeatureIterator.hasNext()){
			
			SimpleFeature sourceFeature = (SimpleFeature) sourceFeatureIterator.next();
			
			String sourceFeatureGeogrName = getGeographicIdentifier(sourceFeature);
			
			List<String> sourceNameList = getAllAlternativeGeographicIdentifier(sourceFeature);

			Map<SimpleFeature, Double> targetFeaturesInRange = getFeatureInRange(sourceFeature, targetGazetteerFeatures.getPayload(), searchDistance);
			
			if(targetFeaturesInRange.size() == 0){
				LOGGER.info("No features in range for feature with id " + sourceFeature.getID());
				continue;
			}
			
			List<GazetteerConflationResultEntry> tmpResults = new ArrayList<GazetteerConflationResultEntry>();
			
			for (String sourceName : sourceNameList) {				
		
				/*
				 * iterate over target features
				 */		
				
				Iterator<SimpleFeature> targetFeatureIterator = targetFeaturesInRange.keySet().iterator();
				
				while(targetFeatureIterator.hasNext()){
					
					SimpleFeature targetFeature = targetFeatureIterator.next();
						
						/*
						 * check names with FuzzyWuzzy
						 * first get source name
						 * we have to do this for each alternativeGeogrId
						 * save the combination with the highest fw score, if tied, use distance 
						 */
						
						String targetName = getAlternativeGeographicIdentifier(targetFeature);

						int fwScore = getFuzzyWuzzyScore(sourceName, targetName);
						
						LOGGER.debug(fwScore + " " + targetFeaturesInRange.get(targetFeature) + " " + sourceFeatureGeogrName + " " + getGeographicIdentifier(targetFeature) + " " + sourceName + " " + targetName);
						
						/*
						 * if above fuzzywuzzy threshold:
						 * save fw score, distance, geographicId_NGA, geographicId_NB, alternativeGeographicIdentifier_NGA, alternativeGeographicIdentifier_NB
						 */
						if(fwScore >= fuzzyWuzzyThreshold){
							tmpResults.add(new GazetteerConflationResultEntry(fwScore, targetFeaturesInRange.get(targetFeature), sourceFeatureGeogrName, getGeographicIdentifier(targetFeature), sourceName, targetName));
						}
					
				}
				
			}
			if(tmpResults.size() >0){
				Collections.sort(tmpResults);
			
				finalResults.add(tmpResults.get(0));
			}
			
			
		}
		Collections.sort(finalResults);
		
		LOGGER.debug("Final result count: " + finalResults.size());
		
		/*
		 * create output csv file TODO: RDF?!
		 */
		
		Map<String, IData> result = new HashMap<String, IData>();
		
		try {
			
			File resultFile = File.createTempFile("gazConflationResult", ".csv");
			
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(resultFile));
			
			bufferedWriter.write("FW Score,Dist(MI),NGA_UFI,NB_ID,NGA_NAME,NB_NAME" + "\n");
			
			for (GazetteerConflationResultEntry gazetteerConflationResultEntry : finalResults) {
				bufferedWriter.write(gazetteerConflationResultEntry.toString() + "\n");
			}
			
			bufferedWriter.close();
			
			result.put(outputFile, new GenericFileDataBinding(new GenericFileData(resultFile, "text/csv")));

		} catch (IOException e) {
			LOGGER.error("Could not create result.", e);
		}
		
		return result;
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
			
			double range = isInRange(sourceFeaturePointInNad83, candidateFeature, distanceThreshold);
			
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

	private double isInRange(Point sourceFeaturePoint, SimpleFeature f2,
			double distanceThreshold) {

		if (f2.getDefaultGeometry() instanceof Point) {

			/*
			 * TODO: coordinates from second WFS-G seem to be in a different order
			 * right now, we just revert the order of the coordinates
			 * additionally we could check, whether they are not inside the bounding box of the target
			 *  
			 */
			Point p2 = (Point) f2.getDefaultGeometry();
			
//			Coordinate reversedP2Coordinate = new Coordinate(p2.getY(), p2.getX());
//			
//			Point p2Reversed = new GeometryFactory().createPoint(reversedP2Coordinate);
			
			/*
			 * transform to NAD83 projected coordinate system
			 */			
			Point p2Nad83 = transformWGS84ToNAD83(p2);
			
			//get the distance in meter
			double tmpDistance = sourceFeaturePoint
					.distance(p2Nad83);
			LOGGER.info(getAlternativeGeographicIdentifier(f2));
			LOGGER.info(tmpDistance/1000 + " " + (distanceThreshold * kmInMilesFactor));
			
			//check against threshold, convert both values to kilometer
			if ((tmpDistance/1000) < (distanceThreshold * kmInMilesFactor)) {
				//return distance in miles
				return (tmpDistance/1000) * (1/kmInMilesFactor);
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
	
	private String buildNGARequest(String request, double[] lowerCorner, double[] upperCorner, String propertyName, String[] filterLiterals, int maxFeatures){
		
		String bbox = WFSGRequestStringConstants.FE100_BBOX.replace(WFSGRequestStringConstants.LXEXP, "" + lowerCorner[0]);
		
		bbox = bbox.replace(WFSGRequestStringConstants.LYEXP,  "" + lowerCorner[1]);
		bbox = bbox.replace(WFSGRequestStringConstants.UXEXP,  "" + upperCorner[0]);
		bbox = bbox.replace(WFSGRequestStringConstants.UYEXP,  "" + upperCorner[1]);
		
		request = request.replace(WFSGRequestStringConstants.BBOXEXP, bbox);
	
		String allOrStatements = createFilter(propertyName, filterLiterals);
		
		request = request.replace(WFSGRequestStringConstants.EQALTOEXP, allOrStatements);
		request = request.replace(WFSGRequestStringConstants.MAXFEATURESEXP, maxFeatures + "");
		
		return request;	
		
	}
	
	private String buildNewBrunswickRequest(String request, String propertyName, String[] filterLiterals, int maxFeatures){
		
		String allOrStatements = createFilter(propertyName, filterLiterals);
		
		request = request.replace(WFSGRequestStringConstants.EQALTOEXP, allOrStatements);
		request = request.replace(WFSGRequestStringConstants.MAXFEATURESEXP, maxFeatures + "");
		
		return request;	
		
	}

	private String createFilter(String propertyName, String[] filterLiterals){
		
		String allOrStatements = "";
		
		for (String literal : filterLiterals) {
			
			String orStatement = WFSGRequestStringConstants.FE100_EQUALTO.replace(WFSGRequestStringConstants.PROPERTYVALEXP, propertyName);
			
			orStatement = orStatement.replace(WFSGRequestStringConstants.LITERALEXP, literal.trim());
			
			allOrStatements = allOrStatements.concat(orStatement);
			
		}
		
		if(filterLiterals.length > 1){
			
			return WFSGRequestStringConstants.FE100_OR.replace(WFSGRequestStringConstants.EQALTOEXP, allOrStatements);
			
		}
		
		return allOrStatements;
	}
	
	@Override
	public List<String> getErrors() {
		return null;
	}

	@Override
	public Class<?> getInputDataType(String id) {
		
		if(id.endsWith(sourceGazetteer)){
			return LiteralAnyURIBinding.class;
		}else if(id.endsWith(targetGazetteer)){
			return LiteralAnyURIBinding.class;			
		}else if(id.endsWith(sourceGazetteerDescriptionFilter)){
			return LiteralStringBinding.class;
		}else if(id.endsWith(targetGazetteerDescriptionFilter)){
			return LiteralStringBinding.class;
		}else if(id.endsWith(boundingBoxFilter)){
			return GTReferenceEnvelope.class;
		}else if(id.endsWith(searchDistance)){
			return LiteralDoubleBinding.class;
		}else if(id.endsWith(fuzzyWuzzyThreshold)){
			return LiteralDoubleBinding.class;
		}else if(id.endsWith(ngaGazetteerMaxFeatures)){
			return LiteralIntBinding.class;
		}else if(id.endsWith(newBrunswickGazetteerMaxFeatures)){
			return LiteralIntBinding.class;
		}
		
		return null;
	}

	@Override
	public Class<GenericFileDataBinding> getOutputDataType(String id) {		
		return GenericFileDataBinding.class;
	}

}
