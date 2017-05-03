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
import java.util.List;
import org.n52.proxy.config.DataSourceConfiguration;
import org.n52.proxy.connector.constellations.QuantityDatasetConstellation;
import static org.n52.proxy.connector.utils.ConnectorHelper.addCategory;
import static org.n52.proxy.connector.utils.ConnectorHelper.addFeature;
import static org.n52.proxy.connector.utils.ConnectorHelper.addOffering;
import static org.n52.proxy.connector.utils.ConnectorHelper.addPhenomenon;
import static org.n52.proxy.connector.utils.ConnectorHelper.addProcedure;
import static org.n52.proxy.connector.utils.ConnectorHelper.addService;
import static org.n52.proxy.connector.utils.ConnectorHelper.createTimePeriodFilter;
import org.n52.proxy.connector.utils.ServiceConstellation;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.QuantityDataEntity;
import org.n52.series.db.dao.DbQuery;
import org.n52.shetland.ogc.gml.AbstractFeature;
import org.n52.shetland.ogc.gml.time.TimeInstant;
import org.n52.shetland.ogc.om.SingleObservationValue;
import org.n52.shetland.ogc.om.features.FeatureCollection;
import org.n52.shetland.ogc.om.features.samplingFeatures.SamplingFeature;
import org.n52.shetland.ogc.om.values.QuantityValue;
import org.n52.shetland.ogc.ows.service.GetCapabilitiesResponse;
import static org.n52.shetland.ogc.sos.Sos2Constants.NS_SOS_20;
import static org.n52.shetland.ogc.sos.Sos2Constants.SERVICEVERSION;
import org.n52.shetland.ogc.sos.SosCapabilities;
import static org.n52.shetland.ogc.sos.SosConstants.SOS;
import org.n52.shetland.ogc.sos.SosObservationOffering;
import org.n52.shetland.ogc.sos.request.GetFeatureOfInterestRequest;
import org.n52.shetland.ogc.sos.response.GetFeatureOfInterestResponse;
import org.n52.shetland.ogc.sos.response.GetObservationResponse;
import org.slf4j.Logger;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Jan Schulte
 */
public class HydroSOSConnector extends SOS2Connector {

    private static final Logger LOGGER = getLogger(HydroSOSConnector.class);

    @Override
    protected boolean canHandle(DataSourceConfiguration config, GetCapabilitiesResponse capabilities) {
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
        GetObservationResponse obsResp = createObservationResponse(seriesEntity, createTimePeriodFilter(
                query));

        List<DataEntity> data = new ArrayList<>();

        obsResp.getObservationCollection().forEach((observation) -> {
            QuantityDataEntity entity = new QuantityDataEntity();
            SingleObservationValue obsValue = (SingleObservationValue) observation.getValue();

            TimeInstant instant = (TimeInstant) obsValue.getPhenomenonTime();
            entity.setTimestart(instant.getValue().toDate());
            entity.setTimeend(instant.getValue().toDate());
            QuantityValue value = (QuantityValue) obsValue.getValue();
            entity.setValue(value.getValue());

            data.add(entity);
        });
        LOGGER.info("Found " + data.size() + " Entries");
        return data;
    }

//    @Override
//    public UnitEntity getUom(DatasetEntity seriesEntity) {
//        // TODO implement
//        throw new UnsupportedOperationException("getUom not supported yet.");
//    }
//
//    @Override
//    public Optional<DataEntity> getFirstObservation(DatasetEntity entity) {
//        // TODO implement
//        throw new UnsupportedOperationException("getFirstObservation not supported yet.");
//    }
//
//    @Override
//    public Optional<DataEntity> getLastObservation(DatasetEntity entity) {
//        // TODO implement
//        throw new UnsupportedOperationException("getLastObservation not supported yet.");
//    }

    @Override
    protected void doForOffering(SosObservationOffering obsOff, ServiceConstellation serviceConstellation, String url) {
        String offeringId = addOffering(obsOff, serviceConstellation);

        obsOff.getProcedures().forEach((procedureId) -> {
            addProcedure(procedureId, true, false, serviceConstellation);
            obsOff.getObservableProperties().forEach(phenomenonId -> {
                addPhenomenon(phenomenonId, serviceConstellation);
                String categoryId = addCategory(phenomenonId, serviceConstellation);

                GetFeatureOfInterestResponse foiResponse = getFeatureOfInterestResponse(procedureId, url);
                AbstractFeature abstractFeature = foiResponse.getAbstractFeature();
                if (abstractFeature instanceof FeatureCollection) {
                    FeatureCollection featureCollection = (FeatureCollection) abstractFeature;
                    featureCollection.getMembers().forEach((key, feature) -> {
                        String featureId = addFeature((SamplingFeature) feature, serviceConstellation);
                        // TODO maybe not only QuantityDatasetConstellation
                        serviceConstellation.add(new QuantityDatasetConstellation(procedureId, offeringId, categoryId,
                                        phenomenonId, featureId));
                    });
                }
            });
        });
    }

    private GetFeatureOfInterestResponse getFeatureOfInterestResponse(String procedureId, String url) {
        GetFeatureOfInterestRequest request = new GetFeatureOfInterestRequest(SOS, SERVICEVERSION);
        request.setProcedures(new ArrayList<>(asList(procedureId)));
        return (GetFeatureOfInterestResponse) getSosResponseFor(request, NS_SOS_20, url);
    }

}
