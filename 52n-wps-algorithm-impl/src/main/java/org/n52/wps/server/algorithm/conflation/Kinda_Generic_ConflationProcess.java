package org.n52.wps.server.algorithm.conflation;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureFactory;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.GeometryAttributeImpl;
import org.geotools.feature.type.GeometryDescriptorImpl;
import org.geotools.feature.type.GeometryTypeImpl;
import org.geotools.filter.identity.GmlObjectIdImpl;
import org.n52.wps.io.GTHelper;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.server.AbstractAlgorithm;
import org.n52.wps.server.ExceptionReport;
import org.opengis.feature.Feature;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.GeometryType;
import org.opengis.filter.identity.Identifier;

import com.vividsolutions.jts.geom.Geometry;

public class Kinda_Generic_ConflationProcess extends AbstractAlgorithm{

	private final String source_id = "Source";
	private final String target_id = "Target";
	private final String rules_id = "Rules";
	private final String output_id = "output";
	private final String rule_conflate_all = "CONFLATE ALL";
	
	@Override
	public Map<String, IData> run(Map<String, List<IData>> inputData)
			throws ExceptionReport {
		
		if (inputData == null || !inputData.containsKey(source_id)) {
			throw new RuntimeException(
					"Error while allocating input parameters");
		}
		
		List<IData> dataList = inputData.get(source_id);
		if (dataList == null || dataList.size() != 1) {
			throw new RuntimeException(
					"Error while allocating input parameters");
		}
		
		IData firstInputData = dataList.get(0);
		FeatureCollection<?, ?> featureCollection = ((GTVectorDataBinding) firstInputData)
				.getPayload();

		FeatureIterator<?> iter = featureCollection.features();
		
		if (inputData == null || !inputData.containsKey(target_id)) {
			throw new RuntimeException(
					"Error while allocating input parameters");
		}
		
		List<IData> dataList1 = inputData.get(target_id);
		if (dataList1 == null || dataList1.size() != 1) {
			throw new RuntimeException(
					"Error while allocating input parameters");
		}
		
		IData firstInputData1 = dataList1.get(0);
		FeatureCollection<?, ?> featureCollection1 = ((GTVectorDataBinding) firstInputData1)
				.getPayload();

		FeatureIterator<?> iter1 = featureCollection1.features();
		
		/*
		 * get source description filter 
		 */
		List<IData> rulesData = inputData.get(rules_id);
		
		String rules = null;
		
		try {
			rules = ((LiteralStringBinding)rulesData.get(0)).getPayload();
		} catch (ClassCastException e) {
			throw new RuntimeException(rules_id + " input value is not a String.");
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new RuntimeException("No value for input " + rules_id + " provided.");
		}
		
		Map<String, String> map = createMapping(rules);

		FeatureType ft = featureCollection.features().next().getType();
		
		SimpleFeature f = null;

		while (iter1.hasNext()) {
			Object o = iter1.next();
			if (o instanceof SimpleFeature) {
				f = (SimpleFeature) o;
				
				SimpleFeature sft = (SimpleFeature) GTHelper.createFeature(f.getIdentifier().getID(), (Geometry) f.getDefaultGeometry(), (SimpleFeatureType)ft);
				
				mapProperties(f, sft, map);
				
			}

		}
		
		return null;
	}

	public Geometry tryCreatingGeom(SimpleFeature feature){
		
		Geometry g = null;		
		if(feature.getDefaultGeometry()==null){
			Collection<org.opengis.feature.Property>properties = feature.getProperties();
			for(org.opengis.feature.Property property : properties){
				try{						
					g = (Geometry)property.getValue();
				}catch(ClassCastException e){
					//do nothing
				}
				
			}
		}
		return g;
	}
	
	public void mapProperties(SimpleFeature target, SimpleFeature newFeature, Map<String, String> map){
		
		//look for mappings target attribute name -> source attribute name
		Collection<Property> properties = target.getProperties();
		
		for (Property property : properties) {
			if(map.keySet().contains(property.getName().getLocalPart())){
				addPropertyValue(newFeature, map.get(property.getName().getLocalPart()), property.getValue());
			}
		}		
	}
	
	public void addPropertyValue(SimpleFeature ft, String propertyName, Object propertyValueToAdd){
		
		Collection<Property> properties = ft.getProperties();
		
		for (Property property : properties) {
			if(property.getName().getLocalPart().equals(propertyName)){
				property.setValue(propertyValueToAdd);
			}
		}	
	}
	
	public Map<String, String> createMapping(String rules){
		
		Map<String, String> map = new HashMap<String, String>();
		
		String[] rulesArray = rules.split(",");
		
		for (String string : rulesArray) {
			if(string.startsWith("mappings")){
				string = string.replace("mappings:[", "");
				string = string.replace("]", "");
				String[] singleRulesArray = string.split(";");
				for (String string2 : singleRulesArray) {
					
					String[] mapping = string2.split("->");
					
					if(mapping.length > 1){
					
						map.put(mapping[0], mapping[1]);
					}
				}
			}
		}
		
		return map;
		
	}
	
	@Override
	public List<String> getErrors() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<?> getInputDataType(String id) {
		if(id.equals(source_id) || id.equals(target_id)){
			return GTVectorDataBinding.class;
		}else if(id.equals(rules_id)){
			return LiteralStringBinding.class;
		}
		return null;
	}

	@Override
	public Class<?> getOutputDataType(String id) {		
		if(id.equals(output_id)){
			return GTVectorDataBinding.class;
		}		
		return null;
	}

}
