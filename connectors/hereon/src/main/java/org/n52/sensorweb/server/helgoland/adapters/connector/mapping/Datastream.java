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

package org.n52.sensorweb.server.helgoland.adapters.connector.mapping;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "identifier", "identifier", "unit", "phenomenonStartTime", "phenomenonEndTime", "resultTime",
        "observedArea" })
public class Datastream extends AbstractEntity {

    private static final long serialVersionUID = 1386105893996142527L;
    @JsonProperty("identifier")
    private String identifier;
    @JsonProperty("unit")
    private String unit;
    @JsonProperty("phenomenonStartTime")
    private String phenomenonStartTime;
    @JsonProperty("phenomenonEndTime")
    private String phenomenonEndTime;
    @JsonProperty("resultTime")
    private String resultTime;
    @JsonProperty("observedArea")
    private String observedArea;

    @JsonProperty("identifier")
    public String getIdentifier() {
        return identifier;
    }

    @JsonProperty("identifier")
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public AbstractEntity withIdentifier(String identifier) {
        this.identifier = identifier;
        return this;
    }

    @JsonProperty("unit")
    public String getUnit() {
        return unit;
    }

    @JsonProperty("unit")
    public void setUnit(String unit) {
        this.unit = unit;
    }

    public AbstractEntity withUnit(String unit) {
        this.unit = unit;
        return this;
    }

    @JsonProperty("phenomenonStartTime")
    public String getPhenomenonStartTime() {
        return phenomenonStartTime;
    }

    @JsonProperty("phenomenonStartTime")
    public void setPhenomenonStartTime(String phenomenonStartTime) {
        this.phenomenonStartTime = phenomenonStartTime;
    }

    public AbstractEntity withPhenomenonStartTime(String phenomenonStartTime) {
        this.phenomenonStartTime = phenomenonStartTime;
        return this;
    }

    @JsonProperty("phenomenonEndTime")
    public String getPhenomenonEndTime() {
        return phenomenonEndTime;
    }

    @JsonProperty("phenomenonEndTime")
    public void setPhenomenonEndTime(String phenomenonEndTime) {
        this.phenomenonEndTime = phenomenonEndTime;
    }

    public AbstractEntity withPhenomenonEndTime(String phenomenonEndTime) {
        this.phenomenonEndTime = phenomenonEndTime;
        return this;
    }

    @JsonProperty("resultTime")
    public String getResultTime() {
        return resultTime;
    }

    @JsonProperty("resultTime")
    public void setResultTime(String resultTime) {
        this.resultTime = resultTime;
    }

    public AbstractEntity withResultTime(String resultTime) {
        this.resultTime = resultTime;
        return this;
    }

    @JsonProperty("observedArea")
    public String getObservedArea() {
        return observedArea;
    }

    @JsonProperty("observedArea")
    public void setObservedArea(String observedArea) {
        this.observedArea = observedArea;
    }

    public AbstractEntity withObservedArea(String observedArea) {
        this.observedArea = observedArea;
        return this;
    }

    @Override
    public Set<String> getFields() {
        Set<String> fields = super.getFields();
        add(fields, getIdentifier());
        add(fields, getUnit());
        add(fields, getPhenomenonStartTime());
        add(fields, getPhenomenonEndTime());
        add(fields, getResultTime());
        return fields;
    }

}
