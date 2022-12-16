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

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "phenomenonTime",
        "resultTime",
        "result",
        "resultQuality",
        "validTime",
        "parameters"
})
public class Observation implements Serializable, Entity {
    private static final long serialVersionUID = -1131449219115560395L;

    @JsonProperty("phenomenonTime")
    private String phenomenonTime;
    @JsonProperty("resultTime")
    private String resultTime;
    @JsonProperty("result")
    private String result;
    @JsonProperty("resultQuality")
    private String resultQuality;
    @JsonProperty("validTime")
    private String validTime;
    @JsonProperty("parameters")
    private List<String> parameters = new LinkedList<>();

    @JsonProperty("phenomenonTime")
    public String getPhenomenonTime() {
        return phenomenonTime;
    }

    @JsonProperty("phenomenonTime")
    public void setPhenomenonTime(String phenomenonTime) {
        this.phenomenonTime = phenomenonTime;
    }

    @JsonProperty("resultTime")
    public String getResultTime() {
        return resultTime;
    }

    @JsonProperty("resultTime")
    public void setResultTime(String resultTime) {
        this.resultTime = resultTime;
    }

    @JsonProperty("result")
    public String getResult() {
        return result;
    }

    @JsonProperty("result")
    public void setResult(String result) {
        this.result = result;
    }

    @JsonProperty("resultQuality")
    public String getResultQuality() {
        return resultQuality;
    }

    @JsonProperty("resultQuality")
    public void setResultQuality(String resultQuality) {
        this.resultQuality = resultQuality;
    }

    @JsonProperty("validTime")
    public String getValidTime() {
        return validTime;
    }

    @JsonProperty("validTime")
    public void setValidTime(String validTime) {
        this.validTime = validTime;
    }

    @JsonProperty("parameters")
    public List<String> getParameters() {
        return Collections.unmodifiableList(parameters);
    }

    @JsonProperty("parameters")
    public void setParameters(List<String> parameters) {
        this.parameters.clear();
        if (parameters != null) {
            this.parameters.addAll(parameters);
        }
    }

    @Override
    public Set<String> getFields() {
        Set<String> fields = new HashSet<>();
        add(fields, getPhenomenonTime());
        add(fields, getResult());
        add(fields, getValidTime());
        add(fields, getResultTime());

        getParameters().forEach(prop -> add(fields, prop));
        return fields;
    }

    protected Set<String> add(Set<String> fields, String field) {
        if (field != null && !field.isEmpty()) {
            fields.add(field);
        }
        return fields;
    }

}
