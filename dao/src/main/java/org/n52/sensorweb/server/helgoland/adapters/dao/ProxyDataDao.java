/*
 * Copyright (C) 2015-2021 52°North Spatial Information Research GmbH
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
package org.n52.sensorweb.server.helgoland.adapters.dao;

import org.n52.sensorweb.server.db.repositories.ParameterDataRepository;
import org.n52.sensorweb.server.db.assembler.ClearAssembler;
import org.n52.sensorweb.server.db.assembler.InsertAssembler;
import org.n52.series.db.beans.DataEntity;

public class ProxyDataDao<T extends DataEntity<?>>
//extends DataDao<T>
implements ClearAssembler<T>, InsertAssembler<T> {

    @Override
    public ParameterDataRepository<T> getParameterRepository() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public T refresh(T entity) {
        return entity;
    }

//    public ProxyDataDao(Session session) {
//        super(session);
//    }
//
//    public ProxyDataDao(Session session, Class<T> clazz) {
//        super(session, clazz);
//    }
//
//    public Long getObservationCount(DatasetEntity entity) {
//        return (Long) getDefaultCriteria(ProxyDbQuery.createDefaults())
//                .add(Restrictions.eq(DataEntity.PROPERTY_DATASET, entity.getId()))
//                .setProjection(Projections.rowCount())
//                .uniqueResult();
//    }
//
//    @Override
//    @SuppressWarnings("unchecked")
//    public void clearUnusedForService(ServiceEntity service) {
//        // this will actuall remove all data entities that are associated with an
//        // non-existing dataset or are neither the first or last value for the
//        // dataset, independent of the service
//
//        DetachedCriteria existingDatasetIds = DetachedCriteria.forClass(DatasetEntity.class)
//                .setProjection(Projections.distinct(Projections
//                        .property(DatasetEntity.PROPERTY_ID)));
//
//        DetachedCriteria minTimeByDataset = DetachedCriteria.forClass(DataEntity.class)
//                .setProjection(Projections.projectionList()
//                        .add(Projections.groupProperty(DataEntity.PROPERTY_DATASET))
//                        .add(Projections.min(DataEntity.PROPERTY_RESULT_TIME)));
//
//        DetachedCriteria maxTimeByDataset = DetachedCriteria.forClass(DataEntity.class)
//                .setProjection(Projections.projectionList()
//                        .add(Projections.groupProperty(DataEntity.PROPERTY_DATASET))
//                        .add(Projections.max(DataEntity.PROPERTY_RESULT_TIME)));
//        Criterion notExistingDataset = Subqueries.propertyNotIn(DataEntity.PROPERTY_DATASET,
//                                                                existingDatasetIds);
//        Criterion notFirstData = Subqueries.propertiesNotIn(new String[] { DataEntity.PROPERTY_DATASET,
//                                                                           DataEntity.PROPERTY_RESULT_TIME },
//                                                            minTimeByDataset);
//        Criterion notLatestData = Subqueries.propertiesNotIn(new String[] { DataEntity.PROPERTY_DATASET,
//                                                                            DataEntity.PROPERTY_RESULT_TIME },
//                                                             maxTimeByDataset);
//
//        session.createCriteria(getEntityClass())
//                .add(Restrictions.or(notExistingDataset, Restrictions.and(notFirstData, notLatestData)))
//                .list()
//                .forEach(session::delete);
//
//    }
//
//    @Override
//    public DataEntity<?> getOrInsertInstance(DataEntity<?> object) {
//        DataEntity<?> instance = getInstance(object);
//        if (instance != null) {
//            return instance;
//        }
//        session.save(object);
//        session.flush();
//        return object;
//    }
//
//    protected DataEntity<?> getInstance(DataEntity<?> object) throws HibernateException {
//        return (DataEntity<?>) session.createCriteria(getEntityClass())
//                .add(Restrictions.eq(DataEntity.PROPERTY_DATASET, object.getDataset()))
//                .add(Restrictions.eq(DataEntity.PROPERTY_SAMPLING_TIME_START, object.getSamplingTimeStart()))
//                .add(Restrictions.eqOrIsNull(DataEntity.PROPERTY_SAMPLING_TIME_END, object.getSamplingTimeEnd()))
//                .add(Restrictions.eqOrIsNull(DataEntity.PROPERTY_RESULT_TIME, object.getResultTime()))
//                .uniqueResult();
//    }

}
