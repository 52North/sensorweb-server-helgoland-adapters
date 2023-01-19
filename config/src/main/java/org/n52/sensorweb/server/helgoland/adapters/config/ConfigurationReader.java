/*
 * Copyright (C) 2015-2023 52°North Spatial Information Research GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public License
 * version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 */
package org.n52.sensorweb.server.helgoland.adapters.config;

import java.io.InputStream;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class ConfigurationReader implements ConfigurationProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationReader.class);
    private static final String CONFIG_FILE = "/config-data-sources.json";
    private static final ObjectMapper OM = new ObjectMapper();
    private DataSourcesConfiguration intervalConfig;

    @Value("${service.config.file:}")
    private String configFile;

    @Value("${service.config.removenonmatchingservices:false}")
    private boolean removeNonMatchingServices;

    @PostConstruct
    private void init() {
        this.intervalConfig = readConfig();
    }

    private DataSourcesConfiguration readConfig() {
        String config = getConfigFile() != null && !getConfigFile().isEmpty() ? getConfigFile() : CONFIG_FILE;
        DataSourcesConfiguration dataSourcesConfig = readConfig(config);
        if (dataSourcesConfig == null && !config.equals(CONFIG_FILE)) {
            dataSourcesConfig = readConfig(CONFIG_FILE);
        }
        return dataSourcesConfig != null ? dataSourcesConfig : new DataSourcesConfiguration();
    }

    private DataSourcesConfiguration readConfig(String file) {
        try (InputStream configStream = getClass().getResourceAsStream(file)) {
            return OM.readValue(configStream, DataSourcesConfiguration.class);
        } catch (Exception e) {
            LOGGER.error("Could not load {}.", file, e);
            return null;
        }
    }

    /**
     * @return the configFile
     */
    private String getConfigFile() {
        return configFile;
    }

    public List<DataSourceConfiguration> getDataSources() {
        return intervalConfig.getDataSources();
    }

    public boolean isRemoveNonMatchingServices() {
        return removeNonMatchingServices;
    }

}
