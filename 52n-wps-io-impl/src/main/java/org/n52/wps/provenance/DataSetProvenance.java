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
package org.n52.wps.provenance;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.n52.wps.io.data.IData;

public class DataSetProvenance implements IData {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String id;
	private Date timestamp;
	private Map<String, Double> quantitiveResults = new HashMap<String, Double>();
	private List<ProcessStep> processSteps = new ArrayList<ProcessStep>();
	private List<Source> sources = new ArrayList<Source>();

	public DataSetProvenance(String string) {
		this.id = string;
	}

	@Override
	public Object getPayload() {
		return this;
	}

	@Override
	public Class<?> getSupportedClass() {
		return DataSetProvenance.class;
	}

	public void addProcessStep(ProcessStep processStep) {
		this.processSteps.add(processStep);
	}
	
	public void addSource(Source source) {
		this.sources.add(source);
	}

	public void setTimestamp(Date date) {
		this.timestamp = date;
	}

	public void addQuantitiveResult(String key, double value) {
		this.quantitiveResults.put(key, value);
	}

	public String getISODateTime() {
		DateTime time = new DateTime(this.timestamp);
		return time.toString(ISODateTimeFormat.dateTime());
	}
	
	public Date getTimestamp() {
		return timestamp;
	}

	public Map<String, Double> getQuantitiveResults() {
		return quantitiveResults;
	}

	public List<ProcessStep> getProcessSteps() {
		return processSteps;
	}

	public String getId() {
		return id;
	}

	public List<Source> getSources() {
		return sources;
	}

	public Source resolveSourceByName(String string) {
		for (Source s : this.sources) {
			if (s.getName().equals(string)) {
				return s;
			}
		}
		return null;
	}
	

}
