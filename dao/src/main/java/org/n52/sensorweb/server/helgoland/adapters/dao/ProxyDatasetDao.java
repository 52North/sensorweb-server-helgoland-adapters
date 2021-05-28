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
import org.n52.sensorweb.server.db.assembler.InsertAssembler;
import org.n52.series.db.beans.DatasetEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyDatasetDao<T extends DatasetEntity>
//extends DatasetDao<T>
implements InsertAssembler<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyDatasetDao.class);

    @Override
    public ParameterDataRepository<T> getParameterRepository() {
        // TODO Auto-generated method stub
        return null;
    }

//    public ProxyDatasetDao(Session session) {
//        super(session);
//    }
//
//    public ProxyDatasetDao(Session session, Class<T> clazz) {
//        super(session, clazz);
//    }
//
//    @Override
//    @SuppressWarnings("unchecked")
//    public T getOrInsertInstance(T dataset) {
//        if (dataset.getUnit() != null) {
//            dataset.setUnit(getOrInsertUnit(dataset.getUnit()));
//        }
//        T instance = getInstance(dataset);
//        if (instance == null) {
//            this.session.save(dataset);
//            LOGGER.info("Save dataset: " + dataset);
//            this.session.flush();
//            this.session.refresh(dataset);
//            return dataset;
//        }
//        return instance;
//    }
//
//    public UnitEntity getOrInsertUnit(UnitEntity unit) {
//        UnitEntity instance = getUnit(unit);
//        if (instance == null) {
//            this.session.save(unit);
//            this.session.flush();
//            return unit;
//        }
//        return instance;
//    }
//
//    public Set<Long> getIdsForService(ServiceEntity service) {
//        return getDatasetsForService(service).stream().map(DescribableEntity::getId).collect(toSet());
//    }
//
//    public void removeDatasets(Set<Long> datasetIds) {
//        datasetIds.forEach(id -> this.session.delete(this.session.get(DatasetEntity.class, id)));
//        this.session.flush();
//    }
//
//    @SuppressWarnings("unchecked")
//    public void removeAllOfService(ServiceEntity service) {
//        getDefaultCriteria(ProxyDbQuery.createDefaults())
//                .add(Restrictions.eq(ServiceEntity.PROPERTY_SERVICE, service))
//                .list()
//                .forEach(session::delete);
//        this.session.createCriteria(UnitEntity.class)
//                .add(Restrictions.eq(UnitEntity.PROPERTY_SERVICE, service))
//                .list().forEach(session::delete);
//        this.session.flush();
//    }
//
//    private UnitEntity getUnit(UnitEntity unit) {
//        return (UnitEntity) this.session.createCriteria(UnitEntity.class)
//                .add(Restrictions.eq(DescribableEntity.PROPERTY_NAME, unit.getName()))
//                .add(Restrictions.eq(DescribableEntity.PROPERTY_SERVICE, unit.getService()))
//                .uniqueResult();
//    }
//
//    @SuppressWarnings("unchecked")
//    private T getInstance(T dataset) {
//        return (T) getDefaultCriteria(ProxyDbQuery.createDefaults())
//                .add(Restrictions.eq(DatasetEntity.PROPERTY_VALUE_TYPE, dataset.getValueType()))
//                .add(Restrictions.eq(DatasetEntity.PROPERTY_CATEGORY, dataset.getCategory()))
//                .add(Restrictions.eq(DatasetEntity.PROPERTY_FEATURE, dataset.getFeature()))
//                .add(Restrictions.eq(DatasetEntity.PROPERTY_PROCEDURE, dataset.getProcedure()))
//                .add(Restrictions.eq(DatasetEntity.PROPERTY_PHENOMENON, dataset.getPhenomenon()))
//                .add(Restrictions.eq(DatasetEntity.PROPERTY_OFFERING, dataset.getOffering()))
//                .add(Restrictions.eq(DatasetEntity.PROPERTY_SERVICE, dataset.getService()))
//                .uniqueResult();
//    }
//
//    @SuppressWarnings("unchecked")
//    private List<T> getDatasetsForService(ServiceEntity service) {
//        return getDefaultCriteria(ProxyDbQuery.createDefaults())
//                .add(Restrictions.eq(DatasetEntity.PROPERTY_SERVICE, service))
//                .list();
//    }
}
