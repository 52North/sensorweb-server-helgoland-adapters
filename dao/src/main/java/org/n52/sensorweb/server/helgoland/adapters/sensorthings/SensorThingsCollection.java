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

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * @author Jan Schulte
 */
public class SensorThingsCollection<T> {

    @SerializedName("@iot.count")
    private int observations;

    @SerializedName("@iot.nextLink")
    private String nextLink;

    private List<T> value;

    /**
     * @return the observations
     */
    public int getObservations() {
        return observations;
    }

    /**
     * @param observations the observations to set
     */
    public void setObservations(int observations) {
        this.observations = observations;
    }

    /**
     * @return the nextLink
     */
    public String getNextLink() {
        return nextLink;
    }

    /**
     * @param nextLink the nextLink to set
     */
    public void setNextLink(String nextLink) {
        this.nextLink = nextLink;
    }

    /**
     * @return the value
     */
    public List<T> getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(List<T> value) {
        this.value = value;
    }

}
