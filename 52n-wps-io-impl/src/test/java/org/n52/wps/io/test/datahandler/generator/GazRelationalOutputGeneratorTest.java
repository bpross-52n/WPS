package org.n52.wps.io.test.datahandler.generator;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.n52.wps.io.data.GazetteerConflationResultEntry;
import org.n52.wps.io.data.binding.complex.GazetteerRelationalOutputDataBinding;
import org.n52.wps.io.datahandler.generator.GazetteerRelationalOutputGenerator;
import org.n52.wps.io.test.datahandler.AbstractTestCase;

public class GazRelationalOutputGeneratorTest extends AbstractTestCase<GazetteerRelationalOutputGenerator> {

	public void testGenerator() {
		
		if(!isDataHandlerActive()){
			return;
		}
		
		
		List<GazetteerConflationResultEntry> testList = new ArrayList<GazetteerConflationResultEntry>();
		
		testList.add(new GazetteerConflationResultEntry(100,3.3095958592753956,"-563601","e69500bfd05211d892e2080020a0f4c9","DIEPPE","Dieppe"));
		testList.add(new GazetteerConflationResultEntry(100,2.9007564763511753,"-569143","0cf6fd72849c20c3c78006d0fb9f7aa4","MEMRAMCOOK","Memramcook"));
		
		GazetteerRelationalOutputDataBinding dataBinding = new GazetteerRelationalOutputDataBinding(testList);
				
		String[] mimetypes2 = dataHandler.getSupportedFormats();
		
		for (String mimeType : mimetypes2) {

			try {
				InputStream generatedStream = dataHandler.generateStream(dataBinding, mimeType, null);
				
				int bite = -1;
				
				String output = "";
				
				try {
					while ((bite = generatedStream.read()) != -1) {
						output = output.concat(String.valueOf((char)bite));
					}
				} catch (IOException e) {
					LOGGER.error("Failed to read result inputstream.");
					fail();
				}
				
				if(mimeType.equals(GazetteerRelationalOutputGenerator.MIME_TYPE_CSV)){
				
					assertTrue(output.contains("FW Score,Dist(MI),NGA_UFI,NB_ID,NGA_NAME,NB_NAME"));
					assertTrue(output.contains("100,3.3095958592753956,-563601,e69500bfd05211d892e2080020a0f4c9,DIEPPE,Dieppe"));
					assertTrue(output.contains("100,2.9007564763511753,-569143,0cf6fd72849c20c3c78006d0fb9f7aa4,MEMRAMCOOK,Memramcook"));
				
				}else if(mimeType.equals(GazetteerRelationalOutputGenerator.MIME_TYPE_RDF)){
					
					assertTrue(output.contains(GazetteerRelationalOutputGenerator.PREFIXES));
					assertTrue(output.contains("<http://earth-info.nga.mil/gns#-563601>"));
					assertTrue(output.contains("<http://www.nrcan.gc.ca/resource/e69500bfd05211d892e2080020a0f4c9>"));
					assertTrue(output.contains("wps:numResults 2;"));
					assertTrue(output.contains("rdfs:label 'Dieppe';"));
					assertTrue(output.contains("id:identifier \"e69500bfd05211d892e2080020a0f4c9\"."));
					assertTrue(output.contains("id:identifier \"-563601\"."));
					assertTrue(output.contains("rdfs:label 'Memramcook';"));
					assertTrue(output.contains("id:identifier \"0cf6fd72849c20c3c78006d0fb9f7aa4\"."));
					assertTrue(output.contains("id:identifier \"-569143\"."));
					
					System.out.println(output);
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	@Override
	protected void initializeDataHandler() {
		dataHandler = new GazetteerRelationalOutputGenerator();
		
	}
	
}
