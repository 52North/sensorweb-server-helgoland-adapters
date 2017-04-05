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
package org.n52.proxy.db.da;

import java.util.Map;
import org.hibernate.Session;
import org.n52.io.response.dataset.measurement.MeasurementData;
import org.n52.io.response.dataset.measurement.MeasurementValue;
import org.n52.proxy.connector.AbstractConnector;
import org.n52.proxy.db.beans.ProxyServiceEntity;
import org.n52.series.db.DataAccessException;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.MeasurementDataEntity;
import org.n52.series.db.beans.MeasurementDatasetEntity;
import org.n52.series.db.dao.DbQuery;

public class ProxyMeasurementDataRepository
        extends org.n52.series.db.da.MeasurementDataRepository
        implements ProxyDataRepository<MeasurementDatasetEntity, MeasurementValue> {

    private Map<String, AbstractConnector> connectorMap;

    @Override
    public void setConnectorMap(Map connectorMap) {
        this.connectorMap = connectorMap;
    }

    private AbstractConnector getConnector(MeasurementDatasetEntity seriesEntity) {
        String connectorName = ((ProxyServiceEntity) seriesEntity.getService()).getConnector();
        return this.connectorMap.get(connectorName);
    }

    @Override
    public MeasurementValue getFirstValue(MeasurementDatasetEntity entity, Session session, DbQuery query) {
        DataEntity firstObservation = this.getConnector(entity).getFirstObservation(entity).orElse(null);
        return createSeriesValueFor((MeasurementDataEntity) firstObservation, entity, query);
    }

    @Override
    public MeasurementValue getLastValue(MeasurementDatasetEntity entity, Session session, DbQuery query) {
        DataEntity lastObservation = this.getConnector(entity).getLastObservation(entity).orElse(null);
        return createSeriesValueFor((MeasurementDataEntity) lastObservation, entity, query);
    }

    @Override
    protected MeasurementData assembleDataWithReferenceValues(MeasurementDatasetEntity datasetEntity, DbQuery dbQuery,
            Session session) throws DataAccessException {
        return assembleData(datasetEntity, dbQuery, session);
    }

    @Override
    protected MeasurementData assembleData(MeasurementDatasetEntity seriesEntity, DbQuery query, Session session)
            throws DataAccessException {
        MeasurementData result = new MeasurementData();
        this.getConnector(seriesEntity)
                .getObservations(seriesEntity, query).stream()
                .map((entry) -> createSeriesValueFor((MeasurementDataEntity) entry, seriesEntity, query))
                .forEach(entry -> result.addNewValue(entry));
        return result;
    }

}
