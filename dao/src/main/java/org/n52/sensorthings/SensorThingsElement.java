/*
 * Copyright (C) 2015-2020 52°North Initiative for Geospatial Open Source
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
package org.n52.sensorthings;

import com.google.gson.annotations.SerializedName;

/**
 * @author Jan Schulte
 */
public class SensorThingsElement {

    @SerializedName("@iot.id")
    private int iotID;

    @SerializedName("@iot.selfLink")
    private String selfLink;

    private String description;
    private String name;

    /**
     * @return the iotID
     */
    public int getIotID() {
        return iotID;
    }

    /**
     * @param iotID the iotID to set
     */
    public void setIotID(int iotID) {
        this.iotID = iotID;
    }

    /**
     * @return the selfLink
     */
    public String getSelfLink() {
        return selfLink;
    }

    /**
     * @param selfLink the selfLink to set
     */
    public void setSelfLink(String selfLink) {
        this.selfLink = selfLink;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

}
