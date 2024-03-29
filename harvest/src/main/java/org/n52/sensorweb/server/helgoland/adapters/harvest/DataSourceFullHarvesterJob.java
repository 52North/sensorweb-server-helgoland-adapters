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

import org.n52.bjornoya.schedule.FullHarvesterJob;
import org.n52.sensorweb.server.helgoland.adapters.connector.AbstractServiceConstellation;
import org.n52.sensorweb.server.helgoland.adapters.connector.ConnectorRequestFailedException;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
@SuppressFBWarnings({ "EI_EXPOSE_REP", "EI_EXPOSE_REP2" })
public class DataSourceFullHarvesterJob extends AbstractDataSourceHarvesterJob implements FullHarvesterJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceFullHarvesterJob.class);

    public DataSourceFullHarvesterJob() {
    }

    protected void process(JobExecutionContext context) throws JobExecutionException {
        DataSourceJobConfiguration dataSourceJobConfiguration = getDataSourceJobConfiguration();
        AbstractServiceConstellation result = determineConstellation(dataSourceJobConfiguration);
        try {
            if (result == null) {
                LOGGER.warn("No connector found for {}", dataSourceJobConfiguration);
            } else {
                FullHarvester harvester = getHelper().getFullHarvester(result.getFullHarvester());
                if (harvester == null) {
                    LOGGER.warn("No harvester found for {}", result.getFullHarvester());
                } else {
                    harvester.process(result.getHavesterContext());
                    submitEvent(result.getEvent());
                }
                for (HarvestingListener listener : getHelper().getHarvestListener()) {
                    try {
                        listener.onResult(result);
                    } catch (Throwable t) {
                        LOGGER.warn("error executing listener " + listener, t);
                    }
                }
            }
        } catch (ConnectorRequestFailedException ex) {
            throw new JobExecutionException(ex);
        }
    }

    protected Class<DataSourceFullHarvesterJob> getClazz() {
        return DataSourceFullHarvesterJob.class;
    }
}
