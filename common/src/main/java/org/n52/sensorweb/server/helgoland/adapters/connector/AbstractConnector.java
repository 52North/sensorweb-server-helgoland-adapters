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
package org.n52.sensorweb.server.helgoland.adapters.connector;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpResponse;
import org.apache.xmlbeans.XmlObject;
import org.n52.sensorweb.server.helgoland.adapters.config.DataSourceConfiguration;
import org.n52.sensorweb.server.helgoland.adapters.web.SimpleHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.primitives.Ints;

/**
 * @author Jan Schulte
 */
public abstract class AbstractConnector {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractConnector.class);
    private static final long CONNECTION_TIMEOUT = TimeUnit.SECONDS.toMillis(30);
    private static final long SOCKET_TIMEOUT = TimeUnit.MINUTES.toMillis(30);

    private Map<String, DataSourceConfiguration> dataSourceConfigurations = new LinkedHashMap<>();
    private final SimpleHttpClient httpClient;

    public AbstractConnector() {
        httpClient = new SimpleHttpClient(Ints.checkedCast(CONNECTION_TIMEOUT), Ints.checkedCast(SOCKET_TIMEOUT));
    }

    public abstract AbstractServiceConstellation getConstellation(ConnectorConfiguration configuration);

    public String getConnectorName() {
        return getName();
    }

    public String getName() {
        return getClass().getName();
    }

    public boolean matches(DataSourceConfiguration config) {
        if (config.getConnector() != null) {
            return getClass().getSimpleName().equals(config.getConnector())
                    || getClass().getName().equals(config.getConnector());
        }
        return false;
    }

    protected HttpResponse sendGetRequest(String uri) throws IOException {
        LOGGER.debug("Executing GET request {}", uri);
        return httpClient.executeGet(uri);
    }

    protected HttpResponse sendPostRequest(XmlObject request, String uri) throws IOException {
        LOGGER.debug("Executing POST request to {}\n{}", uri, request.xmlText());
        return httpClient.executePost(uri, request);
    }

    protected void addServiceConfig(DataSourceConfiguration config) {
        this.dataSourceConfigurations.put(config.getUrl(), config);
    }

    protected DataSourceConfiguration getServiceConfig(String key) {
        return this.dataSourceConfigurations.get(key);
    }
}
