/*
 * Copyright (C) 2013-2017 52°North Initiative for Geospatial Open Source
 * Software GmbH
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
package org.n52.proxy.db.da;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import org.n52.io.handler.ConfigTypedFactory;
import org.n52.io.response.dataset.AbstractValue;
import org.n52.proxy.connector.AbstractConnector;
import org.n52.series.db.DataRepositoryTypeFactory;
import org.n52.series.db.HibernateSessionStore;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.da.DataRepository;
import org.n52.series.db.da.SessionAwareRepository;

public class DataRepositoryFactory<S extends DatasetEntity, E extends DataEntity<T>, V extends AbstractValue<?>, T>
        extends ConfigTypedFactory<ProxyDataRepository<S, E, V, T>>
        implements DataRepositoryTypeFactory {

    private static final String DEFAULT_CONFIG_FILE = "dataset-repository-factory-proxy.properties";

    @Autowired
    private HibernateSessionStore sessionStore;

    private final Map<String, AbstractConnector> connectorMap = new HashMap<>();

    public DataRepositoryFactory() {
        super(DEFAULT_CONFIG_FILE);
    }

    public DataRepositoryFactory(File configFile) {
        super(configFile);
    }

    @Autowired
    public void setConnectors(List<AbstractConnector> connectors) {
        connectors.forEach(connector -> connectorMap.put(connector.getConnectorName(), connector));
    }

    @Override
    protected ProxyDataRepository<S, E, V, T> initInstance(ProxyDataRepository<S, E, V, T> instance) {
        instance.setConnectorMap(connectorMap);
        if (instance instanceof SessionAwareRepository) {
            SessionAwareRepository sessionAwareRepository = (SessionAwareRepository) instance;
            sessionAwareRepository.setSessionStore(sessionStore);
        }
        return instance;
    }

    @Override
    protected String getFallbackConfigResource() {
        return DEFAULT_CONFIG_FILE;
    }

    @Override
    protected Class<ProxyDataRepository> getTargetType() {
        return ProxyDataRepository.class;
    }

    @Override
    public <S extends DatasetEntity, E extends DataEntity<T>, V extends AbstractValue<?>, T> DataRepository<S, E, V, T>
            create(String valueType, Class<S> entityType) {
        /* TODO implement org.n52.proxy.db.da.DataRepositoryFactory.create() */
        throw new UnsupportedOperationException("create() not yet implemented");
    }

    @Override
    public Class<? extends DatasetEntity> getDatasetEntityType(String valueType) {
        /* TODO implement org.n52.proxy.db.da.DataRepositoryFactory.getDatasetEntityType() */
        throw new UnsupportedOperationException("getDatasetEntityType() not yet implemented");
    }
}
