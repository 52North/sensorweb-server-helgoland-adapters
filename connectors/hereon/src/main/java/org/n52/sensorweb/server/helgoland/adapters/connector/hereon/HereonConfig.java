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
package org.n52.sensorweb.server.helgoland.adapters.connector.hereon;

import java.io.InputStream;

import org.n52.sensorweb.server.helgoland.adapters.config.Credentials;
import org.n52.sensorweb.server.helgoland.adapters.connector.mapping.Mapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@Configuration
@SuppressFBWarnings({ "EI_EXPOSE_REP", "EI_EXPOSE_REP2" })
public class HereonConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(HereonConfig.class);
    private static final String DEFAULT_MAPPING = "/mapping.json";

    @Value("${service.connector.hereon.username:}")
    private String username;
    @Value("${service.connector.hereon.password:}")
    private String password;
    @Value("${service.connector.hereon.tokenUrl:}")
    private String tokenUrl;
    @Value("${service.connector.hereon.mapping.file:}")
    private String mappingFile;
    private Mapping mapping;

    private String getUsername() {
        return username;
    }

    private String getPassword() {
        return password;
    }

    private String getTokenUrl() {
        return tokenUrl;
    }

    public Credentials getCredentials() {
        return new Credentials(getUsername(), getPassword(), getTokenUrl());
    }

    public Mapping getMapping() {
        if (mapping == null) {
            loadMapping();
        }
        return mapping;
    }

    private String getMappingFile() {
        return mappingFile;
    }

    private void loadMapping() {
        String file = getMappingFile() != null && !getMappingFile().isEmpty() ? getMappingFile() : DEFAULT_MAPPING;
        try (InputStream config = getClass().getResourceAsStream(file)) {
            ObjectMapper om = new ObjectMapper();
            this.mapping = om.readValue(config, Mapping.class);
        } catch (Exception e) {
            LOGGER.error("Could not load {}. Using default mapping.", mapping, e);
            this.mapping = createDefaultMapping();
        }
    }

    private Mapping createDefaultMapping() {
        return new Mapping();
    }
}
