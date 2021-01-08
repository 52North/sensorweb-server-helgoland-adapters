/*
 * Copyright (C) 2015-2021 52°North Initiative for Geospatial Open Source
 * Software GmbH
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
package org.n52.sensorweb.server.helgoland.adapters.sensorthings;

import com.google.gson.annotations.SerializedName;

/**
 * @author Jan Schulte
 */
public class Datastream extends SensorThingsElement {

    @SerializedName("unitOfMeasurement")
    private UnitOfMeasurement unitOfMeasurement;

    @SerializedName("observationType")
    private String observationType;

    @SerializedName("Observations@iot.navigationLink")
    private String observationsLink;

    @SerializedName("ObservedProperty@iot.navigationLink")
    private String observedPropertyLink;

    @SerializedName("Sensor@iot.navigationLink")
    private String sensorLink;

    @SerializedName("Thing@iot.navigationLink")
    private String thingLink;

    @SerializedName("Sensor")
    private Sensor sensor;

    @SerializedName("Thing")
    private Thing thing;

    @SerializedName("ObservedProperty")
    private ObservedProperty observedProperty;

    /**
     * @return the unitOfMeasurement
     */
    public UnitOfMeasurement getUnitOfMeasurement() {
        return unitOfMeasurement;
    }

    /**
     * @param unitOfMeasurement the unitOfMeasurement to set
     */
    public void setUnitOfMeasurement(UnitOfMeasurement unitOfMeasurement) {
        this.unitOfMeasurement = unitOfMeasurement;
    }

    /**
     * @return the observationType
     */
    public String getObservationType() {
        return observationType;
    }

    /**
     * @param observationType the observationType to set
     */
    public void setObservationType(String observationType) {
        this.observationType = observationType;
    }

    /**
     * @return the observationsLink
     */
    public String getObservationsLink() {
        return observationsLink;
    }

    /**
     * @param observationsLink the observationsLink to set
     */
    public void setObservationsLink(String observationsLink) {
        this.observationsLink = observationsLink;
    }

    /**
     * @return the observedPropertyLink
     */
    public String getObservedPropertyLink() {
        return observedPropertyLink;
    }

    /**
     * @param observedPropertyLink the observedPropertyLink to set
     */
    public void setObservedPropertyLink(String observedPropertyLink) {
        this.observedPropertyLink = observedPropertyLink;
    }

    /**
     * @return the sensorLink
     */
    public String getSensorLink() {
        return sensorLink;
    }

    /**
     * @param sensorLink the sensorLink to set
     */
    public void setSensorLink(String sensorLink) {
        this.sensorLink = sensorLink;
    }

    /**
     * @return the thingLink
     */
    public String getThingLink() {
        return thingLink;
    }

    /**
     * @param thingLink the thingLink to set
     */
    public void setThingLink(String thingLink) {
        this.thingLink = thingLink;
    }

    /**
     * @return the sensor
     */
    public Sensor getSensor() {
        return sensor;
    }

    /**
     * @param sensor the sensor to set
     */
    public void setSensor(Sensor sensor) {
        this.sensor = sensor;
    }

    /**
     * @return the thing
     */
    public Thing getThing() {
        return thing;
    }

    /**
     * @param thing the thing to set
     */
    public void setThing(Thing thing) {
        this.thing = thing;
    }

    /**
     * @return the observedProperty
     */
    public ObservedProperty getObservedProperty() {
        return observedProperty;
    }

    /**
     * @param observedProperty the observedProperty to set
     */
    public void setObservedProperty(ObservedProperty observedProperty) {
        this.observedProperty = observedProperty;
    }

}
