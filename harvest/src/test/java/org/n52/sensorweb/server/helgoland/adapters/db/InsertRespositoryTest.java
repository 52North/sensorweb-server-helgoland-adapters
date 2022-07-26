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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

import javax.inject.Inject;

import org.joda.time.DateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.n52.sensorweb.server.db.old.dao.DbQueryFactory;
import org.n52.sensorweb.server.db.old.dao.DefaultDbQueryFactory;
import org.n52.sensorweb.server.db.repositories.core.DataRepository;
import org.n52.sensorweb.server.db.repositories.core.DatasetRepository;
import org.n52.sensorweb.server.db.repositories.core.FormatRepository;
import org.n52.sensorweb.server.db.repositories.core.ServiceRepository;
import org.n52.sensorweb.server.helgoland.adapters.da.CRUDRepository;
import org.n52.sensorweb.server.helgoland.adapters.harvest.DataSourceJobConfiguration;
import org.n52.sensorweb.server.helgoland.adapters.test.CategoryBuilder;
import org.n52.sensorweb.server.helgoland.adapters.test.DatasetEntityBuilder;
import org.n52.sensorweb.server.helgoland.adapters.test.FeatureBuilder;
import org.n52.sensorweb.server.helgoland.adapters.test.FormatBuilder;
import org.n52.sensorweb.server.helgoland.adapters.test.OfferingBuilder;
import org.n52.sensorweb.server.helgoland.adapters.test.PhenomenonBuilder;
import org.n52.sensorweb.server.helgoland.adapters.test.PlatformBuilder;
import org.n52.sensorweb.server.helgoland.adapters.test.ProcedureBuilder;
import org.n52.sensorweb.server.helgoland.adapters.test.ServiceBuilder;
import org.n52.series.db.beans.CountDataEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.FormatEntity;
import org.n52.series.db.beans.QuantityDataEntity;
import org.n52.series.db.beans.ServiceEntity;
import org.n52.series.db.beans.TextDataEntity;
import org.n52.series.db.beans.dataset.DatasetType;
import org.n52.series.db.beans.dataset.ObservationType;
import org.n52.series.db.beans.dataset.ValueType;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.cosium.spring.data.jpa.entity.graph.repository.support.EntityGraphJpaRepositoryFactoryBean;

@DataJpaTest
@ExtendWith(SpringExtension.class)
@ImportResource("classpath:arctic-sea-test.xml")
public class InsertRespositoryTest extends ProxyTestBase {

    @Inject
    private CRUDRepository insertRespository;

    @Inject
    private ServiceRepository serviceRespository;

    @Inject
    private DatasetRepository datasetRepository;

    @Inject
    private DataRepository<?> dataRepository;

    @Inject
    private FormatRepository formatRepository;

    @Test
    @DisplayName("Test insertion of service")
    public void service_insertion() {
        ServiceEntity insertedService = insertRespository.insertService(
                ServiceBuilder.newService("service", "https://52north.org/service", "SOS 2.0.0").build());
        assertAll("Inserted service", () -> {
            assertThat(serviceRespository.findById(insertedService.getId()).isPresent());
            assertThat(serviceRespository.findByIdentifier("service").isPresent());
        });

    }

    @Test
    @DisplayName("Test deletion of configured service")
    public void service_configured_deletion() {
        ServiceEntity insertedService = insertRespository.insertService(
                ServiceBuilder.newService("service", "https://52north.org/service", "SOS 2.0.0").build());
        assertAll("Inserted service", () -> {
            assertThat(serviceRespository.findById(insertedService.getId()).isPresent());
            assertThat(serviceRespository.findByIdentifier("service").isPresent());
        });
        DataSourceJobConfiguration dataSourceConfiguration = new DataSourceJobConfiguration();
        dataSourceConfiguration.setItemName("service");
        dataSourceConfiguration.setUrl("https://52north.org/service");
        Set<DataSourceJobConfiguration> config = new LinkedHashSet<>();
        config.add(dataSourceConfiguration);
        insertRespository.removeNonMatchingServices(config);
        assertAll("Deleted configured service", () -> {
            assertThat(serviceRespository.findById(insertedService.getId()).isPresent());
            assertThat(serviceRespository.findByIdentifier("service").isPresent());
        });
        config.clear();
        dataSourceConfiguration = new DataSourceJobConfiguration();
        dataSourceConfiguration.setItemName("dfgfdg");
        dataSourceConfiguration.setUrl("https://52north.org/sdgsg");
        config.add(dataSourceConfiguration);
        insertRespository.removeNonMatchingServices(config);
        assertAll("Deleted configured service", () -> {
            assertThat(serviceRespository.findById(insertedService.getId()).isPresent());
            assertThat(serviceRespository.findByIdentifier("service").isPresent());
        });
    }

    @Test
    @DisplayName("Test deletion of service")
    public void service_deletion() {
        ServiceEntity insertedService = insertRespository.insertService(
                ServiceBuilder.newService("service", "https://52north.org/service", "SOS 2.0.0").build());
        assertAll("Inserted service", () -> {
            assertThat(serviceRespository.findById(insertedService.getId()).isPresent());
            assertThat(serviceRespository.findByIdentifier("service").isPresent());
        });
        DataSourceJobConfiguration dataSourceConfiguration = new DataSourceJobConfiguration();
        dataSourceConfiguration.setItemName("dfgfdg");
        dataSourceConfiguration.setUrl("https://52north.org/sdgsg");
        Set<DataSourceJobConfiguration> config = new LinkedHashSet<>();
        config.add(dataSourceConfiguration);
        insertRespository.removeNonMatchingServices(config);
        assertAll("Deleted configured service", () -> {
            assertFalse(serviceRespository.findById(insertedService.getId()).isPresent());
            assertFalse(serviceRespository.findByIdentifier("service").isPresent());
        });
    }

    @Test
    @DisplayName("Test insertion of dataset")
    public void dataset_insertion() {
        ServiceEntity service = insertRespository.insertService(
                ServiceBuilder.newService("service", "https://52north.org/service", "SOS 2.0.0").build());
        DatasetEntity dataset = createDatasetEntity(service);
        DatasetEntity insertedDataset = insertRespository.insertDataset(dataset);
        assertAll("Inserted dataset", () -> {
            Optional<DatasetEntity> optional = datasetRepository.findById(insertedDataset.getId());
            assertTrue(optional.isPresent());
            DatasetEntity datasetEntity = optional.get();

            assertThat(insertRespository.getIdsForService(service).contains(insertedDataset.getId()));

            assertNotNull(datasetEntity.getCategory());
            assertTrue(datasetEntity.getCategory().isSetIdentifier());
            assertEquals("category", datasetEntity.getCategory().getIdentifier());

            assertNotNull(datasetEntity.getFeature());
            assertTrue(datasetEntity.getFeature().isSetIdentifier());
            assertEquals("feature", datasetEntity.getFeature().getIdentifier());

            assertNotNull(datasetEntity.getOffering());
            assertTrue(datasetEntity.getOffering().isSetIdentifier());
            assertEquals("offering", datasetEntity.getOffering().getIdentifier());

            assertNotNull(datasetEntity.getPhenomenon());
            assertTrue(datasetEntity.getPhenomenon().isSetIdentifier());
            assertEquals("phenomenon", datasetEntity.getPhenomenon().getIdentifier());

            assertNotNull(datasetEntity.getPlatform());
            assertTrue(datasetEntity.getPlatform().isSetIdentifier());
            assertEquals("platform", datasetEntity.getPlatform().getIdentifier());

            assertNotNull(datasetEntity.getProcedure());
            assertTrue(datasetEntity.getProcedure().isSetIdentifier());
            assertEquals("procedure", datasetEntity.getProcedure().getIdentifier());

            assertTrue(datasetEntity.hasService());
            assertTrue(datasetEntity.getService().isSetIdentifier());
            assertEquals("service", datasetEntity.getService().getIdentifier());
        });
    }

    @Test
    @DisplayName("Test deletion of dataset")
    public void dataset_deletion() {
        ServiceEntity service = insertRespository.insertService(
                ServiceBuilder.newService("service", "https://52north.org/service", "SOS 2.0.0").build());
        DatasetEntity dataset = createDatasetEntity(service);
        DatasetEntity insertedDataset = insertRespository.insertDataset(dataset);
        DataSourceJobConfiguration dataSourceConfiguration = new DataSourceJobConfiguration();
        dataSourceConfiguration.setItemName("dfgfdg");
        dataSourceConfiguration.setUrl("https://52north.org/sdgsg");
        Set<DataSourceJobConfiguration> config = new LinkedHashSet<>();
        config.add(dataSourceConfiguration);
        insertRespository.removeNonMatchingServices(config);
        assertAll("Deleted dataset", () -> {
            Optional<DatasetEntity> optional = datasetRepository.findById(insertedDataset.getId());
            assertThat(optional.isPresent());

        });
    }

    @Test
    @DisplayName("Test clean up")
    public void clean_up() {
        ServiceEntity service = insertRespository.insertService(
                ServiceBuilder.newService("service", "https://52north.org/service", "SOS 2.0.0").build());
        DatasetEntity dataset = createDatasetEntity(service);
        DatasetEntity insertedDataset = insertRespository.insertDataset(dataset);
        insertRespository.insertData(insertedDataset, createQuantityData(dataset));
        insertRespository.cleanUp(service, insertRespository.getIdsForService(service), true);
        assertAll("Deleted dataset", () -> {
            Optional<DatasetEntity> optional = datasetRepository.findById(insertedDataset.getId());
            assertFalse(optional.isPresent());
            assertTrue(dataRepository.findAll().isEmpty());
        });
    }

    @Test
    @DisplayName("Test insertion of quantity data")
    public void quantity_data_insertion() {
        ServiceEntity service = insertRespository.insertService(
                ServiceBuilder.newService("service", "https://52north.org/service", "SOS 2.0.0").build());
        DatasetEntity dataset = createQuantityDatasetEntity(service);
        DatasetEntity insertedDataset = insertRespository.insertDataset(dataset);
        insertRespository.insertData(insertedDataset, createQuantityData(dataset));
        assertAll("Deleted dataset", () -> {
            Optional<DatasetEntity> optional = datasetRepository.findById(insertedDataset.getId());
            assertTrue(optional.isPresent());
            List<? extends DataEntity<?>> dataEntities = dataRepository.findAllByDataset(insertedDataset);
            assertThat(!dataEntities.isEmpty());
            assertThat(dataEntities.size() == 1);
            assertThat(dataset.getFirstObservation() != null);
            DataEntity<?> data = dataEntities.iterator().next();
            assertThat(data.getId().equals(dataset.getFirstObservation().getId()));
            assertThat(data.getSamplingTimeStart().equals(dataset.getFirstValueAt()));
            assertThat(data.getValue().equals(dataset.getFirstQuantityValue()));

            assertThat(data.getId().equals(dataset.getLastObservation().getId()));
            assertThat(data.getSamplingTimeEnd().equals(dataset.getLastValueAt()));
            assertThat(data.getValue().equals(dataset.getLastQuantityValue()));
        });
    }

    @Test
    @DisplayName("Test insertion of text data")
    public void text_data_insertion() {
        ServiceEntity service = insertRespository.insertService(
                ServiceBuilder.newService("service", "https://52north.org/service", "SOS 2.0.0").build());
        DatasetEntity dataset = createTextDatasetEntity(service);
        DatasetEntity insertedDataset = insertRespository.insertDataset(dataset);
        insertRespository.insertData(insertedDataset, createTextData(dataset));
        assertAll("Deleted dataset", () -> {
            Optional<DatasetEntity> optional = datasetRepository.findById(insertedDataset.getId());
            assertTrue(optional.isPresent());
            List<? extends DataEntity<?>> dataEntities = dataRepository.findAllByDataset(insertedDataset);
            assertThat(!dataEntities.isEmpty());
            assertThat(dataEntities.size() == 1);
            assertThat(dataset.getFirstObservation() != null);
            DataEntity<?> data = dataEntities.iterator().next();
            assertThat(data.getId().equals(dataset.getFirstObservation().getId()));
            assertThat(data.getSamplingTimeStart().equals(dataset.getFirstValueAt()));
            assertThat(dataset.getFirstQuantityValue() == null);

            assertThat(data.getId().equals(dataset.getLastObservation().getId()));
            assertThat(data.getSamplingTimeEnd().equals(dataset.getLastValueAt()));
            assertThat(dataset.getLastQuantityValue() == null);
        });
    }

    @Test
    @DisplayName("Test insertion of count data")
    public void count_data_insertion() {
        ServiceEntity service = insertRespository.insertService(
                ServiceBuilder.newService("service", "https://52north.org/service", "SOS 2.0.0").build());
        DatasetEntity dataset = createCountDatasetEntity(service);
        DatasetEntity insertedDataset = insertRespository.insertDataset(dataset);
        insertRespository.insertData(insertedDataset, createCountData(dataset));
        assertAll("Deleted dataset", () -> {
            Optional<DatasetEntity> optional = datasetRepository.findById(insertedDataset.getId());
            assertTrue(optional.isPresent());
            List<? extends DataEntity<?>> dataEntities = dataRepository.findAllByDataset(insertedDataset);
            assertThat(!dataEntities.isEmpty());
            assertThat(dataEntities.size() == 1);
            assertThat(dataset.getFirstObservation() != null);
            DataEntity<?> data = dataEntities.iterator().next();
            assertThat(data.getId().equals(dataset.getFirstObservation().getId()));
            assertThat(data.getSamplingTimeStart().equals(dataset.getFirstValueAt()));
            assertThat(dataset.getFirstQuantityValue() == null);

            assertThat(data.getId().equals(dataset.getLastObservation().getId()));
            assertThat(data.getSamplingTimeEnd().equals(dataset.getLastValueAt()));
            assertThat(dataset.getLastQuantityValue() == null);
        });
    }

    private DatasetEntity createQuantityDatasetEntity(ServiceEntity service) {
        return createDatasetEntity(service).setDatasetType(DatasetType.timeseries)
                                           .setObservationType(ObservationType.simple)
                                           .setValueType(ValueType.quantity);
    }

    private DatasetEntity createTextDatasetEntity(ServiceEntity service) {
        return createDatasetEntity(service).setDatasetType(DatasetType.timeseries)
                                           .setObservationType(ObservationType.simple)
                                           .setValueType(ValueType.text);
    }

    private DatasetEntity createCountDatasetEntity(ServiceEntity service) {
        return createDatasetEntity(service).setDatasetType(DatasetType.timeseries)
                                           .setObservationType(ObservationType.simple)
                                           .setValueType(ValueType.count);
    }

    private DatasetEntity createDatasetEntity(ServiceEntity service) {
        return (DatasetEntity) createDatasetEntity().setService(service);
    }

    private DatasetEntity createDatasetEntity() {
        FormatEntity format = formatRepository.saveAndFlush(FormatBuilder.newFormat("format").build());
        return DatasetEntityBuilder.newDataset("dataset").setCategory(CategoryBuilder.newCategory("category").build())
                                   .setFeature(FeatureBuilder.newFeature("feature").setFormat(format).build())
                                   .setOffering(OfferingBuilder.newOffering("offering").build())
                                   .setPhenomenon(PhenomenonBuilder.newPhenomenon("phenomenon").build())
                                   .setPlatform(PlatformBuilder.newFeature("platform").build())
                                   .setProcedure(ProcedureBuilder.newProcedure("procedure").setFormat(format).build())
                                   .build();
    }

    private DataEntity<?> createQuantityData(DatasetEntity dataset) {
        return createData(dataset, QuantityDataEntity::new, new BigDecimal("52.7"));
    }

    private DataEntity<?> createTextData(DatasetEntity dataset) {
        return createData(dataset, TextDataEntity::new, "52N");
    }

    private DataEntity<?> createCountData(DatasetEntity dataset) {
        return createData(dataset, CountDataEntity::new, 52);
    }

    private <V, T extends DataEntity<? super V>> T createData(DatasetEntity dataset, Supplier<T> dataFactory, V value) {
        T data = dataFactory.get();
        data.setIdentifier(UUID.randomUUID().toString());
        data.setDataset(dataset);
        Date date = DateTime.now().toDate();
        data.setSamplingTimeStart(date);
        data.setSamplingTimeEnd(date);
        data.setResultTime(date);
        data.setValue(value);
        return data;
    }

    @SpringBootConfiguration
    @EnableJpaRepositories(basePackageClasses = {DatasetRepository.class},
            repositoryFactoryBeanClass = EntityGraphJpaRepositoryFactoryBean.class)
    @ComponentScan({ "org.n52.sensorweb.server.db.repository.core", "org.n52.sensorweb.server.db.old",
            "org.n52.sensorweb.server.db.assembler.core", "org.n52.sensorweb.server.db.assembler.mapper",
            "org.n52.sensorweb.server.db.factory", "org.n52.sensorweb.server.db.old.dao",
            "org.n52.sensorweb.server.helgoland.adapters"})
    static class Config extends ProxyTestRepositoryConfig<DatasetEntity> {
        public Config() {
            super("/mapping/proxy/persistence.xml");
        }

        @Override
        public ProxyTestRepositories testRepositories() {
            return new ProxyTestRepositories();
        }

        @Bean
        public DbQueryFactory dbQueryFactory() {
            return new DefaultDbQueryFactory();
        }
    }
}
