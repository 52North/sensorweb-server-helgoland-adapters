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
package org.n52.sensorweb.server.helgoland.adapters.db;

import java.io.IOException;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.n52.sensorweb.server.db.factory.HibernatePersistenceLoadingLocalContainerEntityManagerFactoryBean;
import org.n52.series.db.beans.DatasetEntity;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

/**
 * Configures a {@link HibernatePersistenceLoadingLocalContainerEntityManagerFactoryBean} which scans for a
 * persistence xml at a given location. Within the persistence xml file, all mapping files shall be listed
 * which are important for the tests.
 * <p>
 * This enables flexible setup for different tests loading different kinds of mapping assemblies (e.g. of
 * different profiles).
 * <p>
 * Test configuration can just inherit from this class and set proper annotation config
 * ({@link SpringBootCondition}, {@link EnableJpaRepositories} etc.) needed within that test class.
 *
 * @see HibernatePersistenceLoadingLocalContainerEntityManagerFactoryBean
 */
public abstract class ProxyTestRepositoryConfig<T extends DatasetEntity> {

    private final String xmlPersistenceLocation;

    public ProxyTestRepositoryConfig(final String xmlPersistenceLocation) {
        this.xmlPersistenceLocation = xmlPersistenceLocation;
    }

    @Bean
    public abstract ProxyTestRepositories testRepositories();

    @Bean
    public EntityManagerFactory entityManagerFactory(final DataSource datasource, final JpaProperties properties)
            throws IOException {
        final LocalContainerEntityManagerFactoryBean emf = createEntityManagerFactoryBean(datasource,
                                                                                    properties,
                                                                                    xmlPersistenceLocation);
        return emf.getNativeEntityManagerFactory();
    }

    private LocalContainerEntityManagerFactoryBean createEntityManagerFactoryBean(final DataSource datasource,
                                                                                  final JpaProperties properties,
                                                                                  final String xmlPersistenceLocation) {
        return new HibernatePersistenceLoadingLocalContainerEntityManagerFactoryBean(datasource,
                                                                                     properties,
                                                                                     xmlPersistenceLocation);
    }
}
