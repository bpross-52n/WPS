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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vividsolutions.jts.geom.Geometry;

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
	private GregorianCalendar calendar = new GregorianCalendar(2014, 2, 26, 14, 23);	
	private String algorithmGeneratedAt = dateformat.format(calendar.getTime());		
	private String baseURL = "http://www.opengis.net/ogc/ows10/52n/provenance-52n-march.ttl";
	private String attributeTo = "f2n:arne";
	private String generatedAt = dateformat.format(new GregorianCalendar().getTime());
	
	private final String type = "f2n:Kinda_Generic_ConflationProcess_v110";
	private String superType = "f2n:ConflationAlgorithm_52N";	
	private String algorithmAttributedTo = "f2n:benjamin";		
	private String agent = "f2n:benjamin";
	private String role = "ows10:developer";
	private String plan = "f2n:Kinda_Generic_ConflationProcess_v110";	
	private String specializationOf = "f2n:OWS10conflatedmap_52N";
	private String outputMapNameAttributedTo = "f2n:benjamin";
	private String outputMapQualifiedGenerationRole = "ows10:conflatedMapOutput";
	
	private String entityName = "f2n:WPS_52N_";	
	private String outputMapName = "f2n:OWS10conflatedmap_52N_";
	private String startTime = generatedAt;
	private String outputMapGeneratedAtTime = generatedAt;
	private String outputMapEntityName = entityName;
	private String outputMapActivity = entityName;
	
	private String dataset1Name = "ows10:SC_USGS";
	private String dataset1Role = "ows10:referenceMapSource";
	private String dataset2Name = "ows10:SC_NGA";
	private String dataset2Role = "ows10:referenceMapSource";
	private String datasetUnknownName = "ows10:SC_Unknown";
	private String datasetUnknownRole = "ows10:unknownMapSource";
	private String endTime = dateformat.format(new GregorianCalendar().getTime());
	
	private Properties properties;
	
	private Map<?, ?> mappingsMap;
	private Map<?, ?> fixedAttributeValuesMap;
	
	private StringBuilder featureTypeStatementBuilder = new StringBuilder(); 
	private StringBuilder sourceMemberStatementBuilder = new StringBuilder(); 
	private StringBuilder targetMemberStatementBuilder = new StringBuilder(); 
	private StringBuilder attributeOriginStatementBuilder = new StringBuilder(); 
	private StringBuilder generatedByStatementBuilder = new StringBuilder(); 
	private StringBuilder featureOriginStatementBuilder = new StringBuilder();
	private StringBuilder featureAttributeStatementBuilder = new StringBuilder();
	private StringBuilder resultFeatureAttributeStatementBuilder = new StringBuilder();
	
	private Map<String, RDFProvenanceFeature> targetFeatureMap;
	private Map<String, RDFProvenanceFeature> sourceFeatureMap;
	private Map<String, RDFProvenanceFeature> resultFeatureMap;
	private Map<RDFProvenanceFeature, RDFProvenanceFeature> targetResultFeatureMap;
	
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
		sourceFeatureMap = new HashMap<String, RDFProvenanceFeature>();
		targetFeatureMap = new HashMap<String, RDFProvenanceFeature>();
		resultFeatureMap = new HashMap<String, RDFProvenanceFeature>();
		targetResultFeatureMap = new HashMap<RDFProvenanceFeature, RDFProvenanceFeature>();
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
		
		IData firstInputData = dataList.get(0);
		
		GTVectorDataBindingWithSourceURL firstInputBindingWithSourceURL = null;
		
		if(firstInputData instanceof GTVectorDataBindingWithSourceURL){
			firstInputBindingWithSourceURL = (GTVectorDataBindingWithSourceURL)firstInputData;
		}
		
		FeatureCollection<?, ?> featureCollection = firstInputBindingWithSourceURL.getPayload();
		
		LOGGER.info(firstInputBindingWithSourceURL.getSourceURL());

		String featureCollectionNamespace = featureCollection.getSchema().getName().getNamespaceURI();

		featureCollectionNamespace = featureCollectionNamespace.replace("http://", "");
		
		if(properties.containsKey(featureCollectionNamespace)){
			
			String[] nameAndRole = properties.get(featureCollectionNamespace).toString().split(",");
			
			dataset1Name = nameAndRole[0];
			dataset1Role = nameAndRole[1];
		}else{
			dataset1Name = datasetUnknownName;
			dataset1Role = datasetUnknownRole;
		}
		
		if (inputData == null || !inputData.containsKey(target_id)) {
			throw new RuntimeException(
					"Error while allocating input parameters");
		}
		
		List<IData> dataList1 = inputData.get(target_id);
		if (dataList1 == null || dataList1.size() != 1) {
			throw new RuntimeException(
					"Error while allocating input parameters");
		}
		
		IData firstInputData1 = dataList1.get(0);
		
		GTVectorDataBindingWithSourceURL firstInputBindingWithSourceURL1 = null;
		
		if(firstInputData1 instanceof GTVectorDataBindingWithSourceURL){
			firstInputBindingWithSourceURL1 = (GTVectorDataBindingWithSourceURL)firstInputData1;
		}
		
		FeatureCollection<?, ?> featureCollection1 = firstInputBindingWithSourceURL1.getPayload();
		
		LOGGER.info(firstInputBindingWithSourceURL1.getSourceURL());

		FeatureIterator<?> iter1 = featureCollection1.features();
		
		String featureCollection1Namespace = featureCollection1.getSchema().getName().getNamespaceURI();
		
		featureCollection1Namespace = featureCollection1Namespace.replace("http://", "");
		
		if(properties.containsKey(featureCollection1Namespace)){
			
			String[] nameAndRole = properties.get(featureCollection1Namespace).toString().split(",");
			
			dataset2Name = nameAndRole[0];
			dataset2Role = nameAndRole[1];
		}else{
			dataset2Name = datasetUnknownName;
			dataset2Role = datasetUnknownRole;
		}
		
		/*
		 * get source description filter 
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

		FeatureType ft = featureCollection.features().next().getType();
		
		createSourceProvenanceFeatureMap(featureCollection.features());
		
		List<SimpleFeature> oldFeatures = Arrays.asList(featureCollection.toArray(new SimpleFeature[]{}));
		
		List<SimpleFeature> newFeatures = new ArrayList<SimpleFeature>();
		
		newFeatures.addAll(oldFeatures);
		
		runConflation(iter1, newFeatures, ft);
		
		FeatureCollection<?, ?> result = new ListFeatureCollection((SimpleFeatureType)ft, newFeatures);
		
		Date endDate = new GregorianCalendar().getTime();
		
		long endMillis = System.currentTimeMillis();
		
		endTime = dateformat.format(endDate);
		
		LOGGER.info("Conflation process took " + (endMillis - startMillis)/ 1000 + " seconds");
		
		String rdfProvenance = createRDFProvenance2();
		
		Map<String, IData> resultMap = new HashMap<String, IData>(2);
		
		resultMap.put(output_id_conflated_result, new GTVectorDataBinding(result));
		resultMap.put(output_id_provenance, new LiteralStringBinding(rdfProvenance));
		
		return resultMap;
	}

	public void runConflation(FeatureIterator<?> iter, List<SimpleFeature> newFeatures, FeatureType ft){

		SimpleFeatureType sourceSimpleFeatureType = (SimpleFeatureType)ft;
		
		SimpleFeature targetFeature = (SimpleFeature) iter.next();
		
//		String targetID = targetFeature.getIdentifier().getID();
//		
//		targetMemberStatementBuilder.append(RDFUtil.createMapHadMemberFeatureTriple("USGS", "usgs", targetID, "usgs", true, false));
		
		SimpleFeature newFeature = createNewFeature(targetFeature, sourceSimpleFeatureType);
		
		newFeatures.add(newFeature);
		
		while (iter.hasNext()) {
			Object o = iter.next();
			if (o instanceof SimpleFeature) {
				targetFeature = (SimpleFeature) o;
				
//				targetID = targetFeature.getIdentifier().getID();
//				
//				boolean endOfStatement = !iter.hasNext();
//				
//				targetMemberStatementBuilder.append(RDFUtil.createMapHadMemberFeatureTriple("USGS", "usgs", targetID, "usgs", false, endOfStatement));
				
				newFeature = createNewFeature(targetFeature, sourceSimpleFeatureType);
				
				newFeatures.add(newFeature);
			}

		}
	}
	
	public void createSourceProvenanceFeatureMap(FeatureIterator<?> features) {
		
		while (features.hasNext()) {
			SimpleFeature sf = (SimpleFeature) features.next();
			
			RDFProvenanceFeature provenanceFeature = new RDFProvenanceFeature(sf.getID());
			
			provenanceFeature.setProvenanceType(ProvenanceType.SAME_AS);
			
			sourceFeatureMap.put(sf.getID(), provenanceFeature);
			
			targetResultFeatureMap.put(provenanceFeature, provenanceFeature);
			
		}
		
	}
	
	private SimpleFeature createNewFeature(SimpleFeature targetFeature, SimpleFeatureType sourceSimpleFeatureType){
		String targetID = targetFeature.getIdentifier().getID();
		
		SimpleFeature newFeature = (SimpleFeature) GTHelper.createFeature2(targetID, (Geometry) targetFeature.getDefaultGeometry(), sourceSimpleFeatureType);
		
		RDFProvenanceFeature targetProvenanceFeature = new RDFProvenanceFeature(targetID);
		
		addProvenancePositionInfo(targetProvenanceFeature);
		
		targetFeatureMap.put(targetFeature.getID(), targetProvenanceFeature);
		
		RDFProvenanceFeature resultProvenanceFeature = new RDFProvenanceFeature(targetID);
		
		addProvenancePositionInfo(resultProvenanceFeature);
		
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
	
	private void addProvenancePositionInfo(RDFProvenanceFeature rdfProvenanceFeature){
		rdfProvenanceFeature.putPropertyID("position", "Position" + createUUIDString());
	}
	
	public void mapProperties(SimpleFeature target, SimpleFeature newFeature){
		
		//look for mappings target attribute name -> source attribute name
		Collection<Property> properties = target.getProperties();
		
		String entity1 = RDFUtil.USGS_FEATURE + target.getID();
		
//		System.out.println(RDFUtil.createTriple(entity1, RDFUtil.A, RDFUtil.USGS_FEATURE, false));
//		
//		System.out.println(RDFUtil.createTriple("", RDFUtil.PREDICATE_OWS_HAD_GEOMETRY, RDFUtil.USGS_POSITION + UUID.randomUUID().toString().substring(0, 5), false));
		
		for (Property property : properties) {
			
			String propertyName = property.getName().getLocalPart();
			
			if(mappingsMap.keySet().contains(propertyName)){				
				
				String mappedPropertyName = (String)mappingsMap.get(propertyName);
				
//				System.out.println(RDFUtil.createTriple("", RDFUtil.PREDICATE_OWL_HAD_GEOMETRY, RDFUtil.USGS_ + UUID.randomUUID().toString().substring(0, 5), false));
				
				RDFProvenanceFeature targetProvenanceFeature = targetFeatureMap.get(target.getID());
				
				targetProvenanceFeature.putPropertyID(propertyName, propertyName + createUUIDString());
				
				RDFProvenanceFeature resultProvenanceFeature = resultFeatureMap.get(newFeature.getID());
				
				resultProvenanceFeature.putPropertyID(propertyName, mappedPropertyName + createUUIDString());
				
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

	public String createRDFProvenance2(){
		
		String rdfProvenance = createPrefixes(includeBundle, baseURL);		
		
		createSourceFeatureMapProvenance();
		createTargetFeatureMapProvenance();
		createResultFeatureOriginProvenance();
		createFeatureAttributeProvenance(targetFeatureMap.values(), "usgs");
		createFeatureAttributeProvenance(resultFeatureMap.values(), "usgs");
		
		System.out.println(featureOriginStatementBuilder);
		System.out.println(attributeOriginStatementBuilder);
		System.out.println(featureAttributeStatementBuilder);
		
		return rdfProvenance;
	}
	
	private void createFeatureAttributeProvenance(
			Collection<RDFProvenanceFeature> values, String prefix) {
		
		Iterator<RDFProvenanceFeature> iterator = values.iterator();
		
		while (iterator.hasNext()) {
			RDFProvenanceFeature rdfProvenanceFeature = (RDFProvenanceFeature) iterator
					.next();
			
			Iterator<String> propertyIterator = rdfProvenanceFeature.getPropertyIDMap().keySet().iterator();
			
			featureAttributeStatementBuilder.append(RDFUtil.createFeatureTypeTriple(rdfProvenanceFeature.getID(), prefix, "", prefix, !propertyIterator.hasNext()));
			
			while (propertyIterator.hasNext()) {
				String propertyName = (String) propertyIterator.next();
				
				if (propertyName.contains("Position")) {				
					featureAttributeStatementBuilder.append(RDFUtil.createFeatureHadGeometryTriple(rdfProvenanceFeature.getPropertyID(propertyName), prefix, !propertyIterator.hasNext()));
				}else{
					featureAttributeStatementBuilder.append(RDFUtil.createFeatureHadPropertyTriple(rdfProvenanceFeature.getPropertyID(propertyName), prefix, !propertyIterator.hasNext()));
				}
			}
			
			featureAttributeStatementBuilder.append("\n");
		}
		
	}

	private void createResultFeatureOriginProvenance(){
		Iterator<RDFProvenanceFeature> targetResultFeatureIterator = targetResultFeatureMap.keySet().iterator();
		
		while (targetResultFeatureIterator.hasNext()) {
			RDFProvenanceFeature targetRDFProvenanceFeature = (RDFProvenanceFeature) targetResultFeatureIterator
					.next();
			RDFProvenanceFeature resultRDFProvenanceFeature = targetResultFeatureMap.get(targetRDFProvenanceFeature);
			
			String triple = "";
			
			switch (resultRDFProvenanceFeature.getProvenanceType()) {
			case SAME_AS:
				triple = RDFUtil.createFeatureSameAsTriple(resultRDFProvenanceFeature.getID(), "nga_conf", targetRDFProvenanceFeature.getID(), "nga");
				break;
			case DERIVED_FROM:
				triple = RDFUtil.createFeatureDerivedFromTriple(resultRDFProvenanceFeature.getID(), "nga_conf", targetRDFProvenanceFeature.getID(), "usgs");
				break;
			case REVISION_OF:
				triple = RDFUtil.createFeatureRevisionOfTriple(resultRDFProvenanceFeature.getID(), "nga_conf", targetRDFProvenanceFeature.getID(), "usgs");
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
				triple = RDFUtil.createAttributeSameAsTriple(resultRDFProvenanceFeature.getPropertyID(propertyName), "nga_conf", targetRDFProvenanceFeature.getPropertyID(propertyName), "nga");
				break;
			case DERIVED_FROM:
				triple = RDFUtil.createAttributeDerivedFromTriple(resultRDFProvenanceFeature.getPropertyID(propertyName), "nga_conf", targetRDFProvenanceFeature.getPropertyID(propertyName), "usgs");
				break;
			case REVISION_OF:
				triple = RDFUtil.createAttributeRevisionOfTriple(resultRDFProvenanceFeature.getPropertyID(propertyName), "nga_conf", targetRDFProvenanceFeature.getPropertyID(propertyName), "usgs");
				break;
			default:
				break;
			}
			
			attributeOriginStatementBuilder.append(triple);
			attributeOriginStatementBuilder.append("\n");
			
		}
		
		
	}
	
	private void createSourceFeatureMapProvenance(){
		Iterator<String> sourceFeatureIterator = sourceFeatureMap.keySet().iterator();
		
		if(sourceFeatureIterator.hasNext()){
			
			String id = (String) sourceFeatureIterator.next();
			
			sourceMemberStatementBuilder.append(RDFUtil.createMapHadMemberFeatureTriple("NGA", "nga", id, "nga", true, false));
			
			while (sourceFeatureIterator.hasNext()) {
				id = (String) sourceFeatureIterator.next();
				
				boolean endOfStatement = !sourceFeatureIterator.hasNext();
				
				sourceMemberStatementBuilder.append(RDFUtil.createMapHadMemberFeatureTriple("NGA", "nga", id, "nga", false, endOfStatement));
				
			}
		}
		
		System.out.println(sourceMemberStatementBuilder);
	}
	
	private void createTargetFeatureMapProvenance(){
		Iterator<String> targetFeatureIterator = targetFeatureMap.keySet().iterator();
		
		if(targetFeatureIterator.hasNext()){
			
			String id = (String) targetFeatureIterator.next();
			
			targetMemberStatementBuilder.append(RDFUtil.createMapHadMemberFeatureTriple("USGS", "usgs", id, "usgs", true, false));
			
			while (targetFeatureIterator.hasNext()) {
				id = (String) targetFeatureIterator.next();
				
				boolean endOfStatement = !targetFeatureIterator.hasNext();
				
				targetMemberStatementBuilder.append(RDFUtil.createMapHadMemberFeatureTriple("USGS", "usgs", id, "usgs", false, endOfStatement));
				
			}
		}
		
		System.out.println(targetMemberStatementBuilder);
	}
	
	public String createRDFProvenance(){
		
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
