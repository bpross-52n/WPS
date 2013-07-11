package org.n52.wps.io.test.datahandler;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.feature.NameImpl;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class EnviroCarDataReaderTest {

	SimpleFeatureTypeBuilder typeBuilder;

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		new EnviroCarDataReaderTest();

	}

	private EnviroCarDataReaderTest() throws Exception {
		URL u = new URL(
				"https://giv-car.uni-muenster.de/dev/rest/tracks/51dcfb5ee4b0a504afb7c182");

		InputStream in = u.openStream();

		ObjectMapper objMapper = new ObjectMapper();

		Map<?, ?> map = objMapper.readValue(in, Map.class);

		ArrayList<?> features = null;

		for (Object o : map.keySet()) {
			Object entry = map.get(o);

			if (o.equals("features")) {
				features = (ArrayList<?>) entry;
			}
		}

		GeometryFactory geomFactory = new GeometryFactory();

		List<SimpleFeature> simpleFeatureList = new ArrayList<SimpleFeature>();
		
		String uuid = UUID.randomUUID().toString().substring(0, 5);

		String namespace = "http://www.52north.org/" + uuid;

		SimpleFeatureType sft = null;

		SimpleFeatureBuilder sfb = null;
		
		typeBuilder = new SimpleFeatureTypeBuilder();
		typeBuilder.setCRS(CRS.decode("EPSG:4326"));

		typeBuilder.setNamespaceURI(namespace);
		Name nameType = new NameImpl(namespace, "Feature-" + uuid);
		typeBuilder.setName(nameType);

		typeBuilder.add("geometry", Point.class);
		typeBuilder.add("id", String.class);
		typeBuilder.add("time", String.class);

		for (Object object : features) {				
			
			if (object instanceof LinkedHashMap<?, ?>) {
				LinkedHashMap<?, ?> featureMap = (LinkedHashMap<?, ?>) object;

				Object geometryObject = featureMap.get("geometry");
				
				Point point = null;
				
				if(geometryObject instanceof LinkedHashMap<?, ?>){
					LinkedHashMap<?, ?> geometryMap = (LinkedHashMap<?, ?>)geometryObject;
					
					Object coordinatesObject = geometryMap.get("coordinates");
					
					if(coordinatesObject instanceof ArrayList<?>){
						ArrayList<?> coordinatesList = (ArrayList<?>)coordinatesObject;
						
						Object xObj = coordinatesList.get(0);
						Object yObj = coordinatesList.get(1);
						
						point = geomFactory.createPoint(new Coordinate(Double.parseDouble(xObj.toString()), Double.parseDouble(yObj.toString())));
						
					}
				}
				
				Object propertiesObject = featureMap.get("properties");

				if (propertiesObject instanceof LinkedHashMap<?, ?>) {
					LinkedHashMap<?, ?> propertiesMap = (LinkedHashMap<?, ?>) propertiesObject;	

					/*
					 * get id and time
					 */
					
					String id = propertiesMap.get("id").toString();
					String time = propertiesMap.get("time").toString();

					Object phenomenonsObject = propertiesMap.get("phenomenons");

					if (phenomenonsObject instanceof LinkedHashMap<?, ?>) {
						LinkedHashMap<?, ?> phenomenonsMap = (LinkedHashMap<?, ?>) phenomenonsObject;
						/*
						 * properties are id, time and phenomenons
						 */
						if(sft == null){
							sft = buildFeatureType(phenomenonsMap);
							sfb = new SimpleFeatureBuilder(sft);
						}
						sfb.set("id", id);
						sfb.set("time", time);
						sfb.set("geometry", point);
						
						for (Object phenomenonKey : phenomenonsMap.keySet()) {

							Object phenomenonValue = phenomenonsMap
									.get(phenomenonKey);

							if (phenomenonValue instanceof LinkedHashMap<?, ?>) {
								LinkedHashMap<?, ?> phenomenonValueMap = (LinkedHashMap<?, ?>) phenomenonValue;

								String value = phenomenonValueMap.get("value")
										.toString();
								String unit = phenomenonValueMap.get("unit")
										.toString();
								
								/*
								 * create property name
								 */
								String propertyName = phenomenonKey.toString() + " (" + unit + ")";
								if(sfb != null){
									sfb.set(propertyName, value);
								}
								
							}

						}
						if(sfb != null){							
							simpleFeatureList.add(sfb.buildFeature(id));
						}
					}
				}

			}
		}
		
		ListFeatureCollection resultFeatureCollection = new ListFeatureCollection(sft, simpleFeatureList);
		
		System.out.println(resultFeatureCollection.size());
	}	

	private SimpleFeatureType buildFeatureType(LinkedHashMap<?, ?> properties) {

		for (Object phenomenonKey : properties.keySet()) {

			Object phenomenonValue = properties.get(phenomenonKey);

			if (phenomenonValue instanceof LinkedHashMap<?, ?>) {
				LinkedHashMap<?, ?> phenomenonValueMap = (LinkedHashMap<?, ?>) phenomenonValue;

				String unit = phenomenonValueMap.get("unit").toString();
				typeBuilder.add(phenomenonKey.toString() + " (" + unit + ")",
						String.class);
			}

		}
		return typeBuilder.buildFeatureType();
	}

}
