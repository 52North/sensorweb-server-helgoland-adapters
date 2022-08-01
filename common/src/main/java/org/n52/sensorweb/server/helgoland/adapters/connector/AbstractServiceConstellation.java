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
package org.n52.sensorweb.server.helgoland.adapters.connector;

import org.n52.janmayen.event.Event;
import org.n52.sensorweb.server.helgoland.adapters.harvest.HarvestContext;
import org.n52.sensorweb.server.helgoland.adapters.utils.EntityBuilder;
import org.n52.series.db.beans.ServiceEntity;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionException;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings({ "EI_EXPOSE_REP", "EI_EXPOSE_REP" })
public abstract class AbstractServiceConstellation implements EntityBuilder {

    private final String temporalHarvester;
    private final String fullHarvester;
    // service
    private ServiceEntity service;

    public AbstractServiceConstellation(String fullHarvester, String temporalHarvester) {
        this.fullHarvester = fullHarvester;
        this.temporalHarvester = temporalHarvester;
    }

    public void setService(ServiceEntity service) {
        this.service = service;
    }

    public ServiceEntity getService() {
        return service;
    }

    public String getTemporalHarvester() {
        return temporalHarvester;
    }

    public String getFullHarvester() {
        return fullHarvester;
    }

    public Event getEvent() {
        return null;
    }

    public HarvestContext getHavesterContext() throws JobExecutionException {
        return new HarvestContext(this);
    }

    public HarvestContext getHavesterContext(JobDataMap jobDataMap) throws JobExecutionException {
        return new HarvestContext(this, jobDataMap);
    }

}