/*
 * Copyright (C) 2015-2023 52°North Spatial Information Research GmbH
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
package org.n52.sensorweb.server.helgoland.adapters;

import org.n52.sensorweb.server.db.factory.AnnotationBasedDataRepositoryFactory;
import org.n52.sensorweb.server.db.factory.DataRepositoryTypeFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.cosium.spring.data.jpa.entity.graph.repository.support.EntityGraphJpaRepositoryFactoryBean;

@EnableWebMvc
@Configuration
@EnableJpaRepositories(repositoryFactoryBeanClass = EntityGraphJpaRepositoryFactoryBean.class,
basePackages = { "org.n52.sensorweb.server.db.repositories" })
@ComponentScan(basePackages = {
    "org.n52.sensorweb.server.db.repositories",
    "org.n52.sensorweb.server.db.assembler.core",
    "org.n52.sensorweb.server.db.assembler.mapper",
    "org.n52.sensorweb.server.db.assembler.value",
    "org.n52.sensorweb.server.srv",
    "org.n52.sensorweb.server.db.factory"
})
public class SpiImplConfig {

    @Bean
    public DataRepositoryTypeFactory dataRepositoryFactory(ApplicationContext appContext) {
        return new AnnotationBasedDataRepositoryFactory(appContext);
    }

}
