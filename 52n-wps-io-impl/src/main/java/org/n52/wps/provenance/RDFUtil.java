package org.n52.wps.provenance;

public class RDFUtil {
	
	public static final String XSD_DATE_TIME = "^^xsd:dateTime";
	
	public static final String BASE_URL_EXP = "%BASE_URL%";
	public static final String PROV_BUNDLE_EXP = "%BUNDLE%";
	public static final String PROV_BUNDLE_ATTRIBUTED_TO_EXP = "%BUNDLE_ATTRIBUTED_TO%";
	public static final String PROV_BUNDLE_GENERATED_AT_EXP = "%BUNDLE_GENERATED_AT%";
	public static final String PROV_RDF_ENTITY_NAME_EXP = "%RDF_ENTITY_NAME%";
	public static final String PROV_RDF_STARTED_AT_TIME_EXP = "%STARTED_AT_TIME%";
	public static final String PROV_RDF_ENDED_AT_TIME_EXP = "%ENDED_AT_TIME%";
	public static final String PROV_ENTITY_GENERATED_AT_EXP = "%ENTITY_GENERATED_AT%";
	public static final String PROV_GENERATED_AT_EXP = "%GENERATED_AT_TIME%";
	public static final String PROV_ALGORITHM_TYPE_EXP = "%ALGORITHM_TYPE%";
	public static final String PROV_ALGORITHM_SUPER_TYPE_EXP = "%ALGORITHM_SUPER_TYPE%";
	public static final String PROV_ALGORITHM_GENERATED_AT_EXP = "%ALGORITHM_GENERATED_AT%";
	public static final String PROV_ALGORITHM_ATTRIBUTED_TO_EXP = "%ALGORITHM_ATTRIBUTED_TO%";
	public static final String PROV_ENTITY_ATTRIBUTED_TO_EXP = "%ENTITY_ATTRIBUTED_TO%";
	public static final String PROV_CONFLATED_MAP_NAME_EXP = "%CONFLATED_MAP_NAME%";
	public static final String PROV_QUALIFIED_ASSOCIATION_AGENT_EXP = "%QUALIFIED_ASSOCIATION_AGENT%";
	public static final String PROV_QUALIFIED_ASSOCIATION_ROLE_EXP = "%QUALIFIED_ASSOCIATION_ROLE%";
	public static final String PROV_QUALIFIED_ASSOCIATION_PLAN_EXP = "%QUALIFIED_ASSOCIATION_PLAN%";
	public static final String PROV_QUALIFIED_GENERATION_ACTIVITY_EXP = "%QUALIFIED_GENERATION_ACTIVITY%";
	public static final String PROV_QUALIFIED_GENERATION_ROLE_EXP = "%QUALIFIED_GENERATION_ROLE%";
	public static final String PROV_OUTPUT_MAP_NAME_EXP = "%OUTPUT_MAP_NAME%";
	public static final String PROV_OUTPUT_MAP_SPECIALIZATION_OF_EXP = "%OUTPUT_MAP_SPECIALIZATION_OF%";	
	public static final String PROV_USED_EXP = "%USED_EXP%";
	public static final String PROV_QUALIFIED_USAGE_ENTITY = "QUALIFIED_USAGE_ENTITY";
	public static final String PROV_QUALIFIED_USAGE_ROLE = "QUALIFIED_USAGE_ROLE";
	
	public static final String DATASET_EXP = "%DATASET_EXP%";
	public static final String DATASET_URL_EXP = "%DATASET_URL_EXP%";

	
	public static final String PREFIX_USGS_DATA = "usgs_data";
	public static final String PREFIX_NGA_DATA = "nga_data";
	public static final String PREFIX_NGA_CONF = "nga_conf";
	public static final String PREFIX_UNKNOWN_DATA = "unknown_data";
	public static final String PREFIX_UNKNOWN_CONF = "unknown_conf";
	
	public static final String PREFIX_F2N = "f2n";
	public static final String PREFIX_RDFS = "rdfs";
	public static final String PREFIX_FOAF = "foaf";
	public static final String PREFIX_PROV = "prov";
	public static final String PREFIX_XSD = "xsd";
	public static final String PREFIX_OWS10 = "ows10";
	public static final String PREFIX_OWS = "ows";
	public static final String PREFIX_NGA = "nga";
	public static final String PREFIX_OWL = "owl";
	
	public static final String BASE = "@base <" + BASE_URL_EXP + "> .\n";
	public static final String PROV_PREFIX_F2N = "@prefix " + PREFIX_F2N + ": <http://www.opengis.net/ogc/ows10/ows10-52n-ontology/> .\n";
	public static final String PROV_PREFIX_RDFS = "@prefix " + PREFIX_RDFS + ": <http://www.w3.org/2000/01/rdf-schema#> .\n";
	public static final String PROV_PREFIX_FOAF = "@prefix " + PREFIX_FOAF + ": <http://xmlns.com/foaf/0.1/> .\n";
	public static final String PROV_PREFIX_PROV = "@prefix " + PREFIX_PROV + ": <http://www.w3.org/ns/prov#> .\n";
	public static final String PROV_PREFIX_XSD = "@prefix " + PREFIX_XSD + ": <http://www.w3.org/2001/XMLSchema#> .\n";
	public static final String PROV_PREFIX_OWS10 = "@prefix " + PREFIX_OWS10 + ": <http://www.opengis.net/ogc/ows10/ows10-core-ontology/> .\n";
	public static final String PROV_PREFIX_OWS = "@prefix " + PREFIX_OWS + ": <http://www.opengis.net/ogc/ows/ows-core-ontology/> .\n";
	public static final String PROV_PREFIX_NGA = "@prefix " + PREFIX_NGA + ": <http://www.opengis.net/ogc/ows10/ows10-nga-ontology/> .\n";
	public static final String PROV_PREFIX_OWL = "@prefix " + PREFIX_OWL + ":  <http://www.w3.org/2002/07/owl#> .\n";
	
	public static final String PROV_PREFIX_USGS_DATA = "@prefix " + PREFIX_USGS_DATA + ":  <http://www.usgs.gov/projects/ows8> .\n";
	public static final String PROV_PREFIX_NGA_DATA = "@prefix " + PREFIX_NGA_DATA + ":  <http://metadata.dod.mil/mdr/ns/GSIP/3.0/tds/3.0> .\n";
	public static final String PROV_PREFIX_NGA_CONF = "@prefix " + PREFIX_NGA_CONF + ":  <http://metadata.dod.mil/mdr/ns/GSIP/3.0/tds/3.0> .\n";
	public static final String PROV_PREFIX_UNKKOWN_DATA = "@prefix " + PREFIX_UNKNOWN_DATA + ": <http://www.opengis.net/ogc/unknown_dataset/> .\n";
	public static final String PROV_PREFIX_UNKKOWN_CONF = "@prefix " + PREFIX_UNKNOWN_CONF + ": <http://www.opengis.net/ogc/unknown_conf-dataset/> .\n";
	
	public static final String LINE_ENDING = ";";
	public static final String TRIPLE_ENDING = ".";
	public static final String A = "a";	
	public static final String USGS_FEATURE = PREFIX_USGS_DATA + ":USGS_Feature";	
	public static final String NGA_FEATURE = PREFIX_USGS_DATA + ":NGA_Feature";
	
	public static final String USGS_POSITION = PREFIX_USGS_DATA + ":USGS_Position";
	public static final String NGA_POSITION = PREFIX_NGA_DATA + ":NGA_Position";
	
	public static final String CONFLATED_MAP = ":ConflatedMap";
	public static final String CONFLATED_MAP_OUTPUT = PREFIX_OWS10 + ":ConflatedMapOutput";
	public static final String CONFLATED_MAP_FEATURE = ":ConflatedMap_Feature";
	public static final String PROV_USAGE = PREFIX_PROV + ":Usage";
	public static final String PROV_GENERATION = PREFIX_PROV + ":Generation";
	public static final String OWS_PROPERTY = PREFIX_OWS + ":Property";
	public static final String OWS_FEATURE = PREFIX_OWS + ":Feature";
	public static final String OWS_POINT = PREFIX_OWS + ":Point";
	public static final String OWS_LINE = PREFIX_OWS + ":Line";
	public static final String OWS_POLYGON = PREFIX_OWS + ":Polygon";
	public static final String OWS_FEATURE_COLLECTION = PREFIX_OWS + ":FeatureCollection";
	public static final String WPS_CONFLATION_EXECUTION = PREFIX_F2N + ":52N_WPSConflationExecution";
	
	
	
	public static final String PREDICATE_OWL_SAME_AS = PREFIX_OWL + ":sameAs";
	public static final String PREDICATE_OWS_HAD_GEOMETRY = PREFIX_OWS + ":hadGeometry";
	public static final String PREDICATE_OWS_HAD_PROPERTY = PREFIX_OWS + ":hadProperty";
	public static final String PREDICATE_OWL_HAD_MEMBER = PREFIX_OWL + ":hadMember ";
	public static final String PREDICATE_PROV_DERIVED_FROM = PREFIX_PROV + ":wasDerivedFrom";
	public static final String PREDICATE_PROV_HAD_MEMBER = PREFIX_PROV + ":hadMember";
	public static final String PREDICATE_PROV_WAS_REVISION_OF = PREFIX_PROV + ":wasRevisionOf";
	public static final String PREDICATE_PROV_USED = PREFIX_PROV + ":used";
	public static final String PREDICATE_PROV_STARTED_AT_TIME = PREFIX_PROV + ":startedAtTime";
	public static final String PREDICATE_PROV_ENDED_AT_TIME = PREFIX_PROV + ":endedAtTime";
	public static final String PREDICATE_PROV_WAS_GENERATED_BY = PREFIX_PROV + ":wasGeneratedBy";
	public static final String PREDICATE_PROV_GENERATED_AT_TIME = PREFIX_PROV + ":generatedAtTime";
	public static final String PREDICATE_PROV_QUALIFIED_USAGE = PREFIX_PROV + ":qualifiedUsage";
	public static final String PREDICATE_PROV_QUALIFIED_GENERATION = PREFIX_PROV + ":qualifiedGeneration";
	public static final String PREDICATE_PROV_ENTITY = PREFIX_PROV + ":entity";
	public static final String PREDICATE_PROV_HAD_ROLE = PREFIX_PROV + ":hadRole";
	public static final String PREDICATE_RDFS_SUBCLASS_OF = PREFIX_RDFS + ":subClassOf";
	public static final String PREDICATE_PROV_ACTIVITY = PREFIX_PROV + ":actity";
	
	
	public static final String BUNDLE_template = "<> a prov:Bundle;\n" +
        "prov:wasAttributedTo " + PROV_BUNDLE_ATTRIBUTED_TO_EXP + " ;\n" +
            "prov:wasGeneratedAt \"" + PROV_BUNDLE_GENERATED_AT_EXP + "\"^^xsd:dateTime .\n";
	
	public static final String RDF_PROV_USED_TEMPLATE = "prov:used  " + PROV_USED_EXP + ";\n";
	
	public static final String RDF_PROV_TEMPLATE = PROV_RDF_ENTITY_NAME_EXP + "\n"
			+ "a ows10:WPS ;\n";
	
	public static final String RDF_PROV_QUALIFIED_USAGE_TEMPLATE = "prov:qualifiedUsage [\n"
			+ "                  a prov:Usage ;\n"
			+ "                  prov:entity   " + PROV_QUALIFIED_USAGE_ENTITY + " ;\n"
			+ "                  prov:hadRole  " + PROV_QUALIFIED_USAGE_ROLE + " ] ;\n";
	
	public static final String RDF_PROV_QUALIFIED_ASSOCIATION_TEMPLATE = "prov:qualifiedAssociation [\n"
			+ "                  a  prov:Association ;\n"
			+ "                  prov:agent    " + PROV_QUALIFIED_ASSOCIATION_AGENT_EXP + " ;\n"
			+ "                  prov:hadRole  " + PROV_QUALIFIED_ASSOCIATION_ROLE_EXP + " ;\n"
			+ "                  prov:hadPlan  " + PROV_QUALIFIED_ASSOCIATION_PLAN_EXP + " ] ;\n";

    public static final String RDF_PROV_TIME_USAGE_TEMPLATE = "prov:startedAtTime \"" + PROV_RDF_STARTED_AT_TIME_EXP + "\"^^xsd:dateTime ;\n"
			+ "prov:endedAtTime   \"" + PROV_RDF_ENDED_AT_TIME_EXP + "\"^^xsd:dateTime .\n"
			+ "\n";
	
	public static final String RDF_PROV_OUTPUT_MAP_TEMPLATE = PROV_OUTPUT_MAP_NAME_EXP + "\n"
			+ "            prov:specializationOf " + PROV_OUTPUT_MAP_SPECIALIZATION_OF_EXP + " ;\n"
			+ "prov:wasGeneratedBy " + PROV_RDF_ENTITY_NAME_EXP + " ;\n"
			+ "prov:wasAttributedTo " + PROV_ENTITY_ATTRIBUTED_TO_EXP + " ;\n"
			+ "prov:qualifiedGeneration [\n"
			+ "                   a prov:Generation ;\n"
			+ "                   prov:activity " + PROV_QUALIFIED_GENERATION_ACTIVITY_EXP + " ;\n"
			+ "                   prov:hadRole  " + PROV_QUALIFIED_GENERATION_ROLE_EXP + " ] ;\n"
			+ "prov:generatedAtTime \"" + PROV_ENTITY_GENERATED_AT_EXP + "\"^^xsd:dateTime .\n"
			+ "\n";
	
	public static final String RDF_NEW_ALGORITHM_TEMPLATE = PROV_ALGORITHM_TYPE_EXP + " a " + PROV_ALGORITHM_SUPER_TYPE_EXP + " ;\n"
			+ "prov:generatedAtTime \"" + PROV_ALGORITHM_GENERATED_AT_EXP + "\"^^xsd:dateTime ;\n"
			+ "prov:wasAttributedTo " + PROV_ALGORITHM_ATTRIBUTED_TO_EXP + " .\n";
	
	public static final String RDF_DATASET_A_FEATURE_COLLECTION = DATASET_EXP + " a ows:FeatureCollection ;\n"
			+ "owl:sameAs" + "<" + DATASET_URL_EXP + "> .\n";
	
	public static final String RDF_CONFLATED_DATASET_A_FEATURE_COLLECTION = DATASET_EXP + " a ows:FeatureCollection .\n";
	
	public static final String RDF_CONFLATED_DATASET_GENERATED_AT = DATASET_EXP + " prov:generatedAtTime " + PROV_GENERATED_AT_EXP + XSD_DATE_TIME + " .\n";

	public static String createTriple(String entity1, String predicate, String entity2, boolean isEndOfStatement){
		
		String triple = entity1 + " " + predicate  + " " + entity2;
		
		if(isEndOfStatement){
			triple = triple.concat(" " + TRIPLE_ENDING);
		}else{
			triple = triple.concat(" " + LINE_ENDING);
		}

		triple = triple.concat("\n");
		
		return triple;		
	}
	
	public static String createMap(String name, String prefix){
		
		String mapString = prefix + ":" + name + "Map";
		
		return mapString;
	}
	
	public static String createMap(String prefix){
		
		String mapString = prefix + ":" + cleanPrefix(prefix).toUpperCase() + "Map";
		
		return mapString;
	}
	
	public static String createConflatedMap(String prefix){
		
		String mapString = prefix + ":ConflatedMap";
		
		return mapString;
	}
	
	public static String createFeature(String id, String prefix){
		
		String mapString = "";
		
		if(id != null && !id.equals("")){		
			mapString = prefix + ":" + cleanPrefix(prefix).toUpperCase() + "Feature_" + id;
		}else{
			mapString = prefix + ":" + cleanPrefix(prefix).toUpperCase() + "Feature";
		}
		
		
		return mapString;
	}
	
	public static String createConflatedFeature(String id, String prefix){
		
		String string = prefix + ":ConflatedMapFeature_" + id;
		
		return string;		
	}
	
	public static String createAttribute(String id, String prefix){
		
		String string = prefix + ":" + cleanPrefix(prefix).toUpperCase() + "_" + id;
		
		return string;		
	}
	
	public static String createPosition(String prefix){
		
		String string = prefix + ":" + cleanPrefix(prefix).toUpperCase() + "_Position";
		
		return string;		
	}
	
	public static String cleanPrefix(String prefix){
		prefix = prefix.replace("_data", "");
		prefix = prefix.replace("_conf", "");
		return prefix;
	}
	
	public static String createConflatedAttribute(String id, String prefix){
		
		String mapString = prefix + ":ConflatedMap_" + id;
		
		return mapString;		
	}

	public static String createMapHadMemberFeatureTriple(String mapName, String mapPrefix, String featureID, String featurePrefix, boolean beginOfStatement, boolean endOfStatement) {
		
		String triple = "";
		
		if(beginOfStatement){		
			triple = createTriple(createMap(mapName, mapPrefix), PREDICATE_PROV_HAD_MEMBER, createFeature(featureID, featurePrefix), false);
		}else{
			triple = createTriple("	", PREDICATE_PROV_HAD_MEMBER, createFeature(featureID, featurePrefix), endOfStatement);
		}
		return triple;
	}
	
	public static String createMapHadMemberFeatureTriple(String mapName, String featureID, boolean beginOfStatement, boolean endOfStatement) {
		
		String triple = "";
		
		if(beginOfStatement){		
			triple = createTriple(mapName, PREDICATE_PROV_HAD_MEMBER, featureID, false);
		}else{
			triple = createTriple("	", PREDICATE_PROV_HAD_MEMBER, featureID, endOfStatement);
		}
		return triple;
	}
	
	public static String createFeatureSameAsTriple(String resultID, String resultPrefix, String targetID, String targetPrefix){
		
		String triple = createTriple(resultID, PREDICATE_OWL_SAME_AS, targetID, true);
		
		return triple;		
	}
	
	public static String createFeatureDerivedFromTriple(String resultID, String resultPrefix, String targetID, String targetPrefix){
		
		String triple = createTriple(resultID, PREDICATE_PROV_DERIVED_FROM, targetID, true);
		
		return triple;		
	}
	
	public static String createFeatureRevisionOfTriple(String resultID, String resultPrefix, String targetID, String targetPrefix){
		
		String triple = createTriple(resultID, PREDICATE_PROV_WAS_REVISION_OF, targetID, true);
		
		return triple;		
	}
	
	public static String createAttributeSameAsTriple(String resultID, String resultPrefix, String targetID, String targetPrefix){
		
		String triple = createTriple(createConflatedAttribute(resultID, resultPrefix), PREDICATE_OWL_SAME_AS, createAttribute(targetID, targetPrefix), true);
		
		return triple;		
	}
	
	public static String createAttributeDerivedFromTriple(String resultID, String resultPrefix, String targetID, String targetPrefix){
		
		String triple = createTriple(createConflatedAttribute(resultID, resultPrefix), PREDICATE_PROV_DERIVED_FROM, createAttribute(targetID, targetPrefix), true);
		
		return triple;		
	}
	
	public static String createAttributeRevisionOfTriple(String resultID, String resultPrefix, String targetID, String targetPrefix){
		
		String triple = createTriple(createConflatedAttribute(resultID, resultPrefix), PREDICATE_PROV_WAS_REVISION_OF, createAttribute(targetID, targetPrefix), true);
		
		return triple;		
	}
	
	public static String createFeatureTypeTriple(String resultID, String targetID, boolean endofStatement){
		
		String triple = createTriple(resultID, A, targetID, endofStatement);
		
		return triple;		
	}
	
	public static String createFeatureHadGeometryTriple(String targetID, String targetPrefix, boolean endofStatement){
		
		String triple = createTriple("	", PREDICATE_OWS_HAD_GEOMETRY, createAttribute(targetID, targetPrefix), endofStatement);
		
		return triple;		
	}
	
	public static String createFeatureHadPropertyTriple(String targetID, String targetPrefix, boolean endofStatement){
		
		String triple = createTriple("	", PREDICATE_OWS_HAD_PROPERTY, createAttribute(targetID, targetPrefix), endofStatement);
		
		return triple;		
	}
	
	public static String createConflationExecutionProvUsedFeaturesTriple(String entityID, String featureID, boolean beginOfStatement, boolean endOfStatement) {
		
		String triple = "";
		
		if(beginOfStatement){		
			triple = createTriple(entityID, PREDICATE_PROV_USED, featureID, endOfStatement);
		}else{
			triple = createTriple("	", PREDICATE_PROV_USED, featureID, endOfStatement);
		}
		return triple;
	}
	
	public static String createFeaturesGeneratedByExecutionTriple(String featureID, String entityID) {
		
		String triple = createTriple(featureID, PREDICATE_PROV_WAS_GENERATED_BY, entityID, true);
		
		return triple;
	}
	
	public static String createFeaturesGeneratedAtTriple(String featureID, String time) {
		
		String triple = createTriple(featureID, PREDICATE_PROV_GENERATED_AT_TIME, time + XSD_DATE_TIME, true);
		
		return triple;
	}
	
	public static String createQualifiedUsageTriple(String entityID, String featureID, String role) {
		
		String triple = entityID + " " + PREDICATE_PROV_QUALIFIED_USAGE + " [\n";
		triple = triple.concat(A + " " + PROV_USAGE + " " + LINE_ENDING + "\n");
		triple = triple.concat(PREDICATE_PROV_ENTITY + " " + featureID + " " + LINE_ENDING + "\n");
		triple = triple.concat(PREDICATE_PROV_HAD_ROLE + " " + role + " " + LINE_ENDING + "\n");
		triple = triple.concat("] " + TRIPLE_ENDING + "\n");
		
		return triple;
	}
	
	public static String createQualifiedGenerationTriple(String entityID, String featureID) {
		
		String triple = featureID + " " + PREDICATE_PROV_QUALIFIED_GENERATION + " [\n";
		triple = triple.concat(A + " " + PROV_GENERATION + " " + LINE_ENDING + "\n");
		triple = triple.concat(PREDICATE_PROV_ACTIVITY + " " + entityID + " " + LINE_ENDING + "\n");
		triple = triple.concat(PREDICATE_PROV_HAD_ROLE + " " + CONFLATED_MAP_OUTPUT + " " + LINE_ENDING + "\n");
		triple = triple.concat("] " + TRIPLE_ENDING + "\n");
		
		return triple;
	}
}
