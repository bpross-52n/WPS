package org.n52.wps.provenance;

import java.util.HashMap;
import java.util.Map;

public class RDFProvenanceFeature implements Comparable<RDFProvenanceFeature>{
	
	private String id;
	private ProvenanceType provenanceType;
	private String featureType;
	private Map<String, String> propertyIDMap;
	
	public RDFProvenanceFeature(){
		propertyIDMap = new HashMap<String, String>();
	}
	
	public RDFProvenanceFeature(String id, String featureType){
		this();
		this.id = id;
		this.featureType = featureType;
	}
	
	public void putPropertyID(String propertyName, String ID){
		propertyIDMap.put(propertyName, ID);
	}
	
	public void setProvenanceType(ProvenanceType type){
		provenanceType = type;
	}
	
	public ProvenanceType getProvenanceType(){
		return provenanceType;
	}
	
	public String getPropertyID(String propertyName){
		return propertyIDMap.get(propertyName);
	}
	
	public Map<String, String> getPropertyIDMap(){
		return propertyIDMap;
	}
	
	public String getID(){
		return id;
	}

	public String getFeatureType() {
		return featureType;
	}

	@Override
	public int compareTo(RDFProvenanceFeature o) {		
		return o.getID().compareTo(id);
	}
}
