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

import java.math.BigDecimal;
import java.util.Date;

import com.google.gson.annotations.SerializedName;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * @author Jan Schulte
 */
public class Observation extends SensorThingsElement {

    private Date phenomenonTime;

    private BigDecimal result;

    private String resultTime;

    @SerializedName("Datastream@iot.navigationLink")
    private String datastreamLink;

    @SerializedName("FeatureOfInterest@iot.navigationLink")
    private String featureOfInterestLink;

    /**
     * @return the phenomenonTime
     */
    @SuppressFBWarnings("EI_EXPOSE_REP")
    public Date getPhenomenonTime() {
        return phenomenonTime;
    }

    /**
     * @param phenomenonTime the phenomenonTime to set
     */
    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public void setPhenomenonTime(Date phenomenonTime) {
        this.phenomenonTime = phenomenonTime;
    }

    /**
     * @return the result
     */
    public BigDecimal getResult() {
        return result;
    }

    /**
     * @param result the result to set
     */
    public void setResult(BigDecimal result) {
        this.result = result;
    }

    /**
     * @return the resultTime
     */
    public String getResultTime() {
        return resultTime;
    }

    /**
     * @param resultTime the resultTime to set
     */
    public void setResultTime(String resultTime) {
        this.resultTime = resultTime;
    }

    /**
     * @return the datastreamLink
     */
    public String getDatastreamLink() {
        return datastreamLink;
    }

    /**
     * @param datastreamLink the datastreamLink to set
     */
    public void setDatastreamLink(String datastreamLink) {
        this.datastreamLink = datastreamLink;
    }

    /**
     * @return the featureOfInterestLink
     */
    public String getFeatureOfInterestLink() {
        return featureOfInterestLink;
    }

    /**
     * @param featureOfInterestLink the featureOfInterestLink to set
     */
    public void setFeatureOfInterestLink(String featureOfInterestLink) {
        this.featureOfInterestLink = featureOfInterestLink;
    }

}
