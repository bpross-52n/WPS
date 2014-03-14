package org.n52.wps.server.algorithm.conflation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.io.GTHelper;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.complex.GTVectorDataBindingWithSourceURL;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.provenance.ProvenanceType;
import org.n52.wps.provenance.RDFProvenanceFeature;
import org.n52.wps.provenance.RDFUtil;
import org.n52.wps.server.AbstractAlgorithm;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.LocalAlgorithmRepository;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class Kinda_Generic_ConflationProcess extends AbstractAlgorithm{

	private static Logger LOGGER = LoggerFactory.getLogger(Kinda_Generic_ConflationProcess.class);
	
	private final String source_id = "Source";
	private final String target_id = "Target";
	private final String rules_id = "Rules";
	private final String output_id_conflated_result = "conflated_result";
	private final String output_id_provenance = "provenance";
	private final String default_String = "noInformation";
	private final Double default_Double = -999999.0;
	private final long default_BigInteger = -999999;
	
	private boolean includeBundle = false;
	private DateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");	
	private GregorianCalendar calendar = new GregorianCalendar(2014, 3, 14, 17, 23);	
	private String algorithmGeneratedAt = dateformat.format(calendar.getTime());		
	private String baseURL = "http://www.opengis.net/ogc/ows10/52n/provenance-52n-march.ttl";
	private String attributeTo = RDFUtil.PREFIX_F2N + ":arne";
	private String generatedAt = dateformat.format(new GregorianCalendar().getTime());
	
	private final String type = RDFUtil.PREFIX_F2N + ":Kinda_Generic_ConflationProcess_v120";
	private String superType = RDFUtil.PREFIX_F2N + ":ConflationAlgorithm_52N";	
	private String algorithmAttributedTo = RDFUtil.PREFIX_F2N + ":benjamin";		
	private String agent = RDFUtil.PREFIX_F2N + ":benjamin";
	private String role = "ows10:developer";
	private String plan = RDFUtil.PREFIX_F2N + ":Kinda_Generic_ConflationProcess_v120";	
	private String specializationOf = RDFUtil.PREFIX_F2N + ":OWS10conflatedmap_52N";
	private String outputMapNameAttributedTo = RDFUtil.PREFIX_F2N + ":benjamin";
	private String outputMapQualifiedGenerationRole = "ows10:conflatedMapOutput";
	
	private String entityName = RDFUtil.PREFIX_F2N + ":52N_ConflationExecution";	
	private String outputMapName = RDFUtil.PREFIX_F2N + ":OWS10conflatedmap_52N_";
	private String startTime = generatedAt;
	private String outputMapGeneratedAtTime = generatedAt;
	
	private String dataset1Name = "ows10:SC_USGS";
	private String dataset1Role = "ows10:referenceMapSource";
	private String dataset2Name = "ows10:SC_NGA";
	private String dataset2Role = "ows10:referenceMapSource";
	private String endTime = dateformat.format(new GregorianCalendar().getTime());
	
	private Properties properties;
	
	private Map<?, ?> mappingsMap;
	private Map<?, ?> fixedAttributeValuesMap;
	private Map<String, String> attributeTypeMap;
	
	private StringBuilder attributeTypeStatementBuilder = new StringBuilder();
	private StringBuilder attributeTypeSubClassStatementBuilder = new StringBuilder();
	private StringBuilder sourceMemberStatementBuilder = new StringBuilder(); 
	private StringBuilder attributeOriginStatementBuilder = new StringBuilder(); 
	private StringBuilder featureOriginStatementBuilder = new StringBuilder();
	private StringBuilder sourceFeatureAttributeStatementBuilder = new StringBuilder();
	private StringBuilder resultFeatureAttributeStatementBuilder = new StringBuilder();
	private StringBuilder executionProvUsedFeaturesStatementBuilder = new StringBuilder();
	private StringBuilder featuresGeneratedByExecutionStatementBuilder = new StringBuilder();
	private StringBuilder featuresGeneratedAtStatementBuilder = new StringBuilder();
	private StringBuilder resultMemberStatementBuilder = new StringBuilder();
	private StringBuilder qualifiedUsageStatementBuilder = new StringBuilder();
	private StringBuilder qualifiedGenerationStatementBuilder = new StringBuilder();
	private StringBuilder individualExecutionStatementBuilder = new StringBuilder();
	private StringBuilder involvedFeatureCollectionsStatementBuilder = new StringBuilder();
	private StringBuilder featureTypeSubClassStatementBuilder = new StringBuilder();
	
	private Map<String, RDFProvenanceFeature> targetFeatureMap;
	private Map<String, RDFProvenanceFeature> sourceFeatureMap;
	private Map<String, RDFProvenanceFeature> resultFeatureMap;
	private Map<RDFProvenanceFeature, RDFProvenanceFeature> targetResultFeatureMap;
	
	private String sourceRole = "ows10:unknownMapSource";
	private String targetRole = "ows10:unknownMapSource";
	private String sourceNamespace = RDFUtil.PREFIX_UNKNOWN_DATA;
	private String targetNamespace = RDFUtil.PREFIX_UNKNOWN_DATA;
	private String resultNamespace = RDFUtil.PREFIX_UNKNOWN_CONF;

	private String targetURL;
	private String sourceURL;
	
	public Kinda_Generic_ConflationProcess(){
		
		properties = new Properties();
		
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
						"mappingsfile")) {
					String mappingsfile = property.getStringValue();
					
					try {
						properties.load(new FileInputStream(new File(mappingsfile)));
					} catch (IOException e) {
						LOGGER.error(e.getMessage());
					}
				}
			}
		}
		
		mappingsMap = new HashMap<String, String>();
		fixedAttributeValuesMap = new HashMap<String, String>();		
		attributeTypeMap = new HashMap<String, String>();
		sourceFeatureMap = new HashMap<String, RDFProvenanceFeature>();
		targetFeatureMap = new HashMap<String, RDFProvenanceFeature>();
		resultFeatureMap = new HashMap<String, RDFProvenanceFeature>();
		targetResultFeatureMap = new HashMap<RDFProvenanceFeature, RDFProvenanceFeature>();
		
		entityName = entityName + createUUIDString();
		
		sourceMemberStatementBuilder.append("###################################################################\n");
		sourceMemberStatementBuilder.append("#     Individual features of the datasets\n");
		sourceMemberStatementBuilder.append("###################################################################\n");
		sourceMemberStatementBuilder.append("\n");
		
		attributeTypeStatementBuilder.append("###################################################################\n");
		attributeTypeStatementBuilder.append("#    Individual feature properties of the source datasets\n");
		attributeTypeStatementBuilder.append("###################################################################\n");
		attributeTypeStatementBuilder.append("\n");
		
		attributeTypeSubClassStatementBuilder.append("###################################################################\n");
		attributeTypeSubClassStatementBuilder.append("#    AttributeTypes involved in the conflation process\n");
		attributeTypeSubClassStatementBuilder.append("###################################################################\n");
		attributeTypeSubClassStatementBuilder.append("\n");
		
		featureTypeSubClassStatementBuilder.append("###################################################################\n");
		featureTypeSubClassStatementBuilder.append("#    FeatureTypes involved in the conflation process\n");
		featureTypeSubClassStatementBuilder.append("###################################################################\n");
		featureTypeSubClassStatementBuilder.append("\n");
		
		featureOriginStatementBuilder.append("###################################################################\n");
		featureOriginStatementBuilder.append("#    How Individual conflated features relate to sources\n");
		featureOriginStatementBuilder.append("###################################################################\n");
		featureOriginStatementBuilder.append("\n");
		
		attributeOriginStatementBuilder.append("###################################################################\n");
		attributeOriginStatementBuilder.append("#    How Individual conflated feature properties relate to sources\n");
		attributeOriginStatementBuilder.append("###################################################################\n");
		attributeOriginStatementBuilder.append("\n");
		
		executionProvUsedFeaturesStatementBuilder.append("###################################################################\n");
		executionProvUsedFeaturesStatementBuilder.append("#    Relations between individual features and individual executions\n");
		executionProvUsedFeaturesStatementBuilder.append("###################################################################\n");
		executionProvUsedFeaturesStatementBuilder.append("\n");
		
		qualifiedUsageStatementBuilder.append("###################################################################\n");
		qualifiedUsageStatementBuilder.append("#    Roles for individual executions and features\n");
		qualifiedUsageStatementBuilder.append("###################################################################\n");
		qualifiedUsageStatementBuilder.append("\n");
		
		individualExecutionStatementBuilder.append("###################################################################\n");
		individualExecutionStatementBuilder.append("#                     Individual execution\n");
		individualExecutionStatementBuilder.append("###################################################################\n");
		individualExecutionStatementBuilder.append("\n");
		
		involvedFeatureCollectionsStatementBuilder.append("###################################################################\n");
		involvedFeatureCollectionsStatementBuilder.append("#     FeatureCollections (Datasets) involved in the conflation process\n");
		involvedFeatureCollectionsStatementBuilder.append("###################################################################\n");
		involvedFeatureCollectionsStatementBuilder.append("\n");
		
		resultMemberStatementBuilder.append("###################################################################\n");
		resultMemberStatementBuilder.append("#     Individual features of the result dataset\n");
		resultMemberStatementBuilder.append("###################################################################\n");
		resultMemberStatementBuilder.append("\n");
		
	}
	
	@Override
	public Map<String, IData> run(Map<String, List<IData>> inputData)
			throws ExceptionReport {
		long startMillis = System.currentTimeMillis();
		Date startDate = new GregorianCalendar().getTime();		
		generatedAt = dateformat.format(startDate);
		String entityUUID = UUID.randomUUID().toString().substring(0, 5);
		entityName = entityName.concat(entityUUID);
		outputMapName = outputMapName.concat(entityUUID);
		
		if (inputData == null || !inputData.containsKey(source_id)) {
			throw new RuntimeException(
					"Error while allocating input parameters");
		}
		
		List<IData> dataList = inputData.get(source_id);
		if (dataList == null || dataList.size() != 1) {
			throw new RuntimeException(
					"Error while allocating input parameters");
		}
		
		IData sourceInputData = dataList.get(0);
		
		GTVectorDataBindingWithSourceURL sourceInputBindingWithSourceURL = null;
		
		if(sourceInputData instanceof GTVectorDataBindingWithSourceURL){
			sourceInputBindingWithSourceURL = (GTVectorDataBindingWithSourceURL)sourceInputData;
		}
		
		FeatureCollection<?, ?> sourceFeatureCollection = sourceInputBindingWithSourceURL.getPayload();
		
		LOGGER.info(sourceInputBindingWithSourceURL.getSourceURL());
		
		sourceURL = "<" + sourceInputBindingWithSourceURL.getSourceURL() + ">";
		
		if (inputData == null || !inputData.containsKey(target_id)) {
			throw new RuntimeException(
					"Error while allocating input parameters");
		}
		
		List<IData> targetDataList = inputData.get(target_id);
		if (targetDataList == null || targetDataList.size() != 1) {
			throw new RuntimeException(
					"Error while allocating input parameters");
		}
		
		IData targetInputData = targetDataList.get(0);
		
		GTVectorDataBindingWithSourceURL targetInputBindingWithSourceURL = null;
		
		if(targetInputData instanceof GTVectorDataBindingWithSourceURL){
			targetInputBindingWithSourceURL = (GTVectorDataBindingWithSourceURL)targetInputData;
		}
		
		FeatureCollection<?, ?> targetFeatureCollection = targetInputBindingWithSourceURL.getPayload();
		
		LOGGER.info(targetInputBindingWithSourceURL.getSourceURL());

		targetURL = "<" + targetInputBindingWithSourceURL.getSourceURL() + ">";
		
		FeatureIterator<?> targetFeatureIterator = targetFeatureCollection.features();

		setNamespaces(sourceFeatureCollection, targetFeatureCollection);
		
		/*
		 * get rules 
		 */
		List<IData> rulesData = inputData.get(rules_id);
		
		String rules = null;
		
		try {
			rules = ((LiteralStringBinding)rulesData.get(0)).getPayload();
		} catch (ClassCastException e) {
			throw new RuntimeException(rules_id + " input value is not a String.");
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new RuntimeException("No value for input " + rules_id + " provided.");
		}
		
		createRules(rules);
		
		FeatureType ft = sourceFeatureCollection.features().next().getType();
		
		List<SimpleFeature> oldFeatures = Arrays.asList(sourceFeatureCollection.toArray(new SimpleFeature[]{}));
		
		List<SimpleFeature> newFeatures = new ArrayList<SimpleFeature>();
		
		newFeatures.addAll(oldFeatures);
		
		runConflation(targetFeatureIterator, newFeatures, ft);
		
		FeatureCollection<?, ?> result = new ListFeatureCollection((SimpleFeatureType)ft, newFeatures);
		
		Date endDate = new GregorianCalendar().getTime();
		
		long endMillis = System.currentTimeMillis();
		
		endTime = dateformat.format(endDate);
		
		LOGGER.info("Conflation process took " + (endMillis - startMillis)/ 1000 + " seconds");
		
		String rdfProvenance = createRDFProvenance2(sourceFeatureCollection, targetFeatureCollection);
		
		Map<String, IData> resultMap = new HashMap<String, IData>(2);
		
		resultMap.put(output_id_conflated_result, new GTVectorDataBinding(result));
		resultMap.put(output_id_provenance, new LiteralStringBinding(rdfProvenance));
		
		return resultMap;
	}

	public void setNamespaces(FeatureCollection<?, ?> sourceFeatureCollection,
			FeatureCollection<?, ?> targetFeatureCollection) {
		
		String sourceFeatureCollectionNamespace = sourceFeatureCollection.getSchema().getName().getNamespaceURI();

		sourceFeatureCollectionNamespace = sourceFeatureCollectionNamespace.replace("http://", "");
		
		if(properties.containsKey(sourceFeatureCollectionNamespace)){
			
			String [] nameSpaceRoleArray = ((String) properties.get(sourceFeatureCollectionNamespace)).split(",");
			
			sourceNamespace = nameSpaceRoleArray[0] + "_data";
			sourceRole = nameSpaceRoleArray[1];
			resultNamespace = nameSpaceRoleArray[0] + "_conf";
		}else{
			/*
			 * default one will be used
			 */
		}
		
		String targetFeatureCollectionNamespace = targetFeatureCollection.getSchema().getName().getNamespaceURI();
		
		targetFeatureCollectionNamespace = targetFeatureCollectionNamespace.replace("http://", "");
		
		if(properties.containsKey(targetFeatureCollectionNamespace)){
			
			String [] nameSpaceRoleArray = ((String) properties.get(targetFeatureCollectionNamespace)).split(",");
			
			targetNamespace = nameSpaceRoleArray[0] + "_data";
			targetRole = nameSpaceRoleArray[1];
			
		}else{
			/*
			 * default one will be used
			 */
		}
		
	}

	private void createAttributeTypeSubClassStatements(GeometryType sourceGeometryType,
			GeometryType targetGeometryType) {
		
		addAttributeTypeSubClassStatementsToBuilder(mappingsMap.keySet().iterator(), targetNamespace, false);
		addAttributeTypeSubClassStatementsToBuilder(mappingsMap.values().iterator(), resultNamespace, true);
		
		createPositionTypeStatement(resultNamespace, sourceGeometryType, true);
		createPositionTypeStatement(targetNamespace, targetGeometryType, false);
	}
	
	private void createPositionTypeStatement(String nameSpace, GeometryType geometryType, boolean conflate){
		
		String geometryTypeString = "";
		
		if(geometryType.getBinding().isAssignableFrom(Point.class) || geometryType.getBinding().isAssignableFrom(MultiPoint.class)){
			geometryTypeString = RDFUtil.OWS_POINT;
		}else if(geometryType.getBinding().isAssignableFrom(LineString.class) || geometryType.getBinding().isAssignableFrom(MultiLineString.class)){
			geometryTypeString = RDFUtil.OWS_LINE;
		}else if(geometryType.getBinding().isAssignableFrom(Polygon.class) || geometryType.getBinding().isAssignableFrom(MultiPolygon.class)){
			geometryTypeString = RDFUtil.OWS_POLYGON;
		}
		
		String rdfPosition = "";
		
		if(conflate){
			rdfPosition = RDFUtil.createConflatedPosition(nameSpace);
		}else{
			rdfPosition = RDFUtil.createPosition(nameSpace);
		}
		
		addTripleToStringBuilder(RDFUtil.createTriple(rdfPosition, RDFUtil.PREDICATE_RDFS_SUBCLASS_OF, geometryTypeString, true), attributeTypeSubClassStatementBuilder, true);
		
	}
	
	private void addAttributeTypeSubClassStatementsToBuilder(Iterator<?> attributes, String prefix, boolean conflate){
		
		while (attributes.hasNext()) {
			String mappingsAttribute = (String) attributes.next();
			
			String rdfAttribute = "";
			
			if(conflate){
				rdfAttribute = RDFUtil.createConflatedAttribute(mappingsAttribute, prefix);
			}else{
				rdfAttribute = RDFUtil.createAttribute(mappingsAttribute, prefix);
			}	
			
			String triple = RDFUtil.createTriple(rdfAttribute, RDFUtil.PREDICATE_RDFS_SUBCLASS_OF, RDFUtil.OWS_PROPERTY , true);
		
			attributeTypeSubClassStatementBuilder.append(triple + "\n");
		}
		
	}

	public void runConflation(FeatureIterator<?> iter, List<SimpleFeature> newFeatures, FeatureType ft){

		SimpleFeatureType sourceSimpleFeatureType = (SimpleFeatureType)ft;
		
		SimpleFeature targetFeature = (SimpleFeature) iter.next();
		
		SimpleFeature newFeature = createNewFeature(targetFeature, sourceSimpleFeatureType);
		
		newFeatures.add(newFeature);
		
		while (iter.hasNext()) {
			Object o = iter.next();
			if (o instanceof SimpleFeature) {
				targetFeature = (SimpleFeature) o;
				newFeature = createNewFeature(targetFeature, sourceSimpleFeatureType);
				
				newFeatures.add(newFeature);
			}

		}
	}
	
	public void createSourceProvenanceFeatureMap(FeatureIterator<?> features) {
		
		while (features.hasNext()) {
			SimpleFeature sf = (SimpleFeature) features.next();
			
			RDFProvenanceFeature sourceProvenanceFeature = new RDFProvenanceFeature(RDFUtil.createFeature(sf.getID(), sourceNamespace), RDFUtil.createFeature("", sourceNamespace), new GregorianCalendar().getTime(), sourceRole);
			RDFProvenanceFeature resultProvenanceFeature = new RDFProvenanceFeature(RDFUtil.createConflatedFeature(sf.getID(), resultNamespace), RDFUtil.createFeature("", sourceNamespace), new GregorianCalendar().getTime(), sourceRole);
			
			resultProvenanceFeature.setProvenanceType(ProvenanceType.SAME_AS);
			
			sourceFeatureMap.put(sf.getID(), sourceProvenanceFeature);
			
			targetResultFeatureMap.put(sourceProvenanceFeature, resultProvenanceFeature);
			
		}
		
	}
	
	private SimpleFeature createNewFeature(SimpleFeature targetFeature, SimpleFeatureType sourceSimpleFeatureType){
		String targetID = targetFeature.getIdentifier().getID();
		
		SimpleFeature newFeature = (SimpleFeature) GTHelper.createFeature2(targetID, (Geometry) targetFeature.getDefaultGeometry(), sourceSimpleFeatureType);
		
		RDFProvenanceFeature targetProvenanceFeature = new RDFProvenanceFeature(RDFUtil.createFeature(targetID, targetNamespace), RDFUtil.createFeature("", targetNamespace), new GregorianCalendar().getTime(), targetRole);
		
		addProvenancePositionInfo(targetProvenanceFeature, targetNamespace, false);
		
		targetFeatureMap.put(targetFeature.getID(), targetProvenanceFeature);
		
		RDFProvenanceFeature resultProvenanceFeature = new RDFProvenanceFeature(RDFUtil.createConflatedFeature(targetID, resultNamespace), RDFUtil.createFeature("", sourceNamespace), new GregorianCalendar().getTime(), targetRole);
		
		addProvenancePositionInfo(resultProvenanceFeature, resultNamespace, true);
		
		resultProvenanceFeature.setProvenanceType(ProvenanceType.DERIVED_FROM);
		
		resultFeatureMap.put(newFeature.getID(), resultProvenanceFeature);
		
		targetResultFeatureMap.put(targetProvenanceFeature, resultProvenanceFeature);
		
		mapProperties(targetFeature, newFeature);
		
		addfixedAttributeValues(newFeature);
		
		return newFeature;
	}

	public Geometry tryCreatingGeom(SimpleFeature feature){
		
		Geometry g = null;		
		if(feature.getDefaultGeometry()==null){
			Collection<org.opengis.feature.Property>properties = feature.getProperties();
			for(org.opengis.feature.Property property : properties){
				try{						
					g = (Geometry)property.getValue();
				}catch(ClassCastException e){
					//do nothing
				}
				
			}
		}
		return g;
	}
	
	private void addProvenancePositionInfo(RDFProvenanceFeature rdfProvenanceFeature, String prefix, boolean conflated){
		
		String positionID = "";
		String positionType = "";
		
		if(conflated){
		
		positionID = RDFUtil.createConflatedPosition("Position" + createUUIDString(), prefix);
		
		positionType = RDFUtil.createConflatedPosition(prefix);
		
		}else{
			
			positionID = RDFUtil.createPosition("Position" + createUUIDString(), prefix);
			
			positionType =RDFUtil.createPosition(prefix);
			
		}
		rdfProvenanceFeature.putPropertyID("position", positionID);
		attributeTypeMap.put(positionID, positionType);
	}
	
	public void mapProperties(SimpleFeature target, SimpleFeature newFeature){
		
		//look for mappings target attribute name -> source attribute name
		Collection<Property> properties = target.getProperties();
		
		for (Property property : properties) {
			
			String propertyName = property.getName().getLocalPart();
			
			if(mappingsMap.keySet().contains(propertyName)){				
				
				String mappedPropertyName = (String)mappingsMap.get(propertyName);

				RDFProvenanceFeature targetProvenanceFeature = targetFeatureMap.get(target.getID());
				
				String propertyID = RDFUtil.createAttribute(propertyName + createUUIDString(), targetNamespace);
				
				targetProvenanceFeature.putPropertyID(propertyName, propertyID);
				
				attributeTypeMap.put(propertyID, RDFUtil.createAttribute(propertyName, targetNamespace));
				
				RDFProvenanceFeature resultProvenanceFeature = resultFeatureMap.get(newFeature.getID());
				
				propertyID = RDFUtil.createConflatedAttribute(mappedPropertyName + createUUIDString(), resultNamespace);
				
				resultProvenanceFeature.putPropertyID(propertyName, propertyID);
				
				attributeTypeMap.put(propertyID, RDFUtil.createConflatedAttribute(mappedPropertyName, resultNamespace));
				
				addPropertyValue(newFeature, mappedPropertyName, property.getValue());
			}
		}		
	}

	public String createUUIDString(){
		return "_" + UUID.randomUUID().toString().substring(0, 5);
	}
	
	public void addfixedAttributeValues(SimpleFeature sft){
		
		Iterator<Property> properties = sft.getProperties().iterator();
		
		while (properties.hasNext()) {
			Property property = (Property) properties.next();
			
			if(property.getName().getLocalPart().equals("identifier")){
				continue;
			}
			
			if(fixedAttributeValuesMap.keySet().contains(property.getName().getLocalPart())){
				addPropertyValue(sft, property.getName().getLocalPart(), fixedAttributeValuesMap.get(property.getName().getLocalPart()));
			}else if(!mappingsMap.values().contains(property.getName().getLocalPart()) && !fixedAttributeValuesMap.keySet().contains(property.getName().getLocalPart())){
				addDefaultValue(property);
			}			
			
		}
		
	}	
	
	public void addDefaultValue(Property property){
		
		if(property.getType().getBinding().isAssignableFrom(String.class)){
			property.setValue(default_String);
		}else if(property.getType().getBinding().isAssignableFrom(Double.class)){
			property.setValue(default_Double);
		}else if(property.getType().getBinding().isAssignableFrom(BigInteger.class)){
			property.setValue(default_BigInteger);
		}
	}
	
	public void addPropertyValue(SimpleFeature ft, String propertyName, Object propertyValueToAdd){
		
		Collection<Property> properties = ft.getProperties();
		
		for (Property property : properties) {
			if(property.getName().getLocalPart().equals(propertyName)){
				property.setValue(propertyValueToAdd);
				break;
			}
		}	
	}
	
	public void createRules(String rules){
		
		ObjectMapper objMapper = new ObjectMapper();

		try {
			Map<?, ?> map = objMapper.readValue(rules, Map.class);

			mappingsMap = (Map<?, ?>) map.get("mappings");
			fixedAttributeValuesMap = (Map<?, ?>) map.get("fixedAttributeValues");			
			
			LOGGER.debug(map.size() + "");
		} catch (JsonParseException e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage());
		} catch (JsonMappingException e){
		LOGGER.error(e.getMessage());
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
		}	
	}

	public String createRDFProvenance2(FeatureCollection<?,?> sourceFeatureCollection, FeatureCollection<?,?> targetFeatureCollection){
		
		createSourceProvenanceFeatureMap(sourceFeatureCollection.features());
		
		String rdfProvenance = createPrefixes(includeBundle, baseURL);		

		createAttributeTypeSubClassStatements(sourceFeatureCollection.features().next().getDefaultGeometryProperty().getType(), targetFeatureCollection.features().next().getDefaultGeometryProperty().getType());
		creatFeatureTypeSubClassStatements();
		createInvolvedFeatureCollectionsStatement();
		
		createFeatureMapProvenance(sourceFeatureMap.values(), sourceMemberStatementBuilder, RDFUtil.createMap(sourceNamespace));
		createFeatureMapProvenance(targetFeatureMap.values(), sourceMemberStatementBuilder, RDFUtil.createMap(targetNamespace));
		createFeatureMapProvenance(targetResultFeatureMap.values(), resultMemberStatementBuilder, RDFUtil.createConflatedMap(resultNamespace));

		createResultFeatureOriginProvenance();
		createFeatureAttributeProvenance(targetFeatureMap.values(), sourceFeatureAttributeStatementBuilder, targetNamespace);
		createFeatureAttributeProvenance(sourceFeatureMap.values(), sourceFeatureAttributeStatementBuilder, sourceNamespace);
		createFeatureAttributeProvenance(resultFeatureMap.values(), resultFeatureAttributeStatementBuilder, resultNamespace);
		createConflationExecutionProvUsedFeatures();
		createExecutionRelatedFeatures();
		createIndividualExecutionStatements();
		
		System.out.println(involvedFeatureCollectionsStatementBuilder);
		System.out.println(attributeTypeSubClassStatementBuilder);	
		System.out.println(featureTypeSubClassStatementBuilder);	
		System.out.println(sourceMemberStatementBuilder);
		System.out.println(sourceFeatureAttributeStatementBuilder);
		System.out.println(resultMemberStatementBuilder);
		System.out.println(resultFeatureAttributeStatementBuilder);
		System.out.println(featureOriginStatementBuilder);
		System.out.println(attributeOriginStatementBuilder);
		System.out.println(executionProvUsedFeaturesStatementBuilder);
		System.out.println(featuresGeneratedByExecutionStatementBuilder);
		System.out.println(featuresGeneratedAtStatementBuilder);
		System.out.println(qualifiedUsageStatementBuilder);
		System.out.println(qualifiedGenerationStatementBuilder);
		System.out.println(attributeTypeStatementBuilder);	
		System.out.println(individualExecutionStatementBuilder);	
		
		return rdfProvenance;
	}

	private void creatFeatureTypeSubClassStatements() {
		addTripleToStringBuilder(RDFUtil.createTriple(RDFUtil.createFeature("", sourceNamespace), RDFUtil.PREDICATE_RDFS_SUBCLASS_OF, RDFUtil.OWS_FEATURE, true), featureTypeSubClassStatementBuilder, true);
		addTripleToStringBuilder(RDFUtil.createTriple(RDFUtil.createFeature("", targetNamespace), RDFUtil.PREDICATE_RDFS_SUBCLASS_OF, RDFUtil.OWS_FEATURE, true), featureTypeSubClassStatementBuilder, true);
	}

	private void createInvolvedFeatureCollectionsStatement() {
		
		addTripleToStringBuilder(RDFUtil.createTriple(RDFUtil.createMap(sourceNamespace), RDFUtil.A, RDFUtil.OWS_FEATURE_COLLECTION, false), involvedFeatureCollectionsStatementBuilder, true);
		addTripleToStringBuilder(RDFUtil.createTriple("	", RDFUtil.PREDICATE_OWL_SAME_AS, sourceURL, true), involvedFeatureCollectionsStatementBuilder, true);
		addTripleToStringBuilder(RDFUtil.createTriple(RDFUtil.createMap(targetNamespace), RDFUtil.A, RDFUtil.OWS_FEATURE_COLLECTION, false), involvedFeatureCollectionsStatementBuilder, true);
		addTripleToStringBuilder(RDFUtil.createTriple("	", RDFUtil.PREDICATE_OWL_SAME_AS, targetURL, true), involvedFeatureCollectionsStatementBuilder, true);
		
		String conflatedMap = RDFUtil.createConflatedMap(resultNamespace);
		
		addTripleToStringBuilder(RDFUtil.createTriple(conflatedMap, RDFUtil.A, RDFUtil.OWS_FEATURE_COLLECTION, false), involvedFeatureCollectionsStatementBuilder, true);
		
		addTripleToStringBuilder(RDFUtil.createTriple("	", RDFUtil.PREDICATE_PROV_WAS_REVISION_OF, RDFUtil.createMap(sourceNamespace), false), involvedFeatureCollectionsStatementBuilder, true);
		
		addTripleToStringBuilder(RDFUtil.createTriple("	", RDFUtil.PREDICATE_PROV_GENERATED_AT_TIME, generatedAt + RDFUtil.XSD_DATE_TIME, true), involvedFeatureCollectionsStatementBuilder, true);
		
	}

	private void createIndividualExecutionStatements() {

		addTripleToStringBuilder(RDFUtil.createTriple(entityName, RDFUtil.A, RDFUtil.WPS_CONFLATION_EXECUTION, true), individualExecutionStatementBuilder, true);
		addTripleToStringBuilder(RDFUtil.createTriple(entityName, RDFUtil.PREDICATE_PROV_USED, type, true), individualExecutionStatementBuilder, true);
		addTripleToStringBuilder(RDFUtil.createTriple(entityName, RDFUtil.PREDICATE_PROV_STARTED_AT_TIME, startTime + RDFUtil.XSD_DATE_TIME, false), individualExecutionStatementBuilder, true);
		addTripleToStringBuilder(RDFUtil.createTriple("	", RDFUtil.PREDICATE_PROV_ENDED_AT_TIME, endTime + RDFUtil.XSD_DATE_TIME, true), individualExecutionStatementBuilder, true);
		
	}
	
	private void addTripleToStringBuilder(String triple, StringBuilder stringBuilder, boolean addNewline){
		
		stringBuilder.append(triple);
		
		if(addNewline){
			stringBuilder.append("\n");
		}
		
	}

	private void createExecutionRelatedFeatures() {
		
		Collection<RDFProvenanceFeature> sortCollection = sortCollection(targetResultFeatureMap.values());
		
		Iterator<RDFProvenanceFeature> iterator = sortCollection.iterator();
		
		RDFProvenanceFeature rdfProvenanceFeature = iterator.next();
		
		createExecutionRelatedStatements(entityName, rdfProvenanceFeature, true, false);
		
		while (iterator.hasNext()) {
			rdfProvenanceFeature = (RDFProvenanceFeature) iterator
					.next();
			
			createExecutionRelatedStatements(entityName, rdfProvenanceFeature, false, true);
			
		}
	}
	
	private void createConflationExecutionProvUsedFeatures() {
		
		Collection<RDFProvenanceFeature> sortCollection = sortCollection(targetResultFeatureMap.keySet());
		
		Iterator<RDFProvenanceFeature> iterator = sortCollection.iterator();
		
		RDFProvenanceFeature rdfProvenanceFeature = iterator.next();	

		String targetID = rdfProvenanceFeature.getID();
		
		String triple = RDFUtil.createConflationExecutionProvUsedFeaturesTriple(entityName, rdfProvenanceFeature.getID(), true, false);

		executionProvUsedFeaturesStatementBuilder.append(triple);
		executionProvUsedFeaturesStatementBuilder.append("\n");
		
		triple = RDFUtil.createQualifiedUsageTriple(entityName, targetID, rdfProvenanceFeature.getRole());
		
		qualifiedUsageStatementBuilder.append(triple);		
		qualifiedUsageStatementBuilder.append("\n");
		
		while (iterator.hasNext()) {
			rdfProvenanceFeature = (RDFProvenanceFeature) iterator
					.next();
			triple = RDFUtil.createConflationExecutionProvUsedFeaturesTriple(entityName, rdfProvenanceFeature.getID(), false, !iterator.hasNext());
			
			targetID = rdfProvenanceFeature.getID();
			
			executionProvUsedFeaturesStatementBuilder.append(triple);
			executionProvUsedFeaturesStatementBuilder.append("\n");
			
			triple = RDFUtil.createQualifiedUsageTriple(entityName, targetID, rdfProvenanceFeature.getRole());
			
			qualifiedUsageStatementBuilder.append(triple);		
			qualifiedUsageStatementBuilder.append("\n");
		}
	}
	
	private void createExecutionRelatedStatements(String entityName, RDFProvenanceFeature rdfProvenanceFeature, boolean firstStatement, boolean endStatement){
		
		String targetID = rdfProvenanceFeature.getID();
		String triple = RDFUtil.createFeaturesGeneratedByExecutionTriple(targetID, entityName);
		
		featuresGeneratedByExecutionStatementBuilder.append(triple);		
		featuresGeneratedByExecutionStatementBuilder.append("\n");	
		
		triple = RDFUtil.createFeaturesGeneratedAtTriple(targetID, dateformat.format(rdfProvenanceFeature.getGeneratedAt()));
		
		featuresGeneratedAtStatementBuilder.append(triple);		
		featuresGeneratedAtStatementBuilder.append("\n");
		
		triple = RDFUtil.createQualifiedGenerationTriple(entityName, targetID);
		
		qualifiedGenerationStatementBuilder.append(triple);		
		qualifiedGenerationStatementBuilder.append("\n");
	}

	private Collection<RDFProvenanceFeature> sortCollection(
			Collection<RDFProvenanceFeature> values) {
		
		RDFProvenanceFeature[] provenanceFeatureArray = values.toArray(new RDFProvenanceFeature[]{});
		
		List<RDFProvenanceFeature> rdfProvenanceFeatureList = Arrays.asList(provenanceFeatureArray);
		
		Collections.sort(rdfProvenanceFeatureList);
		
		return rdfProvenanceFeatureList;
	}

	private void createFeatureAttributeProvenance(
			Collection<RDFProvenanceFeature> values, StringBuilder featureAttributeStatementBuilder, String prefix) {
		
		Collection<RDFProvenanceFeature> sortCollection = sortCollection(values);
		
		Iterator<RDFProvenanceFeature> iterator = sortCollection.iterator();
		
		while (iterator.hasNext()) {
			RDFProvenanceFeature rdfProvenanceFeature = (RDFProvenanceFeature) iterator
					.next();
			
			Iterator<String> propertyIterator = rdfProvenanceFeature.getPropertyIDMap().keySet().iterator();
			
			featureAttributeStatementBuilder.append(RDFUtil.createFeatureTypeTriple(rdfProvenanceFeature.getID(), rdfProvenanceFeature.getFeatureType(), !propertyIterator.hasNext()));
			
			while (propertyIterator.hasNext()) {
				String propertyName = (String) propertyIterator.next();				
				
				if (propertyName.equalsIgnoreCase("Position")) {
					attributeTypeStatementBuilder.append(RDFUtil.createTriple(rdfProvenanceFeature.getPropertyID(propertyName), RDFUtil.A, attributeTypeMap.get(rdfProvenanceFeature.getPropertyID(propertyName)), true));
					
					featureAttributeStatementBuilder.append(RDFUtil.createFeatureHadGeometryTriple(rdfProvenanceFeature.getPropertyID(propertyName), prefix, !propertyIterator.hasNext()));
				}else{
					attributeTypeStatementBuilder.append(RDFUtil.createTriple(rdfProvenanceFeature.getPropertyID(propertyName), RDFUtil.A, attributeTypeMap.get(rdfProvenanceFeature.getPropertyID(propertyName)), true));
					
					featureAttributeStatementBuilder.append(RDFUtil.createFeatureHadPropertyTriple(rdfProvenanceFeature.getPropertyID(propertyName), prefix, !propertyIterator.hasNext()));
				}
			}
			attributeTypeStatementBuilder.append("\n");
			featureAttributeStatementBuilder.append("\n");
		}
		
	}

	private void createResultFeatureOriginProvenance(){
		
		Collection<RDFProvenanceFeature> sortCollection = sortCollection(targetResultFeatureMap.keySet());
		
		Iterator<RDFProvenanceFeature> targetResultFeatureIterator = sortCollection.iterator();
		
		while (targetResultFeatureIterator.hasNext()) {
			RDFProvenanceFeature targetRDFProvenanceFeature = (RDFProvenanceFeature) targetResultFeatureIterator
					.next();
			RDFProvenanceFeature resultRDFProvenanceFeature = targetResultFeatureMap.get(targetRDFProvenanceFeature);
			
			String triple = "";
			
			switch (resultRDFProvenanceFeature.getProvenanceType()) {
			case SAME_AS:
				triple = RDFUtil.createFeatureSameAsTriple(resultRDFProvenanceFeature.getID(), resultNamespace, targetRDFProvenanceFeature.getID(), sourceNamespace);
				break;
			case DERIVED_FROM:
				triple = RDFUtil.createFeatureDerivedFromTriple(resultRDFProvenanceFeature.getID(), resultNamespace, targetRDFProvenanceFeature.getID(), targetNamespace);
				break;
			case REVISION_OF:
				triple = RDFUtil.createFeatureRevisionOfTriple(resultRDFProvenanceFeature.getID(), resultNamespace, targetRDFProvenanceFeature.getID(), targetNamespace);
				break;
			default:
				break;
			}
			
			featureOriginStatementBuilder.append(triple);
			featureOriginStatementBuilder.append("\n");
			createResultAttributeOriginProvenance(targetRDFProvenanceFeature, resultRDFProvenanceFeature);
		}
	}
	
	private void createResultAttributeOriginProvenance(RDFProvenanceFeature targetRDFProvenanceFeature, RDFProvenanceFeature resultRDFProvenanceFeature){
		
		Iterator<String> propertyIterator = resultRDFProvenanceFeature.getPropertyIDMap().keySet().iterator();

		while (propertyIterator.hasNext()) {
			String propertyName = (String) propertyIterator.next();
			
			String triple = "";
			
			switch (resultRDFProvenanceFeature.getProvenanceType()) {
			case SAME_AS:
				triple = RDFUtil.createAttributeSameAsTriple(resultRDFProvenanceFeature.getPropertyID(propertyName), resultNamespace, targetRDFProvenanceFeature.getPropertyID(propertyName), sourceNamespace);
				break;
			case DERIVED_FROM:
				triple = RDFUtil.createAttributeDerivedFromTriple(resultRDFProvenanceFeature.getPropertyID(propertyName), resultNamespace, targetRDFProvenanceFeature.getPropertyID(propertyName), targetNamespace);
				break;
			case REVISION_OF:
				triple = RDFUtil.createAttributeRevisionOfTriple(resultRDFProvenanceFeature.getPropertyID(propertyName), resultNamespace, targetRDFProvenanceFeature.getPropertyID(propertyName), targetNamespace);
				break;
			default:
				break;
			}
			
			attributeOriginStatementBuilder.append(triple);
			attributeOriginStatementBuilder.append("\n");
			
		}
		
		
	}
	
	private void createFeatureMapProvenance(Collection<RDFProvenanceFeature> collection, StringBuilder stringBuilder, String mapName){
		
		Collection<RDFProvenanceFeature> sortCollection = sortCollection(collection);
		
		Iterator<RDFProvenanceFeature> sourceFeatureIterator = sortCollection.iterator();
		
		if(sourceFeatureIterator.hasNext()){
			
			String id = ((RDFProvenanceFeature) sourceFeatureIterator.next()).getID();
			
			stringBuilder.append(RDFUtil.createMapHadMemberFeatureTriple(mapName, id, true, false));
			
			while (sourceFeatureIterator.hasNext()) {
				id = ((RDFProvenanceFeature) sourceFeatureIterator.next()).getID();
				
				boolean endOfStatement = !sourceFeatureIterator.hasNext();
				
				stringBuilder.append(RDFUtil.createMapHadMemberFeatureTriple(mapName, id, false, endOfStatement));
				
			}
		}
		
		stringBuilder.append("\n");
	}
	
	private String createRDFProvenance(){
		
		String rdfProvenance = createPrefixes(includeBundle, baseURL);		
		
		if(includeBundle){
			rdfProvenance = rdfProvenance.concat("\n");
			rdfProvenance = rdfProvenance.concat(createBundle(attributeTo, generatedAt));
		}	
		
		rdfProvenance = rdfProvenance.concat("\n");
		
		rdfProvenance = rdfProvenance.concat(createProvEntityName(entityName));
		rdfProvenance = rdfProvenance.concat(createUsageInfo(dataset1Name, dataset1Role, dataset2Name, dataset2Role));
		rdfProvenance = rdfProvenance.concat(createQualifiedAssociation(agent, role, plan));	
		rdfProvenance = rdfProvenance.concat(createTimeUsage(startTime, endTime));		
		
		rdfProvenance = rdfProvenance.concat(createOutputMap(outputMapName, specializationOf, entityName, outputMapNameAttributedTo, entityName, outputMapQualifiedGenerationRole, outputMapGeneratedAtTime));
		
		rdfProvenance = rdfProvenance.concat(createAlgorithm(type, superType, algorithmGeneratedAt, algorithmAttributedTo));
		
		return rdfProvenance;
	}
	
	private String createAlgorithm(String type, String superType, String algorithmGeneratedAt, String algorithmAttributedTo){
		String algorithm = RDFUtil.RDF_NEW_ALGORITHM_TEMPLATE;
		
		algorithm = algorithm.replace(RDFUtil.PROV_ALGORITHM_TYPE_EXP, type);
		algorithm = algorithm.replace(RDFUtil.PROV_ALGORITHM_SUPER_TYPE_EXP, superType);
		algorithm = algorithm.replace(RDFUtil.PROV_ALGORITHM_GENERATED_AT_EXP, algorithmGeneratedAt);
		algorithm = algorithm.replace(RDFUtil.PROV_ALGORITHM_ATTRIBUTED_TO_EXP, algorithmAttributedTo);
		
		return algorithm;
	}
	
	private String createOutputMap(String name, String specializationOf, String entityName, String attributedTo, String activity, String role, String generatedAtTime){
		String outputMap = RDFUtil.RDF_PROV_OUTPUT_MAP_TEMPLATE;
		
		outputMap = outputMap.replace(RDFUtil.PROV_OUTPUT_MAP_NAME_EXP, name);
		outputMap = outputMap.replace(RDFUtil.PROV_OUTPUT_MAP_SPECIALIZATION_OF_EXP, specializationOf);
		outputMap = outputMap.replace(RDFUtil.PROV_RDF_ENTITY_NAME_EXP, entityName);
		outputMap = outputMap.replace(RDFUtil.PROV_ENTITY_ATTRIBUTED_TO_EXP, attributedTo);
		outputMap = outputMap.replace(RDFUtil.PROV_QUALIFIED_GENERATION_ACTIVITY_EXP, activity);
		outputMap = outputMap.replace(RDFUtil.PROV_QUALIFIED_GENERATION_ROLE_EXP, role);
		outputMap = outputMap.replace(RDFUtil.PROV_ENTITY_GENERATED_AT_EXP, generatedAtTime);
		
		return outputMap;
	}
	
	private String createTimeUsage(String startTime, String endTime){
		String timeUsage = RDFUtil.RDF_PROV_TIME_USAGE_TEMPLATE;
		
		timeUsage = timeUsage.replace(RDFUtil.PROV_RDF_STARTED_AT_TIME_EXP, startTime);
		timeUsage = timeUsage.replace(RDFUtil.PROV_RDF_ENDED_AT_TIME_EXP, endTime);
		
		return timeUsage;
	}
	
	private String createQualifiedAssociation(String agent, String role, String plan){
		String qualifiedAssociation = RDFUtil.RDF_PROV_QUALIFIED_ASSOCIATION_TEMPLATE;
		
		qualifiedAssociation = qualifiedAssociation.replace(RDFUtil.PROV_QUALIFIED_ASSOCIATION_AGENT_EXP, agent);
		qualifiedAssociation = qualifiedAssociation.replace(RDFUtil.PROV_QUALIFIED_ASSOCIATION_ROLE_EXP, role);
		qualifiedAssociation = qualifiedAssociation.replace(RDFUtil.PROV_QUALIFIED_ASSOCIATION_PLAN_EXP, plan);
		
		return qualifiedAssociation;
	}
	
	private String createQualifiedUsage(String datasetName, String datasetRole){
		String qualifiedUsage = RDFUtil.RDF_PROV_QUALIFIED_USAGE_TEMPLATE;
		
		qualifiedUsage = qualifiedUsage.replace(RDFUtil.PROV_QUALIFIED_USAGE_ENTITY, datasetName);
		qualifiedUsage = qualifiedUsage.replace(RDFUtil.PROV_QUALIFIED_USAGE_ROLE, datasetRole);
		
		return qualifiedUsage;
	}
	
	private String createUsed(String datasetName){
		String usage = RDFUtil.RDF_PROV_USED_TEMPLATE;
		
		usage = usage.replace(RDFUtil.PROV_USED_EXP, datasetName);
		
		return usage;
	}
	
	private String createUsageInfo(String dataset1Name, String dataset1Role, String dataset2Name, String dataset2Role){
		String usage = createUsed(dataset1Name);
		
		usage = usage.concat(createUsed(dataset2Name));
		
		usage = usage.concat(createQualifiedUsage(dataset1Name, dataset1Role));
		usage = usage.concat(createQualifiedUsage(dataset2Name, dataset2Role));
		
		return usage;
	}
	
	private String createProvEntityName(String entityName){
		String provEntityName = RDFUtil.RDF_PROV_TEMPLATE;
		
		provEntityName = provEntityName.replace(RDFUtil.PROV_RDF_ENTITY_NAME_EXP, entityName);
		
		return provEntityName;
	}
	
	private String createPrefixes( boolean includeBase, String baseURL){	
		String prefixes = "";
		
		if(includeBase){
			prefixes = RDFUtil.BASE.replace(RDFUtil.BASE_URL_EXP, baseURL);
			prefixes = prefixes.concat(RDFUtil.PROV_PREFIX_F2N);
		}else{			
			prefixes = RDFUtil.PROV_PREFIX_F2N;			
		}
		prefixes = prefixes.concat(RDFUtil.PROV_PREFIX_FOAF);
		prefixes = prefixes.concat(RDFUtil.PROV_PREFIX_OWS10);
		prefixes = prefixes.concat(RDFUtil.PROV_PREFIX_PROV);
		prefixes = prefixes.concat(RDFUtil.PROV_PREFIX_RDFS);
		prefixes = prefixes.concat(RDFUtil.PROV_PREFIX_XSD);		
		
		return prefixes;
	}
	
	private String createBundle(String attributeTo, String generatedAt){	
		
		String bundle = RDFUtil.BUNDLE_template;
		
		bundle = bundle.replace(RDFUtil.PROV_BUNDLE_ATTRIBUTED_TO_EXP, attributeTo);
		bundle = bundle.replace(RDFUtil.PROV_BUNDLE_GENERATED_AT_EXP, generatedAt);
		
		return bundle;
		
	}
	
	@Override
	public List<String> getErrors() {
		return null;
	}

	@Override
	public Class<?> getInputDataType(String id) {
		if(id.equals(source_id) || id.equals(target_id)){
			return GTVectorDataBindingWithSourceURL.class;
		}else if(id.equals(rules_id)){
			return LiteralStringBinding.class;
		}
		return null;
	}

	@Override
	public Class<?> getOutputDataType(String id) {
		if(id.equals(output_id_conflated_result)){
			return GTVectorDataBinding.class;
		}else if(id.equals(output_id_provenance)){
			return LiteralStringBinding.class;
		}		
		return null;
	}

}
