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
package org.n52.proxy.db;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.jupiter.api.BeforeEach;
import org.n52.sensorweb.server.db.old.dao.DbQuery;
import org.n52.sensorweb.server.db.old.dao.DefaultDbQueryFactory;
import org.n52.sensorweb.server.db.query.DatasetQuerySpecifications;

public abstract class ProxyTestBase {

    @PersistenceContext
    protected EntityManager entityManager;

    @Inject
    protected ProxyTestRepositories testRepositories;

    protected DatasetQuerySpecifications defaultFilterSpec;

    protected DbQuery defaultQuery;

    @BeforeEach
    public void setUp() {
        this.defaultQuery = new DefaultDbQueryFactory().createDefault();
        this.defaultFilterSpec = DatasetQuerySpecifications.of(defaultQuery, entityManager);
    }

//    protected DatasetEntity uninitializedDataset(final String phenomenonIdentifier,
//                                                        final String offeringIdentifier,
//                                                        final String procedureIdentifier,
//                                                        final String procedureFormat) {
//        return testRepositories.persistSimpleDataset(phenomenonIdentifier,
//                                                     offeringIdentifier,
//                                                     procedureIdentifier,
//                                                     procedureFormat,
//                                                     new DatasetEntity());
//    }
//
//    protected DatasetEntity uninitializedDataset(final String phenomenonIdentifier,
//                                                               final String offeringIdentifier,
//                                                               final String procedureIdentifier,
//                                                               final String procedureFormat,
//                                                               final String featureIdentifier,
//                                                               final String featureFormat) {
//        return testRepositories.persistSimpleDataset(phenomenonIdentifier,
//                                                     offeringIdentifier,
//                                                     procedureIdentifier,
//                                                     procedureFormat,
//                                                     featureIdentifier,
//                                                     featureFormat,
//                                                     new DatasetEntity());
//    }
//
//    protected DatasetEntity quantityDataset(final String phenomenonIdentifier,
//                                                    final String offeringIdentifier,
//                                                    final String procedureIdentifier,
//                                                    final String procedureFormat) {
//        return testRepositories.persistSimpleDataset(phenomenonIdentifier,
//                                                     offeringIdentifier,
//                                                     procedureIdentifier,
//                                                     procedureFormat,
//                                                     new DatasetEntity(DatasetType.timeseries,
//                                                             ObservationType.simple,
//                                                             ValueType.quantity));
//    }
//
//    protected DatasetEntity quantityDataset(final String phenomenonIdentifier,
//                                                    final String offeringIdentifier,
//                                                    final String procedureIdentifier,
//                                                    final String procedureFormat,
//                                                    final String featureIdentifier,
//                                                    final String featureFormat) {
//        return testRepositories.persistSimpleDataset(phenomenonIdentifier,
//                                                     offeringIdentifier,
//                                                     procedureIdentifier,
//                                                     procedureFormat,
//                                                     featureIdentifier,
//                                                     featureFormat,
//                                                     new DatasetEntity(DatasetType.timeseries,
//                                                             ObservationType.simple,
//                                                             ValueType.quantity));
//    }
//
//    protected DatasetEntity quantityProfileDataset(final String phenomenonIdentifier,
//                                                  final String offeringIdentifier,
//                                                  final String procedureIdentifier,
//                                                  final String procedureFormat,
//                                                  final String featureIdentifier,
//                                                  final String featureFormat) {
//        return testRepositories.persistSimpleDataset(phenomenonIdentifier,
//                                                     offeringIdentifier,
//                                                     procedureIdentifier,
//                                                     procedureFormat,
//                                                     featureIdentifier,
//                                                     featureFormat,
//                                                     new DatasetEntity(DatasetType.timeseries,
//                                                             ObservationType.profile,
//                                                             ValueType.quantity));
//    }
//
//    protected DatasetEntity textDataset(final String phenomenonIdentifier,
//                                            final String offeringIdentifier,
//                                            final String procedureIdentifier,
//                                            final String procedureFormat,
//                                            final String featureIdentifier,
//                                            final String featureFormat) {
//        return testRepositories.persistSimpleDataset(phenomenonIdentifier,
//                                                     offeringIdentifier,
//                                                     procedureIdentifier,
//                                                     procedureFormat,
//                                                     featureIdentifier,
//                                                     featureFormat,
//                                                     new DatasetEntity(DatasetType.timeseries,
//                                                             ObservationType.simple,
//                                                             ValueType.text));
//    }

    protected DatasetQuerySpecifications getDatasetQuerySpecification(DbQuery query) {
        return DatasetQuerySpecifications.of(query, entityManager);
    }

}
