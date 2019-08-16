package org.n52.proxy.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import org.joda.time.DateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.n52.proxy.config.DataSourceConfiguration;
import org.n52.proxy.da.InsertRepository;
import org.n52.proxy.test.CategoryBuilder;
import org.n52.proxy.test.DatasetEntityBuilder;
import org.n52.proxy.test.FeatureBuilder;
import org.n52.proxy.test.FormatBuilder;
import org.n52.proxy.test.OfferingBuilder;
import org.n52.proxy.test.PhenomenonBuilder;
import org.n52.proxy.test.PlatformBuilder;
import org.n52.proxy.test.ProcedureBuilder;
import org.n52.proxy.test.ServiceBuilder;
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
import org.n52.series.db.repositories.core.DataRepository;
import org.n52.series.db.repositories.core.DatasetRepository;
import org.n52.series.db.repositories.core.FormatRepository;
import org.n52.series.db.repositories.core.ServiceRepository;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@DataJpaTest
@ExtendWith(SpringExtension.class)
@ImportResource("classpath:artic-sea-test.xml")
public class InsertRespositoryTest extends ProxyTestBase {

    @Inject
    private InsertRepository insertRespository;

    @Inject
    private ServiceRepository serviceRespository;

    @Inject
    private DatasetRepository datasetRepository;

    @Inject
    private DataRepository dataRepository;

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
        DataSourceConfiguration dataSourceConfiguration = new DataSourceConfiguration();
        dataSourceConfiguration.setItemName("service");
        dataSourceConfiguration.setUrl("https://52north.org/service");
        Set<DataSourceConfiguration> config = new LinkedHashSet<>();
        config.add(dataSourceConfiguration);
        insertRespository.removeNonMatchingServices(config);
        assertAll("Deleted configured service", () -> {
            assertThat(serviceRespository.findById(insertedService.getId()).isPresent());
            assertThat(serviceRespository.findByIdentifier("service").isPresent());
        });
        config.clear();
        dataSourceConfiguration = new DataSourceConfiguration();
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
        DataSourceConfiguration dataSourceConfiguration = new DataSourceConfiguration();
        dataSourceConfiguration.setItemName("dfgfdg");
        dataSourceConfiguration.setUrl("https://52north.org/sdgsg");
        Set<DataSourceConfiguration> config = new LinkedHashSet<>();
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
            assertThat(optional.isPresent());
            DatasetEntity datasetEntity = optional.get();

            assertThat(insertRespository.getIdsForService(service).contains(insertedDataset.getId()));

            assertTrue(datasetEntity.getCategory() != null);
            assertTrue(datasetEntity.getCategory().isSetIdentifier());
            assertTrue(datasetEntity.getCategory().getIdentifier().equals("category"));

            assertTrue(datasetEntity.getFeature() != null);
            assertTrue(datasetEntity.getFeature().isSetIdentifier());
            assertTrue(datasetEntity.getFeature().getIdentifier().equals("feature"));

            assertTrue(datasetEntity.getOffering() != null);
            assertTrue(datasetEntity.getOffering().isSetIdentifier());
            assertTrue(datasetEntity.getOffering().getIdentifier().equals("offering"));

            assertTrue(datasetEntity.getPhenomenon() != null);
            assertTrue(datasetEntity.getPhenomenon().isSetIdentifier());
            assertTrue(datasetEntity.getPhenomenon().getIdentifier().equals("phenomenon"));

            assertTrue(datasetEntity.getPlatform() != null);
            assertTrue(datasetEntity.getPlatform().isSetIdentifier());
            assertTrue(datasetEntity.getPlatform().getIdentifier().equals("platform"));

            assertTrue(datasetEntity.getProcedure() != null);
            assertTrue(datasetEntity.getProcedure().isSetIdentifier());
            assertTrue(datasetEntity.getProcedure().getIdentifier().equals("procedure"));

            assertTrue(datasetEntity.hasService());
            assertTrue(datasetEntity.getService().isSetIdentifier());
            assertTrue(datasetEntity.getService().getIdentifier().equals("service"));
        });
    }

    @Test
    @DisplayName("Test deletion of dataset")
    public void dataset_deletion() {
        ServiceEntity service = insertRespository.insertService(
                ServiceBuilder.newService("service", "https://52north.org/service", "SOS 2.0.0").build());
        DatasetEntity dataset = createDatasetEntity(service);
        DatasetEntity insertedDataset = insertRespository.insertDataset(dataset);
        DataSourceConfiguration dataSourceConfiguration = new DataSourceConfiguration();
        dataSourceConfiguration.setItemName("dfgfdg");
        dataSourceConfiguration.setUrl("https://52north.org/sdgsg");
        Set<DataSourceConfiguration> config = new LinkedHashSet<>();
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
        DataEntity<?> insertedData = insertRespository.insertData(insertedDataset, createQuantityData(dataset));
        assertAll("Deleted dataset", () -> {
            Optional<DatasetEntity> optional = datasetRepository.findById(insertedDataset.getId());
            assertTrue(optional.isPresent());
            List<DataEntity> datas = dataRepository.findAllByDataset(insertedDataset);
            assertThat(!datas.isEmpty());
            assertThat(datas.size() == 1);
            assertThat(dataset.getFirstObservation() != null);
            DataEntity data = datas.iterator().next();
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
        DataEntity<?> insertedData = insertRespository.insertData(insertedDataset, createTextData(dataset));
        assertAll("Deleted dataset", () -> {
            Optional<DatasetEntity> optional = datasetRepository.findById(insertedDataset.getId());
            assertTrue(optional.isPresent());
            List<DataEntity> datas = dataRepository.findAllByDataset(insertedDataset);
            assertThat(!datas.isEmpty());
            assertThat(datas.size() == 1);
            assertThat(dataset.getFirstObservation() != null);
            DataEntity data = datas.iterator().next();
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
        DataEntity<?> insertedData = insertRespository.insertData(insertedDataset, createCountData(dataset));
        assertAll("Deleted dataset", () -> {
            Optional<DatasetEntity> optional = datasetRepository.findById(insertedDataset.getId());
            assertTrue(optional.isPresent());
            List<DataEntity> datas = dataRepository.findAllByDataset(insertedDataset);
            assertThat(!datas.isEmpty());
            assertThat(datas.size() == 1);
            assertThat(dataset.getFirstObservation() != null);
            DataEntity data = datas.iterator().next();
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
                .setObservationType(ObservationType.simple).setValueType(ValueType.quantity);
    }

    private DatasetEntity createTextDatasetEntity(ServiceEntity service) {
        return createDatasetEntity(service).setDatasetType(DatasetType.timeseries)
                .setObservationType(ObservationType.simple).setValueType(ValueType.text);
    }

    private DatasetEntity createCountDatasetEntity(ServiceEntity service) {
        return createDatasetEntity(service).setDatasetType(DatasetType.timeseries)
                .setObservationType(ObservationType.simple).setValueType(ValueType.count);
    }

    private DatasetEntity createDatasetEntity(ServiceEntity service) {
        return (DatasetEntity) createDatasetEntity().setService(service);
    }

    private DatasetEntity createDatasetEntity() {
        FormatEntity format = formatRepository.saveAndFlush(FormatBuilder.newFormat("format").build());
        return DatasetEntityBuilder.newDataset().setCategory(CategoryBuilder.newCategory("category").build())
                .setFeature(FeatureBuilder.newFeature("feature").setFormat(format)
                        .build())
                .setOffering(OfferingBuilder.newOffering("offering").build())
                .setPhenomemon(PhenomenonBuilder.newPhenomenon("phenomenon").build())
                .setPlatform(PlatformBuilder.newFeature("platform").build()).setProcedure(ProcedureBuilder
                        .newProcedure("procedure").setFormat(format).build())
                .build();
    }

    private DataEntity<?> createQuantityData(DatasetEntity dataset) {
        QuantityDataEntity data = new QuantityDataEntity();
        data.setDataset(dataset);
        Date date = DateTime.now().toDate();
        data.setSamplingTimeStart(date);
        data.setSamplingTimeEnd(date);
        data.setResultTime(date);
        data.setValue(new BigDecimal(52.7));
        return data;
    }

    private DataEntity<?> createTextData(DatasetEntity dataset) {
        TextDataEntity data = new TextDataEntity();
        data.setDataset(dataset);
        Date date = DateTime.now().toDate();
        data.setSamplingTimeStart(date);
        data.setSamplingTimeEnd(date);
        data.setResultTime(date);
        data.setValue("52N");
        return data;
    }

    private DataEntity<?> createCountData(DatasetEntity dataset) {
        CountDataEntity data = new CountDataEntity();
        data.setDataset(dataset);
        Date date = DateTime.now().toDate();
        data.setSamplingTimeStart(date);
        data.setSamplingTimeEnd(date);
        data.setResultTime(date);
        data.setValue(52);
        return data;
    }

    @SpringBootConfiguration
    @EnableJpaRepositories(basePackageClasses = {DatasetRepository.class})
    @ComponentScan({ "org.n52.series.db.repository.core", "org.n52.series.db.old",
            "org.n52.series.db.assembler.core", "org.n52.proxy" })
    static class Config extends ProxyTestRepositoryConfig<DatasetEntity> {
        public Config() {
            super("/mapping/proxy/persistence.xml");
        }

        @Override
        public ProxyTestRepositories testRepositories() {
            return new ProxyTestRepositories();
        }
    }
}
