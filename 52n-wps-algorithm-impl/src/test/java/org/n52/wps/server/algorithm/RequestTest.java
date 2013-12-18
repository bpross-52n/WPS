package org.n52.wps.server.algorithm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.n52.wps.server.algorithm.conflation.PostClient;
import org.n52.wps.server.algorithm.conflation.WFSGRequestStringConstants;

public class RequestTest {

	public RequestTest() {

		
		String request = WFSGRequestStringConstants.WFS100_GET_FEATURE_WITH_QUERY_REQUEST;
		
		double[] lowerCorner = new double[]{45.27273, -67.32279};
		double[] upperCorner = new double[]{45.77162, -66.35665};
		
		String[] filterLiterals = new String[]{"PPL ", "PPLA", "PPLA2 ", "PPLA3", "PPLA4", "PPLC", "PPLF", "PPLH", "PPLL", "PPLQ", "PPLR", "PPLS", "PPLW", "PPLX", "STLMT"};
				
		String request1 = buildNGARequest(request, lowerCorner, upperCorner, "iso19112:locationType/iso19112:SI_LocationType/iso19112:identification",  filterLiterals);
		
		System.out.println(request1);
		
		request = WFSGRequestStringConstants.WFS100_NEW_BRUNSWICK_GET_FEATURE_WITH_QUERY_REQUEST;
		
//		filterLiterals = new String[]{"CITY", "TOWN", "VILG", "MUN1"};
		filterLiterals = new String[]{"CITY"};
		
		request = buildNewBrunswickRequest(request, "iso19112:locationType//iso19112:name",  filterLiterals);
		
		System.out.println(request);
		
//		String targetURL = "http://services.interactive-instruments.de/xsprojects/ows10/service/gazetteer-simple/wfs";
		String targetURL = "http://ows-svc1.compusult.net/nbgaz/services";
		
//		try {
//			
//	        StringBuffer response = new StringBuffer();
//	        BufferedReader rd = new BufferedReader(new InputStreamReader(PostClient.sendRequestForInputStream(targetURL, request)));
//	        String line;
//	        while ( (line = rd.readLine()) != null) {
//	            response = response.append(line + "\n");
//	        }
//	        rd.close();
//	        
//			System.out.println(response);	        
//			
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {

		new RequestTest();
		
	}
	

	private String buildNGARequest(String request, double[] lowerCorner, double[] upperCorner, String propertyName, String[] filterLiterals){
		
		String bbox = WFSGRequestStringConstants.FE100_BBOX.replace(WFSGRequestStringConstants.LXEXP, "" + lowerCorner[0]);
		
		bbox = bbox.replace(WFSGRequestStringConstants.LYEXP,  "" + lowerCorner[1]);
		bbox = bbox.replace(WFSGRequestStringConstants.UXEXP,  "" + upperCorner[0]);
		bbox = bbox.replace(WFSGRequestStringConstants.UYEXP,  "" + upperCorner[1]);
		
		request = request.replace(WFSGRequestStringConstants.BBOXEXP, bbox);
	
		String allOrStatements = createFilter(propertyName, filterLiterals);
		
		request = request.replace(WFSGRequestStringConstants.EQALTOEXP, allOrStatements);
		
		return request;	
		
	}
	
	private String buildNewBrunswickRequest(String request, String propertyName, String[] filterLiterals){
		
		String allOrStatements = createFilter(propertyName, filterLiterals);
		
		request = request.replace(WFSGRequestStringConstants.EQALTOEXP, allOrStatements);
		
		return request;	
		
	}

	private String createFilter(String propertyName, String[] filterLiterals){
		
		String allOrStatements = "";
		
		for (String literal : filterLiterals) {
			
			String orStatement = WFSGRequestStringConstants.FE100_EQUALTO.replace(WFSGRequestStringConstants.PROPERTYVALEXP, propertyName);
			
			orStatement = orStatement.replace(WFSGRequestStringConstants.LITERALEXP, literal.trim());
			
			allOrStatements = allOrStatements.concat(orStatement);
			
		}
		
		if(filterLiterals.length > 1){
			
			return WFSGRequestStringConstants.FE100_OR.replace(WFSGRequestStringConstants.EQALTOEXP, allOrStatements);
			
		}
		
		return allOrStatements;
	}
	
}
