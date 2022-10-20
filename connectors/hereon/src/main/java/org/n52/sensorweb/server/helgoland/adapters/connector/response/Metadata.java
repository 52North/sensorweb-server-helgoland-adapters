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

package org.n52.sensorweb.server.helgoland.adapters.connector.response;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "features", "exceededTransferLimit" })
public class Metadata implements Serializable {
    private static final long serialVersionUID = 565521313246527281L;
    @JsonProperty("features")
    @Valid
    private List<Feature> features = new LinkedList<>();
    @JsonProperty("exceededTransferLimit")
    private Boolean exceededTransferLimit;
    @JsonIgnore
    @Valid
    private Map<String, Object> additionalProperties = new LinkedHashMap<>();

    @JsonProperty("features")
    public List<Feature> getFeatures() {
        return Collections.unmodifiableList(features);
    }

    @JsonProperty("features")
    public void setFeatures(List<Feature> features) {
        this.features.clear();
        if (features != null) {
            this.features.addAll(features);
        }
    }

    public Metadata withFeatures(List<Feature> features) {
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

    public Metadata withExceededTransferLimit(Boolean exceededTransferLimit) {
        this.exceededTransferLimit = exceededTransferLimit;
        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return Collections.unmodifiableMap(additionalProperties);
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public Metadata withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

}
