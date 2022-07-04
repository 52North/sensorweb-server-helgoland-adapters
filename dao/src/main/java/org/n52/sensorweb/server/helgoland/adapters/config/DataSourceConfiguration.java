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
package org.n52.sensorweb.server.helgoland.adapters.config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings({"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
public class DataSourceConfiguration {

    private String itemName;
    private String url;
    private String version;
    private String connector;
    private String type;
    private boolean supportsFirstLast = true;
    private boolean disableHumanReadableName;
    private boolean supportsGDA;
    private List<String> allowedOfferings;
    private List<String> allowedSensors;
    private Map<String, String> getUrls = new LinkedHashMap<>();
    private Map<String, String> postUrls = new LinkedHashMap<>();

    private DataSourceJobConfiguration job;

    public DataSourceJobConfiguration getJob() {
        return job;
    }

    public void setJob(DataSourceJobConfiguration job) {
        this.job = job;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getConnector() {
        return connector;
    }

    public void setConnector(String connector) {
        this.connector = connector;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isSupportsFirstLast() {
        return supportsFirstLast;
    }

    public void setSupportsFirstLast(boolean supportsFirstLast) {
        this.supportsFirstLast = supportsFirstLast;
    }

    public boolean isSupportsGDA() {
        return supportsGDA;
    }

    public void setSupportsGDA(boolean supportsGDA) {
        this.supportsGDA = supportsGDA;
    }

    public boolean isDisableHumanReadableName() {
        return disableHumanReadableName;
    }

    public void setDisableHumanReadableName(boolean disableHumanReadableName) {
        this.disableHumanReadableName = disableHumanReadableName;
    }

    public List<String> getAllowedOfferings() {
        return allowedOfferings;
    }

    public void setAllowedOfferings(List<String> allowedOfferings) {
        this.allowedOfferings = allowedOfferings;
    }

    public List<String> getAllowedSensors() {
        return allowedSensors;
    }

    public void setAllowedSensors(List<String> allowedSensors) {
        this.allowedSensors = allowedSensors;
    }

    @Override
    public String toString() {
        return "DataSourceConfiguration{" + "itemName=" + itemName + ", url=" + url
            + ", version=" + version + ", connector=" + connector + ", type=" + type + "}";
    }

    public void addGetUrls(String key, String value) {
        getUrls.put(key, value);
    }

    public Map<String, String> getGetUrls() {
        return getUrls;
    }

    public void addPostUrls(String key, String value) {
        postUrls.put(key, value);
    }

    public Map<String, String> getPostUrls() {
        return postUrls;
    }

}
