package org.n52.wps.server.algorithm;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import net.opengis.gml.x32.AbstractFeatureType;
import net.opengis.gml.x32.AbstractGeometryType;
import net.opengis.gml.x32.FeaturePropertyType;
import net.opengis.gml.x32.PointType;
import net.opengis.om.x20.OMObservationDocument;
import net.opengis.om.x20.OMObservationType;
import net.opengis.om.x20.impl.OMObservationDocumentImpl;
import net.opengis.om.x20.impl.OMObservationTypeImpl;
import net.opengis.samplingSpatial.x20.SFSpatialSamplingFeatureDocument;
import net.opengis.samplingSpatial.x20.SFSpatialSamplingFeatureType;
import net.opengis.samplingSpatial.x20.ShapeType;
import net.opengis.samplingSpatial.x20.impl.SFSpatialSamplingFeatureDocumentImpl;
import net.opengis.samplingSpatial.x20.impl.SFSpatialSamplingFeatureTypeImpl;

import org.apache.xerces.parsers.DOMParser;
import org.apache.xmlbeans.XmlException;
import org.n52.wps.io.data.binding.complex.GML32OMWFSDataBinding;
import org.n52.wps.io.datahandler.parser.GML32OMWFSGBasicParser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class OMTest {

	public OMTest() throws SAXException, IOException {

		InputStream tnmin = this.getClass().getResourceAsStream(
				"vgi-wfs-mockup-one-member.xml");

		GML32OMWFSGBasicParser basicParser = new GML32OMWFSGBasicParser();

		GML32OMWFSDataBinding binding = basicParser.parse(tnmin, "", "");

		ArrayList<?> arrayList = (ArrayList<?>) binding
				.getPayload();

		for (Object object : arrayList) {
			if (object instanceof OMObservationType) {

				OMObservationType omObservationType = (OMObservationType) object;

				FeaturePropertyType featureOfInterest = omObservationType
						.getFeatureOfInterest();

				AbstractFeatureType abstractFeature = featureOfInterest.getAbstractFeature();

				if (abstractFeature instanceof SFSpatialSamplingFeatureTypeImpl) {

					SFSpatialSamplingFeatureType featureTypeImpl = (SFSpatialSamplingFeatureType)abstractFeature ;
					
					ShapeType shapeType = featureTypeImpl.getShape();

					AbstractGeometryType abstractGeometryType = shapeType
							.getAbstractGeometry();

					if (abstractGeometryType instanceof PointType) {
						PointType pointType = (PointType) abstractGeometryType;

						System.out.println(pointType.getPos().getStringValue());
					}
				}

			}
		}

		// DOMParser parser = new DOMParser();
		//
		// InputStream tnmin =
		// this.getClass().getResourceAsStream("vgi-wfs-mockup-one-member.xml");
		//
		// InputSource inputSource = new InputSource(tnmin);
		//
		// parser.parse(inputSource);
		//
		// Document d = parser.getDocument();
		//
		// NodeList features =
		// d.getElementsByTagNameNS("http://www.opengis.net/wfs/2.0", "member");
		//
		// for(int i=0;i<features.getLength();i++){
		//
		// Node node = features.item(i);
		//
		// System.out.println(node.getLocalName());
		//
		// if(node.getChildNodes().getLength() > 0){
		// try {
		// OMObservationDocumentImpl observationTypeImpl =
		// (OMObservationDocumentImpl)
		// OMObservationDocument.Factory.parse(node.getChildNodes().item(1));
		//
		// FeaturePropertyType featureOfInterest =
		// observationTypeImpl.getOMObservation().getFeatureOfInterest();
		//
		// SFSpatialSamplingFeatureType featureTypeImpl =
		// SFSpatialSamplingFeatureType.Factory.newInstance();
		//
		// if(featureOfInterest instanceof SFSpatialSamplingFeatureTypeImpl){
		// featureTypeImpl =
		// (SFSpatialSamplingFeatureTypeImpl)featureOfInterest;
		// System.out.println();
		// }else{
		// SFSpatialSamplingFeatureDocument spatialSamplingFeatureType =
		// SFSpatialSamplingFeatureDocument.Factory.parse(featureOfInterest.getDomNode().getChildNodes().item(1));
		//
		// featureTypeImpl =
		// spatialSamplingFeatureType.getSFSpatialSamplingFeature();
		// }
		//
		// ShapeType shapeType = featureTypeImpl.getShape();
		//
		// AbstractGeometryType abstractGeometryType =
		// shapeType.getAbstractGeometry();
		//
		// if(abstractGeometryType instanceof PointType){
		// PointType pointType = (PointType)abstractGeometryType;
		//
		// System.out.println(pointType.getPos().getStringValue());
		// }
		//
		// } catch (XmlException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }
		// }

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			new OMTest();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
