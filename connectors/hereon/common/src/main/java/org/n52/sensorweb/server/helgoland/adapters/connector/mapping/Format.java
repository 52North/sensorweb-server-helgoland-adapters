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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.n52.sensorweb.server.helgoland.adapters.connector.response.Attributes;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "fields", "format" })
@SuppressFBWarnings({ "EI_EXPOSE_REP", "EI_EXPOSE_REP2" })
public class Format implements Serializable {
    private static final long serialVersionUID = -7328974964055792319L;
    private static final String SEMICOLON = ";";
    private static final String COMMA = ",";
    @JsonProperty("name")
    private String name;
    @JsonProperty("fields")
    private String fields;
    @JsonProperty("format")
    @Valid
    private FormatFormat format;

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    public Format withName(String name) {
        this.name = name;
        return this;
    }

    @JsonProperty("fields")
    public String getFields() {
        if (getFormat().isSetOptionals()) {
            return (fields != null && !fields.isEmpty() ? fields + COMMA : "") + getFormat().getOptionalFields();
        }
        return fields;
    }

    @JsonProperty("fields")
    public void setFields(String fields) {
        this.fields = fields;
    }

    public Format withFields(String fields) {
        this.fields = fields;
        return this;
    }

    @JsonProperty("format")
    public FormatFormat getFormat() {
        return format;
    }

    @JsonProperty("format")
    public void setFormat(FormatFormat format) {
        this.format = format;
    }

    public Format withFormat(FormatFormat format) {
        this.format = format;
        return this;
    }

    public String getFormattedString(Attributes attribute) {
        if (!getFormat().hasOnlySeparator()) {
            Map<String, String> values = attribute.getValues(getFields().split(COMMA));
            StringBuffer buffer = new StringBuffer();
            if (getFormat().isSetPrefix()) {
                buffer.append(getFormat().getPrefix());
            }
            if (getFormat().isSetOptionals()) {
                String separator = getFormat().isSetSeparator() ? getFormat().getSeparator() : SEMICOLON;
                for (Optional optional : getFormat().getOptionals()) {
                    if (optional.getField().contains(COMMA)) {
                        List<String> fieldValues = new LinkedList<>();
                        for (String key : optional.getField().split(COMMA)) {
                            String value = values.get(key);
                            if (value != null && !value.isEmpty()) {
                                fieldValues.add(value);
                            }
                        }
                        if (!fieldValues.isEmpty()) {
                            buffer.append(optional.getValue()).append(fieldValues.stream().filter(Objects::nonNull)
                                    .collect(Collectors.joining(optional.getSeparator()))).append(separator);
                        }
                    } else {
                        String value = values.get(optional.getField());
                        if (value != null && !value.isEmpty()) {
                            buffer.append(optional.getValue()).append(value).append(separator);
                        }
                    }

                }
                buffer.deleteCharAt(buffer.lastIndexOf(separator));
            }
            return buffer.toString();
        }
        return attribute.getValueList(getFields().split(COMMA)).stream().filter(Objects::nonNull)
                .collect(Collectors.joining(getFormat().getSeparator()));
    }

}
