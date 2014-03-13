package org.n52.wps.provenance;

import java.util.HashMap;
import java.util.Map;

public class RDFProvenanceFeature {
	
	private String id;
	private ProvenanceType provenanceType;
	
	private Map<String, String> propertyIDMap;
	
	public RDFProvenanceFeature(){
		propertyIDMap = new HashMap<String, String>();
	}
	
	public RDFProvenanceFeature(String id){
		this();
		this.id = id;
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
}
