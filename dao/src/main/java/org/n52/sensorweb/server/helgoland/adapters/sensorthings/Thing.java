/*
 * Copyright (C) 2015-2021 52°North Spatial Information Research GmbH
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
public class Thing extends SensorThingsElement {

    private ThingProperties properties;

    @SerializedName("Datastreams@iot.navigationLink")
    private String datastreamsLink;

    @SerializedName("HistoricalLocations@iot.navigationLink")
    private String historicalLocationsLink;

    @SerializedName("Locations@iot.navigationLink")
    private String locationsLink;


    /**
     * @return the properties
     */
    public ThingProperties getProperties() {
        return properties;
    }

    /**
     * @param properties the properties to set
     */
    public void setProperties(ThingProperties properties) {
        this.properties = properties;
    }

    /**
     * @return the datastreamsLink
     */
    public String getDatastreamsLink() {
        return datastreamsLink;
    }

    /**
     * @param datastreamsLink the datastreamsLink to set
     */
    public void setDatastreamsLink(String datastreamsLink) {
        this.datastreamsLink = datastreamsLink;
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

    /**
     * @return the locationsLink
     */
    public String getLocationsLink() {
        return locationsLink;
    }

    /**
     * @param locationsLink the locationsLink to set
     */
    public void setLocationsLink(String locationsLink) {
        this.locationsLink = locationsLink;
    }
}
