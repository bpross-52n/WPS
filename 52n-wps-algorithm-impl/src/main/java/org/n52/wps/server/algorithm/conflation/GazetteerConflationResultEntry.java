package org.n52.wps.server.algorithm.conflation;

public class GazetteerConflationResultEntry implements Comparable<GazetteerConflationResultEntry>{

	private int fwScore;
	private double distance;
	private String sourceFeatureGeogrName;
	private String targetFeatureGeogrName;
	private String sourceFeatureAlternativeGeogrName;
	private String targetFeatureAlternativeGeogrName;
	
	public GazetteerConflationResultEntry(){
		
	}
	
	public GazetteerConflationResultEntry(int fwScore, double distance, String sourceFeatureGeogrName, String targetFeatureGeogrName, String sourceFeatureAlternativeGeogrName, String targetFeatureAlternativeGeogrName){
		this.fwScore = fwScore;
		this.distance = distance;
		this.sourceFeatureGeogrName = sourceFeatureGeogrName;
		this.targetFeatureGeogrName = targetFeatureGeogrName;
		this.sourceFeatureAlternativeGeogrName = sourceFeatureAlternativeGeogrName;
		this.targetFeatureAlternativeGeogrName = targetFeatureAlternativeGeogrName;
	}
	
	public int getFwScore() {
		return fwScore;
	}

	public void setFwScore(int fwScore) {
		this.fwScore = fwScore;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public String getSourceFeatureGeogrName() {
		return sourceFeatureGeogrName;
	}

	public void setSourceFeatureGeogrName(String sourceFeatureGeogrName) {
		this.sourceFeatureGeogrName = sourceFeatureGeogrName;
	}

	public String getTargetFeatureGeogrName() {
		return targetFeatureGeogrName;
	}

	public void setTargetFeatureGeogrName(String targetFeatureGeogrName) {
		this.targetFeatureGeogrName = targetFeatureGeogrName;
	}

	public String getSourceFeatureAlternativeGeogrName() {
		return sourceFeatureAlternativeGeogrName;
	}

	public void setSourceFeatureAlternativeGeogrName(
			String sourceFeatureAlternativeGeogrName) {
		this.sourceFeatureAlternativeGeogrName = sourceFeatureAlternativeGeogrName;
	}

	public String getTargetFeatureAlternativeGeogrName() {
		return targetFeatureAlternativeGeogrName;
	}

	public void setTargetFeatureAlternativeGeogrName(
			String targetFeatureAlternativeGeogrName) {
		this.targetFeatureAlternativeGeogrName = targetFeatureAlternativeGeogrName;
	}

	@Override
	public int compareTo(GazetteerConflationResultEntry o) {
		
		if(getFwScore() < o.getFwScore()){
			return 1;
		}else if(getFwScore() > o.getFwScore()){
			return -1;
		}else if(getFwScore() == o.getFwScore()){
			
			if(getDistance() < o.getDistance()){
				return 1;
			}else if(getDistance() > o.getDistance()){
				return -1;
			}
			
		}
		
		return 0;
	}
	
	@Override
	public String toString() {
		return getFwScore() + "," + getDistance() + "," + getSourceFeatureGeogrName() + "," + getTargetFeatureGeogrName() + "," + getSourceFeatureAlternativeGeogrName() + "," + getTargetFeatureAlternativeGeogrName();
	}

}
