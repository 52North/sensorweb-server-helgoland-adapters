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

@JsonPropertyOrder({ "metadata", "encodingType" })
public class Sensor extends AbstractEntity {

    private static final long serialVersionUID = 1353028724843766720L;

    @JsonProperty("metadata")
    private String metadata;
    @JsonProperty("encodingType")
    private String encodingType = "text/html";
    
    @JsonProperty("metadata")
    public String getMetadata() {
        return metadata;
    }

    @JsonProperty("metadata")
    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public AbstractEntity withMetadata(String metadata) {
        this.metadata = metadata;
        return this;
    }
    
    @JsonProperty("encodingType")
    public String getEncodingType() {
        return encodingType;
    }

    @JsonProperty("encodingType")
    public void setEncodingType(String encodingType) {
        this.encodingType = encodingType;
    }

    public AbstractEntity withEncodingType(String encodingType) {
        this.encodingType = encodingType;
        return this;
    }
    
    @Override
    public Set<String> getFields() {
        Set<String> fields = super.getFields();
        add(getFields(), getMetadata());
        return fields;
    }
}
