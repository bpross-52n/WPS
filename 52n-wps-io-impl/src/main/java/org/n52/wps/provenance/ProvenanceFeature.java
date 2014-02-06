/**
 * Copyright (C) 2012
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
 */
package org.n52.wps.provenance;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ProvenanceFeature {
	
	
	private static ProvenanceEncoding encoder = new ISOProvenanceEncoding();
	private String id;
	private Map<String, Attribute> qualityAttributes = new HashMap<String, Attribute>();
	private Map<String, String> featureAttributes = new HashMap<String, String>();
	
	private boolean updatedBySource;
	private DataSetProvenance datasetProvenance;
	private Object geometry;

	public ProvenanceFeature(String featureId, DataSetProvenance dataset) {
		this.id = featureId;
		this.datasetProvenance = dataset;
	}
	
	public void setAttribute(String key, String value, String unit) {
		this.qualityAttributes.put(key, new Attribute(key, value, unit));
	}
	
	public String getAttribute(String key) {
		return this.qualityAttributes.get(key).getValue();
	}
	
	public Map<String, Attribute> getAllQualityAttributes() {
		return this.qualityAttributes;
	}

	public String getId() {
		return id;
	}

	public void setUpdatedBySource(boolean b) {
		this.updatedBySource = b;
	}

	public boolean isUpdatedBySource() {
		return updatedBySource;
	}
	
	public String getTargetGmlId() {
		return this.qualityAttributes.containsKey(ProvenanceMechanism.TARGET_GML_ID) ?
				this.qualityAttributes.get(ProvenanceMechanism.TARGET_GML_ID).getValue() : null;
	}
	
	public String getSourceGmlId() {
		return this.qualityAttributes.containsKey(ProvenanceMechanism.SOURCE_GML_ID) ?
			this.qualityAttributes.get(ProvenanceMechanism.SOURCE_GML_ID).getValue() : null;
	}
	
	public boolean isGeometryUpdated() {
		Attribute sourceAtt = this.qualityAttributes.get("SOURCE");
		if (sourceAtt != null && sourceAtt.getValue().equals("f2")) {
			return true;
		}
		return false;
	}
	
	@Override
	public String toString() {
		return this.id + ": "+updatedBySource +"; "+qualityAttributes;
	}

	public void setPropertySource(String property, String string) {
		this.featureAttributes.put(property, string);
	}

	public Element encodeSelf(Document document, Element value, String gmlNamespace, Map<String, String> uriToPrefix) {
		return encoder.encode(this, value, document, gmlNamespace, uriToPrefix);
	}

	public Map<String, String> getFeatureAttributes() {
		return this.featureAttributes;
	}

	public DataSetProvenance getDatasetProvenance() {
		return datasetProvenance;
	}

	public void setGeometry(Object geom) {
		this.geometry = geom;
	}

	public Object getGeometry() {
		return geometry;
	}
	
	
}
