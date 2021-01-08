/*
 * Copyright (C) 2015-2021 52°North Initiative for Geospatial Open Source
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
package org.n52.sensorweb.server.helgoland.adapters.da;

import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.n52.io.response.dataset.Data;
import org.n52.io.response.dataset.count.CountValue;
import org.n52.sensorweb.server.db.old.dao.DbQuery;
import org.n52.sensorweb.server.db.repositories.core.DataRepository;
import org.n52.sensorweb.server.db.repositories.core.DatasetRepository;
import org.n52.sensorweb.server.db.ValueAssemblerComponent;
import org.n52.sensorweb.server.db.assembler.value.CountValueAssembler;
import org.n52.sensorweb.server.db.assembler.value.ValueConnector;
import org.n52.series.db.beans.CountDataEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.springframework.beans.factory.annotation.Autowired;

@ValueAssemblerComponent(value = "count", datasetEntityType = DatasetEntity.class)
public class ProxyCountDataRepository extends CountValueAssembler {

    private Map<String, ValueConnector> connectorMap;

    @Autowired
    public ProxyCountDataRepository(DataRepository<CountDataEntity> dataRepository,
            DatasetRepository datasetRepository) {
        super(dataRepository, datasetRepository);
    }

    @Autowired
    public void setConnectors(List<ValueConnector> connectors) {
        this.connectorMap =
                connectors.stream().collect(toMap(ValueConnector::getName, Function.identity()));
    }

    @Override
    public CountValue getFirstValue(DatasetEntity entity, DbQuery query) {
        DataEntity<?> firstObs = getConnector(entity).getFirstObservation(entity).orElse(null);
        return assembleDataValue((CountDataEntity) firstObs, entity, query);
    }

    @Override
    public CountValue getLastValue(DatasetEntity entity, DbQuery query) {
        DataEntity<?> lastObs = getConnector(entity).getLastObservation(entity).orElse(null);
        return assembleDataValue((CountDataEntity) lastObs, entity, query);
    }

    @Override
    protected Data<CountValue> assembleDataValues(DatasetEntity seriesEntity, DbQuery query) {
        Data<CountValue> result = new Data<>();
        this.getConnector(seriesEntity).getObservations(seriesEntity, query).stream()
                .map(entry -> assembleDataValue((CountDataEntity) entry, seriesEntity, query))
                .forEach(entry -> result.addNewValue(entry));
        return result;
    }

    @Override
    public ValueConnector getConnector(DatasetEntity entity) {
        String connectorName = entity.getService().getConnector();
        return this.connectorMap.get(connectorName);
    }

}
