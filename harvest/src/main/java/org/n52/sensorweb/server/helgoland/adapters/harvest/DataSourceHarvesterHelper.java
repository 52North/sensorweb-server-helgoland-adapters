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
package org.n52.sensorweb.server.helgoland.adapters.harvest;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import org.n52.sensorweb.server.helgoland.adapters.connector.AbstractConnector;
import org.n52.sensorweb.server.helgoland.adapters.connector.ConnectorConfiguration;
import org.n52.sensorweb.server.helgoland.adapters.connector.ConnectorConfigurationFactory;
import org.n52.sensorweb.server.helgoland.adapters.da.CRUDRepository;
import org.springframework.util.ClassUtils;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings({ "EI_EXPOSE_REP" })
public class DataSourceHarvesterHelper {

    @Inject
    private CRUDRepository crudRepository;
    private Set<HarvestingListener> listeners = new LinkedHashSet<>();
    private Set<AbstractConnector> connectors = new LinkedHashSet<>();
    private Set<ConnectorConfigurationFactory> connectorConfigurationFactories = new LinkedHashSet<>();
    private Map<String, TemporalHarvester> temporalHarvester = new LinkedHashMap<>();
    private Map<String, FullHarvester> fullHarvester = new LinkedHashMap<>();

    @Inject
    private void setConnectorConfigurationFactories(
            Collection<ConnectorConfigurationFactory> connectorConfigurationFactories) {
        this.connectorConfigurationFactories.clear();
        if (connectorConfigurationFactories != null) {
            this.connectorConfigurationFactories.addAll(connectorConfigurationFactories);
        }
    }

    @Inject
    private void setHarvestingListeners(Optional<Collection<HarvestingListener>> listeners) {
        this.listeners.clear();
        if (listeners.isPresent()) {
            this.listeners.addAll(listeners.get());
        }
    }

    @Inject
    private void setTemporalHarvester(Collection<TemporalHarvester> harvesters) {
        this.temporalHarvester.clear();
        if (harvesters != null) {
            for (TemporalHarvester harvester : harvesters) {
                this.temporalHarvester.put(ClassUtils.getUserClass(harvester).getName(), harvester);
            }
        }
    }

    @Inject
    private void setFullHarvester(Collection<FullHarvester> harvesters) {
        this.fullHarvester.clear();
        if (harvesters != null) {
            for (FullHarvester harvester : harvesters) {
                this.fullHarvester.put(ClassUtils.getUserClass(harvester).getName(), harvester);
            }
        }
    }

    @Inject
    private void setAbstractConnectors(Optional<Collection<AbstractConnector>> connectors) {
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
        return Collections.unmodifiableSet(connectorConfigurationFactories);
    }

    public Set<HarvestingListener> getHarvestListener() {
        return listeners;
    }

    public Optional<AbstractConnector> getConnector(DataSourceJobConfiguration dataSourceConfiguration) {
        return this.connectors.stream().filter(connector -> connector.matches(dataSourceConfiguration)).findFirst();
    }

    public Optional<AbstractConnector> getConnector(ConnectorConfiguration configuration) {
        return this.connectors.stream().filter(connector -> connector.matches(configuration)).findFirst();
    }

    public TemporalHarvester getTemporalHarvester(String name) {
        return temporalHarvester.get(name);
    }

    public FullHarvester getFullHarvester(String name) {
        return fullHarvester.get(name);
    }

}
