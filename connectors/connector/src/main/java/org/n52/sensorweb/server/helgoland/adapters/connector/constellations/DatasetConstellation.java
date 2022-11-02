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
package org.n52.sensorweb.server.helgoland.adapters.connector.constellations;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import org.n52.series.db.beans.CategoryEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.FeatureEntity;
import org.n52.series.db.beans.FormatEntity;
import org.n52.series.db.beans.OfferingEntity;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.beans.ServiceEntity;
import org.n52.series.db.beans.parameter.ParameterEntity;
import org.n52.series.db.beans.parameter.dataset.DatasetParameterEntity;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * @author Jan Schulte
 */
@SuppressFBWarnings({"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
public abstract class DatasetConstellation {

    private final String procedure;
    private final String offering;
    private final String category;
    private final String phenomenon;
    private final String feature;
    private final String platform;
    private DataEntity<?> first;
    private DataEntity<?> latest;
    private String identifier;
    private String name;
    private String description;
    private boolean mobile;
    private boolean insitu;
    private Date samplingTimeStart;
    private Date samplingTimeEnd;
    private Set<ParameterEntity<?>> parameters = new LinkedHashSet<>();

    public DatasetConstellation(String procedure, String offering, String phenomenon, String feature) {
        this(procedure, offering, phenomenon, phenomenon, feature, feature);
    }

    public DatasetConstellation(String procedure, String offering, String category, String phenomenon, String feature,
            String platform) {
        this.procedure = procedure;
        this.offering = offering;
        this.category = category;
        this.phenomenon = phenomenon;
        this.feature = feature;
        this.platform = platform;
    }

    public String getProcedure() {
        return procedure;
    }

    public String getOffering() {
        return offering;
    }

    public String getCategory() {
        return category;
    }

    public String getPhenomenon() {
        return phenomenon;
    }

    public String getFeature() {
        return feature;
    }

    public String getPlatform() {
        return platform;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "DatasetConstellation{" + "procedure=" + procedure + ", offering=" + offering + ", category=" + category
                + ", phenomenon=" + phenomenon + ", feature=" + feature + '}';
    }

    public final DatasetEntity createDatasetEntity(ProcedureEntity procedure, CategoryEntity category,
            FeatureEntity feature, OfferingEntity offering, PhenomenonEntity phenomenon, PlatformEntity platform,
            ServiceEntity service) {
        DatasetEntity datasetEntity = createDatasetEntity(service);
        datasetEntity.setIdentifier(getIdentifier());
        datasetEntity.setName(getName());
        datasetEntity.setDescription(getDescription());
        datasetEntity.setProcedure(procedure);
        datasetEntity.setCategory(category);
        datasetEntity.setFeature(feature);
        datasetEntity.setPlatform(platform);
        datasetEntity.setPhenomenon(phenomenon);
        datasetEntity.setOffering(offering);
        datasetEntity.setPublished(true);
        datasetEntity.setDeleted(false);
        datasetEntity.setService(service);
        datasetEntity.setGeometryEntity(
                offering.isSetGeometry() ? offering.getGeometryEntity() : feature.getGeometryEntity());

        FormatEntity omObservationType = new FormatEntity();
        omObservationType.setId(5L);
        omObservationType.setFormat("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement");
        datasetEntity.setOMObservationType(omObservationType);

        getFirst().map(DataEntity::getSamplingTimeStart).ifPresent(datasetEntity::setFirstValueAt);
        getLatest().map(DataEntity::getSamplingTimeEnd).ifPresent(datasetEntity::setLastValueAt);
        datasetEntity.setParameters(getParameter(datasetEntity));

        return datasetEntity;
    }

    protected abstract DatasetEntity createDatasetEntity(ServiceEntity service);

    public Optional<DataEntity<?>> getFirst() {
        return Optional.ofNullable(first);
    }

    public void setFirst(DataEntity<?> first) {
        this.first = first;
    }

    public Optional<DataEntity<?>> getLatest() {
        return Optional.ofNullable(latest);
    }

    public void setLatest(DataEntity<?> latest) {
        this.latest = latest;
    }

    public boolean isMobile() {
        return mobile;
    }

    public void setMobile(boolean mobile) {
        this.mobile = mobile;
    }

    public boolean isInsitu() {
        return insitu;
    }

    public void setInsitu(boolean insitu) {
        this.insitu = insitu;
    }

    public Date getSamplingTimeStart() {
        return getFirst().isPresent() ? getFirst().get().getSamplingTimeStart()
                : samplingTimeStart != null ? samplingTimeStart : new Date();
    }

    public DatasetConstellation setSamplingTimeStart(Date samplingTimeStart) {
        this.samplingTimeStart = samplingTimeStart != null ? (Date) samplingTimeStart.clone() : null;
        return this;
    }

    public Date getSamplingTimeEnd() {
        return getLatest().isPresent() ? getLatest().get().getSamplingTimeEnd()
                : samplingTimeEnd != null ? samplingTimeEnd : new Date();
    }

    public DatasetConstellation setSamplingTimeEnd(Date samplingTimeEnd) {
        this.samplingTimeEnd = samplingTimeEnd != null ? (Date) samplingTimeEnd.clone() : null;
        return this;
    }

    private Set<ParameterEntity<?>> getParameter(DatasetEntity datasetEntity) {
        parameters.forEach(p -> ((DatasetParameterEntity<?>) p).setDataset(datasetEntity));
        return parameters;
    }

    public void setParameters(Set<ParameterEntity<?>> parameters) {
        this.parameters.addAll(parameters);
    }
}
