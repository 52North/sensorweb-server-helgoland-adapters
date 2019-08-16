/*
 * Copyright (C) 2015-2019 52°North Initiative for Geospatial Open Source
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
package org.n52.springboot.init;

import org.n52.io.response.CategoryOutput;
import org.n52.io.response.FeatureOutput;
import org.n52.io.response.OfferingOutput;
import org.n52.io.response.ParameterOutput;
import org.n52.io.response.PhenomenonOutput;
import org.n52.io.response.ProcedureOutput;
import org.n52.io.response.ServiceOutput;
import org.n52.series.db.AnnotationBasedDataRepositoryFactory;
import org.n52.series.db.DataRepositoryTypeFactory;
import org.n52.series.spi.srv.ParameterService;
import org.n52.series.srv.AccessService;
import org.n52.series.srv.CategoryService;
import org.n52.series.srv.FeatureService;
import org.n52.series.srv.OfferingService;
import org.n52.series.srv.PhenomenonService;
import org.n52.series.srv.ProcedureService;
import org.n52.series.srv.ServiceService;
import org.n52.web.ctrl.ParameterBackwardsCompatibilityAdapter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@EnableWebMvc
@Configuration
@EnableJpaRepositories(basePackages = { "org.n52.series.db.repositories.core" })
@ComponentScan(basePackages = { "org.n52.series.db.repositories.core", "org.n52.series.db.assembler.core",
        "org.n52.series.srv" })
public class SpiImplConfig {

    @Bean
    public DataRepositoryTypeFactory dataRepositoryFactory(ApplicationContext appContext) {
        return new AnnotationBasedDataRepositoryFactory(appContext);
    }

    @Bean
    @Primary
    public ParameterService<ServiceOutput> serviceParameterService(ServiceService service) {
        return backwardsCompatible(service);
    }

    @Bean
    @Primary
    public ParameterService<OfferingOutput> offeringParameterService(OfferingService service) {
        return backwardsCompatible(service);
    }

    @Bean
    @Primary
    public ParameterService<PhenomenonOutput> phenomenonParameterService(PhenomenonService service) {
        return backwardsCompatible(service);
    }

    @Bean
    @Primary
    public ParameterService<CategoryOutput> categoryParameterService(CategoryService service) {
        return backwardsCompatible(service);
    }

    @Bean
    @Primary
    public ParameterService<FeatureOutput> featureParameterService(FeatureService service) {
        return backwardsCompatible(service);
    }

    @Bean
    @Primary
    public ParameterService<ProcedureOutput> procedureParameterService(ProcedureService service) {
        return backwardsCompatible(service);
    }

    private <T extends ParameterOutput> ParameterBackwardsCompatibilityAdapter<T> backwardsCompatible(AccessService<T> service) {
        return new ParameterBackwardsCompatibilityAdapter<>(service);
    }

}
