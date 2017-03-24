/*
 * Copyright (C) 2013-2017 52°North Initiative for Geospatial Open Source
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
package org.n52.proxy.db.dao;

import org.hibernate.Criteria;
import static org.hibernate.criterion.Restrictions.eq;
import static org.hibernate.criterion.Restrictions.in;
import org.n52.io.request.IoParameters;
import org.n52.series.db.dao.DbQuery;
import org.slf4j.Logger;
import static org.slf4j.LoggerFactory.getLogger;

public class ProxyDbQuery extends DbQuery {

    private static final Logger LOGGER = getLogger(ProxyDbQuery.class);

    private static final String SERVICE_PKID = "service.pkid";

    private String serviceId;

    public ProxyDbQuery(IoParameters parameters) {
        super(parameters);
    }

    public static ProxyDbQuery createFrom(IoParameters parameters) {
        return new ProxyDbQuery(parameters);
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    @Override
    public Criteria addFilters(Criteria criteria, String seriesProperty) {
        super.addFilters(criteria, seriesProperty);
        return addServiceFilter(seriesProperty, criteria);
    }

    public Criteria addServiceFilter(String parameter, Criteria criteria) {
        if (serviceId != null && !serviceId.isEmpty()) {
            criteria.add(eq(SERVICE_PKID, parseToId(serviceId)));
        } else if (getParameters().getService() != null) {
            criteria.add(eq(SERVICE_PKID, parseToId(getParameters().getService())));
        } else if (getParameters().getServices() != null && !getParameters().getServices().isEmpty()) {
            criteria.add(in(SERVICE_PKID, parseToIds(getParameters().getServices())));
        }
        return criteria;
    }

}
