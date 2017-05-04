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
import static java.util.Arrays.asList;
import java.util.List;
import java.util.Optional;
import org.n52.proxy.config.DataSourceConfiguration;
import org.n52.proxy.connector.constellations.QuantityDatasetConstellation;
import org.n52.proxy.connector.utils.EntityBuilder;
import org.n52.proxy.connector.utils.ServiceConstellation;
import org.n52.proxy.db.beans.ProxyServiceEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.FeatureEntity;
import org.n52.series.db.beans.UnitEntity;
import org.n52.series.db.dao.DbQuery;
import org.n52.shetland.ogc.filter.FilterConstants;
import org.n52.shetland.ogc.filter.SpatialFilter;
import org.n52.shetland.ogc.filter.TemporalFilter;
import org.n52.shetland.ogc.gml.AbstractFeature;
import org.n52.shetland.ogc.om.ObservationValue;
import org.n52.shetland.ogc.om.OmObservation;
import org.n52.shetland.ogc.om.features.FeatureCollection;
import org.n52.shetland.ogc.om.features.samplingFeatures.SamplingFeature;
import org.n52.shetland.ogc.om.values.SweDataArrayValue;
import org.n52.shetland.ogc.om.values.Value;
import org.n52.shetland.ogc.ows.OwsCapabilities;
import org.n52.shetland.ogc.ows.OwsServiceProvider;
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
        // TODO implement
        throw new UnsupportedOperationException("getObservations not supported yet.");
    }

    @Override
    public UnitEntity getUom(DatasetEntity seriesEntity) {
        GetObservationResponse observationResponse = createObservationResponse(seriesEntity, null,
                "text/xml;subtype=\"http://www.opengis.net/om/2.0\"");
        List<OmObservation> omColl = observationResponse.getObservationCollection();
        if (omColl.size() == 1) {
            OmObservation observation = omColl.get(0);
            ObservationValue<? extends Value<?>> observationValue = observation.getValue();
//            Time phenomenonTime = observationValue.getPhenomenonTime();
            if (observationValue.getValue() instanceof SweDataArrayValue) {
                SweDataArray dataArray = ((SweDataArrayValue) observationValue.getValue()).getValue();
                SweAbstractDataComponent elementType = dataArray.getElementType();
                if (elementType instanceof SweDataRecord) {
                    SweDataRecord sweDataRecord = (SweDataRecord) elementType;
                    List<SweField> fields = sweDataRecord.getFields();
                    if (fields.size() == 2) {
                        SweField field = fields.get(1);
                        if (field.getElement() instanceof SweQuantity) {
                            SweQuantity sweQuantity = (SweQuantity) field.getElement();
                            String description = sweQuantity.getDefinition();
                            String uom = sweQuantity.getUom();
                            return EntityBuilder.createUnit(uom, description,
                                    (ProxyServiceEntity) seriesEntity.getService());
                        }
                    }
                }
            }
        }
        return null;
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

    protected void addDatasets(ServiceConstellation serviceConstellation, SosCapabilities sosCaps, String url) {
        if (sosCaps != null) {
            sosCaps.getContents().ifPresent((obsOffs) -> {
                obsOffs.forEach((SosObservationOffering obsOff) -> {
                    // TODO remove if
                    if (obsOff.getIdentifier().equals("CORIOLIS-RECOPESCA")) {
                        addElem(obsOff, serviceConstellation, url);
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
                            // TODO use components.size() instead of 1
                            for (int i = 0; i < 2; i++) {
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
                servConst.add(new QuantityDatasetConstellation(procedureComponentId, offeringId,
                        obsProp,
                        obsProp,
                        foiId));
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
        request.setProcedures(new ArrayList(Arrays.asList(procedureId)));
        request.setObservedProperties(new ArrayList(Arrays.asList(obsProp)));
        Object response = getSosResponseFor(request, Sos2Constants.NS_SOS_20, url);
        return (FeatureCollection) response;
    }

    @Override
    protected GetObservationResponse createObservationResponse(DatasetEntity seriesEntity, TemporalFilter temporalFilter,
            String responseFormat) {
        GetObservationRequest request = new GetObservationRequest(SOS, Sos2Constants.SERVICEVERSION);
        request.setProcedures(new ArrayList<>(asList(seriesEntity.getProcedure().getDomainId())));
        request.setObservedProperties(new ArrayList<>(asList(seriesEntity.getPhenomenon().getDomainId())));
        request.setSpatialFilter(createSpatialFilter(seriesEntity.getFeature()));
        if (temporalFilter != null) {
            request.setTemporalFilters(new ArrayList<>(asList(temporalFilter)));
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
