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
package org.n52.proxy.db.dao;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;

import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.ServiceEntity;
import org.n52.series.db.dao.DataDao;

public class ProxyDataDao<T extends DataEntity<?>> extends DataDao<T> implements ClearDao<T>, InsertDao<DataEntity<?>> {

    public ProxyDataDao(Session session) {
        super(session);
    }

    public ProxyDataDao(Session session, Class<T> clazz) {
        super(session, clazz);
    }

    public Long getObservationCount(DatasetEntity<?> entity) {
        return (Long) getDefaultCriteria(ProxyDbQuery.createDefaults())
                .add(Restrictions.eq(DataEntity.PROPERTY_SERIES_PKID, entity.getPkid()))
                .setProjection(Projections.rowCount())
                .uniqueResult();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void clearUnusedForService(ServiceEntity service) {
        // this will actuall remove all data entities that are associated with an
        // non-existing dataset or are neither the first or last value for the
        // dataset, independent of the service

        DetachedCriteria existingDatasetIds = DetachedCriteria.forClass(DatasetEntity.class)
                .setProjection(Projections.distinct(Projections
                        .property(DatasetEntity.PROPERTY_PKID)));

        DetachedCriteria minTimeByDataset = DetachedCriteria.forClass(DataEntity.class)
                .setProjection(Projections.projectionList()
                        .add(Projections.groupProperty(DataEntity.PROPERTY_SERIES_PKID))
                        .add(Projections.min(DataEntity.PROPERTY_RESULTTIME)));

        DetachedCriteria maxTimeByDataset = DetachedCriteria.forClass(DataEntity.class)
                .setProjection(Projections.projectionList()
                        .add(Projections.groupProperty(DataEntity.PROPERTY_SERIES_PKID))
                        .add(Projections.max(DataEntity.PROPERTY_RESULTTIME)));
        Criterion notExistingDataset = Subqueries.propertyNotIn(DataEntity.PROPERTY_SERIES_PKID,
                                                                existingDatasetIds);
        Criterion notFirstData = Subqueries.propertiesNotIn(new String[] { DataEntity.PROPERTY_SERIES_PKID,
                                                                           DataEntity.PROPERTY_RESULTTIME },
                                                            minTimeByDataset);
        Criterion notLatestData = Subqueries.propertiesNotIn(new String[] { DataEntity.PROPERTY_SERIES_PKID,
                                                                            DataEntity.PROPERTY_RESULTTIME },
                                                             maxTimeByDataset);

        session.createCriteria(getEntityClass())
                .add(Restrictions.or(notExistingDataset, Restrictions.and(notFirstData, notLatestData)))
                .list()
                .forEach(session::delete);

    }

    @Override
    public DataEntity<?> getOrInsertInstance(DataEntity<?> object) {
        DataEntity<?> instance = getInstance(object);
        if (instance != null) {
            return instance;
        }
        session.save(object);
        return object;
    }

    protected DataEntity<?> getInstance(DataEntity<?> object) throws HibernateException {
        return (DataEntity<?>) session.createCriteria(getEntityClass())
                .add(Restrictions.eq(DataEntity.PROPERTY_SERIES_PKID, object.getSeriesPkid()))
                .add(Restrictions.eq(DataEntity.PROPERTY_TIMESTART, object.getTimestart()))
                .add(Restrictions.eqOrIsNull(DataEntity.PROPERTY_TIMEEND, object.getTimeend()))
                .add(Restrictions.eqOrIsNull(DataEntity.PROPERTY_RESULTTIME, object.getResultTime()))
                .uniqueResult();
    }

}
