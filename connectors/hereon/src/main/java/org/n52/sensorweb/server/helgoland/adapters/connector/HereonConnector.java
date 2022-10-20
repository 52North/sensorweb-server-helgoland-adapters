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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import org.n52.sensorweb.server.db.assembler.value.ValueConnector;
import org.n52.sensorweb.server.db.old.dao.DbQuery;
import org.n52.sensorweb.server.helgoland.adapters.connector.mapping.Thing;
import org.n52.sensorweb.server.helgoland.adapters.connector.request.builder.RequestBuilderFactory;
import org.n52.sensorweb.server.helgoland.adapters.connector.request.builder.ThingRequestBuilder;
import org.n52.sensorweb.server.helgoland.adapters.connector.response.Attributes;
import org.n52.sensorweb.server.helgoland.adapters.connector.response.Feature;
import org.n52.sensorweb.server.helgoland.adapters.connector.response.Metadata;
import org.n52.sensorweb.server.helgoland.adapters.connector.utils.ServiceConstellation;
import org.n52.sensorweb.server.helgoland.adapters.harvest.DataSourceJobConfiguration;
import org.n52.sensorweb.server.helgoland.adapters.harvest.DefaultFullHarvester;
import org.n52.sensorweb.server.helgoland.adapters.harvest.DefaultTemporalHarvester;
import org.n52.sensorweb.server.helgoland.adapters.utils.EntityBuilder;
import org.n52.sensorweb.server.helgoland.adapters.utils.ProxyException;
import org.n52.sensorweb.server.helgoland.adapters.web.ArcgisRestHttpClient;
import org.n52.sensorweb.server.helgoland.adapters.web.response.Response;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.UnitEntity;
import org.n52.series.db.beans.parameter.ParameterEntity;
import org.n52.series.db.beans.parameter.ParameterFactory;
import org.n52.series.db.beans.parameter.ParameterFactory.EntityType;
import org.n52.series.db.beans.parameter.TextParameterEntity;
import org.n52.series.db.beans.parameter.platform.PlatformParameterEntity;
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
        if (!isHttpClientInitialized()
                || isHttpClientInitialized() && !(getHttpClient() instanceof ArcgisRestHttpClient)) {
            setHttpClient(new ArcgisRestHttpClient(Ints.checkedCast(CONNECTION_TIMEOUT),
                    Ints.checkedCast(SOCKET_TIMEOUT), configuration.getDataSourceJobConfiguration().getCredentials()));
        }
    }

    private void createDatasets(ServiceConstellation serviceConstellation, String url) throws ProxyException {
        LOGGER.debug("Harvesting started!");
        processThings(serviceConstellation, url);
        LOGGER.debug("Harvesting finished!");

    }

    private void processThings(ServiceConstellation serviceConstellation, String url) throws ProxyException {
        ThingRequestBuilder builder = requestBuilderFactory.getThingRequestBuilder();
        Response response = getHttpClient().execute(url, builder.getRequest());
        try {
            Metadata metadata = OM.readValue(response.getEntity(), Metadata.class);
            Thing thing = builder.getTypeMapping();
            for (Feature feature : metadata.getFeatures()) {
                Attributes attribute = feature.getAttributes();
                PlatformEntity platform =
                        createPlatform(attribute.getValue(thing.getIdentifier()), attribute.getValue(thing.getName()),
                                attribute.getValue(thing.getDescription()), serviceConstellation.getService());
                if (thing.isSetProperties()) {
                    platform.setParameters(createParameters(platform, thing.getProperties(), attribute));
                }
                serviceConstellation.putPlatform(platform);
            }
        } catch (JsonProcessingException e) {
            throw new ProxyException("Error while processing Things!").causedBy(e);
        }
    }

    private Set<ParameterEntity<?>> createParameters(PlatformEntity platform, List<String> properties,
            Attributes attribute) {
        Set<ParameterEntity<?>> parameters = new LinkedHashSet<>();
        for (String property : properties) {
            ParameterEntity<?> parameter = createValueParameter(platform, property, attribute.getValue(property));
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
            param.setName("value");
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
}
