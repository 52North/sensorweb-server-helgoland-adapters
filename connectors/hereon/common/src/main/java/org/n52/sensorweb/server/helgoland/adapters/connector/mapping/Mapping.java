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
import java.util.LinkedHashSet;
import java.util.Set;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "thing", "sensor", "observedProperty", "feature", "datastream", "observation", "general" })
@SuppressFBWarnings({ "EI_EXPOSE_REP", "EI_EXPOSE_REP2" })
public class Mapping implements Serializable, Entity {
    private static final long serialVersionUID = 6912484994408704525L;
    @JsonProperty("thing")
    @Valid
    private Thing thing;
    @JsonProperty("sensor")
    @Valid
    private Sensor sensor;
    @JsonProperty("observedProperty")
    @Valid
    private ObservedProperty observedProperty;
    @JsonProperty("feature")
    @Valid
    private Feature feature;
    @JsonProperty("datastream")
    @Valid
    private Datastream datastream;
    @JsonProperty("observation")
    @Valid
    private Observation observation;
    @JsonProperty("general")
    @Valid
    private General general;

    @JsonProperty("thing")
    public Thing getThing() {
        return thing;
    }

    @JsonProperty("thing")
    public void setThing(Thing thing) {
        this.thing = thing;
    }

    public Mapping withThing(Thing thing) {
        this.thing = thing;
        return this;
    }

    @JsonProperty("sensor")
    public Sensor getSensor() {
        return sensor;
    }

    @JsonProperty("sensor")
    public void setSensor(Sensor sensor) {
        this.sensor = sensor;
    }

    public Mapping withSensor(Sensor sensor) {
        this.sensor = sensor;
        return this;
    }

    @JsonProperty("observedProperty")
    public ObservedProperty getObservedProperty() {
        return observedProperty;
    }

    @JsonProperty("observedProperty")
    public void setObservedProperty(ObservedProperty observedProperty) {
        this.observedProperty = observedProperty;
    }

    public Mapping withObservedProperty(ObservedProperty observedProperty) {
        this.observedProperty = observedProperty;
        return this;
    }

    @JsonProperty("feature")
    public Feature getFeature() {
        return feature;
    }

    @JsonProperty("feature")
    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    public Mapping withFeature(Feature feature) {
        this.feature = feature;
        return this;
    }

    @JsonProperty("datastream")
    public Datastream getDatastream() {
        return datastream;
    }

    @JsonProperty("datastream")
    public void setDatastream(Datastream datastream) {
        this.datastream = datastream;
    }

    public Mapping withDatastream(Datastream datastream) {
        this.datastream = datastream;
        return this;
    }

    @JsonProperty("general")
    public General getGeneral() {
        return general;
    }

    @JsonProperty("general")
    public void setGeneral(General general) {
        this.general = general;
    }

    public Mapping withGeneral(General general) {
        this.general = general;
        return this;
    }

    @JsonProperty("observation")
    public Observation getObservation() {
        return observation;
    }

    @JsonProperty("observation")
    public void setObservation(Observation observation) {
        this.observation = observation;
    }

    public Mapping withObservation(Observation observation) {
        this.observation = observation;
        return this;
    }

    @Override
    public Set<String> getFields() {
        Set<String> fields = new LinkedHashSet<>();
        fields.addAll(getThing().getFields());
        fields.addAll(getSensor().getFields());
        fields.addAll(getObservedProperty().getFields());
        fields.addAll(getFeature().getFields());
        fields.addAll(getDatastream().getFields());
        fields.addAll(getObservation().getFields());
        fields.add(getGeneral().getMetadataId());
        fields.add(getGeneral().getDataServiceUrl());
        return fields;
    }

    public String getName() {
        return null;
    }

}
