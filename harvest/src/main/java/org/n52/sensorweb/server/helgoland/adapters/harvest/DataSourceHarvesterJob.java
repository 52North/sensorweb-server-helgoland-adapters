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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import org.n52.bjornoya.schedule.FullHarvesterJob;
import org.n52.bjornoya.schedule.JobConfiguration;
import org.n52.bjornoya.schedule.ScheduledJob;
import org.n52.sensorweb.server.helgoland.adapters.config.DataSourceConfiguration;
import org.n52.sensorweb.server.helgoland.adapters.connector.AbstractConnector;
import org.n52.sensorweb.server.helgoland.adapters.connector.AbstractServiceConstellation;
import org.n52.sensorweb.server.helgoland.adapters.connector.ConnectorConfiguration;
import org.n52.sensorweb.server.helgoland.adapters.connector.ConnectorConfigurationFactory;
import org.n52.sensorweb.server.helgoland.adapters.connector.ConnectorRequestFailedException;
import org.n52.sensorweb.server.helgoland.adapters.connector.utils.ServiceConstellation;
import org.n52.sensorweb.server.helgoland.adapters.da.CRUDRepository;
import org.n52.series.db.beans.CategoryEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.beans.FeatureEntity;
import org.n52.series.db.beans.OfferingEntity;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.beans.ServiceEntity;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
@SuppressFBWarnings({"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
public class DataSourceHarvesterJob extends ScheduledJob implements FullHarvesterJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceHarvesterJob.class);

    @Inject
    private CRUDRepository crudRepository;
    @Inject
    private Optional<Set<HarvestingListener>> listeners;
    @Inject
    private Optional<Set<AbstractConnector>> connectors;
    @Inject
    private Set<ConnectorConfigurationFactory> connectorConfigurationFactory;

    private DataSourceConfiguration config;

    public DataSourceHarvesterJob() {
    }

    @Override
    public JobDetail createJobDetails() {
        JobDataMap dataMap = getJobDataMap();
        dataMap.put(JOB_CONFIG, config);
        return JobBuilder.newJob(DataSourceHarvesterJob.class).withIdentity(getJobName()).usingJobData(dataMap)
                .build();
    }

    private DataSourceConfiguration recreateConfig(JobDataMap jobDataMap) {
        return (DataSourceConfiguration) jobDataMap.get(JOB_CONFIG);
    }

    protected void process(JobExecutionContext context) throws JobExecutionException {
        DataSourceConfiguration dataSource = recreateConfig(context.getJobDetail().getJobDataMap());
        AbstractServiceConstellation result = determineConstellation(dataSource);
        try {
            if (result == null) {
                LOGGER.warn("No connector found for {}", dataSource);
            } else {
                saveConstellation(result);
                if (listeners.isPresent()) {
                    for (HarvestingListener listener : listeners.get()) {
                        try {
                            listener.onResult(result);
                        } catch (Throwable t) {
                            LOGGER.warn("error executing listener " + listener, t);
                        }
                    }
                }
            }
        } catch (ConnectorRequestFailedException ex) {
            throw new JobExecutionException(ex);
        }
    }

    public void init(JobConfiguration initConfig) {
        super.init(initConfig);
        if (initConfig instanceof DataSourceConfiguration) {
            DataSourceConfiguration dsc = (DataSourceConfiguration) initConfig;
            setJobName(dsc.getItemName());
        }
    }

    private void setConfig(DataSourceConfiguration initConfig) {
        this.config = initConfig;
    }

    @Transactional(rollbackFor = Exception.class)
    protected void saveConstellation(AbstractServiceConstellation abstractConstellation) {
        if (abstractConstellation instanceof ServiceConstellation) {
            ServiceConstellation constellation = (ServiceConstellation) abstractConstellation;
            // serviceEntity
            ServiceEntity service = crudRepository.insertService(constellation.getService());
            Set<Long> datasetIds = crudRepository.getIdsForService(service);
            int datasetCount = datasetIds.size();

            // save all constellations
            constellation.getDatasets().forEach(dataset -> {
                ProcedureEntity procedure = constellation.getProcedures().get(dataset.getProcedure());
                CategoryEntity category = constellation.getCategories().get(dataset.getCategory());
                FeatureEntity feature = constellation.getFeatures().get(dataset.getFeature());
                OfferingEntity offering = constellation.getOfferings().get(dataset.getOffering());
                PhenomenonEntity phenomenon = constellation.getPhenomena().get(dataset.getPhenomenon());
                PlatformEntity platform = constellation.getPlatforms().get(dataset.getPlatform());

                List<DescribableEntity> entities =
                        Arrays.asList(procedure, category, feature, offering, phenomenon, platform);
                if (entities.stream().allMatch(Objects::nonNull)) {
                    entities.stream().forEach(x -> x.setService(service));
                    DatasetEntity ds = crudRepository.insertDataset(dataset.createDatasetEntity(procedure, category,
                            feature, offering, phenomenon, platform, service));
                    if (ds != null) {
                        datasetIds.remove(ds.getId());

                        dataset.getFirst().ifPresent(data -> crudRepository.insertData(ds, data));
                        dataset.getLatest().ifPresent(data -> crudRepository.insertData(ds, data));
                        LOGGER.info("Added dataset: {}", dataset);
                    } else {
                        LOGGER.warn("Can't save dataset: {}", dataset);
                    }
                } else {
                    LOGGER.warn("Can't add dataset: {}", dataset);
                }
            });

            crudRepository.cleanUp(service, datasetIds, datasetCount > 0 && datasetIds.size() == datasetCount);
        }
    }

    private AbstractServiceConstellation determineConstellation(DataSourceConfiguration dataSource) {
        if (dataSource.getType() == null && connectors.isPresent()) {
            return null;
        }
        for (ConnectorConfigurationFactory factory : connectorConfigurationFactory) {
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
        return this.connectors.get().stream()
                .filter(connector -> connector.matches(configuration.getDataSourceConfiguration()))
                .map(connector -> connector.getConstellation(configuration)).findFirst().orElse(null);
    }

}
