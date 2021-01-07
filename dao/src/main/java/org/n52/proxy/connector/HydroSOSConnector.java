/*
 * Copyright (C) 2015-2020 52°North Initiative for Geospatial Open Source
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
package org.n52.proxy.connector;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Optional;

import org.n52.janmayen.function.Functions;
import org.n52.proxy.config.DataSourceConfiguration;
import org.n52.proxy.connector.constellations.QuantityDatasetConstellation;
import org.n52.proxy.connector.utils.ServiceConstellation;
import org.n52.proxy.connector.utils.ServiceMetadata;
import org.n52.sensorweb.server.db.old.dao.DbQuery;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.shetland.ogc.gml.AbstractFeature;
import org.n52.shetland.ogc.om.features.FeatureCollection;
import org.n52.shetland.ogc.om.features.samplingFeatures.AbstractSamplingFeature;
import org.n52.shetland.ogc.ows.service.GetCapabilitiesResponse;
import org.n52.shetland.ogc.sos.Sos2Constants;
import org.n52.shetland.ogc.sos.SosCapabilities;
import org.n52.shetland.ogc.sos.SosObservationOffering;
import org.n52.shetland.ogc.sos.response.GetFeatureOfInterestResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author Jan Schulte
 */
@Component
public class HydroSOSConnector extends SOS2Connector {

    private static final Logger LOGGER = LoggerFactory.getLogger(HydroSOSConnector.class);

    @Override
    protected boolean canHandle(DataSourceConfiguration config, GetCapabilitiesResponse capabilities) {
        return false;
    }

    @Override
    public ServiceConstellation getConstellation(DataSourceConfiguration config, GetCapabilitiesResponse capabilities) {
        ServiceConstellation serviceConstellation = new ServiceConstellation();
        config.setVersion(Sos2Constants.SERVICEVERSION);
        config.setConnector(getConnectorName());
        addService(config, serviceConstellation, ServiceMetadata.createXmlServiceMetadata(capabilities.getXmlString()));
        SosCapabilities sosCaps = (SosCapabilities) capabilities.getCapabilities();
        addBindingUrls(sosCaps, config);
        addServiceConfig(config);
        addDatasets(serviceConstellation, sosCaps, config);
        return serviceConstellation;
    }

    @Override
    public List<DataEntity<?>> getObservations(DatasetEntity seriesEntity, DbQuery query) {
        List<DataEntity<?>> data = getObservation(seriesEntity, createTimeFilter(query))
                .getObservationCollection().toStream()
                .map(Functions.currySecond(this::createDataEntity, seriesEntity))
                .collect(toList());
        LOGGER.info("Found {} Entries", data.size());
        return data;
    }

    @Override
    public Optional<DataEntity<?>> getLastObservation(DatasetEntity entity) {
        return getObservation(entity, null).getObservationCollection().toStream().findFirst()
                .map(obs -> createDataEntity(obs, entity));
    }

    @Override
    protected void doForOffering(SosObservationOffering obsOff, ServiceConstellation serviceConstellation,
                                 DataSourceConfiguration config) {
        String offeringId = addOffering(obsOff, serviceConstellation);

        obsOff.getProcedures().forEach(procedureId -> {
            addProcedure(procedureId, true, false, serviceConstellation);
            obsOff.getObservableProperties().forEach(phenomenonId -> {
                addPhenomenon(phenomenonId, serviceConstellation);
                String categoryId = addCategory(phenomenonId, serviceConstellation);

                GetFeatureOfInterestResponse foiResponse = getFeatureOfInterestByProcedure(procedureId,
                                                                                           config.getUrl());
                AbstractFeature abstractFeature = foiResponse.getAbstractFeature();
                if (abstractFeature instanceof FeatureCollection) {
                    FeatureCollection featureCollection = (FeatureCollection) abstractFeature;
                    featureCollection.getMembers().forEach((key, feature) -> {
                        String featureId = addFeature((AbstractSamplingFeature) feature, serviceConstellation);
                        // TODO maybe not only QuantityDatasetConstellation
                        serviceConstellation.add(new QuantityDatasetConstellation(procedureId, offeringId, categoryId,
                                                                                  phenomenonId, featureId, featureId));
                    });
                }
            });
        });
    }
}
