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

import java.util.ArrayList;
import static java.util.Arrays.asList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import static java.util.Optional.of;
import org.joda.time.DateTime;
import org.n52.proxy.config.DataSourceConfiguration;
import org.n52.proxy.connector.constellations.QuantityDatasetConstellation;
import static org.n52.proxy.connector.utils.ConnectorHelper.addCategory;
import static org.n52.proxy.connector.utils.ConnectorHelper.addOffering;
import static org.n52.proxy.connector.utils.ConnectorHelper.addPhenomenon;
import static org.n52.proxy.connector.utils.ConnectorHelper.addProcedure;
import static org.n52.proxy.connector.utils.ConnectorHelper.addService;
import static org.n52.proxy.connector.utils.ConnectorHelper.createTimeInstantFilter;
import static org.n52.proxy.connector.utils.EntityBuilder.createUnit;
import org.n52.proxy.connector.utils.ServiceConstellation;
import org.n52.proxy.db.beans.ProxyServiceEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.GeometryEntity;
import org.n52.series.db.beans.QuantityDataEntity;
import org.n52.series.db.beans.UnitEntity;
import org.n52.series.db.dao.DbQuery;
import org.n52.shetland.ogc.filter.TemporalFilter;
import org.n52.shetland.ogc.gml.time.TimeInstant;
import org.n52.shetland.ogc.om.NamedValue;
import org.n52.shetland.ogc.om.SingleObservationValue;
import org.n52.shetland.ogc.om.values.GeometryValue;
import org.n52.shetland.ogc.om.values.QuantityValue;
import org.n52.shetland.ogc.ows.OwsCapabilities;
import org.n52.shetland.ogc.ows.OwsServiceProvider;
import org.n52.shetland.ogc.ows.exception.OwsExceptionReport;
import org.n52.shetland.ogc.ows.service.GetCapabilitiesResponse;
import static org.n52.shetland.ogc.sos.Sos2Constants.NS_SOS_20;
import static org.n52.shetland.ogc.sos.Sos2Constants.SERVICEVERSION;
import org.n52.shetland.ogc.sos.SosCapabilities;
import static org.n52.shetland.ogc.sos.SosConstants.SOS;
import org.n52.shetland.ogc.sos.SosObservationOffering;
import static org.n52.shetland.ogc.sos.gda.GetDataAvailabilityConstants.NS_GDA_20;
import org.n52.shetland.ogc.sos.gda.GetDataAvailabilityRequest;
import org.n52.shetland.ogc.sos.gda.GetDataAvailabilityResponse;
import org.n52.shetland.ogc.sos.request.GetObservationRequest;
import org.n52.shetland.ogc.sos.response.GetObservationResponse;
import org.slf4j.Logger;
import static org.slf4j.LoggerFactory.getLogger;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
public class TrajectorySOSConnector extends AbstractSosConnector {

    private static final Logger LOGGER = getLogger(TrajectorySOSConnector.class);

    /**
     * Matches when the provider name is equal "52North" and service version is 2.0.0
     */
    @Override
    protected boolean canHandle(DataSourceConfiguration config, GetCapabilitiesResponse capabilities) {
        OwsCapabilities owsCaps = capabilities.getCapabilities();
        if (owsCaps.getVersion().equals(SERVICEVERSION) && owsCaps.getServiceProvider().isPresent()) {
            OwsServiceProvider servProvider = owsCaps.getServiceProvider().get();
            if (servProvider.getProviderName().equals("52North")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ServiceConstellation getConstellation(DataSourceConfiguration config, GetCapabilitiesResponse capabilities) {
        ServiceConstellation serviceConstellation = new ServiceConstellation();
        config.setVersion(SERVICEVERSION);
        config.setConnector(getConnectorName());
        addService(config, serviceConstellation);
        SosCapabilities sosCaps = (SosCapabilities) capabilities.getCapabilities();
        addDatasets(serviceConstellation, sosCaps, config.getUrl());
        return serviceConstellation;
    }

    @Override
    public List<DataEntity> getObservations(DatasetEntity seriesEntity, DbQuery query) {
        Date start = new Date();
        LOGGER.info("Start GetObs request");
        GetObservationResponse obsResp = createObservationResponse(seriesEntity, null);

        LOGGER.info("Process GetObs response");

        List<DataEntity> data = new ArrayList<>();

        try {
            obsResp.getObservationCollection().forEachRemaining((observation) -> {
                QuantityDataEntity entity = new QuantityDataEntity();
                SingleObservationValue obsValue = (SingleObservationValue) observation.getValue();
                TimeInstant instant = (TimeInstant) obsValue.getPhenomenonTime();
                entity.setTimestart(instant.getValue().toDate());
                entity.setTimeend(instant.getValue().toDate());
                QuantityValue value = (QuantityValue) obsValue.getValue();
                entity.setValue(value.getValue());
                Collection<NamedValue<?>> parameters = observation.getParameter();
                parameters.forEach((parameter) -> {
                    if (parameter.getName().getHref().equals(
                            "http://www.opengis.net/def/param-name/OGC-OM/2.0/samplingGeometry")
                            && parameter.getValue() instanceof GeometryValue) {
                        GeometryValue geom = (GeometryValue) parameter.getValue();
                        GeometryEntity geometryEntity = new GeometryEntity();
                        geometryEntity.setLat(geom.getGeometry().getCoordinate().x);
                        geometryEntity.setLon(geom.getGeometry().getCoordinate().y);
                        geometryEntity.setAlt(geom.getGeometry().getCoordinate().z);
                        entity.setGeometryEntity(geometryEntity);
                    }
                });
                data.add(entity);
            });
        } catch (OwsExceptionReport e) {
            LOGGER.error("Error while querying and processing observations!", e);
        }
        LOGGER.info("Found " + data.size() + " Entries");
        LOGGER.info("End GetObs request in " + ((new Date()).getTime() - start.getTime()) + " ms");
        return data;
    }

    @Override
    public UnitEntity getUom(DatasetEntity seriesEntity) {
        GetDataAvailabilityResponse availabilityResponse = getDataAvailabilityResponse(seriesEntity);
        if (availabilityResponse.getDataAvailabilities().size() == 1) {
            DateTime start = availabilityResponse.getDataAvailabilities().get(0).getPhenomenonTime().getStart();
            GetObservationResponse response = createObservationResponse(seriesEntity,
                    createTimeInstantFilter(start));
            try {
                if (response.getObservationCollection().hasNext()) {
                    String unit = response.getObservationCollection().next().getValue().getValue().getUnit();
                    return createUnit(unit, null, (ProxyServiceEntity) seriesEntity.getService());
                }
            } catch (NoSuchElementException | OwsExceptionReport e) {
                LOGGER.error("Error while querying unit from observation!", e);
            }
        }
        return null;
    }

    @Override
    public Optional<DataEntity> getFirstObservation(DatasetEntity entity) {
        // currently only return default first observation
        QuantityDataEntity quantityDataEntity = new QuantityDataEntity();
        quantityDataEntity.setTimestart(new Date());
        quantityDataEntity.setTimeend(new Date());
        quantityDataEntity.setValue(0.0);
        return of(quantityDataEntity);
    }

    @Override
    public Optional<DataEntity> getLastObservation(DatasetEntity entity) {
        // currently only return default last observation
        QuantityDataEntity quantityDataEntity = new QuantityDataEntity();
        quantityDataEntity.setTimestart(new Date());
        quantityDataEntity.setTimeend(new Date());
        quantityDataEntity.setValue(0.0);
        return of(quantityDataEntity);
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
        GetDataAvailabilityResponse gdaResponse = getDataAvailabilityResponse(procedureId, offeringId, obsProp,
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

    private GetDataAvailabilityResponse getDataAvailabilityResponse(String procedureId, String offeringId,
            String obsPropId, String url) {
        GetDataAvailabilityRequest request = new GetDataAvailabilityRequest(SOS, SERVICEVERSION);
        request.setNamespace(NS_GDA_20);
        request.setProcedures(asList(procedureId));
        request.addOffering(offeringId);
        request.setObservedProperty(asList(obsPropId));
        return (GetDataAvailabilityResponse) getSosResponseFor(request, NS_SOS_20, url);
    }

    private GetDataAvailabilityResponse getDataAvailabilityResponse(DatasetEntity seriesEntity) {
        GetDataAvailabilityRequest request = new GetDataAvailabilityRequest(SOS, SERVICEVERSION);
        request.setProcedures(asList(seriesEntity.getProcedure().getDomainId()));
        request.addOffering(seriesEntity.getOffering().getDomainId());
        request.setObservedProperty(asList(seriesEntity.getPhenomenon().getDomainId()));
        request.setFeatureOfInterest(asList(seriesEntity.getFeature().getDomainId()));
        return (GetDataAvailabilityResponse) getSosResponseFor(request, NS_SOS_20,
                seriesEntity.getService().getUrl());
    }

    private GetObservationResponse createObservationResponse(DatasetEntity seriesEntity,
            TemporalFilter temporalFilter) {
        GetObservationRequest request = new GetObservationRequest(SOS, SERVICEVERSION);
        request.setProcedures(asList(seriesEntity.getProcedure().getDomainId()));
        request.setOfferings(asList(seriesEntity.getOffering().getDomainId()));
        request.setObservedProperties(asList(seriesEntity.getPhenomenon().getDomainId()));
        request.setFeatureIdentifiers(asList(seriesEntity.getFeature().getDomainId()));
        if (temporalFilter != null) {
            request.setTemporalFilters(asList(temporalFilter));
        }
        // TODO use inspire omso 3.0 format later, when trajectory encoder/decoder are available
//        request.setResponseFormat("http://inspire.ec.europa.eu/schemas/omso/3.0");
        return (GetObservationResponse) this.getSosResponseFor(request, NS_SOS_20,
                seriesEntity.getService().getUrl());
    }

}
