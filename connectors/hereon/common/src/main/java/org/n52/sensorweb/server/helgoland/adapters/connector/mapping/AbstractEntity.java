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
package org.n52.sensorweb.server.helgoland.adapters.connector.mapping;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.validation.Valid;

import org.n52.sensorweb.server.helgoland.adapters.connector.response.Attributes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "description", "properties", "formats" })
public abstract class AbstractEntity implements Serializable, Entity {
    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";
    private static final long serialVersionUID = -4168578691389102123L;
    @JsonProperty("name")
    private String name;
    @JsonProperty("description")
    private String description;
    @JsonProperty("properties")
    @Valid
    private List<String> properties = new LinkedList<>();
    @JsonProperty("formats")
    @Valid
    private List<Format> formats = new LinkedList<>();

    @JsonIgnore
    public String getIdentifier() {
        return getName();
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    public AbstractEntity withName(String name) {
        this.name = name;
        return this;
    }

    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    public AbstractEntity withDescription(String description) {
        this.description = description;
        return this;
    }

    @JsonProperty("properties")
    public List<String> getProperties() {
        return Collections.unmodifiableList(properties);
    }

    @JsonProperty("properties")
    public void setProperties(List<String> properties) {
        this.properties.clear();
        if (properties != null) {
            this.properties.addAll(properties);
        }
    }

    public AbstractEntity withProperties(List<String> properties) {
        if (properties != null) {
            this.properties.addAll(properties);
        }
        return this;
    }

    public boolean isSetProperties() {
        return !getProperties().isEmpty();
    }

    @JsonProperty("formats")
    public List<Format> getFormats() {
        return Collections.unmodifiableList(formats);
    }

    @JsonProperty("formats")
    public void setFormats(List<Format> formats) {
        this.formats.clear();
        if (formats != null) {
            this.formats.addAll(formats);
        }
    }

    public AbstractEntity withFormats(List<Format> formats) {
        if (formats != null) {
            this.formats.addAll(formats);
        }
        return this;
    }

    @JsonIgnore
    public boolean isSetFormats() {
        return !getFormats().isEmpty();
    }

    @JsonIgnore
    public Format getNameFormat() {
        return hasNameFormat() ? getFormat(NAME).get() : null;
    }

    @JsonIgnore
    public Format getDescriptionFormat() {
        return hasDescriptionFormat() ? getFormat(DESCRIPTION).get() : null;
    }

    @JsonIgnore
    public boolean hasNameFormat() {
        return hasFormat(NAME);
    }

    @JsonIgnore
    public boolean hasDescriptionFormat() {
        return hasFormat(DESCRIPTION);
    }

    @JsonIgnore
    public boolean hasFormat(String string) {
        return isSetFormats() && getFormat(string).isPresent();
    }

    @JsonIgnore
    private Optional<Format> getFormat(String string) {
        return getFormats().stream().filter(f -> f.getName().equals(string)).findFirst();
    }

    public Set<String> getFields() {
        Set<String> fields = new LinkedHashSet<>();
        add(fields, getName());
        add(fields, getDescription());
        getProperties().forEach(p -> add(fields, p));
        getFormats().forEach(f -> add(fields, f.getFields()));
        return fields;
    }

    protected Set<String> add(Set<String> fields, String field) {
        if (field != null && !field.isEmpty()) {
            fields.add(field);
        }
        return fields;
    }

    public boolean checkForRequiredFields(Attributes attribute) {
        return attribute.hasValue(getName());
    }
}
