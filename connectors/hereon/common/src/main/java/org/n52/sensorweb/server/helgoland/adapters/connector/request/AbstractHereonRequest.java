/*
 * Copyright (C) 2015-2023 52°North Spatial Information Research GmbH
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
package org.n52.sensorweb.server.helgoland.adapters.connector.request;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.n52.sensorweb.server.helgoland.adapters.connector.HereonConstants;
import org.n52.sensorweb.server.helgoland.adapters.web.request.AbstractGetRequest;
import org.n52.shetland.arcgis.service.feature.FeatureServiceConstants;
import org.n52.shetland.util.CollectionHelper;

public abstract class AbstractHereonRequest extends AbstractGetRequest
        implements HereonConstants, FeatureServiceConstants.WhereOperators {

    private String where;
    private boolean distinctValues;
    private boolean returnGeometry = true;
    private String format = Formats.JSON;
    private Set<String> outFields = new LinkedHashSet<>();
    private Long resultOffset;
    private Long resultRecordCount;

    @Override
    public Map<String, String> getQueryParameters() {
        Map<String, String> map = createMap();
        addWhere(map);
        addFormat(map);
        addDistinctValues(map);
        addReturnGeometry(map);
        addOutFields(map);
        addQueryParameters(map);
        addResultOffset(map);
        addResultRecordCount(map);
        return map;
    }

    @Override
    public String getPath() {
        return Parameter.QUERY;
    }

    /*
     * ( '<=' | '>=' | '<' | '>' | '=' | '!=' | '<>' | LIKE ) (AND | OR) (IS |
     * IS_NOT) (IN | NOT_IN) ( '(' ( expr ( ',' expr )* )? ')' ) COLUMN_NAME
     * BETWEEN LITERAL_VALUE AND LITERAL_VALUE
     */
    public AbstractHereonRequest withWhere(String where) {
        this.where = where;
        return this;
    }

    public AbstractHereonRequest withDistinctValues(boolean distinctValues) {
        this.distinctValues = distinctValues;
        return this;
    }

    public AbstractHereonRequest withGeometry(boolean returnGeometry) {
        this.returnGeometry = returnGeometry;
        return this;
    }

    public AbstractHereonRequest withOutField(String value) {
        if (value != null && !value.isEmpty()) {
            this.outFields.add(value);
        }
        return this;
    }

    public AbstractHereonRequest withOutFields(String... values) {
        if (values != null) {
            withOutFields(CollectionHelper.set(values));
        }
        return this;
    }

    public AbstractHereonRequest withOutFields(Collection<String> values) {
        if (values != null && !values.isEmpty()) {
            values.forEach(this::withOutField);
        }
        return this;
    }

    public AbstractHereonRequest withFormat(String format) {
        if (format != null && !format.isEmpty()) {
            this.format = format;
        }
        return this;
    }

    public AbstractHereonRequest withResultOffset(Long resultOffset) {
        this.resultOffset = resultOffset;
        return this;
    }

    public AbstractHereonRequest withResultRecordCount(Long resultRecordCount) {
        this.resultRecordCount = resultRecordCount;
        return this;
    }

    protected void addQueryParameters(Map<String, String> map) {

    }

    private void addFormat(Map<String, String> map) {
        map.put(Parameter.FORMAT, format);
    }

    private void addWhere(Map<String, String> map) {
        map.put(Parameter.WHERE, hasWhere() ? getWhere() : Values.DEFAULT_WHERE_VALUE);
    }

    private void addDistinctValues(Map<String, String> map) {
        if (distinctValues) {
            map.put(Parameter.RETURN_DISTINCT_VALUES, Boolean.toString(distinctValues));
        }
    }

    private void addReturnGeometry(Map<String, String> map) {
        if (returnGeometry) {
            map.put(Parameter.RETURN_GEOMETRY, Boolean.toString(returnGeometry));
        }
    }

    private void addResultOffset(Map<String, String> map) {
        if (hasResultOffset()) {
            map.put(Parameter.RESULT_OFFSET, getResultOffset().toString());
        }
    }

    private boolean hasResultOffset() {
        return getResultOffset() != null && getResultOffset() > 0;
    }

    private Long getResultOffset() {
        return resultOffset;
    }

    private void addResultRecordCount(Map<String, String> map) {
        if (hasResultRecordCount()) {
            map.put(Parameter.RESULT_RECORD_COUNT, getResultRecordCount().toString());
        }
    }

    private boolean hasResultRecordCount() {
        return getResultRecordCount() != null && getResultRecordCount() > 0;
    }

    private Long getResultRecordCount() {
        return resultRecordCount;
    }

    private void addOutFields(Map<String, String> map) {
        if (hasOutFields()) {
            map.put(Parameter.OUT_FIELDS, String.join(",", getOutFields()));
        }
    }

    private boolean hasOutFields() {
        return !getOutFields().isEmpty();
    }

    private Set<String> getOutFields() {
        return outFields;
    }

    private String getWhere() {
        return where;
    }

    private boolean hasWhere() {
        return getWhere() != null && !getWhere().isEmpty();
    }

}
