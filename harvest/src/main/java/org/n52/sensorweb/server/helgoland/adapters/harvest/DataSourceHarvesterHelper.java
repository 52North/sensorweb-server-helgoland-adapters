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
package org.n52.sensorweb.server.helgoland.adapters.harvest;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import org.n52.sensorweb.server.helgoland.adapters.connector.AbstractConnector;
import org.n52.sensorweb.server.helgoland.adapters.connector.ConnectorConfiguration;
import org.n52.sensorweb.server.helgoland.adapters.connector.ConnectorConfigurationFactory;
import org.n52.sensorweb.server.helgoland.adapters.da.CRUDRepository;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings({ "EI_EXPOSE_REP" })
public class DataSourceHarvesterHelper {

    private Set<HarvestingListener> listeners = new LinkedHashSet<>();
    private Set<AbstractConnector> connectors = new LinkedHashSet<>();

    @Inject
    private CRUDRepository crudRepository;
    @Inject
    private Set<ConnectorConfigurationFactory> connectorConfigurationFactory;

    @Inject
    private void setHarvestingListeners(Optional<Set<HarvestingListener>> listeners) {
        this.listeners.clear();
        if (listeners.isPresent()) {
            this.listeners.addAll(listeners.get());
        }
    }

    @Inject
    private void setAbstractConnectors(Optional<Set<AbstractConnector>> connectors) {
        this.connectors.clear();
        if (connectors.isPresent()) {
            this.connectors.addAll(connectors.get());
        }
    }

    public void setAbstractConnectors(Set<AbstractConnector> connectors) {
        this.connectors.clear();
        if (connectors != null) {
            this.connectors.addAll(connectors);
        }
    }

    public void addAbstractConnector(AbstractConnector connector) {
        if (connector != null) {
            this.connectors.add(connector);
        }
    }

    public CRUDRepository getCRUDRepository() {
        return crudRepository;
    }

    public Set<ConnectorConfigurationFactory> getConnectorConfigurationFactory() {
        return connectorConfigurationFactory;
    }

    public Set<HarvestingListener> getHarvestListener() {
        return listeners;
    }

//    public Optional<AbstractConnector> getConnector(String name) {
//        return this.connectors.stream().filter(connector -> connector.matches(name)).findFirst();
//    }

    public Optional<AbstractConnector> getConnector(DataSourceJobConfiguration dataSourceConfiguration) {
        return this.connectors.stream().filter(connector -> connector.matches(dataSourceConfiguration)).findFirst();
    }

    public Optional<AbstractConnector> getConnector(ConnectorConfiguration configuration) {
        return this.connectors.stream().filter(connector -> connector.matches(configuration)).findFirst();
    }

}
