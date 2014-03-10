package org.n52.wps.io.datahandler.generator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;

public class RDFOutputGenerator extends AbstractGenerator {
	
	public static final String MIME_TYPE_RDF = "application/rdf+xml";
	public static final String MIME_TYPE_TEXT_TURTLE = "text/turtle";
	public static final String MIME_TYPE_APPLICATION_X_TURTLE = "application/x-turtle";

	public RDFOutputGenerator(){
		super();
		this.supportedIDataTypes.add(LiteralStringBinding.class);
	}
	
	@Override
	public InputStream generateStream(IData data, String mimeType, String schema)
			throws IOException {
		if(mimeType.equals(MIME_TYPE_RDF) || mimeType.equals(MIME_TYPE_TEXT_TURTLE) || mimeType.equals(MIME_TYPE_APPLICATION_X_TURTLE)){			
			if(data instanceof LiteralStringBinding){				
				return new ByteArrayInputStream(((LiteralStringBinding)data).getPayload().getBytes());				
			}
		}
		return null;
	}

}
