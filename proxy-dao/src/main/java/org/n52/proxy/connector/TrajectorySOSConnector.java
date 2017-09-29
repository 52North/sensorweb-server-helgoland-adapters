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
package org.n52.proxy.connector;

import static java.util.stream.Collectors.toList;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;

import org.n52.janmayen.Stopwatch;
import org.n52.janmayen.function.Functions;
import org.n52.proxy.config.DataSourceConfiguration;
import org.n52.proxy.connector.constellations.QuantityDatasetConstellation;
import org.n52.proxy.connector.utils.EntityBuilder;
import org.n52.proxy.connector.utils.ServiceConstellation;
import org.n52.proxy.db.beans.ProxyServiceEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.QuantityDataEntity;
import org.n52.series.db.beans.UnitEntity;
import org.n52.series.db.dao.DbQuery;
import org.n52.shetland.ogc.filter.TemporalFilter;
import org.n52.shetland.ogc.om.ObservationValue;
import org.n52.shetland.ogc.om.OmObservation;
import org.n52.shetland.ogc.ows.OwsServiceProvider;
import org.n52.shetland.ogc.ows.service.GetCapabilitiesResponse;
import org.n52.shetland.ogc.sos.Sos2Constants;
import org.n52.shetland.ogc.sos.SosCapabilities;
import org.n52.shetland.ogc.sos.SosObservationOffering;
import org.n52.shetland.ogc.sos.gda.GetDataAvailabilityResponse;
import org.n52.shetland.ogc.sos.response.GetObservationResponse;

@Configurable
public class TrajectorySOSConnector extends AbstractSosConnector {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrajectorySOSConnector.class);

    /**
     * Matches when the provider name is equal "52North" and service version is 2.0.0
     */
    @Override
    protected boolean canHandle(DataSourceConfiguration config, GetCapabilitiesResponse response) {
        return response.getCapabilities().getVersion().equals(Sos2Constants.SERVICEVERSION) &&
               response.getCapabilities().getServiceProvider()
                       .map(OwsServiceProvider::getProviderName)
                       .filter(name -> name.equals("52North"))
                       .isPresent();
    }

    @Override
    public ServiceConstellation getConstellation(DataSourceConfiguration config, GetCapabilitiesResponse capabilities) {
        ServiceConstellation serviceConstellation = new ServiceConstellation();
        config.setVersion(Sos2Constants.SERVICEVERSION);
        config.setConnector(getConnectorName());
        addService(config, serviceConstellation);
        SosCapabilities sosCaps = (SosCapabilities) capabilities.getCapabilities();
        addDatasets(serviceConstellation, sosCaps, config.getUrl());
        return serviceConstellation;
    }

    @Override
    public List<DataEntity<?>> getObservations(DatasetEntity<?> seriesEntity, DbQuery query) {
        Stopwatch stopwatch = new Stopwatch().start();
        LOGGER.info("Start GetObs request");
        GetObservationResponse obsResp = createObservationResponse(seriesEntity, null);

        LOGGER.info("Process GetObs response");

        List<DataEntity<?>> data = obsResp.getObservationCollection().toStream()
                 .map(Functions.currySecond(this::createDataEntity, seriesEntity))
                .collect(toList());
        LOGGER.info("Found {}  Entries", data.size());
        LOGGER.info("End GetObs request in {}", stopwatch);
        return data;
    }

    @Override
    public UnitEntity getUom(DatasetEntity<?> seriesEntity) {
        GetDataAvailabilityResponse availabilityResponse = getDataAvailability(seriesEntity);
        if (availabilityResponse.getDataAvailabilities().size() == 1) {
            DateTime start = availabilityResponse.getDataAvailabilities().get(0).getPhenomenonTime().getStart();
            GetObservationResponse response = createObservationResponse(seriesEntity,
                                                                        createTimeFilter(start));
            return response.getObservationCollection().toStream()
                    .findFirst()
                    .map(OmObservation::getValue)
                    .map(ObservationValue::getValue)
                    .map(v -> v.getUnit())
                    .map(unit -> EntityBuilder.createUnit(unit, null, (ProxyServiceEntity) seriesEntity.getService()))
                    .orElse(null);
        }
        return null;
    }

    @Override
    public Optional<DataEntity<?>> getFirstObservation(DatasetEntity<?> entity) {
        // currently only return default first observation
        QuantityDataEntity quantityDataEntity = new QuantityDataEntity();
        quantityDataEntity.setTimestart(new Date());
        quantityDataEntity.setTimeend(new Date());
        quantityDataEntity.setValue(0.0);
        return Optional.of(quantityDataEntity);
    }

    @Override
    public Optional<DataEntity<?>> getLastObservation(DatasetEntity<?> entity) {
        // currently only return default last observation
        QuantityDataEntity quantityDataEntity = new QuantityDataEntity();
        quantityDataEntity.setTimestart(new Date());
        quantityDataEntity.setTimeend(new Date());
        quantityDataEntity.setValue(0.0);
        return Optional.of(quantityDataEntity);
    }

    private void addDatasets(ServiceConstellation serviceConstellation, SosCapabilities sosCaps, String serviceUri) {
        if (sosCaps != null) {
            sosCaps.getContents().ifPresent((obsOffs) -> {
//                obsOffs.forEach((obsOff) -> {
//                    doForOffering(obsOff, serviceConstellation, serviceUri);
//                });
                doForOffering(obsOffs.first(), serviceConstellation, serviceUri);
            });
        }
    }

    private void doForOffering(SosObservationOffering offering, ServiceConstellation serviceConstellation,
                               String serviceUri) {
        String offeringId = addOffering(offering, serviceConstellation);
//        offering.getProcedures().forEach((procedureId) -> {
//            offering.getObservableProperties().forEach((obsProp) -> {
//                doDataAvailability(obsProp, procedureId, offeringId, serviceUri, serviceConstellation);
//            });
//        });
        doDataAvailability(offering.getObservableProperties().first(), offering.getProcedures().first(), offeringId,
                           serviceUri, serviceConstellation);
    }

    private void doDataAvailability(String obsProp, String procedureId, String offeringId, String serviceUri,
                                    ServiceConstellation serviceConstellation) {
        GetDataAvailabilityResponse gdaResponse = getDataAvailability(procedureId, offeringId, obsProp, null,
                                                                              serviceUri);
        gdaResponse.getDataAvailabilities().forEach((dataAval) -> {
            String featureId = addFeature(dataAval, serviceConstellation);
            addProcedure(dataAval, true, true, serviceConstellation);
            String phenomenonId = addPhenomenon(dataAval, serviceConstellation);
            String categoryId = addCategory(dataAval, serviceConstellation);
            // TODO maybe not only QuantityDatasetConstellation
            serviceConstellation.add(new QuantityDatasetConstellation(procedureId, offeringId, categoryId,
                                                                      phenomenonId,
                                                                      featureId));
        });
    }

    private String addFeature(GetDataAvailabilityResponse.DataAvailability dataAval,
                              ServiceConstellation serviceConstellation) {
        String featureId = dataAval.getFeatureOfInterest().getHref();
        String featureName = dataAval.getFeatureOfInterest().getTitle();
        serviceConstellation.putFeature(featureId, featureName, null, 0, 0, 0);
        return featureId;
    }

    private GetObservationResponse createObservationResponse(DatasetEntity<?> seriesEntity,
                                                             TemporalFilter temporalFilter) {
        String responseFormat = null;
        // TODO use inspire omso 3.0 format later, when trajectory encoder/decoder are available
//        request.setResponseFormat("http://inspire.ec.europa.eu/schemas/omso/3.0");
        return getObservation(seriesEntity, temporalFilter, responseFormat);
    }

}
