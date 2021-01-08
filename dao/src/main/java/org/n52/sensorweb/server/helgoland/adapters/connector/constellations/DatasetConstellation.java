/*
 * Copyright (C) 2015-2021 52°North Initiative for Geospatial Open Source
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
package org.n52.sensorweb.server.helgoland.adapters.connector.constellations;

import java.util.Date;
import java.util.Optional;

import org.n52.series.db.beans.CategoryEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.FeatureEntity;
import org.n52.series.db.beans.OfferingEntity;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.beans.ServiceEntity;

/**
 * @author Jan Schulte
 */
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
    private boolean mobile;
    private boolean insitu;
    private Date samplingTimeStart;
    private Date samplingTimeEnd;

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
        datasetEntity.setProcedure(procedure);
        datasetEntity.setCategory(category);
        datasetEntity.setFeature(feature);
        datasetEntity.setPlatform(platform);
        datasetEntity.setPhenomenon(phenomenon);
        datasetEntity.setOffering(offering);
        datasetEntity.setPublished(true);
        datasetEntity.setDeleted(false);
        datasetEntity.setService(service);

        getFirst().map(DataEntity::getSamplingTimeStart).ifPresent(datasetEntity::setFirstValueAt);
        getLatest().map(DataEntity::getSamplingTimeEnd).ifPresent(datasetEntity::setLastValueAt);

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
        this.samplingTimeStart = (Date) samplingTimeStart.clone();
        return this;
    }

    public Date getSamplingTimeEnd() {
        return getLatest().isPresent() ? getLatest().get().getSamplingTimeEnd()
                : samplingTimeEnd != null ? samplingTimeEnd : new Date();
    }

    public DatasetConstellation setSamplingTimeEnd(Date samplingTimeEnd) {
        this.samplingTimeEnd = (Date) samplingTimeEnd.clone();
        return this;
    }

}
