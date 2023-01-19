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
package org.n52.sensorweb.server.helgoland.adapters.utils;

import java.math.BigDecimal;
import java.util.UUID;

import org.joda.time.DateTime;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.n52.series.db.beans.CategoryEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.beans.FeatureEntity;
import org.n52.series.db.beans.FormatEntity;
import org.n52.series.db.beans.GeometryEntity;
import org.n52.series.db.beans.OfferingEntity;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.beans.QuantityDataEntity;
import org.n52.series.db.beans.ServiceEntity;
import org.n52.series.db.beans.ServiceMetadataEntity;
import org.n52.series.db.beans.TagEntity;
import org.n52.series.db.beans.UnitEntity;
import org.n52.series.db.beans.dataset.DatasetType;
import org.n52.series.db.beans.dataset.ObservationType;
import org.n52.series.db.beans.dataset.ValueType;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.shetland.ogc.om.OmConstants;
import org.n52.shetland.ogc.om.features.SfConstants;
import org.n52.shetland.ogc.sensorML.SensorML20Constants;
import org.n52.shetland.ogc.sos.SosConstants;

public interface EntityBuilder {

    default CategoryEntity createCategory(String identifier, String name, String description, ServiceEntity service) {
        CategoryEntity entity = new CategoryEntity();
        addDescribeableData(entity, identifier, name, description, service);
        return entity;
    }

    default CategoryEntity createCategory(String identifier, String name, ServiceEntity service) {
        return createCategory(identifier, name, null, service);
    }

    default FeatureEntity createFeature(String identifier, String name, String description, ServiceEntity service) {
        FeatureEntity entity = new FeatureEntity();
        addDescribeableData(entity, identifier, name, description, service);
        entity.setFeatureType(getDefaultFeatureFormat());
        return entity;
    }

    default FeatureEntity createFeature(String identifier, String name, String description, Geometry geometry,
            ServiceEntity service) {
        FeatureEntity feature = new FeatureEntity();
        addDescribeableData(feature, identifier, name, description, service);
        feature.setGeometryEntity(createGeometry(geometry));
        return feature;
    }

    default FeatureEntity createFeature(String identifier, String name, String description, GeometryEntity geometry,
            ServiceEntity service) {
        FeatureEntity feature = new FeatureEntity();
        addDescribeableData(feature, identifier, name, description, service);
        feature.setGeometryEntity(geometry);
        return feature;
    }

    default LocationEntity createLocation(String identifier, String name, String description, GeometryEntity geometry,
            ServiceEntity service) {
        LocationEntity location = new LocationEntity();
        addDescribeableData(location, identifier, name, description, service);
        location.setGeometryEntity(geometry);
        return location;
    }

    default OfferingEntity createOffering(String identifier, String name, String description, ServiceEntity service) {
        OfferingEntity entity = new OfferingEntity();
        addDescribeableData(entity, identifier, name, description, service);
        return entity;
    }

    default OfferingEntity createOffering(String identifier, String name, ServiceEntity service) {
        return createOffering(identifier, name, null, service);
    }

    default ProcedureEntity createProcedure(String identifier, String name, String description,
            ServiceEntity service) {
        ProcedureEntity entity = new ProcedureEntity();
        addDescribeableData(entity, identifier, name, description, service);
        entity.setFormat(getDefaultProcedureFormat());
        return entity;
    }

    default ProcedureEntity createProcedure(String identifier, String name, ServiceEntity service) {
        return createProcedure(identifier, name, null, service);
    }

    default PhenomenonEntity createPhenomenon(String identifier, String name, String description,
            ServiceEntity service) {
        PhenomenonEntity entity = new PhenomenonEntity();
        addDescribeableData(entity, identifier, name, description, service);
        return entity;
    }

    default PhenomenonEntity createPhenomenon(String identifier, String name, ServiceEntity service) {
        return createPhenomenon(identifier, name, null, service);
    }

    default PlatformEntity createPlatform(String identifier, String name, String description, ServiceEntity service) {
        PlatformEntity entity = new PlatformEntity();
        addDescribeableData(entity, identifier, name, description, service);
        return entity;
    }

    default TagEntity createTag(String identifier, String name, String description, ServiceEntity service) {
        TagEntity entity = new TagEntity();
        addDescribeableData(entity, identifier, name, description, service);
        return entity;
    }

    default UnitEntity createUnit(String identifier, String name, String description, ServiceEntity service) {
        UnitEntity entity = new UnitEntity();
        addDescribeableData(entity, identifier.replace(" ", ""), name, description, service);
        return entity;
    }

    default UnitEntity createUnit(String unit, String unitDescription, ServiceEntity service) {
        return createUnit(unit, unit, unitDescription, service);
    }

    default UnitEntity createUnit(String unit, String unitDescription) {
        return createUnit(unit, unit, unitDescription, null);
    }

    default FormatEntity createFormat(String format) {
        FormatEntity entity = new FormatEntity();
        entity.setFormat(format);
        return entity;
    }

    default GeometryEntity createGeometry(Geometry geometry) {
        GeometryEntity geometryEntity = new GeometryEntity();
        geometryEntity.setGeometry(geometry);
        geometryEntity.setSrid(geometry.getSRID());
        geometryEntity.setGeometryFactory(geometry.getFactory());
        return geometryEntity;
    }

    default Geometry createGeometry(Double latitude, Double longitude) {
        return createGeometry(latitude, longitude, 4326);
    }

    default Geometry createGeometry(Double latitude, Double longitude, Integer srid) {
        GeometryEntity geometryEntity = new GeometryEntity();
        geometryEntity.setGeometryFactory(new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), srid));
        geometryEntity.setLat(latitude);
        geometryEntity.setLon(longitude);
        return geometryEntity.getGeometry();
    }

    default QuantityDataEntity createDataEntity(DateTime time, BigDecimal value, Long id) {
        QuantityDataEntity entity = createDataEntity(time, id);
        entity.setValue(value);
        return entity;
    }

    default QuantityDataEntity createDataEntity(DateTime time, BigDecimal value) {
        return createDataEntity(time, value, null);
    }

    default QuantityDataEntity createDataEntity(DateTime time, Long id) {
        QuantityDataEntity entity = new QuantityDataEntity();
        if (id != null) {
            entity.setId(id);
        }
        entity.setSamplingTimeStart(time.toDate());
        entity.setSamplingTimeEnd(time.toDate());
        addDescribeableData(entity, null, null, null, null);
        return entity;
    }

    default DatasetEntity createDataset(String identifier, String name, String description, ServiceEntity service) {
        DatasetEntity entity = new DatasetEntity(DatasetType.timeseries, ObservationType.simple, ValueType.quantity);
        addDescribeableData(entity, identifier, name, description, service);
        entity.setOMObservationType(createFormat(OmConstants.OBS_TYPE_MEASUREMENT));
        entity.setDeleted(Boolean.FALSE);
        entity.setPublished(Boolean.TRUE);
        return entity;
    }

    default ServiceEntity createService(String name, String description, String connector, String url,
            String version) {
        return createService(name, description, connector, url, version, true);
    }

    default ServiceEntity createService(String name, String description, String connector, String url, String version,
            Boolean supportsFirstLast) {
        return createService(name, description, connector, url, version, supportsFirstLast, null, SosConstants.SOS);
    }

    default ServiceEntity createService(String name, String description, String connector, String url, String version,
            ServiceMetadataEntity serviceMetadata) {
        return createService(name, description, connector, url, version, true, serviceMetadata);
    }

    default ServiceEntity createService(String name, String description, String connector, String url, String version,
            Boolean supportsFirstLast, ServiceMetadataEntity serviceMetadata) {
        return createService(name, description, connector, url, version, supportsFirstLast, serviceMetadata,
                SosConstants.SOS);
    }

    default ServiceEntity createService(String name, String description, String connector, String url, String version,
            Boolean supportsFirstLast, ServiceMetadataEntity serviceMetadata, String type) {
        ServiceEntity service = new ServiceEntity();
        service.setIdentifier(name);
        service.setName(name);
        service.setDescription(description);
        service.setVersion(version);
        service.setType(type);
        service.setUrl(url);
        service.setSupportsFirstLast(supportsFirstLast);
        service.setConnector(connector);
        service.setServiceMetadata(serviceMetadata);
        return service;
    }

    default FormatEntity getDefaultProcedureFormat() {
        return createFormat(SensorML20Constants.NS_SML_20);
    }

    default FormatEntity getDefaultFeatureFormat() {
        return createFormat(SfConstants.SAMPLING_FEAT_TYPE_SF_SAMPLING_POINT);
    }

    default void addDescribeableData(DescribableEntity entity, String identifier, String name, String description,
            ServiceEntity service) {
        entity.setIdentifier(identifier);
        entity.setName(name);
        entity.setDescription(description);
        entity.setStaIdentifier(UUID.randomUUID().toString());
        entity.setService(service);
    }

}
