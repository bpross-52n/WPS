package org.n52.wps.provenance;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class RDFProvenanceFeature implements Comparable<RDFProvenanceFeature>{
	
	private String id;
	private ProvenanceType provenanceType;
	private String featureType;
	private String role;
	private String gmlID;
	private Date generatedAt;
	private Map<String, String> propertyIDMap;
	
	public RDFProvenanceFeature(){
		propertyIDMap = new HashMap<String, String>();
	}
	
	public RDFProvenanceFeature(String id, String featureType, Date generatedAt, String role){
		this();
		this.id = id;
		this.featureType = featureType;
		this.generatedAt = generatedAt;
		this.role = role;
	}
	
	public RDFProvenanceFeature(String id, String featureType, Date generatedAt, String role, String gmlID){
		this();
		this.id = id;
		this.featureType = featureType;
		this.generatedAt = generatedAt;
		this.role = role;
		this.gmlID = gmlID;
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

	public Date getGeneratedAt() {
		return generatedAt;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public void setFeatureType(String featureType) {
		this.featureType = featureType;
	}

	public void setGeneratedAt(Date generatedAt) {
		this.generatedAt = generatedAt;
	}

	public void setPropertyIDMap(Map<String, String> propertyIDMap) {
		this.propertyIDMap = propertyIDMap;
	}

	public String getGmlID() {
		return gmlID;
	}

	public void setGmlID(String gmlID) {
		this.gmlID = gmlID;
	}

	@Override
	public int compareTo(RDFProvenanceFeature o) {		
		return o.getID().compareTo(id);
	}
}
