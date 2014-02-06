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
import java.util.List;

public class ProcessStep extends XLinkLinkable {

	private String name;
	private List<Source> sources = new ArrayList<Source>();

	public ProcessStep(String name) {
		this.name = name;
	}

	public void addSource(Source source) {
		this.sources.add(source);
	}

	public String getName() {
		return name;
	}

	public List<Source> getSources() {
		return sources;
	}


	@Override
	public String getTitle() {
		return createXLinkReference(getName());
	}

	@Override
	public String getHref() {
		StringBuilder sb = new StringBuilder();
		sb.append("#");
		sb.append(createXLinkReference(getName()));
		return sb.toString();
	}
	
}
