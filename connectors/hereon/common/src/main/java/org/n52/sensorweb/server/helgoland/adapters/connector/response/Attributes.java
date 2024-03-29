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
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Attributes implements Serializable {
    private static final long serialVersionUID = 1839884324462571777L;
    @JsonIgnore
    @Valid
    private Map<String, Object> additionalProperties = new LinkedHashMap<>();

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return Collections.unmodifiableMap(additionalProperties);
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public Attributes withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    @JsonIgnore
    public String getValue(String key) {
        if (key.contains(",")) {
            return getValues(key.split(",")).values().stream().filter(Objects::nonNull)
                    .collect(Collectors.joining("_"));
        } else {
            if (getAdditionalProperties().containsKey(key) && getAdditionalProperties().get(key) != null) {
                String value = getAdditionalProperties().get(key).toString();
                return value != null && !value.isEmpty() ? value : null;
            }
        }
        return null;
    }

    @JsonIgnore
    public Map<String, String> getValues(String... keys) {
        Map<String, String> values = new LinkedHashMap<>();
        for (String key : keys) {
            values.put(key, getValue(key));
        }
        return values;
    }

    public List<String> getValueList(String... keys) {
        List<String> values = new LinkedList<>();
        for (String key : keys) {
            values.add(getValue(key));
        }
        return values;
    }

    public boolean hasValue(String key) {
        if (key.contains(",")) {
            for (String value : getValues(key.split(",")).values()) {
                if (value != null && !value.isEmpty()) {
                    return true;
                }
            }
        } else {
            if (getAdditionalProperties().containsKey(key)) {
                return getAdditionalProperties().get(key) != null
                        && !getAdditionalProperties().get(key).toString().isEmpty();
            }
        }
        return false;
    }

}
