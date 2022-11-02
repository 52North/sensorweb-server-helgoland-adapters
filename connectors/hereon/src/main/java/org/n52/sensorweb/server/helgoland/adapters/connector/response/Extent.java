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
import java.util.HashMap;
import java.util.Map;

import javax.validation.Valid;

import org.geotools.geometry.jts.JTS;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "ymin", "xmin", "ymax", "xmax", "spatialReference" })
@SuppressFBWarnings({ "EI_EXPOSE_REP", "EI_EXPOSE_REP2" })
public class Extent implements Serializable {
    private static final long serialVersionUID = 122572278748898272L;
    @JsonProperty("ymin")
    private Integer ymin;
    @JsonProperty("xmin")
    private Double xmin;
    @JsonProperty("ymax")
    private Double ymax;
    @JsonProperty("xmax")
    private Double xmax;
    @JsonProperty("spatialReference")
    @Valid
    private SpatialReference spatialReference;
    @JsonIgnore
    @Valid
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("ymin")
    public Integer getYmin() {
        return ymin;
    }

    @JsonProperty("ymin")
    public void setYmin(Integer ymin) {
        this.ymin = ymin;
    }

    public Extent withYmin(Integer ymin) {
        this.ymin = ymin;
        return this;
    }

    @JsonProperty("xmin")
    public Double getXmin() {
        return xmin;
    }

    @JsonProperty("xmin")
    public void setXmin(Double xmin) {
        this.xmin = xmin;
    }

    public Extent withXmin(Double xmin) {
        this.xmin = xmin;
        return this;
    }

    @JsonProperty("ymax")
    public Double getYmax() {
        return ymax;
    }

    @JsonProperty("ymax")
    public void setYmax(Double ymax) {
        this.ymax = ymax;
    }

    public Extent withYmax(Double ymax) {
        this.ymax = ymax;
        return this;
    }

    @JsonProperty("xmax")
    public Double getXmax() {
        return xmax;
    }

    @JsonProperty("xmax")
    public void setXmax(Double xmax) {
        this.xmax = xmax;
    }

    public Extent withXmax(Double xmax) {
        this.xmax = xmax;
        return this;
    }

    @JsonProperty("spatialReference")
    public SpatialReference getSpatialReference() {
        return spatialReference;
    }

    @JsonProperty("spatialReference")
    public void setSpatialReference(SpatialReference spatialReference) {
        this.spatialReference = spatialReference;
    }

    public Extent withSpatialReference(SpatialReference spatialReference) {
        this.spatialReference = spatialReference;
        return this;
    }

    @JsonIgnore
    public boolean hasSpatialReference() {
        return getSpatialReference() != null && getSpatialReference().hasWkid();
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return Collections.unmodifiableMap(this.additionalProperties);
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public Extent withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    @JsonIgnore
    public Envelope getEnvelope() {
        return new Envelope(getXmin(), getXmax(), getYmin(), getYmax());
    }

    @JsonIgnore
    public Geometry getGeometry() {
        Polygon geometry = JTS.toGeometry(getEnvelope());
        if (hasSpatialReference()) {
            geometry.setSRID(getSpatialReference().getWkid());
        } else {
            geometry.setSRID(4326);
        }
        return geometry;
    }
}
