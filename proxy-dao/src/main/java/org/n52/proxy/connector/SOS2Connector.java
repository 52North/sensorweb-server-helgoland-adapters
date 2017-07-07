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

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.n52.proxy.connector.utils.ConnectorHelper.addCategory;
import static org.n52.proxy.connector.utils.ConnectorHelper.addFeature;
import static org.n52.proxy.connector.utils.ConnectorHelper.addOffering;
import static org.n52.proxy.connector.utils.ConnectorHelper.addPhenomenon;
import static org.n52.proxy.connector.utils.ConnectorHelper.addProcedure;
import static org.n52.proxy.connector.utils.ConnectorHelper.addService;
import static org.n52.proxy.connector.utils.ConnectorHelper.createFirstTimefilter;
import static org.n52.proxy.connector.utils.ConnectorHelper.createLatestTimefilter;
import static org.n52.proxy.connector.utils.ConnectorHelper.createTimePeriodFilter;
import static org.n52.proxy.connector.utils.DataEntityBuilder.createCountDataEntity;
import static org.n52.proxy.connector.utils.DataEntityBuilder.createQuantityDataEntity;
import static org.n52.proxy.connector.utils.DataEntityBuilder.createTextDataEntity;
import static org.n52.proxy.connector.utils.EntityBuilder.createUnit;
import static org.n52.shetland.ogc.sos.Sos2Constants.NS_SOS_20;
import static org.n52.shetland.ogc.sos.Sos2Constants.SERVICEVERSION;
import static org.n52.shetland.ogc.sos.SosConstants.SOS;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Configurable;

import org.n52.proxy.config.DataSourceConfiguration;
import org.n52.proxy.connector.constellations.QuantityDatasetConstellation;
import org.n52.proxy.connector.utils.ServiceConstellation;
import org.n52.proxy.db.beans.ProxyServiceEntity;
import org.n52.series.db.beans.CountDatasetEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.QuantityDatasetEntity;
import org.n52.series.db.beans.TextDatasetEntity;
import org.n52.series.db.beans.UnitEntity;
import org.n52.series.db.dao.DbQuery;
import org.n52.shetland.ogc.filter.TemporalFilter;
import org.n52.shetland.ogc.gml.AbstractFeature;
import org.n52.shetland.ogc.om.OmObservation;
import org.n52.shetland.ogc.om.features.FeatureCollection;
import org.n52.shetland.ogc.om.features.samplingFeatures.SamplingFeature;
import org.n52.shetland.ogc.ows.OwsCapabilities;
import org.n52.shetland.ogc.ows.OwsOperation;
import org.n52.shetland.ogc.ows.OwsOperationsMetadata;
import org.n52.shetland.ogc.ows.service.GetCapabilitiesResponse;
import org.n52.shetland.ogc.sos.Sos2Constants;
import org.n52.shetland.ogc.sos.SosCapabilities;
import org.n52.shetland.ogc.sos.SosObservationOffering;
import org.n52.shetland.ogc.sos.gda.GetDataAvailabilityRequest;
import org.n52.shetland.ogc.sos.gda.GetDataAvailabilityResponse;
import org.n52.shetland.ogc.sos.request.DescribeSensorRequest;
import org.n52.shetland.ogc.sos.request.GetFeatureOfInterestRequest;
import org.n52.shetland.ogc.sos.request.GetObservationRequest;
import org.n52.shetland.ogc.sos.response.DescribeSensorResponse;
import org.n52.shetland.ogc.sos.response.GetFeatureOfInterestResponse;
import org.n52.shetland.ogc.sos.response.GetObservationResponse;
import org.n52.shetland.ogc.swes.SwesConstants;

@Configurable
public class SOS2Connector extends AbstractSosConnector {

    private static final Logger LOGGER = getLogger(SOS2Connector.class);

    /**
     * Matches when the provider name is equal "52North" and service version is 2.0.0
     * @param config the config
     * @param capabilities the cababilities
     */
    @Override
    protected boolean canHandle(DataSourceConfiguration config, GetCapabilitiesResponse capabilities) {
        OwsCapabilities owsCaps = capabilities.getCapabilities();
        if (owsCaps.getVersion().equals(SERVICEVERSION) && owsCaps.getServiceProvider().isPresent()) {
            return supportsGDA(owsCaps);
        }
        return false;
    }

    protected boolean supportsGDA(OwsCapabilities owsCaps) {
        return owsCaps.getOperationsMetadata()
                .map(OwsOperationsMetadata::getOperations)
                .map(Set::stream)
                .map(operation -> operation.map(OwsOperation::getName))
                .map(names -> names.anyMatch(name -> name.equals("GetDataAvailability")))
                .orElse(false);
    }

    @Override
    public ServiceConstellation getConstellation(DataSourceConfiguration config, GetCapabilitiesResponse capabilities) {
        ServiceConstellation serviceConstellation = new ServiceConstellation();
        config.setVersion(SERVICEVERSION);
        config.setConnector(getConnectorName());
        addService(config, serviceConstellation);
        SosCapabilities sosCaps = (SosCapabilities) capabilities.getCapabilities();
        addDatasets(serviceConstellation, sosCaps, config);
        LOGGER.info("{} requests were sended to harvest the service {}", counter, config.getItemName());
        return serviceConstellation;
    }

    @Override
    public List<DataEntity<?>> getObservations(DatasetEntity<?> seriesEntity, DbQuery query) {
            GetObservationResponse obsResp = createObservationResponse(seriesEntity, createTimePeriodFilter(
                    query));
            List<DataEntity<?>> data = obsResp.getObservationCollection().toStream()
                    .map(observation -> createDataEntity(observation, seriesEntity))
                    .collect(toList());
            LOGGER.info("Found " + data.size() + " Entries");
            return data;
    }

    @Override
    public Optional<DataEntity<?>> getFirstObservation(DatasetEntity<?> entity) {
        return createObservationResponse(entity, createFirstTimefilter())
                .getObservationCollection().toStream()
                .findFirst()
                .map(obs -> createDataEntity(obs, entity));
    }

    @Override
    public Optional<DataEntity<?>> getLastObservation(DatasetEntity<?> entity) {
        return createObservationResponse(entity, createLatestTimefilter())
                .getObservationCollection().toStream()
                .findFirst()
                .map(obs -> createDataEntity(obs, entity));
    }

    @Override
    public UnitEntity getUom(DatasetEntity<?> seriesEntity) {
        GetObservationResponse response = createObservationResponse(seriesEntity,
                                                                    createFirstTimefilter());
        return response.getObservationCollection().toStream()
                .findFirst().map(o -> o.getValue().getValue().getUnit())
                .map(unit -> createUnit(unit, null, (ProxyServiceEntity) seriesEntity.getService()))
                .orElse(null);
    }

    protected DataEntity<?> createDataEntity(OmObservation observation, DatasetEntity<?> seriesEntity) {
        if (seriesEntity instanceof QuantityDatasetEntity) {
            return createQuantityDataEntity(observation);
        } else if (seriesEntity instanceof CountDatasetEntity) {
            return createCountDataEntity(observation);
        } else if (seriesEntity instanceof TextDatasetEntity) {
            return createTextDataEntity(observation);
        } else {
            LOGGER.error("No supported datasetEntity for ", seriesEntity);
            return null;
        }
    }

    protected void addDatasets(ServiceConstellation serviceConstellation, SosCapabilities sosCaps,
            DataSourceConfiguration config) {
        sosCaps.getContents().ifPresent(contents
                -> contents.forEach(sosObsOff
                        -> doForOffering(sosObsOff, serviceConstellation, config)));
    }

    protected void doForOffering(SosObservationOffering offering, ServiceConstellation serviceConstellation,
            DataSourceConfiguration config) {
        String offeringId = addOffering(offering, serviceConstellation);

        offering.getProcedures().forEach((procedureId) -> {
            addProcedure(procedureId, true, false, serviceConstellation);

            GetFeatureOfInterestResponse foiResponse = getFeatureOfInterestResponseByProcedure(procedureId,
                    config.getUrl());
            addAbstractFeature(foiResponse.getAbstractFeature(), serviceConstellation);

            GetDataAvailabilityResponse gdaResponse = getDataAvailabilityResponse(procedureId, config.getUrl());
            if (gdaResponse != null) {
                gdaResponse.getDataAvailabilities().forEach((dataAval) -> {
                    String phenomenonId = addPhenomenon(dataAval, serviceConstellation);
                    String categoryId = addCategory(dataAval, serviceConstellation);
                    String featureId = dataAval.getFeatureOfInterest().getHref();
                    // TODO maybe not only QuantityDatasetConstellation
                    serviceConstellation.add(new QuantityDatasetConstellation(procedureId, offeringId, categoryId,
                            phenomenonId,
                            featureId));
                });
            }
            LOGGER.info(foiResponse.toString());
        });
    }

    protected void addAbstractFeature(AbstractFeature feature, ServiceConstellation serviceConstellation) {
        if (feature instanceof SamplingFeature) {
            addFeature((SamplingFeature) feature, serviceConstellation);
        } else if (feature instanceof FeatureCollection) {
            ((FeatureCollection) feature).forEach((AbstractFeature featureEntry) -> addAbstractFeature(featureEntry,
                    serviceConstellation));
        }
    }

    protected GetFeatureOfInterestResponse getFeatureOfInterestResponseByProcedure(String procedureId, String serviceUri) {
        GetFeatureOfInterestRequest request = new GetFeatureOfInterestRequest(SOS, SERVICEVERSION);
        request.setProcedures(asList(procedureId));
        return (GetFeatureOfInterestResponse) getSosResponseFor(request, NS_SOS_20, serviceUri);
    }

    protected GetFeatureOfInterestResponse getFeatureOfInterestResponseByFeature(String featureId, String serviceUri) {
        GetFeatureOfInterestRequest request = new GetFeatureOfInterestRequest(SOS, SERVICEVERSION);
        request.setFeatureIdentifiers(asList(featureId));
        return (GetFeatureOfInterestResponse) getSosResponseFor(request, NS_SOS_20, serviceUri);
    }

    protected DescribeSensorResponse getDescribeSensorResponse(String procedureId, String url, String format) {
        DescribeSensorRequest request = new DescribeSensorRequest(SOS, Sos2Constants.SERVICEVERSION);
        request.setProcedure(procedureId);
        request.setProcedureDescriptionFormat(format);
        return (DescribeSensorResponse) getSosResponseFor(request, SwesConstants.NS_SWES_20, url);
    }

    private GetDataAvailabilityResponse getDataAvailabilityResponse(String procedureId, String serviceUri) {
        GetDataAvailabilityRequest request = new GetDataAvailabilityRequest(SOS, SERVICEVERSION);
        request.setProcedures(asList(procedureId));
        return (GetDataAvailabilityResponse) getSosResponseFor(request, NS_SOS_20, serviceUri);
    }

    protected GetObservationResponse createObservationResponse(DatasetEntity<?> seriesEntity,
            TemporalFilter temporalFilter) {
        return createObservationResponse(seriesEntity, temporalFilter, null);
    }

    protected GetObservationResponse createObservationResponse(DatasetEntity<?> seriesEntity, TemporalFilter temporalFilter,
            String responseFormat) {
        GetObservationRequest request = new GetObservationRequest(SOS, SERVICEVERSION);
        request.setProcedures(asList(seriesEntity.getProcedure().getDomainId()));
        request.setObservedProperties(asList(seriesEntity.getPhenomenon().getDomainId()));
        request.setFeatureIdentifiers(asList(seriesEntity.getFeature().getDomainId()));
        if (temporalFilter != null) {
            request.setTemporalFilters(asList(temporalFilter));
        }
        if (responseFormat != null) {
            request.setResponseFormat(responseFormat);
        }
        return (GetObservationResponse) this.getSosResponseFor(request, NS_SOS_20,
                seriesEntity.getService().getUrl());
    }

}
