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

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.n52.proxy.config.DataSourceConfiguration;
import org.n52.proxy.connector.constellations.ProfileDatasetConstellation;
import org.n52.proxy.connector.utils.EntityBuilder;
import org.n52.proxy.connector.utils.ServiceConstellation;
import org.n52.proxy.db.beans.ProxyServiceEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.FeatureEntity;
import org.n52.series.db.beans.ProfileDataEntity;
import org.n52.series.db.beans.QuantityDataEntity;
import org.n52.series.db.beans.UnitEntity;
import org.n52.series.db.beans.parameter.Parameter;
import org.n52.series.db.beans.parameter.ParameterQuantity;
import org.n52.series.db.dao.DbQuery;
import org.n52.shetland.ogc.filter.FilterConstants;
import org.n52.shetland.ogc.filter.SpatialFilter;
import org.n52.shetland.ogc.filter.TemporalFilter;
import org.n52.shetland.ogc.gml.AbstractFeature;
import org.n52.shetland.ogc.gml.time.TimeInstant;
import org.n52.shetland.ogc.om.ObservationStream;
import org.n52.shetland.ogc.om.ObservationValue;
import org.n52.shetland.ogc.om.OmObservation;
import org.n52.shetland.ogc.om.features.FeatureCollection;
import org.n52.shetland.ogc.om.features.samplingFeatures.SamplingFeature;
import org.n52.shetland.ogc.om.values.SweDataArrayValue;
import org.n52.shetland.ogc.om.values.Value;
import org.n52.shetland.ogc.ows.OwsCapabilities;
import org.n52.shetland.ogc.ows.OwsServiceProvider;
import org.n52.shetland.ogc.ows.exception.OwsExceptionReport;
import org.n52.shetland.ogc.ows.service.GetCapabilitiesResponse;
import org.n52.shetland.ogc.sensorML.AbstractProcess;
import org.n52.shetland.ogc.sensorML.HasComponents;
import org.n52.shetland.ogc.sensorML.SensorML;
import org.n52.shetland.ogc.sensorML.System;
import org.n52.shetland.ogc.sensorML.elements.SmlComponent;
import org.n52.shetland.ogc.sos.Sos2Constants;
import org.n52.shetland.ogc.sos.SosCapabilities;
import static org.n52.shetland.ogc.sos.SosConstants.SOS;
import org.n52.shetland.ogc.sos.SosObservationOffering;
import org.n52.shetland.ogc.sos.request.DescribeSensorRequest;
import org.n52.shetland.ogc.sos.request.GetFeatureOfInterestRequest;
import org.n52.shetland.ogc.sos.request.GetObservationRequest;
import org.n52.shetland.ogc.sos.response.GetObservationResponse;
import org.n52.shetland.ogc.swe.SweAbstractDataComponent;
import org.n52.shetland.ogc.swe.SweDataArray;
import org.n52.shetland.ogc.swe.SweDataRecord;
import org.n52.shetland.ogc.swe.SweField;
import org.n52.shetland.ogc.swe.simpleType.SweQuantity;
import org.n52.shetland.ogc.swes.SwesConstants;
import org.n52.shetland.util.ReferencedEnvelope;
import org.slf4j.Logger;
import static org.slf4j.LoggerFactory.getLogger;

public class OceanotronSosConnector extends SOS2Connector {

    private static final Logger LOGGER = getLogger(OceanotronSosConnector.class);

    /**
     * Matches when the provider name is equal "Geomatys"
     */
    @Override
    public boolean canHandle(DataSourceConfiguration config, GetCapabilitiesResponse capabilities) {
        OwsCapabilities owsCaps = capabilities.getCapabilities();
        if (owsCaps.getServiceProvider().isPresent()) {
            OwsServiceProvider servProvider = owsCaps.getServiceProvider().get();
            if (servProvider.getProviderName().equals("Geomatys")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<DataEntity> getObservations(DatasetEntity seriesEntity, DbQuery query) {
        GetObservationResponse observationResponse = createObservationResponse(seriesEntity, null,
                "text/xml;subtype=\"http://www.opengis.net/om/2.0\"");
        ArrayList<DataEntity> data = new ArrayList<>();
        try {
            observationResponse.getObservationCollection().forEachRemaining((observation) -> {
                data.add(createProfileDataEntity(observation, seriesEntity));
            });
        } catch (OwsExceptionReport e) {
            LOGGER.error("Error while querying and processing observations!", e);
        }
        return data;
    }

    @Override
    public UnitEntity getUom(DatasetEntity seriesEntity) {
        GetObservationResponse observationResponse = createObservationResponse(seriesEntity, null,
                "text/xml;subtype=\"http://www.opengis.net/om/2.0\"");
        ObservationStream omColl = observationResponse.getObservationCollection();
        try {
            if (omColl.hasNext()) {
                OmObservation observation = omColl.next();
                ObservationValue<? extends Value<?>> observationValue = observation.getValue();
                if (observationValue.getValue() instanceof SweDataArrayValue) {
                    SweDataArray dataArray = ((SweDataArrayValue) observationValue.getValue()).getValue();
                    SweAbstractDataComponent elementType = dataArray.getElementType();
                    if (elementType instanceof SweDataRecord) {
                        SweDataRecord sweDataRecord = (SweDataRecord) elementType;
                        List<SweField> fields = sweDataRecord.getFields();
                        if (fields.size() == 2) {
                            return createUnitEntity(fields.get(1), (ProxyServiceEntity) seriesEntity.getService());
                        }
                    }
                }
            }
        } catch (OwsExceptionReport e) {
            LOGGER.error("Error while querying unit observations!", e);
        }
        return null;
    }

    private DataEntity createProfileDataEntity(OmObservation observation, DatasetEntity seriesEntity) {
        ProfileDataEntity dataEntity = new ProfileDataEntity();
        ObservationValue<? extends Value<?>> obsValue = observation.getValue();
        Date timestamp = ((TimeInstant) obsValue.getPhenomenonTime()).getValue().toDate();
        dataEntity.setTimestart(timestamp);
        dataEntity.setTimeend(timestamp);

        Set<DataEntity<?>> values = new HashSet<>();

        if (obsValue.getValue() instanceof SweDataArrayValue) {
            SweDataArray dataArray = ((SweDataArrayValue) obsValue.getValue()).getValue();
            SweAbstractDataComponent elementType = dataArray.getElementType();
            UnitEntity verticalUnit = null;
            if (elementType instanceof SweDataRecord) {
                SweDataRecord sweDataRecord = (SweDataRecord) elementType;
                List<SweField> fields = sweDataRecord.getFields();
                if (fields.size() == 2) {
                    verticalUnit = createUnitEntity(fields.get(0),
                            (ProxyServiceEntity) seriesEntity.getService());
                }
            }
            for (List<String> valueEntry : dataArray.getValues()) {
                double measurement = Double.valueOf(valueEntry.get(1));
                double verticalValue = Double.valueOf(valueEntry.get(0));
                LOGGER.info("Value: {}, VerticalValue: {}", measurement, verticalValue);
                values.add(createVerticalEntry(measurement, timestamp, verticalUnit, verticalValue));
            }
        }

        dataEntity.setValue(values);
        return dataEntity;
    }

    private UnitEntity createUnitEntity(SweField field, ProxyServiceEntity service) {
        if (!(field.getElement() instanceof SweQuantity)) {
            return null;
        }
        SweQuantity sweQuantity = (SweQuantity) field.getElement();
        String description = sweQuantity.getDefinition();
        String uom = sweQuantity.getUom();
        return EntityBuilder.createUnit(uom, description, service);
    }

    private QuantityDataEntity createVerticalEntry(double measurement, Date timestamp, UnitEntity verticalUnit,
            double verticalValue) {
        QuantityDataEntity quantityDataEntity = new QuantityDataEntity();
        quantityDataEntity.setValue(measurement);
        quantityDataEntity.setTimestart(timestamp);
        quantityDataEntity.setTimeend(timestamp);
        Set<Parameter<?>> parameters = new HashSet<>();
        ParameterQuantity parameterQuantity = new ParameterQuantity();
        parameterQuantity.setUnit(verticalUnit);
        parameterQuantity.setName("depth");
        parameterQuantity.setValue(verticalValue);
        parameters.add(parameterQuantity);
        quantityDataEntity.setParameters(parameters);
        return quantityDataEntity;
    }

    @Override
    public Optional<DataEntity> getFirstObservation(DatasetEntity entity) {
        // TODO implement
        return Optional.empty();
    }

    @Override
    public Optional<DataEntity> getLastObservation(DatasetEntity entity) {
        // TODO implement
        return Optional.empty();
    }

    protected void addDatasets(ServiceConstellation serviceConstellation, SosCapabilities sosCaps,
            DataSourceConfiguration config) {
        if (sosCaps != null) {
            sosCaps.getContents().ifPresent((obsOffs) -> {
                obsOffs.forEach((SosObservationOffering obsOff) -> {
                    if (config.getAllowedOfferings().contains(obsOff.getIdentifier())) {
                        addElem(obsOff, serviceConstellation, config.getUrl());
                    }
                });
            });
        }
    }

    private void addElem(SosObservationOffering obsOff, ServiceConstellation servConst, String url) {

        String offeringId = obsOff.getOffering().getIdentifier();
        servConst.putOffering(offeringId, offeringId);

        LOGGER.info("Harvest for Offering {} with {} procedure(s), {} observableProperperties", offeringId,
                obsOff.getProcedures().size(), obsOff.getObservableProperties().size());
        obsOff.getProcedures().forEach((String procedureId) -> {
            LOGGER.info("Harvest Procedure {}", procedureId);
            SensorML sensorML = getDescribeSensorResponse(procedureId, url);
            sensorML.getMembers().forEach((AbstractProcess member) -> {
                obsOff.getObservableProperties().forEach((String obsProp) -> {
                    if (!obsProp.equals("sea_water_salinity")) {
                        if (member instanceof System) {
                            List<SmlComponent> components = ((HasComponents<System>) member).getComponents();
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
        FeatureCollection featureColl = getFeatureOfInterestResponse(procedureComponentId, obsProp, url);

        featureColl.getMembers().forEach((String key, AbstractFeature feature) -> {
            String foiId = addFeature(feature, servConst);
            // TODO maybe not only QuantityDatasetConstellation
            if (foiId != null) {
                ProfileDatasetConstellation profileDatasetConstellation = new ProfileDatasetConstellation(
                        procedureComponentId, offeringId,
                        obsProp,
                        obsProp,
                        foiId);
                servConst.add(profileDatasetConstellation);
            }
        });
    }

    private String addFeature(AbstractFeature feature, ServiceConstellation servConst) {
        if (feature instanceof SamplingFeature) {
            String foiId = feature.getGmlId();
            String foiName = feature.getFirstName() != null ? feature.getFirstName().getValue() : feature.getGmlId();
            String foiDescription = feature.getDescription();
            SamplingFeature samplingFeature = (SamplingFeature) feature;
            int srid = samplingFeature.getGeometry().getSRID();
            double lon = samplingFeature.getGeometry().getCoordinate().x;
            double lat = samplingFeature.getGeometry().getCoordinate().y;
            servConst.putFeature(foiId, foiName, foiDescription, lat, lon, srid);
            return foiId;
        }
        return null;
    }

    private SensorML getDescribeSensorResponse(String procedureId, String url) {
        DescribeSensorRequest request = new DescribeSensorRequest(SOS, Sos2Constants.SERVICEVERSION);
        request.setProcedure(procedureId);
        request.setProcedureDescriptionFormat("http://www.opengis.net/sensorML/1.0.0");
        return (SensorML) getSosResponseFor(request, SwesConstants.NS_SWES_20, url);
    }

    private FeatureCollection getFeatureOfInterestResponse(String procedureId, String obsProp, String url) {
        GetFeatureOfInterestRequest request = new GetFeatureOfInterestRequest(SOS, Sos2Constants.SERVICEVERSION);
        request.setProcedures(Arrays.asList(procedureId));
        request.setObservedProperties(Arrays.asList(obsProp));
        Object response = getSosResponseFor(request, Sos2Constants.NS_SOS_20, url);
        return (FeatureCollection) response;
    }

    @Override
    protected GetObservationResponse createObservationResponse(DatasetEntity seriesEntity, TemporalFilter temporalFilter,
            String responseFormat) {
        GetObservationRequest request = new GetObservationRequest(SOS, Sos2Constants.SERVICEVERSION);
        request.setProcedures(Arrays.asList(seriesEntity.getProcedure().getDomainId()));
        request.setObservedProperties(Arrays.asList(seriesEntity.getPhenomenon().getDomainId()));
        request.setSpatialFilter(createSpatialFilter(seriesEntity.getFeature()));
        if (temporalFilter != null) {
            request.setTemporalFilters(Arrays.asList(temporalFilter));
        }
        if (responseFormat != null) {
            request.setResponseFormat(responseFormat);
        }
        return (GetObservationResponse) this.getSosResponseFor(request, Sos2Constants.NS_SOS_20,
                seriesEntity.getService().getUrl());
    }

    private SpatialFilter createSpatialFilter(FeatureEntity feature) {
        SpatialFilter spatialFilter = new SpatialFilter();
        Geometry geometry = feature.getGeometry();
        if (geometry instanceof Point) {
            Point point = (Point) geometry;
            double x = point.getCoordinate().x;
            double y = point.getCoordinate().y;
            ReferencedEnvelope referencedEnvelope = new ReferencedEnvelope(new Envelope(x, x, y, y), point.getSRID());
            spatialFilter.setGeometry(referencedEnvelope);
            spatialFilter.setOperator(FilterConstants.SpatialOperator.Overlaps);
            return spatialFilter;
        }
        return null;
    }

}
