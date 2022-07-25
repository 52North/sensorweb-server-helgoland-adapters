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

import static java.util.stream.Collectors.toSet;

import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.n52.bjornoya.schedule.JobHandler;
import org.n52.bjornoya.schedule.ScheduledJob;
import org.n52.janmayen.lifecycle.Constructable;
import org.n52.sensorweb.server.helgoland.adapters.config.ConfigurationReader;
import org.n52.sensorweb.server.helgoland.adapters.config.DataSourceConfiguration;
import org.n52.sensorweb.server.helgoland.adapters.da.CRUDRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataSourceHarvestingJobFactory implements Constructable {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceHarvestingJobFactory.class);

    @Inject
    private ConfigurationReader configurationProvider;
    @Inject
    private CRUDRepository crudRepository;
    @Inject
    private JobHandler jobHandler;

    @Override
    public void init() {
        Set<DataSourceConfiguration> configuredServices = configurationProvider.getDataSource()
                .stream()
                .filter(t -> t.getJob().isEnabled())
                .collect(toSet());
        crudRepository.removeNonMatchingServices(configuredServices);

        Set<ScheduledJob> jobs = configurationProvider.getDataSource().stream()
                .peek(config -> LOGGER.info("{} {}", config.getItemName(), config.getUrl())).map(config -> {
                    DataSourceHarvesterJob job = new DataSourceHarvesterJob();
                    job.init(config);
                    return job;
                }).collect(Collectors.toSet());
        jobHandler.addScheduledJobs(jobs);
    }

}
