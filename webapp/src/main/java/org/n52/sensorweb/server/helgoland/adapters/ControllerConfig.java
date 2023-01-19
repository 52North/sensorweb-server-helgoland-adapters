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

import org.n52.io.handler.DefaultIoFactory;
import org.n52.io.response.dataset.AbstractValue;
import org.n52.io.response.dataset.DatasetOutput;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "org.n52.web.ctrl")
public class ControllerConfig {

    @Bean
    public DefaultIoFactory<DatasetOutput<AbstractValue< ? >>, AbstractValue< ? >> defaultIoFactory() {
        return new DefaultIoFactory<>();
    }

//    private <T extends ParameterController<ParameterOutput>> T withLicenseExtension(T controller) {
//        controller.addMetadataExtension(new LicenseExtension());
//        return controller;
//    }
//
//    @Bean
//    public DefaultIoFactory<DatasetOutput<AbstractValue<?>>, AbstractValue<?>> defaultIoFactory() {
//        return new DefaultIoFactory<>();
//    }
//
//    @Bean
//    public DatabaseMetadataExtension databaseMetadataExtension(DatasetRepository datasetRepository,
//            DbQueryFactory dbQueryFactory) {
//        MetadataAssembler repository = new MetadataAssembler(datasetRepository, dbQueryFactory);
//        return new DatabaseMetadataExtension(repository);
//    }
//
//    @Bean
//    public ResultTimeExtension resultTimeExtension(DatasetController datasetController, EntityManager entityManager,
//            DatasetRepository datasetRepository, DbQueryFactory dbQueryFactory) {
//        ResultTimeAssembler repository = new ResultTimeAssembler(entityManager, datasetRepository, dbQueryFactory);
//        ResultTimeService resultTimeService = new ResultTimeService(repository);
//        ResultTimeExtension extension = new ResultTimeExtension(resultTimeService);
//        datasetController.addMetadataExtension(extension);
//        return extension;
//    }
//
//    @Bean
//    public StatusIntervalsExtension<DatasetOutput<?>> statusIntervalExtension(DatasetController datasetController) {
//        StatusIntervalsExtension<DatasetOutput<?>> extension = new StatusIntervalsExtension<>();
//        datasetController.addMetadataExtension(extension);
//        return extension;
//    }
//
//
//    @Bean
//    public RenderingHintsExtension<DatasetOutput<?>> renderingHintsExtension(DatasetController datasetController) {
//        RenderingHintsExtension<DatasetOutput<?>> extension = new RenderingHintsExtension<>();
//        datasetController.addMetadataExtension(extension);
//        return extension;
//    }

}
