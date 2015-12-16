/**
 * ﻿Copyright (C) 2007 - 2014 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.n52.wps.server.modules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.n52.wps.server.LocalAlgorithmRepository;
import org.n52.wps.webapp.api.AlgorithmEntry;
import org.n52.wps.webapp.api.ClassKnowingModule;
import org.n52.wps.webapp.api.ConfigurationCategory;
import org.n52.wps.webapp.api.ConfigurationKey;
import org.n52.wps.webapp.api.FormatEntry;
import org.n52.wps.webapp.api.types.ConfigurationEntry;
import org.n52.wps.webapp.api.types.IntegerConfigurationEntry;
import org.n52.wps.webapp.api.types.StringConfigurationEntry;

public class LocalAlgorithmRepositoryCM extends ClassKnowingModule{

	private boolean isActive = true;

	private List<AlgorithmEntry> algorithmEntries;
	       
	public final static String virtuosoUserKey = "virtuoso.user";
        
        private String virtuosoUser;
        
        private ConfigurationEntry<String> virtuosoUserEntry = new StringConfigurationEntry(virtuosoUserKey, "Virtuoso username", "",
                true, "dba");
        
        public final static String virtuosoPwdKey = "virtuoso.pwd";
        
        private String virtuosoPwd;
        
        private ConfigurationEntry<String> virtuosoPwdEntry = new StringConfigurationEntry(virtuosoPwdKey, "Virtuoso password", "",
                true, "dba");
        
        public final static String virtuosoJDBCUrlKey = "virtuoso.jdbcurl";
        
        private String virtuosoJDBCUrl;
        
        private ConfigurationEntry<String> virtuosoJDBCUrlEntry = new StringConfigurationEntry(virtuosoJDBCUrlKey, "Virtuoso JDBC URL", "",
                true, "jdbc:virtuoso://localhost:1111");
        
        public final static String startPosKey = "start.pos";
        
        private int startPos;
        
        private ConfigurationEntry<Integer> startPosEntry = new IntegerConfigurationEntry(startPosKey, "Catalog harvesting start position", "",
                false, 1);
	
	private List<? extends ConfigurationEntry<?>> configurationEntries = Arrays.asList(virtuosoJDBCUrlEntry, virtuosoUserEntry, virtuosoPwdEntry, startPosEntry);
	
	public LocalAlgorithmRepositoryCM() {
		algorithmEntries = new ArrayList<>();
	}
	
	@Override
	public String getModuleName() {
		return "LocalAlgorithmRepository Configuration Module";
	}

	@Override
	public boolean isActive() {
		return isActive;
	}

	@Override
	public void setActive(boolean active) {
		this.isActive = active;
	}

	@Override
	public ConfigurationCategory getCategory() {
		return ConfigurationCategory.REPOSITORY;
	}

	@Override
	public List<? extends ConfigurationEntry<?>> getConfigurationEntries() {
		return configurationEntries;
	}

	@Override
	public List<AlgorithmEntry> getAlgorithmEntries() {
		return algorithmEntries;
	}

	@Override
	public List<FormatEntry> getFormatEntries() {
		return null;
	}

	@Override
	public String getClassName() {
		return LocalAlgorithmRepository.class.getName();
	}

    public String getVirtuosoUser() {
        return virtuosoUser;
    }

    @ConfigurationKey(key = virtuosoUserKey)
    public void setVirtuosoUser(String virtuosoUser) {
        this.virtuosoUser = virtuosoUser;
    }

    public String getVirtuosoPwd() {
        return virtuosoPwd;
    }

    @ConfigurationKey(key = virtuosoPwdKey)
    public void setVirtuosoPwd(String virtuosoPwd) {
        this.virtuosoPwd = virtuosoPwd;
    }

    public String getVirtuosoJDBCUrl() {
        return virtuosoJDBCUrl;
    }

    @ConfigurationKey(key = virtuosoJDBCUrlKey)
    public void setVirtuosoJDBCUrl(String virtuosoJDBCUrl) {
        this.virtuosoJDBCUrl = virtuosoJDBCUrl;
    }

    public int getStartPos() {
        return startPos;
    }

    @ConfigurationKey(key = startPosKey)
    public void setStartPos(int startPos) {
        this.startPos = startPos;
    }

}
