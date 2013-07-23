package org.n52.wps.io.test.datahandler.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.datahandler.generator.GML3BasicGenerator;
import org.n52.wps.io.datahandler.parser.GML3BasicParser;
import org.n52.wps.io.test.datahandler.AbstractTestCase;

public class GML3BasicParserTest extends AbstractTestCase<GML3BasicParser> {

	public void testParsingMultipleWFSGML3FeatureMembers() {
		
		if(!isDataHandlerActive()){
			return;
		}

		String testFilePath = projectRoot
				+ "/52n-wps-io-impl/src/test/resources/spearfish_restricted_sites_gml3.xml";
		
		try {
			testFilePath = URLDecoder.decode(testFilePath, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
			fail(e1.getMessage());
		}

//		String[] mimetypes = theParser.getSupportedFormats();

		InputStream input = null;

		try {
			input = new FileInputStream(new File(testFilePath));
		} catch (FileNotFoundException e) {
			fail(e.getMessage());
		}

		// for (String mimetype : mimetypes) {

		GTVectorDataBinding theBinding = dataHandler.parse(input,
				"text/xml; subtype=gml/3.1.1",
				"http://schemas.opengis.net/gml/3.1.1/base/feature.xsd");

		String mimeType = "text/xml; subtype=gml/3.1.1";
		
		String schema = "http://schemas.opengis.net/gml/3.1.1/base/feature.xsd";
		
		GML3BasicGenerator theGenerator = new GML3BasicGenerator();

		assertNotNull(theBinding.getPayload());
		assertTrue(theBinding.getPayloadAsShpFile().exists());
		assertTrue(!theBinding.getPayload().isEmpty());
		
		try {
			InputStream stream = theGenerator.generateStream(theBinding, mimeType, schema);
			
			theBinding = dataHandler.parse(stream, mimeType, schema);

			assertNotNull(theBinding.getPayload());
			assertTrue(theBinding.getPayloadAsShpFile().exists());
			assertTrue(!theBinding.getPayload().isEmpty());
			
		} catch (IOException e) {
			System.err.println(e);
			fail(e.getMessage());
		}

		// }

	}
	
	public void testParsingMultipleOGRGML3FeatureMembers() {
		
        assertTrue(isDataHandlerActive());

		String testFilePath = projectRoot
				+ "/52n-wps-io-impl/src/test/resources/OGRGML3MultipleFeatures.xml";

		try {
			testFilePath = URLDecoder.decode(testFilePath, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			fail(e1.getMessage());
		}
		
		GML3BasicGenerator theGenerator = new GML3BasicGenerator();

//		String[] mimetypes = theParser.getSupportedFormats();

		InputStream input = null;

		try {
			input = new FileInputStream(new File(testFilePath));
		} catch (FileNotFoundException e) {
			fail(e.getMessage());
		}

		// for (String mimetype : mimetypes) {

		String mimeType = "text/xml; subtype=gml/3.1.1";
		
		String schema = "http://schemas.opengis.net/gml/3.1.1/base/feature.xsd";
		
		GTVectorDataBinding theBinding = dataHandler.parse(input, mimeType, schema);
		
		try {
			InputStream resultStream = theGenerator.generateStream(theBinding, mimeType, schema);
			
			dataHandler = new GML3BasicParser();
			
			GTVectorDataBinding parsedGeneratedBinding = dataHandler.parse(resultStream, mimeType, schema);
			
			assertNotNull(parsedGeneratedBinding.getPayload());
			assertTrue(theBinding.getPayload().size()==theBinding.getPayload().size());
			assertTrue(parsedGeneratedBinding.getPayloadAsShpFile().exists());
			assertTrue(!parsedGeneratedBinding.getPayload().isEmpty());

			InputStream resultStreamBase64 = theGenerator.generateBase64Stream(theBinding, mimeType, schema);
			
			GTVectorDataBinding parsedGeneratedBindingBase64 = (GTVectorDataBinding) dataHandler.parseBase64(resultStreamBase64, mimeType, schema);
			
			assertNotNull(parsedGeneratedBindingBase64.getPayload());
			assertTrue(parsedGeneratedBindingBase64.getPayloadAsShpFile().exists());
			assertTrue(!parsedGeneratedBindingBase64.getPayload().isEmpty());
			
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		// }

	}

	@Override
	protected void initializeDataHandler() {
		dataHandler = new GML3BasicParser();		
	}

}
