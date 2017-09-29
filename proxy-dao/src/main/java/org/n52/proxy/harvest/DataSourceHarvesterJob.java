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
package org.n52.proxy.harvest;

import static org.apache.xmlbeans.XmlObject.Factory.parse;
import static org.n52.svalbard.util.CodingHelper.getDecoderKey;
import static org.quartz.JobBuilder.newJob;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.PersistJobDataAfterExecution;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import org.n52.io.task.ScheduledJob;
import org.n52.proxy.config.DataSourceConfiguration;
import org.n52.proxy.config.DataSourceJobConfiguration;
import org.n52.proxy.connector.AbstractConnector;
import org.n52.proxy.connector.AbstractSosConnector;
import org.n52.proxy.connector.ConnectorRequestFailedException;
import org.n52.proxy.connector.SensorThingsConnector;
import org.n52.proxy.connector.utils.ServiceConstellation;
import org.n52.proxy.db.beans.ProxyServiceEntity;
import org.n52.proxy.db.da.InsertRepository;
import org.n52.proxy.web.SimpleHttpClient;
import org.n52.series.db.beans.CategoryEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.beans.FeatureEntity;
import org.n52.series.db.beans.OfferingEntity;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.shetland.ogc.ows.service.GetCapabilitiesResponse;
import org.n52.svalbard.decode.DecoderRepository;
import org.n52.svalbard.decode.exception.DecodingException;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class DataSourceHarvesterJob extends ScheduledJob implements Job {

    private static final Logger LOGGER = getLogger(DataSourceHarvesterJob.class);

    private static final String JOB_CONFIG = "config";

    private DataSourceConfiguration config;

    @Autowired
    private InsertRepository insertRepository;

    @Autowired
    private DecoderRepository decoderRepository;

    @Autowired
    private Set<AbstractConnector> connectors;

    public DataSourceHarvesterJob() {
    }

    public DataSourceConfiguration getConfig() {
        return config;
    }

    public void setConfig(DataSourceConfiguration config) {
        this.config = config;
    }

    @Override
    public JobDetail createJobDetails() {
        JobDataMap dataMap = new JobDataMap();
        dataMap.put(JOB_CONFIG, config);
        return newJob(DataSourceHarvesterJob.class)
                .withIdentity(getJobName())
                .usingJobData(dataMap)
                .build();
    }

    private DataSourceConfiguration recreateConfig(JobDataMap jobDataMap) {
        return (DataSourceConfiguration) jobDataMap.get(JOB_CONFIG);
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobKey key = context.getJobDetail().getKey();
        LOGGER.info("{} execution starts.", key);

        DataSourceConfiguration dataSource = recreateConfig(context.getJobDetail().getJobDataMap());

        ServiceConstellation constellation;
        try {
            constellation = determineConstellation(dataSource);
            if (constellation == null) {
                LOGGER.warn("No connector found for {}", dataSource);
            } else {
                saveConstellation(constellation);
            }

            LOGGER.info("{} execution ends.", key);
        } catch (IOException | DecodingException | ConnectorRequestFailedException ex) {
            throw new JobExecutionException(ex);
        }

    }

    private ServiceConstellation determineConstellation(DataSourceConfiguration dataSource)
            throws IOException, DecodingException {
        if (dataSource.getType() == null) {
            return null;
        }
        if (dataSource.getType().equals("SOS")) {
            GetCapabilitiesResponse capabilities = getCapabilities(dataSource.getUrl());
            return determineSOSConstellation(dataSource, capabilities);
        }
        if (dataSource.getType().equals("SensorThings")) {
            return determineSensorThingsConstellation(dataSource);
        }
        return null;
    }

    private ServiceConstellation determineSOSConstellation(DataSourceConfiguration dataSource,
                                                           GetCapabilitiesResponse capabilities)
            throws IOException, DecodingException {
        for (AbstractConnector connector : connectors) {
            if (connector instanceof AbstractSosConnector) {
                AbstractSosConnector sosConnector = (AbstractSosConnector) connector;
                if (sosConnector.matches(dataSource, capabilities)) {
                    LOGGER.info("{} create a constellation for {}", connector.toString(), dataSource);
                    return sosConnector.getConstellation(dataSource, capabilities);
                }
            }
        }
        return null;
    }

    private ServiceConstellation determineSensorThingsConstellation(DataSourceConfiguration dataSource) {
        for (AbstractConnector connector : connectors) {
            if (connector instanceof SensorThingsConnector) {
                SensorThingsConnector sosConnector = (SensorThingsConnector) connector;
                return sosConnector.getConstellation(dataSource);
            }
        }
        return null;
    }

    public void init(DataSourceConfiguration initConfig) {
        setConfig(initConfig);
        setJobName(initConfig.getItemName());
        if (initConfig.getJob() != null) {
            DataSourceJobConfiguration job = initConfig.getJob();
            setEnabled(job.isEnabled());
            setCronExpression(job.getCronExpression());
            setTriggerAtStartup(job.isTriggerAtStartup());
        }
    }

    private void saveConstellation(ServiceConstellation constellation) {
        // serviceEntity
        ProxyServiceEntity service = insertRepository.insertService(constellation.getService());
        Set<Long> datasetIds = insertRepository.getIdsForService(service);

        // save all constellations
        constellation.getDatasets().forEach((dataset) -> {
            ProcedureEntity procedure = constellation.getProcedures().get(dataset.getProcedure());
            CategoryEntity category = constellation.getCategories().get(dataset.getCategory());
            FeatureEntity feature = constellation.getFeatures().get(dataset.getFeature());
            OfferingEntity offering = constellation.getOfferings().get(dataset.getOffering());
            PhenomenonEntity phenomenon = constellation.getPhenomena().get(dataset.getPhenomenon());

            List<DescribableEntity> entities = Arrays.asList(procedure, category, feature, offering, phenomenon);
            if (entities.stream().allMatch(Objects::nonNull)) {
                entities.stream().forEach(x -> x.setService(service));
                DatasetEntity<?> ds = insertRepository.insertDataset(dataset
                        .createDatasetEntity(procedure, category, feature, offering, phenomenon, service));
                datasetIds.remove(ds.getPkid());

                dataset.getFirst().ifPresent(data -> insertRepository.insertData(ds, data));
                dataset.getLatest().ifPresent(data -> insertRepository.insertData(ds, data));

                LOGGER.info("Add dataset constellation: {}", dataset);
            } else {
                LOGGER.warn("Can't add dataset: {}", dataset);
            }
        });

        insertRepository.cleanUp(service, datasetIds);
    }

    private GetCapabilitiesResponse getCapabilities(String serviceUrl) throws IOException, DecodingException {
        try {
            SimpleHttpClient simpleHttpClient = new SimpleHttpClient();
            String url = serviceUrl;
            if (url.contains("?")) {
                url += "&";
            } else {
                url += "?";
            }
            HttpResponse response = simpleHttpClient.executeGet(url + "service=SOS&request=GetCapabilities");
            XmlObject xmlResponse = parse(response.getEntity().getContent());
            return (GetCapabilitiesResponse) decoderRepository
                    .getDecoder(getDecoderKey(xmlResponse))
                    .decode(xmlResponse);
        } catch (XmlException ex) {
            throw new DecodingException(ex);
        }
    }

}
