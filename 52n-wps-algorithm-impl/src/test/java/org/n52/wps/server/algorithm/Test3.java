package org.n52.wps.server.algorithm;

import java.io.InputStream;

import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.datahandler.parser.GML3BasicParser;

public class Test3 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		InputStream tdsin = Test3.class.getResourceAsStream("tds311.xml");
//		InputStream tdsin = Test3.class.getResourceAsStream("tnm_firestations.xml");
//		InputStream tdsin = Test3.class.getResourceAsStream("tds_firestations.xml");
		
		GML3BasicParser parser = new GML3BasicParser();
		
		GTVectorDataBinding gtv = parser.parse(tdsin, "", "");
		
		System.out.println(gtv.getPayload().size());

	}

}
