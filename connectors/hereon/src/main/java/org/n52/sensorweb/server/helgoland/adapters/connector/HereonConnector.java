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

import java.util.List;
import java.util.Optional;

import org.n52.sensorweb.server.db.assembler.value.ValueConnector;
import org.n52.sensorweb.server.db.old.dao.DbQuery;
import org.n52.sensorweb.server.helgoland.adapters.connector.request.GetPlatformsRequest;
import org.n52.sensorweb.server.helgoland.adapters.connector.utils.ServiceConstellation;
import org.n52.sensorweb.server.helgoland.adapters.harvest.DataSourceJobConfiguration;
import org.n52.sensorweb.server.helgoland.adapters.harvest.DefaultFullHarvester;
import org.n52.sensorweb.server.helgoland.adapters.harvest.DefaultTemporalHarvester;
import org.n52.sensorweb.server.helgoland.adapters.utils.EntityBuilder;
import org.n52.sensorweb.server.helgoland.adapters.utils.ProxyException;
import org.n52.sensorweb.server.helgoland.adapters.web.response.Response;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.UnitEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class HereonConnector extends AbstractServiceConnector implements ValueConnector, EntityBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(HereonConnector.class);

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
        // TODO Auto-generated method stub
        return Optional.empty();
    }

    @Override
    public AbstractServiceConstellation getConstellation(ConnectorConfiguration configuration) {
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

    private void createDatasets(ServiceConstellation serviceConstellation, String url) throws ProxyException {
        Response response = getHttpClient().execute(url, new GetPlatformsRequest());
        System.out.println(response.getEntity());

    }
}
