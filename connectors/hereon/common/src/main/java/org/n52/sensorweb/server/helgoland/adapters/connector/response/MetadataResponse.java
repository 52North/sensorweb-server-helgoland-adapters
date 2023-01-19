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
package org.n52.sensorweb.server.helgoland.adapters.connector.response;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"features", "exceededTransferLimit"})
@JsonIgnoreProperties({
        "hasM",
        "hasZ",
        "globalIdFieldName",
        "objectIdFieldName",
        "fields",
        "geometryType",
        "spatialReference"})
public class MetadataResponse implements Serializable {
    private static final long serialVersionUID = 565521313246527281L;

    @JsonProperty("features")
    @Valid
    private List<MetadataFeature> features = new LinkedList<>();

    @JsonProperty("exceededTransferLimit")
    @Valid
    private Boolean exceededTransferLimit;

    @JsonProperty("features")
    @Valid
    public List<MetadataFeature> getFeatures() {
        return Collections.unmodifiableList(features);
    }

    @JsonProperty("features")
    public void setFeatures(List<MetadataFeature> features) {
        this.features.clear();
        if (features != null) {
            this.features.addAll(features);
        }
    }

    public MetadataResponse withFeatures(List<MetadataFeature> features) {
        this.features.clear();
        if (features != null) {
            this.features.addAll(features);
        }
        return this;
    }

    @JsonProperty("exceededTransferLimit")
    public Boolean getExceededTransferLimit() {
        return exceededTransferLimit;
    }

    @JsonProperty("exceededTransferLimit")
    public void setExceededTransferLimit(Boolean exceededTransferLimit) {
        this.exceededTransferLimit = exceededTransferLimit;
    }

    public MetadataResponse withExceededTransferLimit(Boolean exceededTransferLimit) {
        this.exceededTransferLimit = exceededTransferLimit;
        return this;
    }

    public boolean isSetFeatures() {
        return getFeatures() != null && !getFeatures().isEmpty();
    }

}
