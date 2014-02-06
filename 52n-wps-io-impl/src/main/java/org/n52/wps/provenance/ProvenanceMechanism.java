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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.opengis.feature.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProvenanceMechanism {
	
	public enum DatasetIdentifier { SOURCE, TARGET };

	public static final String TARGET_GML_ID = "gml_id";
	public static final String SOURCE_GML_ID = "gml_id_1";
	
	private static final Logger logger = LoggerFactory.getLogger(ProvenanceMechanism.class);
	
	private Set<String> provenanceProperties;
	private Map<String, ProvenanceFeature> features = new HashMap<String, ProvenanceFeature>();
	private String sourceUrl;
	private String targetUrl;
	private DataSetProvenance dataset;
	private Set<String> distanceProperties;
	private Set<String> angleProperties;
	private Set<String> booleanProperties;
	
	public ProvenanceMechanism(String source, String target) {
		setupProperties();
		setSourceUrl(source);
		setTargetUrl(target);
		
		dataset = new DataSetProvenance("Conflation Result");
		String geomName = "Geometry Conflation applied by OWS-9 GMU WPS 1.0.0 using the RoadMatcher library";
		ProcessStep geom = new ProcessStep(geomName);
		Source targetSource = new Source(DatasetIdentifier.TARGET, targetUrl);
		Source sourceSource = new Source(DatasetIdentifier.SOURCE, sourceUrl);
		geom.addSource(targetSource);
		geom.addSource(sourceSource);

		ProcessStep attr = new ProcessStep("Attribute Conflation applied by OWS-9 52North WPS 1.0.0");
		try {
			attr.addSource(new Source(geomName, URLEncoder.encode("http://ows9.csiss.gmu.edu:9004/wps/WebProcessingService?Request=DescribeProcess&Service=WPS&identifier=edu.gmu.csiss.conflation.wps.RoadMatcherConflation", "UTF-8")));
		} catch (UnsupportedEncodingException e) {
			logger.warn(e.getMessage(), e);
		}
		dataset.addProcessStep(geom);
		dataset.addProcessStep(attr);
		
		dataset.addSource(targetSource);
		dataset.addSource(sourceSource);
		
		dataset.setTimestamp(new Date());
	}

	private void setupProperties() {
		this.provenanceProperties = new HashSet<String>();
		this.provenanceProperties.add(TARGET_GML_ID); //TARGET id
		this.provenanceProperties.add(SOURCE_GML_ID); //SOURCE id
		this.provenanceProperties.add("SOURCE");//f1 = TARGET, f2 = SOURCE
		this.provenanceProperties.add("MAXDIST");
		this.provenanceProperties.add("ADJSIZE");
		this.provenanceProperties.add("TRIMDIST");
		this.provenanceProperties.add("NEARNESS");
		this.provenanceProperties.add("ADJUSTED");
		this.provenanceProperties.add("ADJANGDEL");
		this.provenanceProperties.add("SRCSTATE");
		this.provenanceProperties.add("RESSTATE");
		this.provenanceProperties.add("SPLITSTART");
		this.provenanceProperties.add("SPLITEND");
		this.provenanceProperties.add("MANMATCH");
		this.provenanceProperties.add("MANADJ");
		this.provenanceProperties.add("MANADJCNT");
		this.provenanceProperties.add("MATCHORIEN");
		this.provenanceProperties.add("REVIEWED");
		this.provenanceProperties.add("UPDATETIME");
		
		this.distanceProperties =  new HashSet<String>();
		this.distanceProperties.add("MAXDIST");
		this.distanceProperties.add("ADJSIZE");
		this.distanceProperties.add("TRIMDIST");
		this.distanceProperties.add("NEARNESS");
		
		this.angleProperties =  new HashSet<String>();
		this.angleProperties.add("ADJANGDEL");
		
		this.booleanProperties =  new HashSet<String>();
		this.booleanProperties.add("ADJUSTED");
		this.booleanProperties.add("SPLITSTART");
		this.booleanProperties.add("SPLITEND");
	}

	public void addProvenanceProperty(String identifier, Property prop) {
		if (prop.getValue() == null) return; 
		ProvenanceFeature feature = resolveFeature(identifier);
		feature.setAttribute(prop.getName().getLocalPart(), prop.getValue().toString(),
				resolveUnit(prop.getName().getLocalPart()));
	}
	
	private String resolveUnit(String property) {
		if (distanceProperties.contains(property)) {
			return "m";
		}
		if (angleProperties.contains(property)) {
			return "degree";
		}
		if (booleanProperties.contains(property)) {
			return "boolean";
		}
		return null;
	}

	public void setSourceUrl(String url) {
		this.sourceUrl = url;
	}
	
	public void setTargetUrl(String url) {
		this.targetUrl = url;
	}

	private ProvenanceFeature resolveFeature(String identifier) {
		if (!this.features.containsKey(identifier)) {
			ProvenanceFeature feature = new ProvenanceFeature(identifier, dataset);
			this.features.put(identifier, feature);
			return feature;
		} else {
			return this.features.get(identifier);
		}
	}

	public DataSetProvenance getDataSetProvenance() {
		return dataset;
	}


	public String getSourceUrl() {
		return sourceUrl;
	}

	public String getTargetUrl() {
		return targetUrl;
	}

	public ProvenanceFeature setFeatureState(String identifier, boolean onlyOriginalProperties) {
		ProvenanceFeature feature = resolveFeature(identifier);
		feature.setUpdatedBySource(!onlyOriginalProperties);
		return feature;
	}

	public Set<String> getRelevantProperties() {
		return this.provenanceProperties;
	}

	public void setFeaturePropertySource(String id, Property prop,
			DatasetIdentifier dataset) {
		ProvenanceFeature feature = resolveFeature(id);
		feature.setPropertySource(prop.getName().getLocalPart(), dataset.toString());
	}
	
}
