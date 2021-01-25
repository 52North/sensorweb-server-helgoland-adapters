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
package org.n52.sensorweb.server.helgoland.adapters.connector.utils;

import org.slf4j.LoggerFactory;

import org.n52.series.db.beans.ServiceEntity;
import org.n52.series.db.beans.ServiceMetadataEntity;
import org.n52.series.db.beans.CategoryEntity;
import org.n52.series.db.beans.FeatureEntity;
import org.n52.series.db.beans.GeometryEntity;
import org.n52.series.db.beans.OfferingEntity;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.beans.UnitEntity;
import org.n52.shetland.ogc.sos.SosConstants;

public final class EntityBuilder {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(EntityBuilder.class);

    private EntityBuilder() {
    }

    public static ServiceEntity createService(String name, String description, String connector, String url,
            String version, ServiceMetadata serviceMetadata) {
        ServiceEntity service = new ServiceEntity();
        service.setIdentifier(name);
        service.setName(name);
        service.setDescription(description);
        service.setVersion(version);
        service.setType(SosConstants.SOS);
        service.setUrl(url);
        service.setConnector(connector);
        if (serviceMetadata != null) {
            service.setServiceMetadata(new ServiceMetadataEntity().setMetadata(serviceMetadata.getMetadata())
                    .setFormat(serviceMetadata.getFormat()));
        }
        return createService(name, description, connector, url, version, true, serviceMetadata);
    }

    public static ServiceEntity createService(String name, String description, String connector, String url,
            String version, Boolean supportsFirstLast, ServiceMetadata serviceMetadata) {
        ServiceEntity service = new ServiceEntity();
        service.setIdentifier(name);
        service.setName(name);
        service.setDescription(description);
        service.setVersion(version);
        service.setType(SosConstants.SOS);
        service.setUrl(url);
        service.setSupportsFirstLast(supportsFirstLast);
        service.setConnector(connector);
        if (serviceMetadata != null) {
            service.setServiceMetadata(new ServiceMetadataEntity().setMetadata(serviceMetadata.getMetadata())
                    .setFormat(serviceMetadata.getFormat()));
        }
        return service;
    }

    public static ProcedureEntity createProcedure(String identifier, String name, ServiceEntity service) {
        ProcedureEntity procedure = new ProcedureEntity();
        procedure.setName(name);
        procedure.setIdentifier(identifier);
        procedure.setService(service);
        return procedure;
    }

    public static OfferingEntity createOffering(String identifier, String name, ServiceEntity service) {
        OfferingEntity offering = new OfferingEntity();
        offering.setIdentifier(identifier);
        offering.setName(name);
        offering.setService(service);
        return offering;
    }

    public static CategoryEntity createCategory(String identifier, String name, ServiceEntity service) {
        CategoryEntity category = new CategoryEntity();
        category.setName(name);
        category.setIdentifier(identifier);
        category.setService(service);
        return category;
    }

    public static FeatureEntity createFeature(String identifier, String name, String description,
            GeometryEntity geometry, ServiceEntity service) {
        FeatureEntity feature = new FeatureEntity();
        feature.setName(name);
        feature.setDescription(description);
        feature.setIdentifier(identifier);
        feature.setGeometryEntity(geometry);
        feature.setService(service);
        return feature;
    }

    public static PlatformEntity createPlatform(String identifier, String name, String description,
            ServiceEntity service) {
        PlatformEntity platform = new PlatformEntity();
        platform.setName(name);
        platform.setDescription(description);
        platform.setIdentifier(identifier);
        platform.setService(service);
        return platform;
    }

    public static PhenomenonEntity createPhenomenon(String identifier, String name, ServiceEntity service) {
        PhenomenonEntity phenomenon = new PhenomenonEntity();
        phenomenon.setName(name);
        phenomenon.setIdentifier(identifier);
        phenomenon.setService(service);
        return phenomenon;
    }

    public static PhenomenonEntity createPhenomenon(String identifier, String name, String description,
            ServiceEntity service) {
        PhenomenonEntity phenomenon = createPhenomenon(identifier, name, service);
        phenomenon.setDescription(description);
        return phenomenon;
    }

    public static UnitEntity createUnit(String unit, String unitDescription, ServiceEntity service) {
        UnitEntity entity = new UnitEntity();
        entity.setName(unit);
        entity.setDescription(unitDescription);
        entity.setService(service);
        return entity;
    }

    public static UnitEntity createUnit(String unit, String unitDescription) {
        UnitEntity entity = new UnitEntity();
        entity.setSymbol(unit);
        entity.setName(unit);
        entity.setDescription(unitDescription);
        return entity;
    }

}
