package org.n52.wps.provenance;

public class RDFUtil {
	
	public static final String BASE_URL_EXP = "%BASE_URL%";
	public static final String PROV_BUNDLE_EXP = "%BUNDLE%";
	public static final String PROV_BUNDLE_ATTRIBUTED_TO_EXP = "%BUNDLE_ATTRIBUTED_TO%";
	public static final String PROV_BUNDLE_GENERATED_AT_EXP = "%BUNDLE_GENERATED_AT%";
	public static final String PROV_RDF_ENTITY_NAME_EXP = "%RDF_ENTITY_NAME%";
	public static final String PROV_RDF_STARTED_AT_TIME_EXP = "%STARTED_AT_TIME%";
	public static final String PROV_RDF_ENDED_AT_TIME_EXP = "%ENDED_AT_TIME%";
	public static final String PROV_ENTITY_GENERATED_AT_EXP = "%ENTITY_GENERATED_AT%";
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

	public static final String BASE = "@base <" + BASE_URL_EXP + "> .\n";
	public static final String PREFIX_F2N = "@prefix f2n: <http://www.opengis.net/ogc/ows10/ows10-52n-ontology/> .\n";
	public static final String PREFIX_RDFS = "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n";
	public static final String PREFIX_FOAF = "@prefix foaf: <http://xmlns.com/foaf/0.1/> .\n";
	public static final String PREFIX_PROV = "@prefix prov: <http://www.w3.org/ns/prov#> .\n";
	public static final String PREFIX_XSD = "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n";
	public static final String PREFIX_OWS10 = "@prefix ows10: <http://www.opengis.net/ogc/ows10/ows10-core-ontology/> .\n";
	public static final String PREFIX_OWS = "@prefix ows: <http://www.opmw.org/ogc/ows/ows-core-ontology/> .\n";
	public static final String PREFIX_NGA = "@prefix nga: <http://www.opengis.net/ogc/ows10/ows10-nga-ontology/> .\n";
	public static final String PREFIX_OWL = "@prefix owl:  <http://www.w3.org/2002/07/owl#> .\n";
	
	
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

}
