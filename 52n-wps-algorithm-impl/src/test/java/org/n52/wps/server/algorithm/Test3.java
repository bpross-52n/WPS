package org.n52.wps.server.algorithm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.datahandler.generator.GML3ApplicationSchemaGenerator;
import org.n52.wps.io.datahandler.generator.GML3BasicGenerator;
import org.n52.wps.io.datahandler.parser.GML311BasicParser;
import org.n52.wps.io.datahandler.parser.GML3ApplicationSchemaParser;

public class Test3 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
//		InputStream tdsin = Test3.class.getResourceAsStream("tds311.xml");
//		InputStream tdsin = Test3.class.getResourceAsStream("tnm_firestations.xml");
		InputStream tdsin = Test3.class.getResourceAsStream("tds_firestations.xml");
		
//		GML311BasicParser parser = new GML311BasicParser();
		GML3ApplicationSchemaParser parser = new GML3ApplicationSchemaParser();
		
		GTVectorDataBinding gtv = parser.parse(tdsin, "", "");
		
		System.out.println(gtv.getPayload().size());
		
//		GML3BasicGenerator generator = new GML3BasicGenerator();
		GML3ApplicationSchemaGenerator generator = new GML3ApplicationSchemaGenerator();
		
		try {
			InputStream in = generator.generateStream(gtv, "", "");
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File("d:/tmp/generatedConflationResult2.xml")));
			
			String line = "";
			
			while((line = reader.readLine()) != null){
				writer.write(line);
			}
			writer.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
