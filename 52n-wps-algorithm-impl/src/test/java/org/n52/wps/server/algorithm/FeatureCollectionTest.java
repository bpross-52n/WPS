package org.n52.wps.server.algorithm;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.FeatureCollection;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.spatial.Intersects;

import com.vividsolutions.jts.geom.Envelope;

public class FeatureCollectionTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		String getCapabilities = "http://localhost:8080/geoserver/wfs?REQUEST=GetCapabilities";
//
//		Map connectionParameters = new HashMap();
//		connectionParameters.put("WFSDataStoreFactory:GET_CAPABILITIES_URL", getCapabilities );
//
//		// Step 2 - connection
//		DataStore data = DataStoreFinder.getDataStore( connectionParameters );
//
//		// Step 3 - discouvery
//		String typeNames[] = data.getTypeNames();
//		String typeName = typeNames[0];
//		SimpleFeatureType schema = data.getSchema( typeName );
//
//		// Step 4 - target
//		FeatureSource<SimpleFeatureType, SimpleFeature> source = data.getFeatureSource( typeName );
//		System.out.println( "Metadata Bounds:"+ source.getBounds() );
//
//		// Step 5 - query
//		String geomName = schema.getDefaultGeometry().getLocalName();
//		Envelope bbox = new Envelope( -100.0, -70, 25, 40 );
//
//		FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2( GeoTools.getDefaultHints() );
//		Object polygon = JTS.toGeometry( bbox );
//		Intersects filter = ff.intersects( ff.property( geomName ), ff.literal( polygon ) );
//
//		Query query = new DefaultQuery( typeName, filter, new String[]{ geomName } );
//		FeatureCollection<SimpleFeatureType, SimpleFeature> features = source.getFeatures( query );
//
//		ReferencedEnvelope bounds = new ReferencedEnvelope();
//		Iterator<SimpleFeature> iterator = features.iterator();
//		try {
//		    while( iterator.hasNext() ){
//		        Feature feature = (Feature) iterator.next();
//		    bounds.include( feature.getBounds() );
//		}
//		    System.out.println( "Calculated Bounds:"+ bounds );
//		}
//		finally {
//		    features.close( iterator );
//		}

	}

}
