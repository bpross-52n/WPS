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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;


import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ISOProvenanceEncoding implements ProvenanceEncoding {

	private static final String GMD_NAMESPACE = "http://www.isotc211.org/2005/gmd";
	private static final String GCO_NAMESPACE = "http://www.isotc211.org/2005/gco";
	private static final String CON_NAMESPACE = "http://www.opengis.net/ows-9/cci/conflation";
	private static final String XLINK_NAMESPACE = "http://www.w3.org/1999/xlink";
	private String gmdPrefix;
	private String gcoPrefix;
	private String conPrefix;
	private String xlinkPrefix;
	private String gmlPrefix;
	private String gmlNamespace;
	private boolean firstRun = true;
	private Map<Document, Set<XLinkLinkable>> createdLinkables = new HashMap<Document, Set<XLinkLinkable>>();
	private AtomicInteger idIncrementor = new AtomicInteger(1);

	@Override
	public Element encode(ProvenanceFeature provenanceFeature,
			Element containingElement, Document parentDocument, String gmlNamespace,
			Map<String, String> uriToPrefix) {
		resolvePrefixes(uriToPrefix, gmlNamespace, containingElement);

		Element genericMetaData = parentDocument.createElementNS(this.gmlNamespace, gmlPrefix+":GenericMetaData");
		genericMetaData.setAttributeNS(gmlNamespace, gmlPrefix+":id", provenanceFeature.getId()+"-metadata");

		containingElement.appendChild(encodeMetadata(genericMetaData, provenanceFeature, parentDocument));
		return containingElement;
	}


	private void resolvePrefixes(Map<String, String> uriToPrefix, String gmlNamespace,
			Element containingElement) {
		synchronized (this) {
			if (firstRun) {
				if (gmlNamespace != null) {
					gmlPrefix = containingElement.lookupPrefix(gmlNamespace);
				}
				if (gmlPrefix == null) {
					gmlPrefix = "gml";
					this.gmlNamespace = containingElement.lookupNamespaceURI(gmlPrefix);
				} 
				if (this.gmlNamespace == null) {
					this.gmlNamespace = gmlNamespace;
				}

				for (String uri : uriToPrefix.keySet()) {
					if (uri.equals(gmlNamespace)) {
						this.gmlNamespace = gmlNamespace;
						this.gmlPrefix = uriToPrefix.get(uri);
					}
					else if (uri.equals(CON_NAMESPACE)) {
						this.conPrefix = uriToPrefix.get(uri);
					}
					else if (uri.equals(GCO_NAMESPACE)) {
						this.gcoPrefix = uriToPrefix.get(uri);
					}
					else if (uri.equals(GMD_NAMESPACE)) {
						this.gmdPrefix = uriToPrefix.get(uri);
					}
					else if (uri.equals(XLINK_NAMESPACE)) {
						this.xlinkPrefix = uriToPrefix.get(uri);
					}
				}
				firstRun = false;
			}

		}
	}

	private Element encodeMetadata(Element genericMetaData, ProvenanceFeature provenanceFeature, Document parentDocument) {
		Element mdMetadata = createGMDElement(parentDocument, "MD_Metadata");
		mdMetadata.setPrefix(gmdPrefix);

		Element contact = createElementWithNilReason("inapplicable", GMD_NAMESPACE, gmdPrefix+":contact", parentDocument);
		Element dateStamp = createElementWithNilReason("inapplicable", GMD_NAMESPACE, gmdPrefix+":dateStamp", parentDocument);
		Element identificationInfo = createElementWithNilReason("inapplicable", GMD_NAMESPACE, gmdPrefix+":identificationInfo", parentDocument);

		mdMetadata.appendChild(contact);
		mdMetadata.appendChild(dateStamp);
		mdMetadata.appendChild(identificationInfo);

		appendDataQualityInfo(mdMetadata, parentDocument, provenanceFeature);
//		appendFeatureAttributes(mdMetadata, parentDocument, provenanceFeature);

		genericMetaData.appendChild(mdMetadata);

		return genericMetaData;
	}

	private void appendFeatureAttributes(Element mdMetadata,
			Document parentDocument, ProvenanceFeature provenanceFeature) {
		Map<String, String> atts = provenanceFeature.getFeatureAttributes();
		Element featureAttribute;
		for (String key : atts.keySet()) {
			featureAttribute = createGMDElement(parentDocument, "featureAttribute");
			Element conflatedAttribute = parentDocument.createElementNS(CON_NAMESPACE, conPrefix+":ConflatedAttribute");
			conflatedAttribute.setAttribute("attributeName", key);

			Element sourceDataset = parentDocument.createElementNS(CON_NAMESPACE, conPrefix+":source");
			Source s = provenanceFeature.getDatasetProvenance().resolveSourceByName(atts.get(key));
			if (hasBeenCreatedForDocument(s, parentDocument)) {
				setXLinkHref(sourceDataset, s);
			} 
			else {
				createLISource(s, parentDocument, sourceDataset);
			}

			conflatedAttribute.appendChild(sourceDataset);
			featureAttribute.appendChild(conflatedAttribute);
			mdMetadata.appendChild(featureAttribute);
		}
	}

	private void appendDataQualityInfo(Element mdMetadata, Document parentDocument, ProvenanceFeature provenanceFeature) {
		Element dq = fillDataQualityTemplate(provenanceFeature, parentDocument, mdMetadata);
		mdMetadata.appendChild(parentDocument.importNode(dq, true));
	}

	private Element fillDataQualityTemplate(ProvenanceFeature provenanceFeature,
			Document parentDocument, Element mdMetadata) {
		return createdDataQuality(mdMetadata, parentDocument, provenanceFeature);
	}


	private Element createdDataQuality(Element mdMetadata, Document parentDocument,
			ProvenanceFeature provenanceFeature) {
		Element dqInfo = createGMDElement(parentDocument, "dataQualityInfo");
		Element dqQuality = createGMDElement(parentDocument, "DQ_DataQuality");
		dqQuality.appendChild(createScope(parentDocument));

		Map<String, Attribute> qualityAttributes = provenanceFeature.getAllQualityAttributes();
		Attribute attribute;
		for (String key : qualityAttributes.keySet()) {
			attribute = qualityAttributes.get(key);
			if (attribute.getUnit() != null) {
				dqQuality.appendChild(createReport(key, attribute, parentDocument, dqQuality));
			}
		}

		dqQuality.appendChild(createLineage(parentDocument, dqQuality, provenanceFeature));
		dqInfo.appendChild(dqQuality);
		return dqInfo;
	}


	private Node createReport(String att, Attribute attribute,
			Document parentDocument, Element dqQuality) {
		Element report = createGMDElement(parentDocument, "report");
		Element dqAbsExt = createGMDElement(parentDocument, "DQ_AbsoluteExternalPositionalAccuracy");

		Element result1 = createGMDElement(parentDocument, "result");
		Element dqConfRes = createDQConformanceResult(attribute, parentDocument);
		result1.appendChild(dqConfRes);

		Element result2 = createGMDElement(parentDocument, "result");
		Element dqQuantRes = createDQQuantitiveResult(attribute, parentDocument);
		result2.appendChild(dqQuantRes);

		dqAbsExt.appendChild(result1);
		dqAbsExt.appendChild(result2);

		report.appendChild(dqAbsExt);
		return report;
	}

	private Element createDQQuantitiveResult(Attribute attribute,
			Document parentDocument) {
		Element dqQuantRes = createGMDElement(parentDocument, "DQ_QuantitativeResult");
		Element valueUnit = createGMDElement(parentDocument, "valueUnit");
		Element unitDefinition = createGMLElement(parentDocument, "UnitDefinition");
		unitDefinition.setAttributeNS(gmlNamespace, gmlPrefix+":id", attribute.getKey()+"-unit-"+idIncrementor.getAndIncrement());
		Element identifier = createGMLElement(parentDocument, "identifier");
		identifier.setAttribute("codeSpace", "inapplicable");
		identifier.appendChild(parentDocument.createTextNode(attribute.getUnit()));
		unitDefinition.appendChild(identifier);
		valueUnit.appendChild(unitDefinition);

		Element errorStatistic = createGMDElement(parentDocument, "errorStatistic");
		Element charString = createGCOElement(parentDocument, "CharacterString");
		charString.appendChild(parentDocument.createTextNode("error value"));
		errorStatistic.appendChild(charString);

		Element value = createGMDElement(parentDocument, "value");
		Element record = createGCOElement(parentDocument, "Record");
		record.appendChild(parentDocument.createTextNode(attribute.getValue()));
		value.appendChild(record);

		dqQuantRes.appendChild(valueUnit);
		dqQuantRes.appendChild(errorStatistic);
		dqQuantRes.appendChild(value);
		return dqQuantRes;
	}

	private Element createDQConformanceResult(Attribute attribute,
			Document parentDocument) {
		Element dqConfRes = createGMDElement(parentDocument, "DQ_ConformanceResult");
		Element specification = createGMDElement(parentDocument, "specification");
		Element ciCitation = createGMDElement(parentDocument, "CI_Citation");
		Element title = createGMDElement(parentDocument, "title");
		Element charString = createGCOElement(parentDocument, "CharacterString");
		charString.appendChild(parentDocument.createTextNode("Measurements"));
		Element date = createGMDElement(parentDocument, "date");
		date.setAttributeNS(GCO_NAMESPACE, gcoPrefix+":nilReason", "inapplicable");
		ciCitation.appendChild(title);
		ciCitation.appendChild(date);
		specification.appendChild(ciCitation);

		Element explanation = createGMDElement(parentDocument, "explanation");
		Element charString2 = createGCOElement(parentDocument, "CharacterString");
		charString2.appendChild(parentDocument.createTextNode(attribute.getKey()));
		explanation.appendChild(charString2);

		Element pass = createGMDElement(parentDocument, "pass");
		Element bool = createGCOElement(parentDocument, "Boolean");
		bool.appendChild(parentDocument.createTextNode("true"));

		dqConfRes.appendChild(specification);
		dqConfRes.appendChild(explanation);
		dqConfRes.appendChild(pass);
		return dqConfRes;
	}


	private Element createLineage(Document parentDocument, Element containingElement, ProvenanceFeature provenanceFeature) {
		if (provenanceFeature.getDatasetProvenance() != null) {
			return encodeLineage(provenanceFeature.getDatasetProvenance(), provenanceFeature, parentDocument);

		} else {
			Element lineage = createGMDElement(parentDocument, "lineage");
			Element liLineage = createGMDElement(parentDocument, "LI_Lineage");
			Element statement = createGMDElement(parentDocument, "statement");
			Element charString = createGCOElement(parentDocument, "CharacterString");
			charString.appendChild(parentDocument.createTextNode(createLineageStatement(provenanceFeature)));
			statement.appendChild(charString);
			liLineage.appendChild(statement);
			lineage.appendChild(liLineage);
			return lineage;
		}
	}


	private String createLineageStatement(ProvenanceFeature provenanceFeature) {
		StringBuilder sb = new StringBuilder();
		sb.append("Dataset Conflation applied by OWS-9 52North WPS 1.0.0. ");
		String sourceId = provenanceFeature.getSourceGmlId();
		String targetId = provenanceFeature.getTargetGmlId();
		if (sourceId != null) {
			sb.append("Source dataset feature '");
			sb.append(sourceId);
			sb.append("'; ");
		}
		if (targetId != null) {
			sb.append("Target dataset feature '");
			sb.append(targetId);
			sb.append("'; ");
		}
		sb.append("Geom_Source = ");
		sb.append(provenanceFeature.isGeometryUpdated() ? "SOURCE" : "TARGET");			
		return sb.toString();
	}


	private Element createScope(Document parentDocument) {
		Element scope = createGMDElement(parentDocument, "scope");
		Element dqScope = createGMDElement(parentDocument, "DQ_Scope");
		Element level = createGMDElement(parentDocument, "level");
		Element mdScopeCode = createGMDElement(parentDocument, "MD_ScopeCode");
		mdScopeCode.setAttribute("codeList", "http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists.xml#MD_ScopeCode");
		mdScopeCode.setAttribute("codeListValue", "feature");
		level.appendChild(mdScopeCode);
		dqScope.appendChild(level);
		scope.appendChild(dqScope);
		return scope;
	}


	private Element createGMDElement(Document parentDocument, String localName) {
		return createElementForNamespace(GMD_NAMESPACE, gmdPrefix, localName, parentDocument);
	}

	private Element createGMLElement(Document parentDocument, String localName) {
		return createElementForNamespace(gmlNamespace, gmlPrefix, localName, parentDocument);
	}

	private Element createGCOElement(Document parentDocument, String localName) {
		return createElementForNamespace(GCO_NAMESPACE, gcoPrefix, localName, parentDocument);
	}


	public static Element createElementForNamespace(String namespace,
			String prefix, String localName, Document parentDocument) {
		StringBuilder sb = new StringBuilder();
		sb.append(prefix);
		sb.append(":");
		sb.append(localName);
		return parentDocument.createElementNS(namespace, sb.toString());
	}


	private Element createElementWithNilReason(String string,
			String namespace, String localName, Document parentDocument) {
		Element result = parentDocument.createElementNS(namespace, localName);
		result.setAttributeNS(GCO_NAMESPACE, gcoPrefix+":nilReason", string);
		return result;
	}


	public Element encodeLineage(DataSetProvenance dataSetProvenance,
			ProvenanceFeature provenanceFeature, Document parentDocument) {
		Element lineage = createGMDElement(parentDocument, "lineage");
		Element liLineage = createGMDElement(parentDocument, "LI_Lineage");
		Element statement = createGMDElement(parentDocument, "statement");
		Element charString = createGCOElement(parentDocument, "CharacterString");
		charString.appendChild(parentDocument.createTextNode(createLineageStatement(provenanceFeature)));
		statement.appendChild(charString);
		liLineage.appendChild(statement);


		for (ProcessStep ps : dataSetProvenance.getProcessSteps()) {
			appendProcessStep(ps, liLineage, parentDocument);
		}

		for (Source s : dataSetProvenance.getSources()) {
			appendSource(s, liLineage, parentDocument);
		}

		lineage.appendChild(liLineage);
		return lineage;
	}



	private void appendSource(Source s, Element liLineage,
			Document parentDocument) {
		if (hasBeenCreatedForDocument(s, parentDocument)) {
			Element source = createGMDElement(parentDocument, "source");
			setXLinkHref(source, s);
			liLineage.appendChild(source);
		}
		else {
			Element source = createGMDElement(parentDocument, "source");
			setXLinkTitle(source, s);
			createLISource(s, parentDocument, source);
			liLineage.appendChild(source);
			setCreatedForDocument(s, parentDocument);
		}
	}


	private void createLISource(Source s, Document parentDocument, Element source) {
		Element liSource = createGMDElement(parentDocument, "LI_Source");
		Element description = createGMDElement(parentDocument, "description");
		Element charString = createGCOElement(parentDocument, "CharacterString");
		charString.appendChild(parentDocument.createTextNode(s.getUrl()));
		description.appendChild(charString);
		liSource.appendChild(description);
		source.appendChild(liSource);
	}

	private void setXLinkTitle(Element source, XLinkLinkable s) {
		source.setAttributeNS(XLINK_NAMESPACE, xlinkPrefix+":title", s.getTitle());		
	}

	private void setXLinkHref(Element source, XLinkLinkable s) {
		source.setAttributeNS(XLINK_NAMESPACE, xlinkPrefix+":href", s.getHref());
		source.setAttributeNS(GCO_NAMESPACE, gcoPrefix+":nilReason", "withheld");		
	}


	private void setCreatedForDocument(XLinkLinkable s, Document parentDocument) {
		Set<XLinkLinkable> list;
		if (createdLinkables.containsKey(parentDocument)) {
			list = createdLinkables.get(parentDocument);
		} else {
			list = new HashSet<XLinkLinkable>();
			createdLinkables.put(parentDocument, list);
		}
		list.add(s);		
	}


	private boolean hasBeenCreatedForDocument(XLinkLinkable s, Document parentDocument) {
		if (createdLinkables.containsKey(parentDocument)) {
			Set<XLinkLinkable> list = createdLinkables.get(parentDocument);
			return list.contains(s);
		}		

		return false;
	}


	private void appendProcessStep(ProcessStep ps, Element liLineage,
			Document parentDocument) {
		if (hasBeenCreatedForDocument(ps, parentDocument)) {
			Element processStep = createGMDElement(parentDocument, "processStep");
			setXLinkHref(processStep, ps);
			liLineage.appendChild(processStep);
		}
		else {
			Element processStep = createGMDElement(parentDocument, "processStep");
			setXLinkTitle(processStep, ps);
			Element liProcessStep = createGMDElement(parentDocument, "LI_ProcessStep");
			Element description = createGMDElement(parentDocument, "description");
			Element charString = createGCOElement(parentDocument, "CharacterString");
			charString.appendChild(parentDocument.createTextNode(ps.getName()));
			description.appendChild(charString);
			Element dateTime = createGMDElement(parentDocument, "dateTime");
			Element gcoDateTime = createGCOElement(parentDocument, "DateTime");
			gcoDateTime.appendChild(parentDocument.createTextNode(new DateTime().toString(ISODateTimeFormat.dateTime())));
			dateTime.appendChild(gcoDateTime);
			liProcessStep.appendChild(description);
			liProcessStep.appendChild(dateTime);
			processStep.appendChild(liProcessStep);
			liLineage.appendChild(processStep);
			setCreatedForDocument(ps, parentDocument);
		}
	}



}
