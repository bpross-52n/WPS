package org.n52.wps.io.data.binding.complex;

import java.util.List;

import org.n52.wps.io.data.GazetteerConflationResultEntry;
import org.n52.wps.io.data.IComplexData;

public class GazetteerRelationalOutputDataBinding implements IComplexData {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4703211210705479994L;

	private List<GazetteerConflationResultEntry> payload;
	
	private boolean gazetteerMatching = true;
	
	public GazetteerRelationalOutputDataBinding(List<GazetteerConflationResultEntry> payload) {
		this.payload = payload;
	}
	
	@Override
	public List<GazetteerConflationResultEntry> getPayload() {
		return payload;
	}

	@Override
	public Class<?> getSupportedClass() {
		return payload.getClass();
	}

	public boolean isGazetteerMatching() {
		return gazetteerMatching;
	}

	public void setGazetteerMatching(boolean gazetteerMatching) {
		this.gazetteerMatching = gazetteerMatching;
	}

	@Override
	public void dispose() {
		
	}

}
