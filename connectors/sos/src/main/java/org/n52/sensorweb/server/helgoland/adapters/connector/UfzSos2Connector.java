/*
 * Copyright (C) 2015-2022 52°North Spatial Information Research GmbH
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
package org.n52.sensorweb.server.helgoland.adapters.connector;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedSet;

import org.n52.sensorweb.server.db.old.dao.DbQuery;
import org.n52.sensorweb.server.helgoland.adapters.connector.constellations.QuantityDatasetConstellation;
import org.n52.sensorweb.server.helgoland.adapters.connector.utils.ServiceConstellation;
import org.n52.sensorweb.server.helgoland.adapters.harvest.DataSourceJobConfiguration;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.UnitEntity;
import org.n52.shetland.ogc.gml.AbstractFeature;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.shetland.ogc.gml.time.TimeInstant;
import org.n52.shetland.ogc.gml.time.TimePeriod;
import org.n52.shetland.ogc.om.features.samplingFeatures.AbstractSamplingFeature;
import org.n52.shetland.ogc.sensorML.AbstractProcess;
import org.n52.shetland.ogc.sensorML.SensorML20Constants;
import org.n52.shetland.ogc.sensorML.elements.SmlCharacteristic;
import org.n52.shetland.ogc.sensorML.elements.SmlCharacteristics;
import org.n52.shetland.ogc.sensorML.elements.SmlComponent;
import org.n52.shetland.ogc.sensorML.elements.SmlIdentifier;
import org.n52.shetland.ogc.sensorML.elements.SmlIo;
import org.n52.shetland.ogc.sensorML.v20.AbstractProcessV20;
import org.n52.shetland.ogc.sensorML.v20.PhysicalSystem;
import org.n52.shetland.ogc.sos.SosCapabilities;
import org.n52.shetland.ogc.sos.SosObservationOffering;
import org.n52.shetland.ogc.sos.SosProcedureDescription;
import org.n52.shetland.ogc.sos.response.DescribeSensorResponse;
import org.n52.shetland.ogc.sos.response.GetFeatureOfInterestResponse;
import org.n52.shetland.ogc.swe.SweAbstractDataComponent;
import org.n52.shetland.ogc.swe.simpleType.SweQuantity;
import org.n52.shetland.ogc.swe.simpleType.SweText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UfzSos2Connector extends SOS2Connector {

    private static final Logger LOGGER = LoggerFactory.getLogger(UfzSos2Connector.class);

    private Map<String, AbstractFeature> featureCache = new LinkedHashMap<>();

    @Override
    public List<DataEntity<?>> getObservations(DatasetEntity seriesEntity, DbQuery query) {
        return Collections.emptyList();
        // List<DataEntity<?>> data = getObservation(seriesEntity,
        // createTimeFilter(query))
        // .getObservationCollection().toStream()
        // .map(Functions.currySecond(this::createDataEntity, seriesEntity))
        // .collect(toList());
        // LOGGER.info("Found {} Entries", data.size());
        // return data;
    }

    @Override
    public Optional<DataEntity<?>> getFirstObservation(DatasetEntity dataset) {
        return Optional.empty();
        // return getObservation(dataset, createFirstTimefilter(dataset))
        // .getObservationCollection().toStream().findFirst()
        // .map(obs -> createDataEntity(obs, dataset));
    }

    @Override
    public Optional<DataEntity<?>> getLastObservation(DatasetEntity dataset) {
        return Optional.empty();
        // return getObservation(dataset, createLatestTimefilter(dataset))
        // .getObservationCollection().toStream().findFirst()
        // .map(obs -> createDataEntity(obs, dataset));
    }

    @Override
    protected void addDatasets(ServiceConstellation serviceConstellation, SosCapabilities sosCaps,
            DataSourceJobConfiguration config) {
        this.featureCache.clear();
        super.addDatasets(serviceConstellation, sosCaps, config);
    }

    @Override
    protected void doForOffering(SosObservationOffering obsOff, ServiceConstellation serviceConstellation,
            DataSourceJobConfiguration config) {
        String offeringId = addOffering(obsOff, serviceConstellation);

        obsOff.getProcedures().stream().filter(procedureId -> {
            for (String sensor : config.getAllowedSensors()) {
                if (procedureId.endsWith(sensor)) {
                    return true;
                }
            }
            return false;
        }).forEach(procedureId -> {
            try {
                String format = getFormat(obsOff.getProcedureDescriptionFormats());
                DescribeSensorResponse dsr = describeSensor(procedureId, format, config);
                if (dsr.isSetProcedureDescriptions()) {
                    for (SosProcedureDescription<?> pd : dsr.getProcedureDescriptions()) {
                        if (format.equals(SensorML20Constants.NS_SML_20)) {
                            if (pd.getProcedureDescription() instanceof PhysicalSystem) {
                                PhysicalSystem ps = (PhysicalSystem) pd.getProcedureDescription();
                                String categoryId = getCategory(ps, serviceConstellation);
                                String featureId = getFeatureId(ps, config, serviceConstellation);
                                String platformId = getPlatformId(ps, serviceConstellation);
                                if (ps.isSetComponents()) {
                                    // Set<String> children = new
                                    // LinkedHashSet<>();
                                    for (SmlComponent component : ps.getComponents()) {
                                        if (component.isSetProcess()) {
                                            AbstractProcess process = (AbstractProcess) component.getProcess();
                                            String componentName = checkForName(process);
                                            String componentId = addProcedure(component.getName().replaceAll("_", ":"),
                                                    componentName != null ? componentName : process.getIdentifier(),
                                                    true, false, serviceConstellation);
                                            // children.add(componentId);
                                            for (SmlIo outputs : process.getOutputs()) {
                                                SweAbstractDataComponent ioValue = outputs.getIoValue();
                                                if (ioValue instanceof SweQuantity) {
                                                    SweQuantity q = (SweQuantity) ioValue;
                                                    String phenomenonId = addPhenomenon(q.getDefinition(),
                                                            q.getLabel(), serviceConstellation);
                                                    UnitEntity unit = createUnit(q.getUom(), q.getUom());
                                                    QuantityDatasetConstellation constellation =
                                                            new QuantityDatasetConstellation(componentId, offeringId,
                                                                    categoryId, phenomenonId, featureId, platformId);
                                                    constellation.setUnit(unit);
                                                    setPhenomenonTime(obsOff, constellation);
                                                    serviceConstellation.add(constellation);
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    String name = checkForName(ps);
                                    String procId =
                                            addProcedure(ps.getIdentifier(), name != null ? name : ps.getIdentifier(),
                                                    true, false, serviceConstellation);
                                    for (SmlIo outputs : ps.getOutputs()) {
                                        SweAbstractDataComponent ioValue = outputs.getIoValue();
                                        if (ioValue instanceof SweQuantity) {
                                            SweQuantity q = (SweQuantity) ioValue;
                                            String phenomenonId = addPhenomenon(q.getDefinition(), q.getLabel(),
                                                    serviceConstellation);
                                            serviceConstellation.add(new QuantityDatasetConstellation(procId,
                                                    offeringId, categoryId, phenomenonId, featureId, featureId));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.error(String.format("Error while harvesting offering '%s'!", offeringId), e);
            }

            // getchilds
            // add parent^
            // add childs
            // getfois check cache
            // addProcedure(procedureId, true, false, serviceConstellation);
            // obsOff.getObservableProperties().forEach(phenomenonId -> {
            // addPhenomenon(phenomenonId, serviceConstellation);
            // String categoryId = addCategory(phenomenonId,
            // serviceConstellation);
            //
            // GetFeatureOfInterestResponse foiResponse =
            // getFeatureOfInterestByProcedure(procedureId,
            // config.getUrl());
            // AbstractFeature abstractFeature =
            // foiResponse.getAbstractFeature();
            // if (abstractFeature instanceof FeatureCollection) {
            // FeatureCollection featureCollection = (FeatureCollection)
            // abstractFeature;
            // featureCollection.getMembers().forEach((key, feature) -> {
            // String featureId = addFeature((AbstractSamplingFeature) feature,
            // serviceConstellation);
            // // TODO maybe not only QuantityDatasetConstellation
            // serviceConstellation.add(new
            // QuantityDatasetConstellation(procedureId, offeringId, categoryId,
            // phenomenonId, featureId, featureId));
            // });
            // }
            // });
        });
    }

    private String checkForName(AbstractProcess process) {
        for (SmlIdentifier identifier : process.getIdentifications()) {
            if (identifier.getDefinition().equalsIgnoreCase("urn:ogc:def:identifier:OGC:longName")) {
                return identifier.getValue();
            }
        }
        return null;
    }

    private void setPhenomenonTime(SosObservationOffering offering, QuantityDatasetConstellation constellation) {
        Time phenomenonTime = offering.getPhenomenonTime();
        if (phenomenonTime instanceof TimePeriod) {
            constellation.setSamplingTimeStart(((TimePeriod) phenomenonTime).getStart().toDate());
            constellation.setSamplingTimeEnd(((TimePeriod) phenomenonTime).getEnd().toDate());
        } else if (phenomenonTime instanceof TimeInstant) {
            Date date = ((TimeInstant) phenomenonTime).getValue().toDate();
            constellation.setSamplingTimeStart(date);
            constellation.setSamplingTimeEnd(date);
        } else {
            constellation.setSamplingTimeStart(new Date());
            constellation.setSamplingTimeEnd(new Date());
        }
    }

    private String getCategory(PhysicalSystem ps, ServiceConstellation serviceConstellation) {
        if (ps.isSetCharacteristics()) {
            for (SmlCharacteristics characteristics : ps.getCharacteristics()) {
                if (characteristics.getName().equalsIgnoreCase("loggerType")) {
                    for (SmlCharacteristic characteristic : characteristics.getCharacteristic()) {
                        if (characteristic.getName().equalsIgnoreCase("loggerTypeName")
                                && characteristic.getAbstractDataComponent().getDefinition()
                                        .equalsIgnoreCase("urn:ogc:def:characteristic:OGC:loggerTypeName")
                                && characteristic.getAbstractDataComponent() instanceof SweText) {
                            return addCategory(((SweText) characteristic.getAbstractDataComponent()).getValue(),
                                    serviceConstellation);
                        }
                    }
                }
            }
        }
        return null;
    }

    private String getPlatformId(PhysicalSystem ps, ServiceConstellation serviceConstellation) {
        return addPlatform(ps.getIdentifier(), checkForName(ps), ps.getDescription(), serviceConstellation);
    }

    private String getFeatureId(AbstractProcessV20 ap, DataSourceJobConfiguration config,
            ServiceConstellation serviceConstellation) {
        if (ap.isSetSmlFeatureOfInterest() && ap.getSmlFeatureOfInterest().isSetFeatures()) {
            for (String identifier : ap.getSmlFeatureOfInterest().getFeaturesOfInterest()) {
                GetFeatureOfInterestResponse foiResponse = getFeatureOfInterestById(identifier, config.getUrl());
                AbstractFeature abstractFeature = foiResponse.getAbstractFeature();
                return addFeature((AbstractSamplingFeature) abstractFeature, serviceConstellation);
            }
        }
        return null;
    }

    private String getFormat(SortedSet<String> formats) {
        return formats.contains(SensorML20Constants.NS_SML_20) ? SensorML20Constants.NS_SML_20
                : formats.contains(SensorML20Constants.NS_SML) ? SensorML20Constants.NS_SML
                        : formats.stream().findFirst().orElse(SensorML20Constants.NS_SML_20);
    }
}
