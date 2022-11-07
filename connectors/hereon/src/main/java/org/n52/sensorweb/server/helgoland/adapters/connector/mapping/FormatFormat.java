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
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "separator", "prefix", "optionals" })
@Generated("jsonschema2pojo")
public class FormatFormat implements Serializable {
    private static final long serialVersionUID = -6940072861756141395L;
    @JsonProperty("separator")
    private String separator = "_";
    @JsonProperty("prefix")
    private String prefix;
    @JsonProperty("optionals")
    private List<Optional> optionals = new LinkedList<>();

    @JsonProperty("separator")
    public String getSeparator() {
        return separator;
    }

    @JsonProperty("separator")
    public void setSeparator(String separator) {
        this.separator = separator;
    }

    public FormatFormat withSeparator(String separator) {
        this.separator = separator;
        return this;
    }

    @JsonProperty("prefix")
    public String getPrefix() {
        return prefix;
    }

    @JsonProperty("prefix")
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public FormatFormat withPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    @JsonProperty("optionals")
    public List<Optional> getOptionals() {
        return Collections.unmodifiableList(optionals);
    }

    @JsonProperty("optionals")
    public void setOptionals(List<Optional> optionals) {
        this.optionals.clear();
        if (optionals != null) {
            this.optionals.addAll(optionals);
        }
    }

    public FormatFormat withOptionals(List<Optional> optionals) {
        if (optionals != null) {
            this.optionals.addAll(optionals);
        }
        return this;
    }

    public boolean hasOnlySeparator() {
        return !(isSetPrefix() || isSetOptionals());
    }

    public boolean isSetSeparator() {
        return getSeparator() != null && !getSeparator().isEmpty();
    }

    public boolean isSetOptionals() {
        return getOptionals() != null && !getOptionals().isEmpty();
    }

    public boolean isSetPrefix() {
        return getPrefix() != null && !getPrefix().isEmpty();
    }

    public String getOptionalFields() {
        return optionals.stream().map(o -> o.getField()).filter(Objects::nonNull).collect(Collectors.joining(","));
    }

}
