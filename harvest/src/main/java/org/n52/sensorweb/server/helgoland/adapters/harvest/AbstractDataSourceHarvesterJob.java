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

import java.util.Optional;

import javax.inject.Inject;

import org.n52.bjornoya.schedule.ScheduledJob;
import org.n52.sensorweb.server.helgoland.adapters.connector.AbstractConnector;
import org.n52.sensorweb.server.helgoland.adapters.connector.AbstractServiceConstellation;
import org.n52.sensorweb.server.helgoland.adapters.connector.ConnectorConfiguration;
import org.n52.sensorweb.server.helgoland.adapters.connector.ConnectorConfigurationFactory;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractDataSourceHarvesterJob extends ScheduledJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDataSourceHarvesterJob.class);

    @Inject
    private DataSourceHarvesterHelper helper;

    protected DataSourceHarvesterHelper getHelper() {
        return helper;
    }

    protected DataSourceJobConfiguration getDataSourceJobConfiguration() {
        return getJobConfiguration() != null && getJobConfiguration() instanceof DataSourceJobConfiguration
                ? (DataSourceJobConfiguration) getJobConfiguration()
                : null;
    }

    protected AbstractServiceConstellation determineConstellation(DataSourceJobConfiguration dataSource) {
        if (dataSource.getType() == null && getHelper().getConnector(dataSource) != null) {
            return null;
        }
        for (ConnectorConfigurationFactory factory : getHelper().getConnectorConfigurationFactory()) {
            if (factory.checkDatasource(dataSource)) {
                try {
                    return determineConstellation(factory.createConfiguration(dataSource));
                } catch (JobExecutionException e) {
                    LOGGER.error("Error while creating constellation!", e);
                }
            }
        }
        return null;
    }

    private AbstractServiceConstellation determineConstellation(ConnectorConfiguration configuration) {
        Optional<AbstractConnector> connector = getHelper().getConnector(configuration);
        return connector.isPresent() ? connector.get().getConstellation(configuration) : null;
    }
}