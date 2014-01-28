package org.n52.wps.server.algorithm.conflation;

public class WFSGRequestStringConstants {

	public static final String LXEXP = "$lX$";
	public static final String LYEXP = "$lY$";
	public static final String UXEXP = "$uX$";
	public static final String UYEXP = "$uY$";
	public static final String BBOXEXP = "$BBOX$";
	public static final String EQALTOEXP = "$EQUALTO$";
	public static final String LITERALEXP = "$LITERAL$";
	public static final String PROPERTYVALEXP = "$PROPERTYVAL$";
	public static final String MAXFEATURESEXP = "$MAXFEATURES$";

	public static final String WFS100_GET_FEATURE_WITH_QUERY_REQUEST = "<wfs:GetFeature xmlns:gml=\"http://www.opengis.net/gml\"\n"
			+ " xmlns:iso19112=\"http://www.isotc211.org/19112\" xmlns:wfs=\"http://www.opengis.net/wfs\"\n"
			+ " xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
			+ " service=\"WFS\" version=\"1.1.0\"\n"
			+ " xsi:schemaLocation=\"http://www.opengis.net/wfs http://schemas.opengis.net/wfs/1.1.0/wfs.xsd\" maxFeatures=\"" + MAXFEATURESEXP + "\">\n"
			+ "<wfs:Query typeName=\"iso19112:SI_LocationInstance\">\n"
			+ "<ogc:Filter>\n"
			+ "<ogc:And>\n"
			+ BBOXEXP
			+ EQALTOEXP
			+ "</ogc:And>\n"
			+ "</ogc:Filter>\n"
			+ "</wfs:Query>" + "</wfs:GetFeature>\n";

	public static final String WFS100_NEW_BRUNSWICK_GET_FEATURE_WITH_QUERY_REQUEST = "<wfs:GetFeature xmlns:gml=\"http://www.opengis.net/gml\""
			+ " xmlns:iso19112=\"http://www.isotc211.org/19112\" xmlns:wfs=\"http://www.opengis.net/wfs\""
			+ " xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
			+ " service=\"WFS\" version=\"1.1.0\" outputFormat=\"text/xml; subtype=gml/3.2.1\""
			+ " xsi:schemaLocation=\"http://www.opengis.net/wfs http://schemas.opengis.net/wfs/1.1.0/wfs.xsd\" maxFeatures=\"" + MAXFEATURESEXP + "\">"
			+ "<wfs:Query typeName=\"iso19112:SI_LocationInstance\">"
			+ "<ogc:Filter>"
			+ EQALTOEXP
			+ "</ogc:Filter>" + "</wfs:Query>" + "</wfs:GetFeature>\n";

	public static final String FE100_BBOX = "<ogc:BBOX>\n"
			+ "<gml:Envelope srsName=\"urn:ogc:def:crs:EPSG::4326\">\n"
			+ "<gml:lowerCorner>" + LXEXP + " " + LYEXP
			+ "</gml:lowerCorner>\n" + "<gml:upperCorner>" + UXEXP + " "
			+ UYEXP + "</gml:upperCorner>\n" + "</gml:Envelope>"
			+ "</ogc:BBOX>\n";

	public static final String FE100_EQUALTO = "<ogc:PropertyIsEqualTo>\n"
			+ "<ogc:PropertyName>" + PROPERTYVALEXP
			+ "</ogc:PropertyName>" + "<ogc:Literal>" + LITERALEXP
			+ "</ogc:Literal>\n" + "</ogc:PropertyIsEqualTo>\n";

	public static final String FE100_OR = "<ogc:Or>\n"
			+ EQALTOEXP
			+ "</ogc:Or>\n";
	
}
