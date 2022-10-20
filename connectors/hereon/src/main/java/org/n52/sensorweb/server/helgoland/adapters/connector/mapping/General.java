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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "dataServiceUrl", "metadataId" })
public class General implements Serializable {
    private static final long serialVersionUID = 3028121006318512305L;
    @JsonProperty("dataServiceUrl")
    private String dataServiceUrl;
    @JsonProperty("metadataId")
    private String metadataId;

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

}
