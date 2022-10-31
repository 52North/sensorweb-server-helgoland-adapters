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

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import org.joda.time.DateTime;
import org.locationtech.jts.geom.Geometry;
import org.n52.sensorweb.server.db.assembler.value.ValueConnector;
import org.n52.sensorweb.server.db.old.dao.DbQuery;
import org.n52.sensorweb.server.helgoland.adapters.config.Credentials;
import org.n52.sensorweb.server.helgoland.adapters.connector.constellations.QuantityDatasetConstellation;
import org.n52.sensorweb.server.helgoland.adapters.connector.hereon.HereonConfig;
import org.n52.sensorweb.server.helgoland.adapters.connector.mapping.Datastream;
import org.n52.sensorweb.server.helgoland.adapters.connector.mapping.Feature;
import org.n52.sensorweb.server.helgoland.adapters.connector.mapping.ObservedProperty;
import org.n52.sensorweb.server.helgoland.adapters.connector.mapping.Sensor;
import org.n52.sensorweb.server.helgoland.adapters.connector.mapping.Thing;
import org.n52.sensorweb.server.helgoland.adapters.connector.request.GetFeatureGeometryRequest;
import org.n52.sensorweb.server.helgoland.adapters.connector.request.GetFeatureRequest;
import org.n52.sensorweb.server.helgoland.adapters.connector.request.GetMetadataRequest;
import org.n52.sensorweb.server.helgoland.adapters.connector.request.GetObservedPropertyRequest;
import org.n52.sensorweb.server.helgoland.adapters.connector.request.GetSensorRequest;
import org.n52.sensorweb.server.helgoland.adapters.connector.request.GetThingRequest;
import org.n52.sensorweb.server.helgoland.adapters.connector.request.builder.FeatureRequestBuilder;
import org.n52.sensorweb.server.helgoland.adapters.connector.request.builder.MetadataRequestBuilder;
import org.n52.sensorweb.server.helgoland.adapters.connector.request.builder.ObservedPropertyRequestBuilder;
import org.n52.sensorweb.server.helgoland.adapters.connector.request.builder.RequestBuilderFactory;
import org.n52.sensorweb.server.helgoland.adapters.connector.request.builder.SensorRequestBuilder;
import org.n52.sensorweb.server.helgoland.adapters.connector.request.builder.ThingRequestBuilder;
import org.n52.sensorweb.server.helgoland.adapters.connector.response.Attributes;
import org.n52.sensorweb.server.helgoland.adapters.connector.response.ErrorResponse;
import org.n52.sensorweb.server.helgoland.adapters.connector.response.ExtentResponse;
import org.n52.sensorweb.server.helgoland.adapters.connector.response.MetadataFeature;
import org.n52.sensorweb.server.helgoland.adapters.connector.response.MetadataResponse;
import org.n52.sensorweb.server.helgoland.adapters.connector.utils.ServiceConstellation;
import org.n52.sensorweb.server.helgoland.adapters.harvest.DataSourceJobConfiguration;
import org.n52.sensorweb.server.helgoland.adapters.harvest.DefaultFullHarvester;
import org.n52.sensorweb.server.helgoland.adapters.harvest.DefaultTemporalHarvester;
import org.n52.sensorweb.server.helgoland.adapters.utils.EntityBuilder;
import org.n52.sensorweb.server.helgoland.adapters.utils.ProxyException;
import org.n52.sensorweb.server.helgoland.adapters.web.ArcgisRestHttpClient;
import org.n52.sensorweb.server.helgoland.adapters.web.HttpClient;
import org.n52.sensorweb.server.helgoland.adapters.web.response.Response;
import org.n52.series.db.beans.CategoryEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.beans.FeatureEntity;
import org.n52.series.db.beans.FormatEntity;
import org.n52.series.db.beans.OfferingEntity;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.beans.UnitEntity;
import org.n52.series.db.beans.parameter.ParameterEntity;
import org.n52.series.db.beans.parameter.ParameterFactory;
import org.n52.series.db.beans.parameter.ParameterFactory.EntityType;
import org.n52.series.db.beans.parameter.TextParameterEntity;
import org.n52.series.db.beans.parameter.dataset.DatasetParameterEntity;
import org.n52.series.db.beans.parameter.platform.PlatformParameterEntity;
import org.n52.shetland.util.DateTimeHelper;
import org.n52.svalbard.decode.exception.DecodingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.primitives.Ints;

@Component
public class HereonConnector extends AbstractServiceConnector implements ValueConnector, EntityBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(HereonConnector.class);
    private static final ObjectMapper OM = new ObjectMapper();

    @Inject
    private RequestBuilderFactory requestBuilderFactory;
    @Inject
    private HereonConfig hereonConfig;
    private Credentials credentials;
    private Map<String, ArcgisRestHttpClient> dataClients = new LinkedHashMap<>();

    @Override
    public List<DataEntity<?>> getObservations(DatasetEntity entity, DbQuery query) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UnitEntity getUom(DatasetEntity entity) {
        return entity.getUnit();
    }

    @Override
    public Optional<DataEntity<?>> getFirstObservation(DatasetEntity entity) {
        if (entity.getFirstObservation() != null) {
            return Optional.of(entity.getFirstObservation());
        }
        return Optional.empty();
    }

    @Override
    public Optional<DataEntity<?>> getLastObservation(DatasetEntity entity) {
        if (entity.getLastObservation() != null) {
            return Optional.of(entity.getLastObservation());
        }
        return Optional.empty();
    }

    @Override
    public AbstractServiceConstellation getConstellation(ConnectorConfiguration configuration) {
        initHttpClient(configuration);
        ServiceConstellation serviceConstellation = new ServiceConstellation(DefaultFullHarvester.class.getName(),
                DefaultTemporalHarvester.class.getName());
        DataSourceJobConfiguration config = configuration.getDataSourceJobConfiguration();
        config.setConnector(getConnectorName());
        serviceConstellation.setService(createService(config.getItemName(), "here goes description",
                config.getConnector(), config.getUrl(), config.getVersion(), config.isSupportsFirstLast(), null));
        try {
            createDatasets(serviceConstellation, config.getUrl());
        } catch (ProxyException e) {
            LOGGER.error("Error", e);
        }
        return serviceConstellation;
    }

    private void initHttpClient(ConnectorConfiguration configuration) {
        if (credentials == null) {
            this.credentials = configuration.getDataSourceJobConfiguration().getCredentials();
        }
        if (!isHttpClientInitialized()
                || isHttpClientInitialized() && !(getHttpClient() instanceof ArcgisRestHttpClient)) {
            setHttpClient(new ArcgisRestHttpClient(Ints.checkedCast(CONNECTION_TIMEOUT),
                    Ints.checkedCast(SOCKET_TIMEOUT), configuration.getDataSourceJobConfiguration().getCredentials()));
        }
    }

    private void createDatasets(ServiceConstellation serviceConstellation, String url) throws ProxyException {
        LOGGER.debug("Harvesting started!");
        MetadataRequestBuilder builder = requestBuilderFactory.getMetadataRequestBuilder();
        Thing thing = requestBuilderFactory.getThingRequestBuilder().getTypeMapping();
        ObservedProperty observedProperty = requestBuilderFactory.getObservedPropertyRequestBuilder().getTypeMapping();
        Sensor sensor = requestBuilderFactory.getSensorRequestBuilder().getTypeMapping();
        FeatureRequestBuilder featureRequestBuilder = requestBuilderFactory.getFeatureRequestBuilder();
        Feature featureMapping = featureRequestBuilder.getTypeMapping();
        FormatEntity sensorFormat = createFormat(sensor.getEncodingType());
        FormatEntity featureForamt = createFormat(featureMapping.getEncodingType());
        Datastream datastream = hereonConfig.getMapping().getDatastream();

        long resultOffset = 0;
        long resultLimit = hereonConfig.getMapping().getGeneral().hasResultLimit()
                ? hereonConfig.getMapping().getGeneral().getResultLimit()
                : 0;
        boolean exceededTransferLimit = true;
        do {
            GetMetadataRequest request = builder.getRequest();
            if (resultOffset > 0) {
                request.withResultOffset(resultOffset);
            }
            if (resultLimit > 0) {
                request.withResultRecordCount(resultLimit);
            }
            Response response = getHttpClient().execute(url, request);
            try {
                MetadataResponse metadata = encodeResponse(response, request.getResponseClass());
                exceededTransferLimit = metadata.getExceededTransferLimit();
                resultOffset += resultLimit > 0 ? resultLimit : metadata.getFeatures().size();
                for (MetadataFeature metadataFeature : metadata.getFeatures()) {
                    Attributes attribute = metadataFeature.getAttributes();
                    PlatformEntity platform = createThing(serviceConstellation, attribute, thing);
                    PhenomenonEntity phenomenon =
                            creatObservedProperty(serviceConstellation, attribute, observedProperty);
                    CategoryEntity category = createCategory(serviceConstellation, attribute, observedProperty);
                    ProcedureEntity procedure = createSensor(serviceConstellation, attribute, sensor, sensorFormat);
                    OfferingEntity offering = createOffering(serviceConstellation, attribute, sensor);
                    FeatureEntity feature = createFeature(serviceConstellation, attribute, featureMapping,
                            featureForamt, featureRequestBuilder);
                    QuantityDatasetConstellation dataset = new QuantityDatasetConstellation(procedure.getIdentifier(),
                            offering.getIdentifier(), category.getIdentifier(), phenomenon.getIdentifier(),
                            feature.getIdentifier(), platform.getIdentifier());
                    serviceConstellation.add(addDatasetreamValues(dataset, attribute, datastream));
                }
            } catch (JsonProcessingException | DecodingException e) {
                throw new ProxyException("Error while processing Metadata!").causedBy(e);
            }
        } while (exceededTransferLimit);
        LOGGER.debug("Harvesting finished!");
    }

    private QuantityDatasetConstellation addDatasetreamValues(QuantityDatasetConstellation dataset,
            Attributes attribute, Datastream datastream) {
        dataset.setIdentifier(attribute.getValue(datastream.getIdentifier()));
        dataset.setName(attribute.getValue(datastream.getName()));
        dataset.setDescription(attribute.getValue(datastream.getDescription()));
        dataset.setUnit(
                createUnit(attribute.getValue(datastream.getUnit()), attribute.getValue(datastream.getUnit())));
        dataset.setSamplingTimeStart(getDate(attribute.getValue(datastream.getPhenomenonStartTime())));
        dataset.setSamplingTimeEnd(getDate(attribute.getValue(datastream.getPhenomenonEndTime())));
        if (datastream.isSetProperties()) {
            dataset.setParameters(createDatasetParameters(datastream.getProperties(), attribute));
        }
        return dataset;
    }

    private Set<ParameterEntity<?>> createDatasetParameters(List<String> properties, Attributes attribute) {
        Set<ParameterEntity<?>> parameters = new LinkedHashSet<>();
        for (String property : properties) {
            DatasetParameterEntity<?> parameter = createDatasetValueParameter(property, attribute.getValue(property));
            if (parameter != null) {
                parameters.add(parameter);
            }
        }
        return parameters;
    }

    private DatasetParameterEntity<?> createDatasetValueParameter(String name, String value) {
        if (value != null && !value.isEmpty()) {
            DatasetParameterEntity<?> param = (DatasetParameterEntity<?>) ParameterFactory.from(EntityType.DATASET,
                    org.n52.series.db.beans.parameter.ParameterFactory.ValueType.TEXT);
            if (param != null) {
                param.setName(name);
                ((TextParameterEntity) param).setValue(value);
            }
            return param;
        }
        return null;
    }

    private Date getDate(String time) {
        if (time != null && !time.isEmpty()) {
            try {
                return new DateTime(Long.parseLong(time)).toDate();
            } catch (NumberFormatException nfe) {
                return DateTimeHelper.parseIsoString2DateTime(time).toDate();
            }
        }
        return null;
    }

    private PlatformEntity createThing(ServiceConstellation serviceConstellation, Attributes attribute, Thing thing) {
        String id = attribute.getValue(thing.getIdentifier());
        if (!serviceConstellation.hasPlatforms(id)) {
            PlatformEntity platform = createPlatform(id, attribute.getValue(thing.getName()),
                    attribute.getValue(thing.getDescription()), serviceConstellation.getService());
            if (thing.isSetProperties()) {
                platform.setParameters(createParameters(platform, thing.getProperties(), attribute));
            }
            serviceConstellation.putPlatform(platform);
        }
        return serviceConstellation.getPlatform(id);
    }

    private PhenomenonEntity creatObservedProperty(ServiceConstellation serviceConstellation, Attributes attribute,
            ObservedProperty observedProperty) {
        String id = attribute.getValue(observedProperty.getDefinition());
        if (!serviceConstellation.hasPhenomenon(id)) {
            PhenomenonEntity phenomenon = createPhenomenon(id, attribute.getValue(observedProperty.getName()),
                    attribute.getValue(observedProperty.getDescription()), serviceConstellation.getService());
            if (observedProperty.isSetProperties()) {
                phenomenon.setParameters(createParameters(phenomenon, observedProperty.getProperties(), attribute));
            }
            serviceConstellation.putPhenomenon(phenomenon);
        }
        return serviceConstellation.getPhenomenon(id);
    }

    private CategoryEntity createCategory(ServiceConstellation serviceConstellation, Attributes attribute,
            ObservedProperty observedProperty) {
        String id = attribute.getValue(observedProperty.getIdentifier());
        if (!serviceConstellation.hasCategories(id)) {
            CategoryEntity category = createCategory(id, attribute.getValue(observedProperty.getName()),
                    attribute.getValue(observedProperty.getDescription()), serviceConstellation.getService());
            serviceConstellation.putCategory(category);
        }
        return serviceConstellation.getCategory(id);
    }

    private ProcedureEntity createSensor(ServiceConstellation serviceConstellation, Attributes attribute,
            Sensor sensor, FormatEntity format) {
        String id = attribute.getValue(sensor.getIdentifier());
        if (!serviceConstellation.hasOffering(id)) {
            ProcedureEntity procedure = createProcedure(id, attribute.getValue(sensor.getName()),
                    attribute.getValue(sensor.getDescription()), serviceConstellation.getService());
            procedure.setFormat(format);
            procedure.setDescriptionFile(attribute.getValue(sensor.getMetadata()));
            if (sensor.isSetProperties()) {
                procedure.setParameters(createParameters(procedure, sensor.getProperties(), attribute));
            }
            serviceConstellation.putProcedure(procedure);
        }
        return serviceConstellation.getProcedure(id);
    }

    private OfferingEntity createOffering(ServiceConstellation serviceConstellation, Attributes attribute,
            Sensor sensor) {
        String id = attribute.getValue(sensor.getIdentifier());
        if (!serviceConstellation.hasOffering(id)) {
            OfferingEntity offering = createOffering(id, attribute.getValue(sensor.getName()),
                    attribute.getValue(sensor.getDescription()), serviceConstellation.getService());
            serviceConstellation.putOffering(offering);
        }
        return serviceConstellation.getOffering(id);
    }

    private FeatureEntity createFeature(ServiceConstellation serviceConstellation, Attributes attribute,
            Feature feature, FormatEntity format, FeatureRequestBuilder builder) {
        String id = attribute.getValue(feature.getIdentifier());
        if (!serviceConstellation.hasFeature(id)) {
            FeatureEntity featureEntity = createFeature(id, attribute.getValue(feature.getName()),
                    attribute.getValue(feature.getDescription()), serviceConstellation.getService());
            featureEntity.setFeatureType(format);
            featureEntity.setGeometry(queryGeometry(builder, attribute));
            serviceConstellation.putFeature(featureEntity);
        }
        return serviceConstellation.getFeature(id);
    }

    private void processThings(ServiceConstellation serviceConstellation, String url) throws ProxyException {
        ThingRequestBuilder builder = requestBuilderFactory.getThingRequestBuilder();
        GetThingRequest request = builder.getRequest();
        Response response = getHttpClient().execute(url, request);
        try {
            MetadataResponse metadata = encodeResponse(response, request.getResponseClass());
            Thing thing = builder.getTypeMapping();
            for (MetadataFeature feature : metadata.getFeatures()) {
                Attributes attribute = feature.getAttributes();
                createThing(serviceConstellation, attribute, thing);
            }
        } catch (JsonProcessingException | DecodingException e) {
            throw new ProxyException("Error while processing Things!").causedBy(e);
        }
    }

    private void processObservedProperties(ServiceConstellation serviceConstellation, String url)
            throws ProxyException {
        ObservedPropertyRequestBuilder builder = requestBuilderFactory.getObservedPropertyRequestBuilder();
        GetObservedPropertyRequest request = builder.getRequest();
        Response response = getHttpClient().execute(url, request);
        try {
            MetadataResponse metadata = encodeResponse(response, request.getResponseClass());
            ObservedProperty observedProperty = builder.getTypeMapping();
            for (MetadataFeature feature : metadata.getFeatures()) {
                Attributes attribute = feature.getAttributes();
                creatObservedProperty(serviceConstellation, attribute, observedProperty);
                createCategory(serviceConstellation, attribute, observedProperty);
            }
        } catch (JsonProcessingException | DecodingException e) {
            throw new ProxyException("Error while processing ObservedProperties!").causedBy(e);
        }
    }

    private void processSensors(ServiceConstellation serviceConstellation, String url) throws ProxyException {
        SensorRequestBuilder builder = requestBuilderFactory.getSensorRequestBuilder();
        GetSensorRequest request = builder.getRequest();
        Response response = getHttpClient().execute(url, request);
        try {
            MetadataResponse metadata = encodeResponse(response, request.getResponseClass());
            Sensor sensor = builder.getTypeMapping();
            FormatEntity format = createFormat(sensor.getEncodingType());
            for (MetadataFeature feature : metadata.getFeatures()) {
                Attributes attribute = feature.getAttributes();
                createSensor(serviceConstellation, attribute, sensor, format);
                createOffering(serviceConstellation, attribute, sensor);
            }
        } catch (JsonProcessingException | DecodingException e) {
            throw new ProxyException("Error while processing Sensors!").causedBy(e);
        }
    }

    private void processFeature(ServiceConstellation serviceConstellation, String url) throws ProxyException {
        FeatureRequestBuilder builder = requestBuilderFactory.getFeatureRequestBuilder();
        GetFeatureRequest request = builder.getRequest();
        Response response = getHttpClient().execute(url, request);
        try {
            MetadataResponse metadata = encodeResponse(response, request.getResponseClass());
            org.n52.sensorweb.server.helgoland.adapters.connector.mapping.Feature mapping = builder.getTypeMapping();
            FormatEntity format = createFormat(mapping.getEncodingType());
            for (MetadataFeature feature : metadata.getFeatures()) {
                Attributes attribute = feature.getAttributes();
                createFeature(serviceConstellation, attribute, mapping, format, builder);
            }
        } catch (JsonProcessingException | DecodingException e) {
            throw new ProxyException("Error while processing Features!").causedBy(e);
        }
    }

    private Geometry queryGeometry(FeatureRequestBuilder builder, Attributes attribute) {
        GetFeatureGeometryRequest request = builder.getGeomtryRequest();
        if (builder.isRequestGeometryFromDataService()) {
            String dataServiceUrl = attribute.getValue(builder.getGeneralMapping().getDataServiceUrl());
            HttpClient client = getDataServiceClient(dataServiceUrl);
            try {
                Response response = client.execute(hereonConfig.createDataServiceUrl(dataServiceUrl), request);
                ExtentResponse extentResponse = encodeResponse(response, request.getResponseClass());
                return extentResponse.getExtent().getGeometry();
            } catch (ProxyException | JsonProcessingException | DecodingException e) {
                LOGGER.error("Error while processing Feature geometry!", e);
            }
        }
        return null;
    }

    private <T> T encodeResponse(Response response, Class<T> clazz) throws JsonProcessingException, DecodingException {
        try {
            return OM.readValue(response.getEntity(), clazz);
        } catch (JsonProcessingException e) {
            if (!clazz.isInstance(ErrorResponse.class)) {
                ErrorResponse errorResponse = encodeResponse(response, ErrorResponse.class);
                throw new DecodingException(errorResponse.toString());
            } else {
                throw e;
            }
        }
    }

    private Set<ParameterEntity<?>> createParameters(DescribableEntity entity, List<String> properties,
            Attributes attribute) {
        Set<ParameterEntity<?>> parameters = new LinkedHashSet<>();
        for (String property : properties) {
            ParameterEntity<?> parameter = createValueParameter(entity, property, attribute.getValue(property));
            if (parameter != null) {
                parameters.add(parameter);
            }
        }
        return parameters;
    }

    private ParameterEntity<?> createValueParameter(DescribableEntity entity, String name, String value) {
        ParameterEntity<?> param =
                createParameterEntity(entity, org.n52.series.db.beans.parameter.ParameterFactory.ValueType.TEXT);
        if (param != null) {
            param.setName(name);
            ((TextParameterEntity) param).setValue(value);
        }
        return param;
    }

    private ParameterEntity<?> createParameterEntity(DescribableEntity entity,
            org.n52.series.db.beans.parameter.ParameterFactory.ValueType valueType) {
        if (entity instanceof PlatformEntity) {
            PlatformParameterEntity<?> param =
                    (PlatformParameterEntity<?>) ParameterFactory.from(EntityType.PLATFORM, valueType);
            param.setPlatform((PlatformEntity) entity);
            return param;
        }
        return null;
    }

    private HttpClient getDataServiceClient(String dataServiceUrl) {
        String tokenUrl = createUrl(dataServiceUrl);
        if (dataServiceUrl != null && !dataServiceUrl.isEmpty()) {
            if (tokenUrl.equalsIgnoreCase(credentials.getTokenUrl())) {
                return getHttpClient();
            }
            if (!dataClients.containsKey(tokenUrl)) {
                dataClients.put(tokenUrl,
                        new ArcgisRestHttpClient(Ints.checkedCast(CONNECTION_TIMEOUT),
                                Ints.checkedCast(SOCKET_TIMEOUT),
                                new Credentials(credentials.getUsername(), credentials.getPassword(), tokenUrl)));
            }
            return dataClients.get(tokenUrl);
        }
        return null;
    }

    private String createUrl(String dataServiceUrl) {
        return hereonConfig.createTokenUrl(dataServiceUrl);
    }
}
