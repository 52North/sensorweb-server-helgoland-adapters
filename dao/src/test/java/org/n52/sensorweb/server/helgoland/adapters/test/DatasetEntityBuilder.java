/*
 * Copyright (C) 2015-2021 52°North Spatial Information Research GmbH
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
package org.n52.sensorweb.server.helgoland.adapters.test;

import org.n52.series.db.beans.CategoryEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.FeatureEntity;
import org.n52.series.db.beans.OfferingEntity;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.beans.dataset.DatasetType;
import org.n52.series.db.beans.dataset.ObservationType;
import org.n52.series.db.beans.dataset.ValueType;

public class DatasetEntityBuilder extends DescribableEntityBuilder<DatasetEntity> {

    private PhenomenonEntity phenomenon;

    private ProcedureEntity procedure;

    private OfferingEntity offering;

    private CategoryEntity category;

    private FeatureEntity feature;

    private PlatformEntity platform;

    private DatasetEntityBuilder(String identifier) {
        super(identifier);
    }

    public DatasetEntity build(DatasetEntity entity) {
        entity.setOffering(offering);
        entity.setProcedure(procedure);
        entity.setPhenomenon(phenomenon);
        entity.setCategory(category);
        entity.setFeature(feature);
        entity.setPlatform(platform);
        entity.setDatasetType(entity.getDatasetType() != null ? entity.getDatasetType() : DatasetType.not_initialized);
        entity.setObservationType(
                entity.getObservationType() != null ? entity.getObservationType() : ObservationType.not_initialized);
        entity.setValueType(entity.getValueType() != null ? entity.getValueType() : ValueType.not_initialized);
        return entity;
    }

    public DatasetEntity build() {
        return build(new DatasetEntity());
    }

    public static DatasetEntityBuilder newDataset(String identifier) {
        return new DatasetEntityBuilder(identifier);
    }

    public DatasetEntityBuilder setOffering(OfferingEntity entity) {
        this.offering = entity;
        return this;
    }

    public DatasetEntityBuilder setProcedure(ProcedureEntity entity) {
        this.procedure = entity;
        return this;
    }

    public DatasetEntityBuilder setPhenomenon(PhenomenonEntity entity) {
        this.phenomenon = entity;
        return this;
    }

    public DatasetEntityBuilder setCategory(CategoryEntity entity) {
        this.category = entity;
        return this;
    }

    public DatasetEntityBuilder setFeature(FeatureEntity entity) {
        this.feature = entity;
        return this;
    }

    public DatasetEntityBuilder setPlatform(PlatformEntity entity) {
        this.platform = entity;
        return this;
    }

}
