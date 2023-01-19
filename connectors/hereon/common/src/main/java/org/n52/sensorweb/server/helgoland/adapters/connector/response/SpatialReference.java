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
import java.util.HashMap;
import java.util.Map;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "latestWkid", "wkid" })
public class SpatialReference implements Serializable {
    private static final long serialVersionUID = 1760883659086222902L;
    @JsonProperty("latestWkid")
    private Integer latestWkid;
    @JsonProperty("wkid")
    private Integer wkid;
    @JsonIgnore
    @Valid
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("latestWkid")
    public Integer getLatestWkid() {
        return latestWkid;
    }

    @JsonProperty("latestWkid")
    public void setLatestWkid(Integer latestWkid) {
        this.latestWkid = latestWkid;
    }

    public SpatialReference withLatestWkid(Integer latestWkid) {
        this.latestWkid = latestWkid;
        return this;
    }

    @JsonProperty("wkid")
    public Integer getWkid() {
        return wkid;
    }

    @JsonProperty("wkid")
    public void setWkid(Integer wkid) {
        this.wkid = wkid;
    }

    public SpatialReference withWkid(Integer wkid) {
        this.wkid = wkid;
        return this;
    }

    @JsonIgnore
    public boolean hasWkid() {
        return getWkid() != null && getWkid() > 0;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return Collections.unmodifiableMap(this.additionalProperties);
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public SpatialReference withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }
}
