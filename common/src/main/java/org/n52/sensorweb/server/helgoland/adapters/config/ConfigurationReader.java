/*
 * Copyright (C) 2015-2022 52°North Spatial Information Research GmbH
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class ConfigurationReader implements ConfigurationProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationReader.class);

    private static final String CONFIG_FILE = "/config-data-sources.json";

    private final DataSourcesConfiguration intervalConfig = readConfig();

    private DataSourcesConfiguration readConfig() {
        try (InputStream config = getClass().getResourceAsStream(CONFIG_FILE)) {
            ObjectMapper om = new ObjectMapper();
            return om.readValue(config, DataSourcesConfiguration.class);
        } catch (Exception e) {
            LOGGER.error("Could not load {). Using empty config.", CONFIG_FILE, e);
            return new DataSourcesConfiguration();
        }
    }

    public List<DataSourceConfiguration> getDataSources() {
        return intervalConfig.getDataSources();
    }

}
