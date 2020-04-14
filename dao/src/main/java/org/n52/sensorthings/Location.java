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
public class Location extends SensorThingsElement {

    private String encodingType;

    private Point location;

    @SerializedName("Things@iot.navigationLink")
    private String thingsLink;

    @SerializedName("HistoricalLocations@iot.navigationLink")
    private String historicalLocationsLink;

    /**
     * @return the encodingType
     */
    public String getEncodingType() {
        return encodingType;
    }

    /**
     * @param encodingType the encodingType to set
     */
    public void setEncodingType(String encodingType) {
        this.encodingType = encodingType;
    }

    /**
     * @return the location
     */
    public Point getLocation() {
        return location;
    }

    /**
     * @param location the location to set
     */
    public void setLocation(Point location) {
        this.location = location;
    }

    /**
     * @return the thingsLink
     */
    public String getThingsLink() {
        return thingsLink;
    }

    /**
     * @param thingsLink the thingsLink to set
     */
    public void setThingsLink(String thingsLink) {
        this.thingsLink = thingsLink;
    }

    /**
     * @return the historicalLocationsLink
     */
    public String getHistoricalLocationsLink() {
        return historicalLocationsLink;
    }

    /**
     * @param historicalLocationsLink the historicalLocationsLink to set
     */
    public void setHistoricalLocationsLink(String historicalLocationsLink) {
        this.historicalLocationsLink = historicalLocationsLink;
    }

}
