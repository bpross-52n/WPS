package org.n52.wps.io.data.binding.complex;

import java.util.ArrayList;
import java.util.List;

import net.opengis.om.x20.OMObservationType;

import org.n52.wps.io.data.IComplexData;

public class GML32OMWFSDataBinding implements IComplexData{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7982399748462612511L;

	private ArrayList<OMObservationType> payload;
	
	public GML32OMWFSDataBinding(ArrayList<OMObservationType> payload){
		this.payload = payload;
	}
	
	@Override
	public List<OMObservationType> getPayload() {
		return payload;
	}

	@Override
	public Class<?> getSupportedClass() {
		return payload.getClass();
	}

	@Override
	public void dispose() {
	}

}
