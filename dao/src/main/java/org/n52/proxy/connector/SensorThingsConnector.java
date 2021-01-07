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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentType;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.n52.proxy.config.DataSourceConfiguration;
import org.n52.proxy.connector.constellations.QuantityDatasetConstellation;
import org.n52.proxy.connector.utils.EntityBuilder;
import org.n52.proxy.connector.utils.ServiceConstellation;
import org.n52.sensorthings.Datastream;
import org.n52.sensorthings.Datastreams;
import org.n52.sensorthings.Location;
import org.n52.sensorthings.Locations;
import org.n52.sensorthings.Observation;
import org.n52.sensorthings.Observations;
import org.n52.sensorthings.ObservedProperty;
import org.n52.sensorthings.Sensor;
import org.n52.sensorthings.Thing;
import org.n52.sensorweb.server.db.old.dao.DbQuery;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.QuantityDataEntity;
import org.n52.series.db.beans.UnitEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.github.filosganga.geogson.gson.GeometryAdapterFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

@Component
public class SensorThingsConnector extends AbstractConnector {

    private static final Logger LOGGER = LoggerFactory.getLogger(SensorThingsConnector.class);

    private final Gson gson = new GsonBuilder().registerTypeAdapterFactory(new GeometryAdapterFactory()).create();

    private final DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z");

    @Override
    public List<DataEntity<?>> getObservations(DatasetEntity seriesEntity, DbQuery query) {
        return createObservations(seriesEntity, query.getTimespan().getStart(), query.getTimespan().getEnd());
    }

    @Override
    public UnitEntity getUom(DatasetEntity seriesEntity) {
        return seriesEntity.getUnit();
    }

    @Override
    public Optional<DataEntity<?>> getFirstObservation(DatasetEntity entity) {
        return Optional.of(createObservationBounds(entity, "asc"));
    }

    @Override
    public Optional<DataEntity<?>> getLastObservation(DatasetEntity entity) {
        return Optional.of(createObservationBounds(entity, "desc"));
    }

    public ServiceConstellation getConstellation(DataSourceConfiguration config) {
        ServiceConstellation serviceConstellation = new ServiceConstellation();
        config.setConnector(getConnectorName());
        addService(config, serviceConstellation, null);
        createDatasets(serviceConstellation, config.getUrl());
        return serviceConstellation;
    }

    private void createDatasets(ServiceConstellation serviceConstellation, String url) {
        Datastreams datastreams = getDatastreams(url);
        doForDatastreams(datastreams, serviceConstellation);
        while (datastreams.getNextLink() != null) {
            datastreams = (Datastreams) doGetRequest(datastreams.getNextLink(), Datastreams.class);
            doForDatastreams(datastreams, serviceConstellation);
        }
    }

    private void doForDatastreams(Datastreams datastreams, ServiceConstellation serviceConstellation) {
        datastreams.getValue().forEach((Datastream datastream) -> {
            doForDatastream(datastream, serviceConstellation);
        });
    }

    private void doForDatastream(Datastream datastream, ServiceConstellation serviceConstellation) {
        String offeringId = addOffering(datastream.getThing(), serviceConstellation);
        String phenomenonId = addPhenomenon(datastream.getObservedProperty(), serviceConstellation);
        String procedureId = addProcedure(datastream.getSensor(), serviceConstellation);
        String categoryId = addCategory(datastream.getObservedProperty(), serviceConstellation);
        Locations locations = (Locations) doGetRequest(datastream.getThing().getLocationsLink(), Locations.class);
        if (locations != null) {
            String featureId = addFeature(locations.getValue().get(0), serviceConstellation);
            QuantityDatasetConstellation constellation = new QuantityDatasetConstellation(procedureId,
                                                                                          offeringId,
                                                                                          categoryId,
                                                                                          phenomenonId,
                                                                                          featureId,
                                                                                          offeringId);
            constellation.setIdentifier(Integer.toString(datastream.getIotID()));
            constellation.setUnit(EntityBuilder.createUnit(datastream.getUnitOfMeasurement().getSymbol(),
                                                           datastream.getUnitOfMeasurement().getDefinition(),
                                                           serviceConstellation.getService()));
            serviceConstellation.add(constellation);
        }
    }

    private Object doGetRequest(String urlString, Class<?> clazz) {
        try {
            HttpResponse response = sendGetRequest(urlString);
            try (Reader reader = new InputStreamReader(response.getEntity().getContent(), getEncoding(response))) {
                return gson.fromJson(reader, clazz);
            }
        } catch (MalformedURLException ex) {
            LOGGER.error(ex.getLocalizedMessage(), ex);
        } catch (JsonSyntaxException | IOException ex) {
            LOGGER.error(ex.getLocalizedMessage(), ex);
        }
        return null;
    }

    private Object doGetRequest(String url, String entity, Class<?> clazz) {
        return doGetRequest(url + entity, clazz);
    }

    private Datastreams getDatastreams(String url) {
        return (Datastreams) doGetRequest(url, "Datastreams?$expand=Sensor,Thing,ObservedProperty", Datastreams.class);
    }

    private List<DataEntity<?>> createObservations(DatasetEntity seriesEntity, DateTime start, DateTime end) {
        String entity = String.format("Datastreams(%s)/Observations?$filter=" +
                                      "phenomenonTime%%20gt%%20'%s'" +
                                      "%%20and%%20" +
                                      "phenomenonTime%%20lt%%20'%s'",
                                      seriesEntity.getIdentifier(),
                                      start.toString(formatter),
                                      end.toString(formatter));
        Observations observations = (Observations) doGetRequest(seriesEntity.getService().getUrl(),
                                                                entity, Observations.class);
        List<DataEntity<?>> list = new LinkedList<>();
        addObservationsToList(observations, list);
        while (observations.getNextLink() != null) {
            observations = (Observations) doGetRequest(observations.getNextLink(), Observations.class);
            addObservationsToList(observations, list);
        }
        return list;
    }

    private void addObservationsToList(Observations observations, List<DataEntity<?>> list) {
        observations.getValue().stream().map(this::createObservation).forEach(list::add);
    }

    private DataEntity<?> createObservationBounds(DatasetEntity entity, String order) {
        String e = String.format("Datastreams(%s)/Observations?$orderby=phenomenonTime%%20%s&$top=1",
                                 entity.getIdentifier(), order);
        Observations observations = (Observations) doGetRequest(entity.getService().getUrl(), e, Observations.class);
        if (observations.getValue().size() == 1) {
            return createObservation(observations.getValue().get(0));
        }
        return null;
    }

    private String addOffering(Thing thing, ServiceConstellation serviceConstellation) {
        return addOffering(Integer.toString(thing.getIotID()), thing.getName(), serviceConstellation);
    }

    private String addPhenomenon(ObservedProperty obsProp, ServiceConstellation serviceConstellation) {
        return addPhenomenon(Integer.toString(obsProp.getIotID()), obsProp.getName(), serviceConstellation);
    }

    private String addProcedure(Sensor sensor, ServiceConstellation serviceConstellation) {
        return addProcedure(Integer.toString(sensor.getIotID()), sensor.getName(), true, false, serviceConstellation);
    }

    private String addCategory(ObservedProperty obsProp, ServiceConstellation serviceConstellation) {
        return addCategory(Integer.toString(obsProp.getIotID()), obsProp.getName(), serviceConstellation);
    }

    private String addFeature(Location location, ServiceConstellation serviceConstellation) {
        String featureId = Integer.toString(location.getIotID());
        serviceConstellation.putFeature(featureId, location.getName(), null,
                                        location.getLocation().getCoordinates().get(1),
                                        location.getLocation().getCoordinates().get(0), 4326);
        return featureId;
    }

    private DataEntity<?> createObservation(Observation observation) {
        QuantityDataEntity dataEntity = new QuantityDataEntity();
        dataEntity.setSamplingTimeStart(observation.getPhenomenonTime());
        dataEntity.setSamplingTimeEnd(observation.getPhenomenonTime());
        dataEntity.setValue(observation.getResult());
        return dataEntity;
    }

    private Charset getEncoding(HttpResponse response) {
        ContentType contentType = ContentType.getLenient(response.getEntity());
        if (contentType != null && contentType.getCharset() != null) {
            return contentType.getCharset();
        } else {
            return StandardCharsets.UTF_8;
        }
    }
}
