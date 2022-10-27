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
package org.n52.sensorweb.server.helgoland.adapters.connector.request.builder;

import java.util.LinkedHashSet;
import java.util.Set;

import org.n52.sensorweb.server.helgoland.adapters.connector.hereon.HereonConfig;
import org.n52.sensorweb.server.helgoland.adapters.connector.mapping.Feature;
import org.n52.sensorweb.server.helgoland.adapters.connector.request.GetFeatureGeometryRequest;
import org.n52.sensorweb.server.helgoland.adapters.connector.request.GetFeatureRequest;

public class FeatureRequestBuilder extends AbstractRequestBuilder<GetFeatureRequest, Feature> {

    public FeatureRequestBuilder(HereonConfig hereonConfig) {
        super(hereonConfig);
    }

    @Override
    public GetFeatureRequest getDefaultRequest() {
        return new GetFeatureRequest();
    }

    @Override
    public Feature getTypeMapping() {
        return getMapping().getFeature();
    }
    
    @Override
    protected String getFields() {
        Set<String> fields = new LinkedHashSet<>();
        fields.add(super.getFields());
        fields.add(getGeneralMapping().getMetadataId());
        if (!isRequestGeometryFromDataService()) {
            fields.add(getTypeMapping().getFeature());
        }
        return String.join(",", fields);
    }

    public GetFeatureGeometryRequest getGeomtryRequest() {
        GetFeatureGeometryRequest request = new GetFeatureGeometryRequest();
        String field = getTypeMapping().getFeature().replace(getDataServicePrefix(), "");
        if (field.equals("extent")) {
            
        } else {
            add
        }
        return request;
    }

    public boolean isRequestGeometryFromDataService() {
        return getTypeMapping().getFeature().contains(getDataServicePrefix());
    }

}
