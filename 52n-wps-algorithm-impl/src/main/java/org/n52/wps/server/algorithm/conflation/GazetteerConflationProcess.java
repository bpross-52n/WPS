package org.n52.wps.server.algorithm.conflation;

import java.util.List;
import java.util.Map;

import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.bbox.GTReferenceEnvelope;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralAnyURIBinding;
import org.n52.wps.io.data.binding.literal.LiteralDoubleBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.server.AbstractAlgorithm;
import org.n52.wps.server.ExceptionReport;

public class GazetteerConflationProcess extends AbstractAlgorithm {

	private final String SourceGazetteer = "Source_Gazetteer";
	private final String TargetGazetteer = "Target_Gazetteer";
	private final String SourceGazetteerDescriptionFilter = "Source_Gazetteer_Description_Filter";
	private final String TargetGazetteerDescriptionFilter = "Target_Gazetteer_Description_Filter";
	private final String BoundingBoxFilters = "Bounding_Box_Filters";
	private final String SearchDistance = "Search_Distance";
	private final String FuzzyWuzzyThreshold = "FuzzyWuzzy_Threshold";
	private final String OutputFile = "Output_File";

	@Override
	public Map<String, IData> run(Map<String, List<IData>> inputData)
			throws ExceptionReport {
		
		/*
		 * get inputs
		 * 
		 * request source gazeteer with bbox and filter
		 * whats the result?
		 * 
		 * request target gezeteer with filter
		 * 
		 * source gazetteer features are processed (sequentially, one at a time)
		 * 	get coordinates
		 * 	filter gathered target features by search distance 
		 * 	- couldn't we just request the target gazetteer over again with spatial and description filter?!
		 *  match all found features against source feature - filter results by fuzzywuzzy threshold 
		 *  also calculate distance
		 *  
		 * 
		 */
		
		
		return null;
	}

	@Override
	public List<String> getErrors() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<?> getInputDataType(String id) {
		
		if(id.endsWith(SourceGazetteer)){
			return LiteralAnyURIBinding.class;
		}else if(id.endsWith(TargetGazetteer)){
			return LiteralAnyURIBinding.class;			
		}else if(id.endsWith(SourceGazetteerDescriptionFilter)){
			return LiteralStringBinding.class;
		}else if(id.endsWith(TargetGazetteerDescriptionFilter)){
			return LiteralStringBinding.class;
		}else if(id.endsWith(BoundingBoxFilters)){
			return GTReferenceEnvelope.class;
		}else if(id.endsWith(SearchDistance)){
			return LiteralDoubleBinding.class;
		}else if(id.endsWith(FuzzyWuzzyThreshold)){
			return LiteralDoubleBinding.class;
		}
		
		return null;
	}

	@Override
	public Class<GenericFileDataBinding> getOutputDataType(String id) {		
		return GenericFileDataBinding.class;
	}

}
