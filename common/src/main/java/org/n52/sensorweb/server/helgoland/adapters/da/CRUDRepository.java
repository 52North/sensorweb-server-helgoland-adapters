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
package org.n52.sensorweb.server.helgoland.adapters.da;

import static java.util.stream.Collectors.toSet;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.hibernate.Hibernate;
import org.n52.io.request.IoParameters;
import org.n52.sensorweb.server.db.assembler.core.CategoryAssembler;
import org.n52.sensorweb.server.db.assembler.core.DatasetAssembler;
import org.n52.sensorweb.server.db.assembler.core.FeatureAssembler;
import org.n52.sensorweb.server.db.assembler.core.OfferingAssembler;
import org.n52.sensorweb.server.db.assembler.core.PhenomenonAssembler;
import org.n52.sensorweb.server.db.assembler.core.PlatformAssembler;
import org.n52.sensorweb.server.db.assembler.core.ProcedureAssembler;
import org.n52.sensorweb.server.db.assembler.core.ServiceAssembler;
import org.n52.sensorweb.server.db.assembler.core.TagAssembler;
import org.n52.sensorweb.server.db.old.dao.DbQueryFactory;
import org.n52.sensorweb.server.db.query.DatasetQuerySpecifications;
import org.n52.sensorweb.server.db.repositories.core.DataRepository;
import org.n52.sensorweb.server.db.repositories.core.DatasetRepository;
import org.n52.sensorweb.server.db.repositories.core.UnitRepository;
import org.n52.sensorweb.server.helgoland.adapters.harvest.DataSourceJobConfiguration;
import org.n52.series.db.beans.AbstractFeatureEntity;
import org.n52.series.db.beans.CategoryEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.beans.OfferingEntity;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.beans.QuantityDataEntity;
import org.n52.series.db.beans.ServiceEntity;
import org.n52.series.db.beans.TagEntity;
import org.n52.series.db.beans.UnitEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class CRUDRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(CRUDRepository.class);

    @Inject
    private DbQueryFactory dbQueryFactory;

    @Inject
    private CategoryAssembler categoryAssembler;

    @Inject
    private FeatureAssembler featureAssembler;

    @Inject
    private OfferingAssembler offeringAssembler;

    @Inject
    private PhenomenonAssembler phenomenonAssembler;

    @Inject
    private ProcedureAssembler procedureAssembler;

    @Inject
    private PlatformAssembler platformAssembler;

    @Inject
    private TagAssembler tagAssembler;

    @Inject
    private ServiceAssembler serviceAssembler;

    @Inject
    private DatasetAssembler<?> datasetAssembler;

    @Inject
    private DatasetRepository datasetRepository;

    @Inject
    private UnitRepository unitRepository;

    @Inject
    private DataRepository dataRepository;

    public synchronized void removeNonMatchingServices(Set<DataSourceJobConfiguration> configuredServices) {
        serviceAssembler.getParameterRepository().findAll().stream()
                .filter(service -> !isConfigured(configuredServices, service)).forEach(this::removeService);
    }

    public synchronized Set<Long> getIdsForService(ServiceEntity service) {
        DatasetQuerySpecifications dsQS =
                DatasetQuerySpecifications.of(dbQueryFactory.createFrom(IoParameters.createDefaults()), null);
        Specification<DatasetEntity> specification = dsQS.matchServices(service.getId().toString());
        return datasetRepository.findAll(specification).stream().map(DescribableEntity::getId).collect(toSet());
    }

    protected boolean isConfigured(Set<DataSourceJobConfiguration> configuredServices, ServiceEntity service) {
        return configuredServices.stream().anyMatch(configuration -> equals(configuration, service));
    }

    protected boolean equals(DataSourceJobConfiguration configuration, ServiceEntity service) {
        return (configuration.getUrl() != null ? configuration.getUrl().equals(service.getUrl()) : true)
                && configuration.getItemName().equals(service.getName());
    }

    private void removeService(ServiceEntity service) {
        removeServiceRelatedData(service);
        serviceAssembler.clearUnusedForService(service);
    }

    public synchronized void removeServiceRelatedData(ServiceEntity service) {
        DatasetQuerySpecifications dsQS = getDatasetQuerySpecification();
        for (DatasetEntity dataset : datasetRepository
                .findAll(dsQS.matchServices(Long.toString(service.getId())).and(dsQS.isNotHidden()))) {
            Optional<DatasetEntity> optional = datasetRepository.findById(dataset.getId());
            if (optional.isPresent()) {
                DatasetEntity datasetEntity = optional.get();
                removeRelatedData(datasetEntity);
                DatasetEntity save = removeData(datasetEntity);
                datasetRepository.delete(save);
            }
        }
        categoryAssembler.clearUnusedForService(service);
        offeringAssembler.clearUnusedForService(service);
        procedureAssembler.clearUnusedForService(service);
        featureAssembler.clearUnusedForService(service);
        phenomenonAssembler.clearUnusedForService(service);
        platformAssembler.clearUnusedForService(service);
    }

    public synchronized ServiceEntity insertService(ServiceEntity service) {
        return serviceAssembler.getOrInsertInstance(service);
    }

    public synchronized DatasetEntity removeRelatedData(DatasetEntity dataset) {
        DatasetEntity instance = datasetRepository.getReferenceById(dataset.getId());
        if (instance.hasReferenceValues()) {
            for (DatasetEntity refDataset : instance.getReferenceValues()) {
                removeData(refDataset);
            }
        }
        return removeData(instance);
    }

    public void cleanUp(ServiceEntity service, Set<Long> datasetIds, boolean removeService) {
        if (datasetIds != null && !datasetIds.isEmpty()) {
            for (Long id : datasetIds) {
                dataRepository.deleteByDataset(datasetRepository.getReferenceById(id));
            }
            datasetRepository.deleteByIdIn(datasetIds);
        }
        if (removeService) {
            removeService(service);
        }
    }

    private DatasetEntity removeData(DatasetEntity dataset) {
        dataset.setFirstObservation(null);
        dataset.setLastObservation(null);
        DatasetEntity saved = datasetRepository.saveAndFlush(dataset);
        dataRepository.deleteByDataset(saved);
        return saved;
    }

    public synchronized void removeFeature(AbstractFeatureEntity<?> entity) {
        featureAssembler.getParameterRepository().delete(entity);
    }

    public void removeFeature(Set<Long> features) {
        featureAssembler.getParameterRepository().deleteAllById(features);
    }

    public synchronized void removePlatform(PlatformEntity entity) {
        platformAssembler.getParameterRepository().delete(entity);
    }

    public void removePlatform(Set<Long> platforms) {
        platformAssembler.getParameterRepository().deleteAllById(platforms);
    }

    public synchronized DatasetEntity insertDataset(DatasetEntity dataset) {
        ProcedureEntity procedure = insertProcedure(dataset.getProcedure());
        CategoryEntity category = insertCategory(dataset.getCategory());
        OfferingEntity offering = insertOffering(dataset.getOffering());
        AbstractFeatureEntity<?> feature = insertFeature(dataset.getFeature());
        PhenomenonEntity phenomenon = insertPhenomenon(dataset.getPhenomenon());
        PlatformEntity platform = insertPlatform(dataset.getPlatform());
        UnitEntity unit = insertUnit(dataset.getUnit());
        Set<TagEntity> insertTags = new LinkedHashSet<>();
        if (dataset.hasTags()) {
            insertTags = insertTags(dataset.getTags());
        }
        return insertDataset(dataset, category, procedure, offering, feature, phenomenon, platform, unit, insertTags);
    }

    private DatasetEntity insertDataset(DatasetEntity dataset, CategoryEntity category, ProcedureEntity procedure,
            OfferingEntity offering, AbstractFeatureEntity<?> feature, PhenomenonEntity phenomenon,
            PlatformEntity platform, UnitEntity unit, Set<TagEntity> tags) {
        dataset.setCategory(category);
        dataset.setProcedure(procedure);
        dataset.setOffering(offering);
        dataset.setFeature(feature);
        dataset.setPhenomenon(phenomenon);
        dataset.setPlatform(platform);
        dataset.setUnit(unit);
        if (tags != null && !tags.isEmpty()) {
            dataset.setTags(tags);
        }
        return unproxy(datasetAssembler.getOrInsertInstance(dataset));
    }

    public synchronized AbstractFeatureEntity<?> insertFeature(AbstractFeatureEntity<?> feature) {
        return unproxy(featureAssembler.getOrInsertInstance(feature));
    }

    private OfferingEntity insertOffering(OfferingEntity offering) {
        return unproxy(offeringAssembler.getOrInsertInstance(offering));
    }

    private ProcedureEntity insertProcedure(ProcedureEntity procedure) {
        return unproxy(procedureAssembler.getOrInsertInstance(procedure));
    }

    private CategoryEntity insertCategory(CategoryEntity category) {
        return unproxy(categoryAssembler.getOrInsertInstance(category));
    }

    private PhenomenonEntity insertPhenomenon(PhenomenonEntity phenomenon) {
        return unproxy(phenomenonAssembler.getOrInsertInstance(phenomenon));
    }

    private PlatformEntity insertPlatform(PlatformEntity platform) {
        return unproxy(platformAssembler.getOrInsertInstance(platform));
    }

    private Set<TagEntity> insertTags(Collection<TagEntity> tags) {
        return tags.stream().filter(Objects::nonNull).map(t -> insertTag(t)).collect(Collectors.toSet());
    }

    private TagEntity insertTag(TagEntity tag) {
        return unproxy(tagAssembler.getOrInsertInstance(tag));
    }

    private UnitEntity insertUnit(UnitEntity unit) {
        if (unit != null && unit.isSetIdentifier()) {
            UnitEntity instance = unitRepository.getInstance(unit);
            if (instance != null) {
                return unproxy(instance);
            }
            return unitRepository.saveAndFlush(unit);
        }
        return null;
    }

    public synchronized <T extends DataEntity<?>> T insertData(DatasetEntity dataset, T data) {
        return insertData(dataset, data, false);
    }

    public synchronized <T extends DataEntity<?>> T insertData(DatasetEntity dataset, T data, boolean forceMinChange) {
        data.setDataset(dataset);
        boolean minChanged = false;
        boolean maxChanged = false;
        if (!dataset.isSetFirstValueAt()
                || forceMinChange && dataset.getFirstValueAt() != null && data.getSamplingTimeStart() != null
                        && dataset.getFirstValueAt().before(data.getSamplingTimeStart())) {
            dataset.setFirstValueAt(data.getSamplingTimeStart());
            minChanged = true;
        } else {
            if (!dataset.isSetFirstValueAt()
                    || dataset.isSetFirstValueAt() && dataset.getFirstValueAt().after(data.getSamplingTimeStart())) {
                minChanged = true;
                dataset.setFirstValueAt(data.getSamplingTimeStart());
            }
        }
        if (!dataset.isSetLastValueAt()
                || dataset.isSetLastValueAt() && dataset.getLastValueAt().before(data.getSamplingTimeEnd())) {
            maxChanged = true;
            dataset.setLastValueAt(data.getSamplingTimeEnd());
        }
        DataEntity<?> insertedData = null;
        if (minChanged) {
            if (dataset.getFirstObservation() != null) {
                data.setId(dataset.getFirstObservation().getId());
            }
            insertedData = (DataEntity<?>) dataRepository.saveAndFlush(data);
            dataset.setFirstObservation(insertedData);
        }
        if (maxChanged) {
            if (insertedData != null) {
                dataset.setLastObservation(insertedData);
            } else {
                if (dataset.getLastObservation() != null
                        && (dataset.getFirstObservation() == null || dataset.getFirstObservation() != null && !dataset
                                .getFirstObservation().getId().equals(dataset.getLastObservation().getId()))) {
                    data.setId(dataset.getLastObservation().getId());
                }
                insertedData = (DataEntity<?>) dataRepository.saveAndFlush(data);
                dataset.setLastObservation(insertedData);
            }
        }

        if (insertedData instanceof QuantityDataEntity) {
            if (minChanged) {
                dataset.setFirstQuantityValue(((QuantityDataEntity) insertedData).getValue());
            }
            if (maxChanged) {
                dataset.setLastQuantityValue(((QuantityDataEntity) insertedData).getValue());
            }
        }
        if (minChanged || maxChanged) {
            updateDataset(dataset);
        }
        updateOffering(dataset.getOffering(), data, forceMinChange);
        return (T) insertedData;
    }

    public synchronized DatasetEntity updateData(DatasetEntity dataset, DataEntity<?> data) {
        dataRepository.saveAndFlush(data);
        return updateDataset(dataset);
    }

    public synchronized DatasetEntity updateDataset(DatasetEntity dataset) {
        return unproxy(datasetRepository.saveAndFlush(dataset));
    }

    public synchronized OfferingEntity updateOffering(OfferingEntity offering, DataEntity<?> data,
            boolean forceMinChange) {

        boolean modified = false;
        if (offering.getSamplingTimeStart() == null
                || forceMinChange && offering.getSamplingTimeStart() != null && data.getSamplingTimeStart() != null
                        && offering.getSamplingTimeStart().before(data.getSamplingTimeStart())) {
            offering.setSamplingTimeStart(data.getSamplingTimeStart());
            modified = true;
        } else {
            if (offering.getSamplingTimeStart() == null
                    || offering.getSamplingTimeStart() != null && data.getSamplingTimeStart() != null
                            && offering.getSamplingTimeStart().after(data.getSamplingTimeStart())) {
                offering.setSamplingTimeStart(data.getSamplingTimeStart());
                modified = true;
            }
        }
        if (offering.getSamplingTimeEnd() == null
                || offering.getSamplingTimeEnd() != null && data.getSamplingTimeEnd() != null
                        && offering.getSamplingTimeEnd().before(data.getSamplingTimeEnd())) {
            offering.setSamplingTimeEnd(data.getSamplingTimeEnd());
            modified = true;
        }
        if (offering.getResultTimeStart() == null || forceMinChange && offering.getResultTimeStart() != null
                && data.getResultTime() != null && offering.getResultTimeStart().before(data.getResultTime())) {
            offering.setResultTimeStart(data.getResultTime());
            modified = true;
        } else {
            if (offering.getResultTimeStart() == null || offering.getResultTimeStart() != null
                    && data.getResultTime() != null && offering.getResultTimeStart().after(data.getResultTime())) {
                offering.setResultTimeStart(data.getResultTime());
                modified = true;
            }
        }
        if (offering.getResultTimeEnd() == null || offering.getResultTimeEnd() != null && data.getResultTime() != null
                && offering.getResultTimeEnd().before(data.getResultTime())) {
            offering.setResultTimeEnd(data.getResultTime());
            modified = true;
        }
        if (offering.getValidTimeStart() == null || forceMinChange && offering.getValidTimeStart() != null
                && data.getValidTimeStart() != null && offering.getValidTimeStart().before(data.getValidTimeStart())) {
            offering.setValidTimeStart(data.getValidTimeStart());
            modified = true;
        } else {
            if (offering.getValidTimeStart() == null
                    || offering.getValidTimeStart() != null && data.getValidTimeStart() != null
                            && offering.getValidTimeStart().after(data.getValidTimeStart())) {
                offering.setValidTimeStart(data.getValidTimeStart());
                modified = true;
            }
        }
        if (offering.getValidTimeEnd() == null || offering.getValidTimeEnd() != null && data.getValidTimeEnd() != null
                && offering.getValidTimeEnd().before(data.getValidTimeEnd())) {
            offering.setValidTimeEnd(data.getValidTimeEnd());
            modified = true;
        }
        if (modified) {
            return offeringAssembler.updateInstance(offering);
        }
        return offering;
    }

    public DatasetQuerySpecifications getDatasetQuerySpecification() {
        return getDatasetQuerySpecification(IoParameters.createDefaults());
    }

    public DatasetQuerySpecifications getDatasetQuerySpecification(IoParameters parameters) {
        return DatasetQuerySpecifications.of(dbQueryFactory.createFrom(parameters),
                datasetAssembler.getEntityManager());
    }

    private <T> T unproxy(T entity) {
        return (T) Hibernate.unproxy(entity);
    }

}
