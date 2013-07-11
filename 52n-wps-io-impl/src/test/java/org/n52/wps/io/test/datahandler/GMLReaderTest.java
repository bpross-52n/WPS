package org.n52.wps.io.test.datahandler;

import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

public class GMLReaderTest {

	public static void main(String[] args) throws Exception {

////		Configuration configurationG = new org.geotools.gml3.v3_2.GMLConfiguration();
//		Configuration configurationG = new GMLConfiguration();
//		Parser parser = new org.geotools.xml.Parser(configurationG);
//		InputStream xml = new FileInputStream(new File("d:/chaos-folder/Polygon.gml"));
//		Object o = parser.parse(xml); 
//		
//		System.out.println(o instanceof Polygon);
		
		URL u =  new URL("https://giv-car.uni-muenster.de/dev/rest/tracks/51dcfb5ee4b0a504afb7c182");
		
		InputStream in = u.openStream();
		
		ObjectMapper objMapper = new ObjectMapper();
		
		Map<?, ?> map = objMapper.readValue(in, Map.class);
		
		System.out.println(map.size());
	}
}
