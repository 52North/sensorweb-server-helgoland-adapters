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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "dataServiceUrl", "dataServiceUrlPostfix", "metadataId", "dataServicePrefix",
        "stringFormatPrefix", "dataServiceTokenUrlPostfix" })
public class General implements Serializable {
    private static final long serialVersionUID = 3028121006318512305L;
    @JsonProperty("dataServiceUrl")
    private String dataServiceUrl;
    @JsonProperty("dataServiceUrlPostfix")
    private String dataServiceUrlPostfix;
    @JsonProperty("metadataId")
    private String metadataId;
    @JsonProperty("dataServicePrefix")
    private String dataServicePrefix;
    @JsonProperty("stringFormatPrefix")
    private String stringFormatPrefix;
    @JsonProperty("dataServiceTokenUrlPostfix")
    private String dataServiceTokenUrlPostfix;
    @JsonProperty("resultLimit")
    private Integer resultLimit;

    @JsonProperty("dataServiceUrl")
    public String getDataServiceUrl() {
        return dataServiceUrl;
    }

    @JsonProperty("dataServiceUrl")
    public void setDataServiceUrl(String dataServiceUrl) {
        this.dataServiceUrl = dataServiceUrl;
    }

    public General withDataServiceUrl(String dataServiceUrl) {
        this.dataServiceUrl = dataServiceUrl;
        return this;
    }

    @JsonProperty("dataServiceUrlPostfix")
    public String getDataServiceUrlPostfix() {
        return dataServiceUrlPostfix;
    }

    @JsonProperty("dataServiceUrlPostfix")
    public void setDataServiceUrlPostfix(String dataServiceUrlPostfix) {
        this.dataServiceUrlPostfix = dataServiceUrlPostfix;
    }

    public General withDataServiceUrlPostfix(String dataServiceUrlPostfix) {
        this.dataServiceUrlPostfix = dataServiceUrlPostfix;
        return this;
    }

    @JsonProperty("metadataId")
    public String getMetadataId() {
        return metadataId;
    }

    @JsonProperty("metadataId")
    public void setMetadataId(String metadataId) {
        this.metadataId = metadataId;
    }

    public General withMetadataId(String metadataId) {
        this.metadataId = metadataId;
        return this;
    }

    @JsonProperty("dataServicePrefix")
    public String getDataServicePrefix() {
        return dataServicePrefix;
    }

    @JsonProperty("dataServicePrefix")
    public void setDataServicePrefix(String dataServicePrefix) {
        this.dataServicePrefix = dataServicePrefix;
    }

    public General withDataServicePrefix(String dataServicePrefix) {
        this.dataServicePrefix = dataServicePrefix;
        return this;
    }

    @JsonProperty("stringFormatPrefix")
    public String getStringFormatPrefix() {
        return stringFormatPrefix;
    }

    @JsonProperty("stringFormatPrefix")
    public void setStringFormatPrefix(String stringFormatPrefix) {
        this.stringFormatPrefix = stringFormatPrefix;
    }

    public General withStringFormatPrefix(String stringFormatPrefix) {
        this.stringFormatPrefix = stringFormatPrefix;
        return this;
    }

    @JsonProperty("dataServiceTokenUrlPostfix")
    public String getDataServiceTokenUrlPostfix() {
        return dataServiceTokenUrlPostfix;
    }

    @JsonProperty("dataServiceTokenUrlPostfix")
    public void setDataServiceTokenUrlPostfix(String dataServiceTokenUrlPostfix) {
        this.dataServiceTokenUrlPostfix = dataServiceTokenUrlPostfix;
    }

    public General withDataServiceTokenUrlPostfix(String dataServiceTokenUrlPostfix) {
        this.dataServiceTokenUrlPostfix = dataServiceTokenUrlPostfix;
        return this;
    }

    @JsonProperty("resultLimit")
    public Integer getResultLimit() {
        return resultLimit;
    }

    @JsonIgnore
    public boolean hasResultLimit() {
        return getResultLimit() != null && getResultLimit() > 0;
    }

    @JsonProperty("resultLimit")
    public void setesultLimit(Integer resultLimit) {
        this.resultLimit = resultLimit;
    }

    public General withesultLimit(Integer resultLimit) {
        this.resultLimit = resultLimit;
        return this;
    }
}
