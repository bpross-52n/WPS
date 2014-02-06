/**
 * Copyright (C) 2012
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
 */
package org.n52.wps.io.datahandler;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;


import org.eclipse.xsd.XSDSchema;
import org.geotools.data.DataUtilities;
import org.geotools.gml3.ApplicationSchemaXSD;
import org.geotools.gml3.v3_2.GML;
import org.geotools.xml.SchemaLocationResolver;
import org.n52.wps.io.datahandler.SchemaLocationHandler.FeatureTypeSchema;

public class ApplicationSchemaXSDWithGMLVersion extends ApplicationSchemaXSD {

	private String gmlNamespace;

	public ApplicationSchemaXSDWithGMLVersion(String namespaceURI,
			String schemaLocation, String gml) {
		super(namespaceURI, schemaLocation);
		this.gmlNamespace = gml;
		
	}
	
	
	public String getGmlNamespace() {
		return gmlNamespace;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected void addDependencies(Set dependencies) {
		if (gmlNamespace.equals(FeatureTypeSchema.GML_32_NAMESPACE)) {
			dependencies.add(GML.getInstance());
		} else {
			dependencies.add(org.geotools.gml3.GML.getInstance());
		}
	}
	
	@Override
	public String getSchemaLocation() {
		return super.getSchemaLocation();
	}
	
	@Override
	public SchemaLocationResolver createSchemaLocationResolver() {
        return new SchemaLocationResolver(this) {
                public String resolveSchemaLocation(XSDSchema schema, String uri, String location) {
                    String schemaLocation;

                    if (schema == null) {
                        schemaLocation = getSchemaLocation();
                    } else {
                        schemaLocation = schema.getSchemaLocation();
                    }

                    String locationUri = null;
                    String schemaLocationFolder = schemaLocation;
                    if ((null != schemaLocation) && !("".equals(schemaLocation))) {
                        
                        int lastSlash = schemaLocation.lastIndexOf('/');

                        if (lastSlash > 0) {
                            schemaLocationFolder = schemaLocation.substring(0, lastSlash);
                        }

                        if (schemaLocationFolder.startsWith("file:")) {
                            try {
                                schemaLocationFolder = DataUtilities.urlToFile(
                                        new URL(schemaLocationFolder)).getPath();
                            } catch (MalformedURLException e) {
                                // this can't be a good outcome, but try anyway
                                schemaLocationFolder = schemaLocationFolder.substring("file:".length());
                            }
                        }

                        File locationFile = new File(schemaLocationFolder, location);

                        if (locationFile.exists()) {
                            locationUri = locationFile.toURI().toString();
                        } else if (schemaLocationFolder.startsWith("http:")) {
                        	if (SchemaLocationHandler.isSchemaDownloadEnabled()) {
                        		locationUri = SchemaLocationHandler.resolveFromLocalSchemaRepository(schemaLocationFolder, location);
                        	}
                        	
                        	if (locationUri == null) {
                        		locationUri = resolveRelativeURL(schemaLocationFolder, location);
                        	}
                    	}
                    }

                    if ((locationUri == null) && (location != null)) {
                    	if (location.startsWith("http:")){
                    		locationUri = location;
                    	}
                    }

                    return locationUri;
                }

				private String resolveRelativeURL(
						String schemaLocationFolder, String location) {
					if (location.contains("..")) {
						return null;
					}
					return schemaLocationFolder + "/" + location;
				}
            };
    }
	
}