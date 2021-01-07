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
import org.n52.janmayen.function.Predicates;
import org.n52.proxy.config.DataSourceConfiguration;
import org.n52.proxy.connector.constellations.QuantityDatasetConstellation;
import org.n52.proxy.connector.utils.EntityBuilder;
import org.n52.proxy.connector.utils.ProxyException;
import org.n52.proxy.connector.utils.ServiceConstellation;
import org.n52.sensorweb.server.db.old.dao.DbQuery;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.UnitEntity;
import org.n52.shetland.ogc.gml.AbstractFeature;
import org.n52.shetland.ogc.gml.ReferenceType;
import org.n52.shetland.ogc.om.features.samplingFeatures.AbstractSamplingFeature;
import org.n52.shetland.ogc.ows.service.GetCapabilitiesResponse;
import org.n52.shetland.ogc.ows.service.OwsServiceResponse;
import org.n52.shetland.ogc.sos.SosObservationOffering;
import org.n52.shetland.ogc.sos.gda.GetDataAvailabilityResponse;
import org.n52.shetland.ogc.sos.response.GetFeatureOfInterestResponse;
import org.n52.shetland.ogc.sos.ro.RelatedOfferingConstants;
import org.n52.shetland.ogc.sos.ro.RelatedOfferings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author Jan Schulte
 */
@Component
public class NestedOfferingsSOSConnector extends SOS2Connector {

    private static final Logger LOGGER = LoggerFactory.getLogger(NestedOfferingsSOSConnector.class);

    @Override
    protected boolean canHandle(DataSourceConfiguration config, GetCapabilitiesResponse capabilities) {
        return false;
    }

    @Override
    protected void doForOffering(SosObservationOffering obsOff,
                                 ServiceConstellation serviceConstellation,
                                 DataSourceConfiguration config) {
        obsOff.getExtension(RelatedOfferingConstants.RELATED_OFFERINGS)
                .filter(Predicates.instanceOf(RelatedOfferings.class))
                .ifPresent(e -> addNestedOfferings((RelatedOfferings) e, serviceConstellation, config.getUrl()));
    }

    @Override
    public Optional<DataEntity<?>> getFirstObservation(DatasetEntity entity) {
        // TODO implement
        return Optional.empty();
    }

    @Override
    public Optional<DataEntity<?>> getLastObservation(DatasetEntity entity) {
        // TODO implement
        return Optional.empty();
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
    public UnitEntity getUom(DatasetEntity seriesEntity) {
        // TODO implement
        return EntityBuilder.createUnit("unit", null, seriesEntity.getService());
    }

    private void addNestedOfferings(RelatedOfferings relatedOfferings, ServiceConstellation serviceConstellation,
                                    String serviceUri) {
        relatedOfferings.getValue().forEach(context -> {
            try {
                ReferenceType relatedOffering = context.getRelatedOffering();
                LOGGER.info("Fetch nested offerings for {}", relatedOffering.getTitle());
                if (relatedOffering.getTitle().equalsIgnoreCase(
                        "http://ressource.brgm-rec.fr/obs/RawGeologicLogs/BSS000AAEU")) {
                    GetDataAvailabilityResponse response = getDataAvailabilityForOffering(relatedOffering.getHref());
                    response.getDataAvailabilities().forEach(dataAvail -> {
                        String procedureId = addProcedure(dataAvail, true, false, serviceConstellation);
                        String phenomenonId = addPhenomenon(dataAvail, serviceConstellation);
                        String categoryId = addCategory(dataAvail, serviceConstellation);
                        String offeringId = addOffering(dataAvail.getOffering(), serviceConstellation);
                        String featureId = dataAvail.getFeatureOfInterest().getHref();
                        if (!serviceConstellation.hasFeature(featureId)) {
                            GetFeatureOfInterestResponse foiResponse = getFeatureOfInterestById(featureId, serviceUri);
                            AbstractFeature abstractFeature = foiResponse.getAbstractFeature();
                            if (abstractFeature instanceof AbstractSamplingFeature) {
                                addFeature((AbstractSamplingFeature) abstractFeature, serviceConstellation);
                            }
                        }
                        // TODO maybe not only QuantityDatasetConstellation
                        serviceConstellation.add(new QuantityDatasetConstellation(procedureId, offeringId, categoryId,
                                                                                  phenomenonId,
                                                                                  featureId, featureId));
                    });
                }
            } catch (ProxyException ex) {
                LOGGER.error(ex.getLocalizedMessage(), ex);
            }
        });
    }

    private GetDataAvailabilityResponse getDataAvailabilityForOffering(String uri) throws ProxyException {
        OwsServiceResponse response = (OwsServiceResponse) getSosResponseFor(uri);
        if (response instanceof GetDataAvailabilityResponse) {
            return (GetDataAvailabilityResponse) response;
        }
        throw new ProxyException("Wrong response - GetDataAvailabilityResponse was expected", response);
    }

}
