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
package org.n52.sensorweb.server.helgoland.adapters.da;

import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.n52.io.response.dataset.Data;
import org.n52.io.response.dataset.quantity.QuantityValue;
import org.n52.sensorweb.server.db.old.dao.DbQuery;
import org.n52.sensorweb.server.db.repositories.core.DataRepository;
import org.n52.sensorweb.server.db.repositories.core.DatasetRepository;
import org.n52.sensorweb.server.db.ValueAssemblerComponent;
import org.n52.sensorweb.server.db.assembler.value.QuantityValueAssembler;
import org.n52.sensorweb.server.db.assembler.value.ValueConnector;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.QuantityDataEntity;
import org.springframework.beans.factory.annotation.Autowired;

@ValueAssemblerComponent(value = "quantity", datasetEntityType = DatasetEntity.class)
public class ProxyQuantityDataRepository extends QuantityValueAssembler {


private Map<String, ValueConnector> connectorMap;

public ProxyQuantityDataRepository(DataRepository<QuantityDataEntity> dataRepository,
            DatasetRepository datasetRepository) {
        super(dataRepository, datasetRepository);
    }

    @Autowired
    public void setConnectors(List<ValueConnector> connectors) {
        this.connectorMap = connectors.stream()
                .collect(toMap(ValueConnector::getName, Function.identity()));
    }

    @Override
    public ValueConnector getConnector(DatasetEntity seriesEntity) {
        String connectorName = seriesEntity.getService().getConnector();
        return this.connectorMap.get(connectorName);
    }

    @Override
    public QuantityValue getFirstValue(DatasetEntity entity, DbQuery query) {
        DataEntity<?> firstObservation = this.getConnector(entity).getFirstObservation(entity).orElse(null);
        return assembleDataValue((QuantityDataEntity) firstObservation, entity, query);
    }

    @Override
    public QuantityValue getLastValue(DatasetEntity entity, DbQuery query) {
        DataEntity<?> lastObservation = this.getConnector(entity).getLastObservation(entity).orElse(null);
        return assembleDataValue((QuantityDataEntity) lastObservation, entity, query);
    }

    @Override
    protected Data<QuantityValue> assembleDataValues(DatasetEntity seriesEntity, DbQuery query) {
        Data<QuantityValue> result = new Data<>();
        this.getConnector(seriesEntity)
                .getObservations(seriesEntity, query).stream()
                .map(entry -> assembleDataValue((QuantityDataEntity) entry, seriesEntity, query))
                .forEach(entry -> result.addNewValue(entry));
        return result;
    }

}
