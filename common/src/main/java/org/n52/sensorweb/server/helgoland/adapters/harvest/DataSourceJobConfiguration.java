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
package org.n52.sensorweb.server.helgoland.adapters.harvest;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.n52.bjornoya.schedule.JobConfiguration;
import org.n52.sensorweb.server.helgoland.adapters.config.Credentials;
import org.n52.sensorweb.server.helgoland.adapters.config.DataSourceConfiguration;

public class DataSourceJobConfiguration extends JobConfiguration {

    private String itemName;
    private String url;
    private String version;
    private String connector;
    private String type;
    private Credentials credentials;
    private boolean supportsFirstLast = true;
    private boolean disableHumanReadableName;
    private boolean supportsGDA;
    private Set<String> allowedOfferings = new LinkedHashSet<>();
    private Set<String> allowedSensors = new LinkedHashSet<>();
    private Map<String, String> getUrls = new LinkedHashMap<>();
    private Map<String, String> postUrls = new LinkedHashMap<>();

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

    public Credentials getCredentials() {
        return credentials;
    }

    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }

    public boolean isSetCredentials() {
        return getCredentials() != null && getCredentials().isSetCredentials();
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

    public Set<String> getAllowedOfferings() {
        return Collections.unmodifiableSet(allowedOfferings);
    }

    public void setAllowedOfferings(Collection<String> allowedOfferings) {
        this.allowedOfferings.clear();
        if (allowedOfferings != null) {
            this.allowedOfferings.addAll(allowedOfferings);
        }
    }

    public Set<String> getAllowedSensors() {
        return Collections.unmodifiableSet(allowedSensors);
    }

    public void setAllowedSensors(Collection<String> allowedSensors) {
        this.allowedSensors.clear();
        if (allowedSensors != null) {
            this.allowedSensors.addAll(allowedSensors);
        }
    }

    @Override
    public String toString() {
        return "DataSourceJobConfiguration{" + "itemName=" + itemName + ", url=" + url + ", version=" + version
                + ", connector=" + connector + ", type=" + type + "}";
    }

    public void setGetUrls(Map<String, String> getUrls) {
        this.getUrls.clear();
        if (getUrls != null) {
            this.getUrls.putAll(getUrls);
        }
    }

    public void addGetUrls(String key, String value) {
        getUrls.put(key, value);
    }

    public Map<String, String> getGetUrls() {
        return Collections.unmodifiableMap(getUrls);
    }

    public void setPostUrls(Map<String, String> postUrls) {
        this.postUrls.clear();
        if (postUrls != null) {
            this.postUrls.putAll(postUrls);
        }
    }

    public void addPostUrls(String key, String value) {
        postUrls.put(key, value);
    }

    public Map<String, String> getPostUrls() {
        return Collections.unmodifiableMap(postUrls);
    }

    public static DataSourceJobConfiguration of(DataSourceConfiguration config, JobConfiguration job) {
        DataSourceJobConfiguration dataSourceJobConfiguration = new DataSourceJobConfiguration();
        dataSourceJobConfiguration.setItemName(config.getItemName());
        dataSourceJobConfiguration.setUrl(config.getUrl());
        dataSourceJobConfiguration.setVersion(config.getVersion());
        dataSourceJobConfiguration.setConnector(config.getConnector());
        dataSourceJobConfiguration.setType(config.getType());
        if (config.isSetCredentials()) {
            dataSourceJobConfiguration.setCredentials(config.getCredentials());
        }
        dataSourceJobConfiguration.setSupportsFirstLast(config.isSupportsFirstLast());
        dataSourceJobConfiguration.setDisableHumanReadableName(config.isDisableHumanReadableName());
        dataSourceJobConfiguration.setSupportsGDA(config.isSupportsGDA());
        dataSourceJobConfiguration.setAllowedOfferings(config.getAllowedOfferings());
        dataSourceJobConfiguration.setAllowedSensors(config.getAllowedSensors());
        dataSourceJobConfiguration.setGetUrls(config.getGetUrls());
        dataSourceJobConfiguration.setPostUrls(config.getPostUrls());
        dataSourceJobConfiguration.setCronExpression(job.getCronExpression());
        dataSourceJobConfiguration.setEnabled(job.isEnabled());
        dataSourceJobConfiguration.setTriggerAtStartup(job.isTriggerAtStartup());
        dataSourceJobConfiguration.setModified(job.isModified());
        dataSourceJobConfiguration.setJobType(job.getJobType());
        dataSourceJobConfiguration.setName(config.getItemName() + " - " + job.getJobType());
        dataSourceJobConfiguration.setGroup(job.getGroup());
        return dataSourceJobConfiguration;
    }

}
