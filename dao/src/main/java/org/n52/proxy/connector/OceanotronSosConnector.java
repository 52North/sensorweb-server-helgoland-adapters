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
import static java.util.stream.Collectors.toSet;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.n52.janmayen.function.Functions;
import org.n52.proxy.config.DataSourceConfiguration;
import org.n52.proxy.connector.constellations.ProfileDatasetConstellation;
import org.n52.proxy.connector.utils.EntityBuilder;
import org.n52.proxy.connector.utils.ServiceConstellation;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.ProfileDataEntity;
import org.n52.series.db.beans.QuantityDataEntity;
import org.n52.series.db.beans.ServiceEntity;
import org.n52.series.db.beans.UnitEntity;
import org.n52.series.db.old.dao.DbQuery;
import org.n52.shetland.ogc.gml.AbstractFeature;
import org.n52.shetland.ogc.gml.time.TimeInstant;
import org.n52.shetland.ogc.om.ObservationValue;
import org.n52.shetland.ogc.om.OmObservation;
import org.n52.shetland.ogc.om.features.FeatureCollection;
import org.n52.shetland.ogc.om.features.samplingFeatures.SamplingFeature;
import org.n52.shetland.ogc.om.values.SweDataArrayValue;
import org.n52.shetland.ogc.om.values.Value;
import org.n52.shetland.ogc.ows.OwsServiceProvider;
import org.n52.shetland.ogc.ows.service.GetCapabilitiesResponse;
import org.n52.shetland.ogc.sensorML.AbstractProcess;
import org.n52.shetland.ogc.sensorML.SensorML;
import org.n52.shetland.ogc.sensorML.System;
import org.n52.shetland.ogc.sensorML.elements.SmlComponent;
import org.n52.shetland.ogc.sos.Sos2Constants;
import org.n52.shetland.ogc.sos.SosCapabilities;
import org.n52.shetland.ogc.sos.SosConstants;
import org.n52.shetland.ogc.sos.SosObservationOffering;
import org.n52.shetland.ogc.sos.request.DescribeSensorRequest;
import org.n52.shetland.ogc.sos.response.GetFeatureOfInterestResponse;
import org.n52.shetland.ogc.sos.response.GetObservationResponse;
import org.n52.shetland.ogc.swe.SweAbstractDataComponent;
import org.n52.shetland.ogc.swe.SweDataArray;
import org.n52.shetland.ogc.swe.SweDataRecord;
import org.n52.shetland.ogc.swe.SweField;
import org.n52.shetland.ogc.swe.simpleType.SweQuantity;
import org.n52.shetland.ogc.swes.SwesConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class OceanotronSosConnector extends SOS2Connector {

    private static final Logger LOGGER = LoggerFactory.getLogger(OceanotronSosConnector.class);
    private static final String OM_2_MIMETYPE = "text/xml;subtype=\"http://www.opengis.net/om/2.0\"";

    /**
     * Matches when the provider name is equal "Geomatys"
     */
    @Override
    public boolean canHandle(DataSourceConfiguration config, GetCapabilitiesResponse capabilities) {
        return capabilities.getCapabilities().getServiceProvider()
                .map(OwsServiceProvider::getProviderName)
                .filter(name -> name.equals("Geomatys")).isPresent();
    }

    @Override
    public List<DataEntity<?>> getObservations(DatasetEntity seriesEntity, DbQuery query) {
        GetObservationResponse observationResponse = getObservation(seriesEntity, null, OM_2_MIMETYPE);
        return observationResponse.getObservationCollection().toStream()
                .map(observation -> createProfileDataEntity(observation, seriesEntity))
                .collect(toList());
    }

    @Override
    public UnitEntity getUom(DatasetEntity seriesEntity) {
        GetObservationResponse observationResponse = getObservation(seriesEntity, null, OM_2_MIMETYPE);
        List<OmObservation> omColl = observationResponse.getObservationCollection().toStream().collect(toList());
        if (omColl.size() == 1) {
            OmObservation observation = omColl.get(0);
            ObservationValue<? extends Value<?>> observationValue = observation.getValue();
            if (observationValue.getValue() instanceof SweDataArrayValue) {
                SweDataArray dataArray = ((SweDataArrayValue) observationValue.getValue()).getValue();
                SweAbstractDataComponent elementType = dataArray.getElementType();
                if (elementType instanceof SweDataRecord) {
                    SweDataRecord sweDataRecord = (SweDataRecord) elementType;
                    List<SweField> fields = sweDataRecord.getFields();
                    if (fields.size() == 2) {
                        return createUnitEntity(fields.get(1), seriesEntity.getService());
                    }
                }
            }
        }
        return null;
    }

    private DataEntity<?> createProfileDataEntity(OmObservation observation, DatasetEntity seriesEntity) {
        ProfileDataEntity dataEntity = new ProfileDataEntity();
        ObservationValue<? extends Value<?>> obsValue = observation.getValue();
        Date timestamp = ((TimeInstant) obsValue.getPhenomenonTime()).getValue().toDate();
        dataEntity.setSamplingTimeStart(timestamp);
        dataEntity.setSamplingTimeEnd(timestamp);
        ServiceEntity service = seriesEntity.getService();
        Optional<Set<DataEntity<?>>> values = Optional.ofNullable(obsValue)
                .map(ObservationValue::getValue)
                .flatMap(Functions.castIfInstanceOf(SweDataArrayValue.class))
                .map(SweDataArrayValue::getValue)
                .map(dataArray -> {
                    SweAbstractDataComponent elementType = dataArray.getElementType();
                    UnitEntity verticalUnit = Optional.ofNullable(elementType)
                            .flatMap(Functions.castIfInstanceOf(SweDataRecord.class))
                            .map(SweDataRecord::getFields)
                            .filter(fields -> fields.size() == 2)
                            .map(fields -> createUnitEntity(fields.get(0), service))
                            .orElse(null);
//TODO VerticalMetadataEntity
                    return dataArray.getValues().stream()
                            .map(valueEntry -> {
                                BigDecimal measurement = new BigDecimal(valueEntry.get(1));
                                BigDecimal verticalValue = new BigDecimal(valueEntry.get(0));
                                LOGGER.info("Value: {}, VerticalValue: {}", measurement, verticalValue);
                                return createVerticalEntry(measurement, timestamp, verticalValue);
                            })
                            .map(x -> (DataEntity<?>) x)
                            .collect(toSet());
                });
        dataEntity.setValue(values.orElseGet(HashSet::new));
        return dataEntity;
    }

    private UnitEntity createUnitEntity(SweField field, ServiceEntity service) {
        if (!(field.getElement() instanceof SweQuantity)) {
            return null;
        }
        SweQuantity sweQuantity = (SweQuantity) field.getElement();
        String description = sweQuantity.getDefinition();
        String uom = sweQuantity.getUom();
        return EntityBuilder.createUnit(uom, description, service);
    }

    private QuantityDataEntity createVerticalEntry(BigDecimal measurement, Date timestamp,
                                                   BigDecimal verticalValue) {
        QuantityDataEntity quantityDataEntity = new QuantityDataEntity();
        quantityDataEntity.setValue(measurement);
        quantityDataEntity.setSamplingTimeStart(timestamp);
        quantityDataEntity.setSamplingTimeEnd(timestamp);
        quantityDataEntity.setVerticalFrom(verticalValue);
        quantityDataEntity.setVerticalFrom(verticalValue);
//        Set<ParameterEntity<?>> parameters = new HashSet<>(1);
//        ParameterQuantityEntity parameterQuantity = new ParameterQuantityEntity();
//        parameterQuantity.setUnit(verticalUnit);
//        parameterQuantity.setName("depth");
//        parameterQuantity.setValue(verticalValue);
//        parameters.add(parameterQuantity);
//        quantityDataEntity.setParameters(parameters);
        return quantityDataEntity;
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
    protected void addDatasets(ServiceConstellation serviceConstellation, SosCapabilities sosCaps,
                               DataSourceConfiguration config) {
        if (sosCaps != null) {
            sosCaps.getContents().ifPresent(obsOffs -> {
                obsOffs.stream()
                        .filter(obsOff -> config.getAllowedOfferings() == null ||
                                          config.getAllowedOfferings().contains(obsOff.getIdentifier()))
                        .forEach(obsOff -> addElem(obsOff, serviceConstellation, config.getUrl()));
            });
        }
    }

    private void addElem(SosObservationOffering obsOff, ServiceConstellation servConst, String url) {

        String offeringId = obsOff.getOffering().getIdentifier();
        addMetadata(servConst.putOffering(offeringId, offeringId), obsOff);

        LOGGER.info("Harvest for Offering {} with {} procedure(s), {} observableProperperties", offeringId,
                    obsOff.getProcedures().size(), obsOff.getObservableProperties().size());
        obsOff.getProcedures().forEach((String procedureId) -> {
            LOGGER.info("Harvest Procedure {}", procedureId);
            SensorML sensorML = getDescribeSensorResponse(procedureId, url);
            sensorML.getMembers().forEach((AbstractProcess member) -> {
                obsOff.getObservableProperties().forEach((String obsProp) -> {
                    if (!obsProp.equals("sea_water_salinity")) {
                        if (member instanceof System) {
                            List<SmlComponent> components = ((System) member).getComponents();
                            for (int i = 0; i < components.size(); i++) {
                                LOGGER.info("Still get " + (components.size() - i) + " components");
                                addElem(components.get(i), obsProp, offeringId, servConst, url);
                            }
                        }
                    }
                });
            });
        });
    }

    private void addElem(SmlComponent component, String obsProp, String offeringId,
                         ServiceConstellation servConst, String url) {
        final String procedureComponentId = component.getName();
        servConst.putProcedure(procedureComponentId, procedureComponentId, true, false);
        servConst.putPhenomenon(obsProp, obsProp);
        servConst.putCategory(obsProp, obsProp);

        LOGGER.info("Send getFOI request with procedure component {} and observedProperty {}", procedureComponentId,
                    obsProp);
        GetFeatureOfInterestResponse response = getFeatureOfInterest(null, procedureComponentId, obsProp, url);
        if (response.getAbstractFeature() instanceof FeatureCollection) {
            ((FeatureCollection) response.getAbstractFeature())
                    .getMembers().forEach((String key, AbstractFeature feature) -> {
                        String foiId = addAbstractFeature(feature, servConst);
                        // TODO maybe not only QuantityDatasetConstellation
                        if (foiId != null) {
                            ProfileDatasetConstellation profileDatasetConstellation = new ProfileDatasetConstellation(
                                    procedureComponentId, offeringId,
                                    obsProp,
                                    foiId);
                            servConst.add(profileDatasetConstellation);
                        }
                    });
        } else {
            String foiId = addAbstractFeature(response.getAbstractFeature(), servConst);
            // TODO maybe not only QuantityDatasetConstellation
            if (foiId != null) {
                ProfileDatasetConstellation profileDatasetConstellation = new ProfileDatasetConstellation(
                        procedureComponentId, offeringId,
                        obsProp,
                        foiId);
                servConst.add(profileDatasetConstellation);
            }

        }
    }

    private String addAbstractFeature(AbstractFeature feature, ServiceConstellation servConst) {
        if (feature instanceof SamplingFeature) {
            String foiId = feature.getGmlId();
            String foiName = feature.getFirstName() != null ? feature.getFirstName().getValue() : feature.getGmlId();
            String foiDescription = feature.getDescription();
            SamplingFeature samplingFeature = (SamplingFeature) feature;
            servConst.putFeature(foiId, foiName, foiDescription, samplingFeature.getGeometry());
            servConst.putPlatform(foiId, foiName, foiDescription);
            return foiId;
        }
        return null;
    }

    private SensorML getDescribeSensorResponse(String procedureId, String url) {
        DescribeSensorRequest request = new DescribeSensorRequest(SosConstants.SOS, Sos2Constants.SERVICEVERSION);
        request.setProcedure(procedureId);
        request.setProcedureDescriptionFormat("http://www.opengis.net/sensorML/1.0.0");
        return (SensorML) getSosResponseFor(request, SwesConstants.NS_SWES_20, url);
    }

}
