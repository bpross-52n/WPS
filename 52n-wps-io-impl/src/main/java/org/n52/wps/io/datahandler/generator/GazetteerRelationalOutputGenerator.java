package org.n52.wps.io.datahandler.generator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.n52.wps.io.data.GazetteerConflationResultEntry;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GazetteerRelationalOutputDataBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GazetteerRelationalOutputGenerator extends AbstractGenerator {

	private static Logger LOGGER = LoggerFactory.getLogger(GazetteerRelationalOutputGenerator.class);

	public static final String MIME_TYPE_CSV = "text/csv";
	public static final String MIME_TYPE_RDF = "application/rdf+xml";
	public static final String MIME_TYPE_TEXT_TURTLE = "text/turtle";
	public static final String MIME_TYPE_APPLICATION_X_TURTLE = "application/x-turtle";
	
	public static final String SOURCE_FEATURE_NS_NGA_GAZETTEER = "http://earth-info.nga.mil/gns#";
	public static final String SOURCE_FEATURE_NS_NGA = "http://metadata.dod.mil/mdr/ns/GSIP/3.0/tds/3.0/";
	public static final String TARGET_FEATURE_NS_GAZETTEER = "http://www.nrcan.gc.ca/resource/";
	public static final String TARGET_FEATURE_NS_VGI = "http://www.52north.org/";

	public static final String NUM_RESULTS_EXP = "%num_results%";
	public static final String RESULT_ID_EXP = "%result_ID%";
	public static final String PROV_PROCESS_ID_EXP = "%prov_process_ID%";
	
	public static final String TARGET_FEATURE_ID_EXP = "%targetFeature_ID%";
	public static final String SOURCE_FEATURE_ID_EXP = "%sourceFeature_ID%";
	public static final String AGGREG_SCORE_EXP = "%aggregated_score%";
	public static final String DISTANCE_EXP = "%distance%";
	public static final String FW_SCORE_EXP = "%fw_score%";
	public static final String ENTITY_NAME_EXP = "%entity_Name%";
	public static final String ENTITY_ID_EXP = "%entity_ID%";
	public static final String ENTITY_NS_EXP = "%entity_ns%";
	public static final String TARGET_ENTITY_NS_EXP = "%target_entity_ns%";
	public static final String SOURCE_ENTITY_NS_EXP = "%source_entity_ns%";
	
	public static final String RDF_SEPARATOR = ",";
	
	public static final String PREFIXES_GAZETTEER = "@prefix id: <http://www.opengis.net/ont/identifier#> .\n"
			+ "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n"
			+ "@prefix gaz: <http://www.opengis.net/ont/gazetteer#> .\n"
			+ "@prefix prov: <http://www.w3.org/ns/prov#>.\n"
			+ "@prefix wps: <http://www.opengis.net/ont/wps/conflation#> .\n";
	
	public static final String PREFIXES_VGI = "@prefix id: <http://www.opengis.net/ont/identifier#> .\n"
			+ "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n"
			+ "@prefix gaz: <http://www.opengis.net/ont/gazetteer#> .\n"
			+ "@prefix prov: <http://www.w3.org/ns/prov#>.\n"
			+ "@prefix nga: <http://metadata.dod.mil/mdr/ns/GSIP/3.0/tds/3.0/>.\n"
			+ "@prefix f2n: <http://www.52north.org>.\n"
			+ "@prefix wps: <http://www.opengis.net/ont/wps/conflation#> .\n";
	
	public static final String RDF_START = ":" + RESULT_ID_EXP + " a wps:LinkSet;\n"
			+ "wps:numResults " + NUM_RESULTS_EXP + ";\n"
			+ "prov:generatedBy :" + PROV_PROCESS_ID_EXP + "; \n"
			+ "wps:hasLink ";
	
	public static final String RDF_ENTITY_GAZETTEER = "<" + ENTITY_NS_EXP + ENTITY_ID_EXP + "> a gaz:Location;\n"
			+ " rdfs:label '" + ENTITY_NAME_EXP + "';\n"
			+ " id:identifier \"" + ENTITY_ID_EXP + "\".\n";
	
	public static final String RDF_ENTITY_NGA_WFS = "<" + ENTITY_NS_EXP + ENTITY_ID_EXP + "> a nga:NGA_Feature;\n"
			+ " rdfs:label '" + ENTITY_NAME_EXP + "';\n"
			+ " id:identifier \"" + ENTITY_ID_EXP + "\".\n";
	
	public static final String RDF_ENTITY_VGI_WFS = "<" + ENTITY_NS_EXP + ENTITY_ID_EXP + "> a f2n:F2N_Feature;\n"
			+ " rdfs:label '" + ENTITY_NAME_EXP + "';\n"
			+ " id:identifier \"" + ENTITY_ID_EXP + "\".\n";
	
	public static final String RDF_SIMILARITY_LINK= "[\n"
			+ " a wps:SimilarityLink ;\n"
			+ " wps:entity1 <" + SOURCE_ENTITY_NS_EXP + SOURCE_FEATURE_ID_EXP + "> ;\n"
			+ " wps::entity2 <" + TARGET_ENTITY_NS_EXP + TARGET_FEATURE_ID_EXP + "> ;\n"
			+ " wps:score " + AGGREG_SCORE_EXP + " ;\n"
			+ " wps:scoreDetails [\n"
			+ " a wps:ScoreDetail;\n"
			+ " wps:distanceInMiles " + DISTANCE_EXP + ";\n"
			+ " wps:fuzzyWuzzy " + FW_SCORE_EXP + "\n"
			+ " ]";
	
	public GazetteerRelationalOutputGenerator(){
		super();
		this.supportedIDataTypes.add(GazetteerRelationalOutputDataBinding.class);
	}
	
	@Override
	public InputStream generateStream(IData data, String mimeType, String schema)
			throws IOException {

		List<GazetteerConflationResultEntry> resultEntries = null;
		
		if(data instanceof GazetteerRelationalOutputDataBinding){			
			resultEntries = ((GazetteerRelationalOutputDataBinding)data).getPayload();			
		}else{
			RuntimeException rte = new RuntimeException("Data is not of class GazetteerRelationalOutputDataBinding.");
			LOGGER.error(rte.getMessage());
			throw rte;
		}
		
		if(mimeType.equals("text/csv")){
			
			StringBuffer stb = new StringBuffer();
			
			stb.append("FW Score,Dist(MI),NGA_UFI,NB_ID,NGA_NAME,NB_NAME" + "\n");
			
			for (GazetteerConflationResultEntry gazetteerConflationResultEntry : resultEntries) {
				stb.append(gazetteerConflationResultEntry.toString() + "\n");
			}
			
			return new ByteArrayInputStream(stb.toString().getBytes());
			
		}else if(mimeType.equals(MIME_TYPE_RDF) || mimeType.equals(MIME_TYPE_APPLICATION_X_TURTLE) || mimeType.equals(MIME_TYPE_TEXT_TURTLE)){
			
			StringBuffer stb = new StringBuffer();
			StringBuffer stb2 = new StringBuffer();
			if(((GazetteerRelationalOutputDataBinding)data).isGazetteerMatching()){				
				stb.append(PREFIXES_GAZETTEER);
			}else{
				stb.append(PREFIXES_VGI);
			}
			
			stb.append("\n");
			stb.append("\n");
			
			String rdfStart = RDF_START.replace(RESULT_ID_EXP, "ConflationResult-" + UUID.randomUUID().toString().substring(0, 4));
			rdfStart = rdfStart.replace(NUM_RESULTS_EXP, resultEntries.size() + "");
			rdfStart = rdfStart.replace(PROV_PROCESS_ID_EXP, "GazetteerConflationProcess-" + UUID.randomUUID().toString().substring(0, 4));
			
			stb.append(rdfStart);
			
			Iterator<GazetteerConflationResultEntry> iterator = resultEntries.iterator();
			
			while (iterator.hasNext()) {
				GazetteerConflationResultEntry gazetteerConflationResultEntry = (GazetteerConflationResultEntry) iterator
						.next();				
				String rdfSimilarityLink =  RDF_SIMILARITY_LINK.replace(TARGET_FEATURE_ID_EXP, gazetteerConflationResultEntry.getTargetFeatureGeogrName());
				
				if(((GazetteerRelationalOutputDataBinding)data).isGazetteerMatching()){	
					rdfSimilarityLink =  rdfSimilarityLink.replace(TARGET_ENTITY_NS_EXP, TARGET_FEATURE_NS_GAZETTEER);
					rdfSimilarityLink =  rdfSimilarityLink.replace(SOURCE_ENTITY_NS_EXP, SOURCE_FEATURE_NS_NGA_GAZETTEER);
				}else{
					rdfSimilarityLink =  rdfSimilarityLink.replace(TARGET_ENTITY_NS_EXP, TARGET_FEATURE_NS_VGI);					
					rdfSimilarityLink =  rdfSimilarityLink.replace(SOURCE_ENTITY_NS_EXP, SOURCE_FEATURE_NS_NGA);					
				}
				
				rdfSimilarityLink =  rdfSimilarityLink.replace(SOURCE_FEATURE_ID_EXP, gazetteerConflationResultEntry.getSourceFeatureGeogrName());
				rdfSimilarityLink =  rdfSimilarityLink.replace(AGGREG_SCORE_EXP, "null");
				rdfSimilarityLink =  rdfSimilarityLink.replace(DISTANCE_EXP, gazetteerConflationResultEntry.getDistance() + "");
				rdfSimilarityLink =  rdfSimilarityLink.replace(FW_SCORE_EXP, gazetteerConflationResultEntry.getFwScore() +"");
				
				stb.append(rdfSimilarityLink);
				
				String entity1 = "";
				String entity2 = "";
				
				if(((GazetteerRelationalOutputDataBinding)data).isGazetteerMatching()){	
					entity1 = RDF_ENTITY_GAZETTEER.replace(ENTITY_NS_EXP, TARGET_FEATURE_NS_GAZETTEER);
					entity1 = entity1.replace(ENTITY_NAME_EXP, gazetteerConflationResultEntry.getTargetFeatureAlternativeGeogrName());
					entity1 = entity1.replace(ENTITY_ID_EXP, gazetteerConflationResultEntry.getTargetFeatureGeogrName());
					
					entity2 = RDF_ENTITY_GAZETTEER.replace(ENTITY_NS_EXP, SOURCE_FEATURE_NS_NGA_GAZETTEER);
					entity2 = entity2.replace(ENTITY_NAME_EXP, gazetteerConflationResultEntry.getSourceFeatureAlternativeGeogrName());
					entity2 = entity2.replace(ENTITY_ID_EXP, gazetteerConflationResultEntry.getSourceFeatureGeogrName());
				}else{
					entity1 = RDF_ENTITY_VGI_WFS.replace(ENTITY_NS_EXP, TARGET_FEATURE_NS_VGI);
					entity1 = entity1.replace(ENTITY_NAME_EXP, gazetteerConflationResultEntry.getTargetFeatureAlternativeGeogrName());
					entity1 = entity1.replace(ENTITY_ID_EXP, gazetteerConflationResultEntry.getTargetFeatureGeogrName());
					
					entity2 = RDF_ENTITY_NGA_WFS.replace(ENTITY_NS_EXP, SOURCE_FEATURE_NS_NGA);
					entity2 = entity2.replace(ENTITY_NAME_EXP, gazetteerConflationResultEntry.getSourceFeatureAlternativeGeogrName());
					entity2 = entity2.replace(ENTITY_ID_EXP, gazetteerConflationResultEntry.getSourceFeatureGeogrName());
				}

				
				stb2.append(entity1);
				stb2.append("\n");
				stb2.append(entity2);
				
				if(iterator.hasNext()){
					stb.append(",\n");
					stb2.append("\n");
				}else{
					stb.append(".");
				}
				
			}
			
			stb.append("\n");
			stb.append("\n");
			stb.append(stb2);
			
			return new ByteArrayInputStream(stb.toString().getBytes());
			
		}
		
		return null;
	}
}
