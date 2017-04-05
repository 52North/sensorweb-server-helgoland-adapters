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

import java.io.IOException;
import java.util.Set;
import org.apache.http.HttpResponse;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import static org.apache.xmlbeans.XmlObject.Factory.parse;
import org.n52.io.task.ScheduledJob;
import org.n52.proxy.config.DataSourceConfiguration;
import org.n52.proxy.config.DataSourceJobConfiguration;
import org.n52.proxy.connector.AbstractConnector;
import org.n52.proxy.connector.AbstractSosConnector;
import org.n52.proxy.connector.SensorThingsConnector;
import org.n52.proxy.connector.utils.ServiceConstellation;
import org.n52.proxy.db.beans.ProxyServiceEntity;
import org.n52.proxy.db.da.InsertRepository;
import org.n52.proxy.web.SimpleHttpClient;
import org.n52.series.db.beans.CategoryEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.FeatureEntity;
import org.n52.series.db.beans.OfferingEntity;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.shetland.ogc.ows.service.GetCapabilitiesResponse;
import org.n52.svalbard.decode.DecoderRepository;
import org.n52.svalbard.decode.exception.DecodingException;
import static org.n52.svalbard.util.CodingHelper.getDecoderKey;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import static org.quartz.JobBuilder.newJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.slf4j.Logger;
import static org.slf4j.LoggerFactory.getLogger;
import org.springframework.beans.factory.annotation.Autowired;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class DataSourceHarvesterJob extends ScheduledJob implements Job {

    private static final Logger LOGGER = getLogger(DataSourceHarvesterJob.class);

    private static final String JOB_CONNECTOR = "connector";
    private static final String JOB_TYPE = "type";
    private static final String JOB_VERSION = "version";
    private static final String JOB_NAME = "name";
    private static final String JOB_URL = "url";

    private DataSourceConfiguration config;

    @Autowired
    private InsertRepository insertRepository;

    @Autowired
    private DecoderRepository decoderRepository;

    @Autowired
    private Set<AbstractConnector> connectors;

    public DataSourceConfiguration getConfig() {
        return config;
    }

    public void setConfig(DataSourceConfiguration config) {
        this.config = config;
    }

    @Override
    public JobDetail createJobDetails() {
        return newJob(DataSourceHarvesterJob.class)
                .withIdentity(getJobName())
                .usingJobData(JOB_URL, config.getUrl())
                .usingJobData(JOB_NAME, config.getItemName())
                .usingJobData(JOB_VERSION, config.getVersion())
                .usingJobData(JOB_CONNECTOR, config.getConnector())
                .usingJobData(JOB_TYPE, config.getType())
                .build();
    }

    private DataSourceConfiguration recreateConfig(JobDataMap jobDataMap) {
        DataSourceConfiguration createdConfig = new DataSourceConfiguration();
        createdConfig.setUrl(jobDataMap.getString(JOB_URL));
        createdConfig.setItemName(jobDataMap.getString(JOB_NAME));
        createdConfig.setVersion(jobDataMap.getString(JOB_VERSION));
        createdConfig.setConnector(jobDataMap.getString(JOB_CONNECTOR));
        createdConfig.setType(jobDataMap.getString(JOB_TYPE));
        return createdConfig;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LOGGER.info(context.getJobDetail().getKey() + " execution starts.");

        JobDetail jobDetail = context.getJobDetail();
        JobDataMap jobDataMap = jobDetail.getJobDataMap();

        DataSourceConfiguration dataSource = recreateConfig(jobDataMap);

        ServiceConstellation constellation = determineConstellation(dataSource);

        if (constellation == null) {
            LOGGER.warn("No connector found for " + dataSource);
        } else {
            saveConstellation(constellation);
        }

        LOGGER.info(context.getJobDetail().getKey() + " execution ends.");
    }

    private ServiceConstellation determineConstellation(DataSourceConfiguration dataSource) {
        ServiceConstellation constellation = null;
        if (dataSource.getType().equals("SOS")) {
            GetCapabilitiesResponse capabilities = getCapabilities(dataSource.getUrl());
            constellation = determineSOSConstellation(dataSource, capabilities);
        }
        if (dataSource.getType().equals("SensorThings")) {
            constellation = determineSensorThingsConstellation(dataSource);
        }
        return constellation;
    }

    private ServiceConstellation determineSOSConstellation(DataSourceConfiguration dataSource,
            GetCapabilitiesResponse capabilities) {
        for (AbstractConnector connector : connectors) {
            if (connector instanceof AbstractSosConnector) {
                AbstractSosConnector sosConnector = (AbstractSosConnector) connector;
                if (sosConnector.matches(dataSource, capabilities)) {
                    LOGGER.info(connector.toString() + " create a constellation for " + dataSource);
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
            final ProcedureEntity procedure = constellation.getProcedures().get(dataset.getProcedure());
            final CategoryEntity category = constellation.getCategories().get(dataset.getCategory());
            final FeatureEntity feature = constellation.getFeatures().get(dataset.getFeature());
            final OfferingEntity offering = constellation.getOfferings().get(dataset.getOffering());
            final PhenomenonEntity phenomenon = constellation.getPhenomena().get(dataset.getPhenomenon());
            if (procedure != null && category != null && feature != null && offering != null && phenomenon != null) {
                procedure.setService(service);
                category.setService(service);
                feature.setService(service);
                offering.setService(service);
                phenomenon.setService(service);
                DatasetEntity entity = dataset.createDatasetEntity(procedure, category, feature, offering, phenomenon,
                        service);
                DatasetEntity inserted = insertRepository.insertDataset(entity);
                datasetIds.remove(inserted.getPkid());
                LOGGER.info("Add dataset constellation: " + dataset);
            } else {
                LOGGER.warn("Can't add dataset: " + dataset);
            }
        });

        insertRepository.cleanUp(service, datasetIds);
    }

    private GetCapabilitiesResponse getCapabilities(String serviceUrl) {
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
        } catch (IOException | XmlException | DecodingException ex) {
            LOGGER.error(ex.getLocalizedMessage(), ex);
        }
        return null;
    }

}
