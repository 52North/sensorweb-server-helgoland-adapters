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

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.n52.sensorweb.server.db.assembler.value.ValueConnector;
import org.n52.sensorweb.server.db.old.dao.DbQuery;
import org.n52.sensorweb.server.helgoland.adapters.config.DataSourceConfiguration;
import org.n52.sensorweb.server.helgoland.adapters.connector.utils.ServiceConstellation;
import org.n52.sensorweb.server.helgoland.adapters.connector.utils.ServiceMetadata;
import org.n52.sensorweb.server.helgoland.adapters.utils.EntityBuilder;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.OfferingEntity;
import org.n52.series.db.beans.ServiceEntity;
import org.n52.series.db.beans.ServiceMetadataEntity;
import org.n52.shetland.ogc.filter.FilterConstants.SpatialOperator;
import org.n52.shetland.ogc.filter.FilterConstants.TimeOperator;
import org.n52.shetland.ogc.filter.SpatialFilter;
import org.n52.shetland.ogc.filter.TemporalFilter;
import org.n52.shetland.ogc.gml.AbstractFeature;
import org.n52.shetland.ogc.gml.CodeType;
import org.n52.shetland.ogc.gml.ReferenceType;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.shetland.ogc.gml.time.TimeInstant;
import org.n52.shetland.ogc.gml.time.TimePeriod;
import org.n52.shetland.ogc.om.OmConstants;
import org.n52.shetland.ogc.om.features.FeatureCollection;
import org.n52.shetland.ogc.om.features.samplingFeatures.AbstractSamplingFeature;
import org.n52.shetland.ogc.sos.ExtendedIndeterminateTime;
import org.n52.shetland.ogc.sos.Sos2Constants;
import org.n52.shetland.ogc.sos.SosObservationOffering;
import org.n52.shetland.ogc.sos.gda.GetDataAvailabilityResponse.DataAvailability;
import org.n52.shetland.util.ReferencedEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jan Schulte
 */
public abstract class AbstractServiceConnector extends AbstractConnector implements ValueConnector, EntityBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractServiceConnector.class);

    public AbstractServiceConnector() {
        super();
    }

    protected void addService(DataSourceConfiguration config, ServiceConstellation serviceConstellation,
            ServiceMetadata serviceMetadata) {
        ServiceMetadataEntity serviceMetadataEntity = null;
        if (serviceMetadata != null) {
            serviceMetadataEntity = new ServiceMetadataEntity().setMetadata(serviceMetadata.getMetadata())
                    .setFormat(serviceMetadata.getFormat());
        }
        ServiceEntity service =
                createService(config.getItemName(), "here goes description", config.getConnector(),
                        config.getUrl(), config.getVersion(), config.isSupportsFirstLast(), serviceMetadataEntity);
        serviceConstellation.setService(service);
    }

    protected String addOffering(SosObservationOffering offering, ServiceConstellation serviceConstellation) {
        String offeringId = offering.getIdentifier();
        CodeType name = offering.getFirstName();
        if (name != null) {
            addMetadata(serviceConstellation.putOffering(offeringId, name.getValue()), offering);
        } else {
            addMetadata(serviceConstellation.putOffering(offeringId, offeringId), offering);
        }
        return offeringId;
    }

    protected String addOffering(ReferenceType offering, ServiceConstellation serviceConstellation) {
        String offeringId = offering.getHref();
        String offeringName = offering.getTitle();
        serviceConstellation.putOffering(offeringId, offeringName);
        return offeringId;
    }

    protected String addOffering(String offeringId, String offeringName, ServiceConstellation serviceConstellation) {
        serviceConstellation.putOffering(offeringId, offeringName);
        return offeringId;
    }

    protected OfferingEntity addMetadata(OfferingEntity entity, SosObservationOffering obsOff) {
        if (obsOff.isSetObservedArea()) {
            entity.setGeometry(obsOff.getObservedArea().toGeometry());
        }
        if (obsOff.isSetPhenomenonTime()) {
            Time time = obsOff.getPhenomenonTime();
            DateTime start = null;
            DateTime end = null;
            if (time instanceof TimePeriod) {
                start = ((TimePeriod) time).getStart();
                end = ((TimePeriod) time).getEnd();
            } else if (time instanceof TimeInstant) {
                start = ((TimeInstant) time).getValue();
                end = start;
            }
            entity.setSamplingTimeStart(start != null ? start.toDate() : null);
            entity.setSamplingTimeEnd(end != null ? end.toDate() : null);
        }
        if (obsOff.isSetResultTime()) {
            Time time = obsOff.getResultTime();
            DateTime start = null;
            DateTime end = null;
            if (time instanceof TimePeriod) {
                start = ((TimePeriod) time).getStart();
                end = ((TimePeriod) time).getEnd();
            } else if (time instanceof TimeInstant) {
                start = ((TimeInstant) time).getValue();
                end = start;
            }
            entity.setResultTimeStart(start != null ? start.toDate() : null);
            entity.setSamplingTimeEnd(end != null ? end.toDate() : null);
        }
        return entity;
    }

    protected String addProcedure(String procedureId, boolean insitu, boolean mobile,
            ServiceConstellation serviceConstellation) {
        serviceConstellation.putProcedure(procedureId, procedureId, insitu, mobile);
        return procedureId;
    }

    protected String addProcedure(DataAvailability dataAval, boolean insitu, boolean mobile,
            ServiceConstellation serviceConstellation) {
        String procedureId = dataAval.getProcedure().getHref();
        String procedureName = dataAval.getProcedure().getTitle();
        serviceConstellation.putProcedure(procedureId, procedureName, insitu, mobile);
        return procedureId;
    }

    protected String addProcedure(String procedureId, String procedureName, boolean insitu, boolean mobile,
            ServiceConstellation serviceConstellation) {
        serviceConstellation.putProcedure(procedureId, procedureName, insitu, mobile);
        return procedureId;
    }

    protected String addPhenomenon(String phenomenonId, ServiceConstellation serviceConstellation) {
        serviceConstellation.putPhenomenon(phenomenonId, phenomenonId);
        return phenomenonId;
    }

    protected String addPhenomenon(String phenomenonId, String phenomenonName,
            ServiceConstellation serviceConstellation) {
        serviceConstellation.putPhenomenon(phenomenonId, phenomenonName);
        return phenomenonId;
    }

    protected String addPhenomenon(String phenomenonId, String phenomenonName, String description,
            ServiceConstellation serviceConstellation) {
        serviceConstellation.putPhenomenon(phenomenonId, phenomenonName, description);
        return phenomenonId;
    }

    protected String addPhenomenon(DataAvailability dataAval, ServiceConstellation serviceConstellation) {
        String phenomenonId = dataAval.getObservedProperty().getHref();
        String phenomenonName = dataAval.getObservedProperty().getTitle();
        serviceConstellation.putPhenomenon(phenomenonId, phenomenonName);
        return phenomenonId;
    }

    protected String addCategory(String categoryId, ServiceConstellation serviceConstellation) {
        serviceConstellation.putCategory(categoryId, categoryId);
        return categoryId;
    }

    protected String addCategory(DataAvailability dataAval, ServiceConstellation serviceConstellation) {
        String categoryId = dataAval.getObservedProperty().getHref();
        String categoryName = dataAval.getObservedProperty().getTitle();
        serviceConstellation.putCategory(categoryId, categoryName);
        return categoryId;
    }

    protected String addCategory(String categoryId, String categoryName, ServiceConstellation serviceConstellation) {
        serviceConstellation.putCategory(categoryId, categoryName);
        return categoryId;
    }

    protected String addPlatform(String id, String name, ServiceConstellation serviceConstellation) {
        serviceConstellation.putPlatform(id, name);
        return id;
    }

    protected String addPlatform(String id, String name, String description,
            ServiceConstellation serviceConstellation) {
        serviceConstellation.putPlatform(id, name, description);
        return id;
    }

    protected void addFeature(AbstractFeature feature, ServiceConstellation serviceConstellation) {
        if (feature instanceof AbstractSamplingFeature) {
            addFeature((AbstractSamplingFeature) feature, serviceConstellation);
        } else if (feature instanceof FeatureCollection) {
            ((FeatureCollection) feature).forEach(featureEntry -> addFeature(featureEntry, serviceConstellation));
        }
    }

    protected String addFeature(AbstractSamplingFeature samplingfeature, ServiceConstellation serviceConstellation) {
        String featureId = samplingfeature.getIdentifier();
        if (!serviceConstellation.containsFeature(featureId)) {
            String featureDescription = samplingfeature.getDescription();
            String featureName =
                    samplingfeature.getFirstName() != null ? samplingfeature.getFirstName().getValue() : featureId;
            if (samplingfeature.getGeometry() != null) {
                serviceConstellation.putFeature(featureId, featureName, featureDescription,
                        samplingfeature.getGeometry());
            } else {
                LOGGER.warn("No geometry found");
            }
            serviceConstellation.putPlatform(featureId, featureName, featureDescription);
        }
        return featureId;
    }

    protected TemporalFilter createFirstTimefilter() {
        return new TemporalFilter(TimeOperator.TM_Equals, new TimeInstant(ExtendedIndeterminateTime.FIRST),
                OmConstants.PHENOMENON_TIME_NAME);
    }

    protected TemporalFilter createFirstTimefilter(DatasetEntity dataset) {
        return dataset.getService().getSupportsFirstLast() ? createFirstTimefilter()
                : createTimeFilter(new DateTime(dataset.getFirstValueAt()));
    }

    protected TemporalFilter createFirstTimefilter(boolean supportsFirstLast, DateTime lastTimestamp) {
        return supportsFirstLast ? createFirstTimefilter() : createTimeFilter(lastTimestamp);
    }

    protected TemporalFilter createLatestTimefilter() {
        return new TemporalFilter(TimeOperator.TM_Equals, new TimeInstant(ExtendedIndeterminateTime.LATEST),
                OmConstants.PHENOMENON_TIME_NAME);
    }

    protected TemporalFilter createLatestTimefilter(DatasetEntity dataset) {
        return dataset.getService().getSupportsFirstLast() ? createLatestTimefilter()
                : createTimeFilter(new DateTime(dataset.getLastValueAt()));
    }

    protected TemporalFilter createTimeFilter(DbQuery query) {
        return createTimeFilter(query.getTimespan());
    }

    protected TemporalFilter createTimeFilter(DateTime dateTime) {
        if (dateTime == null) {
            return null;
        } else {
            TimeInstant instant = new TimeInstant(dateTime);
            TimeOperator operator = TimeOperator.TM_Equals;
            String valueReference = OmConstants.PHENOMENON_TIME_NAME;
            return new TemporalFilter(operator, instant, valueReference);
        }
    }

    protected TemporalFilter createTimeFilter(Interval timespan) {
        if (timespan == null) {
            return null;
        } else if (timespan.toDurationMillis() == 0) {
            return createTimeFilter(timespan.getStart());
        } else {
            String valueReference = OmConstants.PHENOMENON_TIME_NAME;
            TimeOperator operator = TimeOperator.TM_During;
            TimePeriod period = new TimePeriod(timespan);
            return new TemporalFilter(operator, period, valueReference);
        }
    }

    protected SpatialFilter createSpatialFilter(DbQuery query) {
        return createSpatialFilter(query.getSpatialFilter());
    }

    protected SpatialFilter createSpatialFilter(Geometry geometry) {
        if (geometry != null) {
            return geometry.getSRID() > 0 ? createSpatialFilter(geometry.getEnvelopeInternal())
                    : createSpatialFilter(geometry.getEnvelopeInternal(), geometry.getSRID());
        }
        return null;
    }

    protected SpatialFilter createSpatialFilter(Envelope envelope) {
        return createSpatialFilter(envelope, 4326);
    }

    protected SpatialFilter createSpatialFilter(Envelope envelope, int srid) {
        if (envelope == null) {
            return null;
        } else {
            String valueReference = Sos2Constants.VALUE_REFERENCE_SPATIAL_FILTERING_PROFILE;
            return new SpatialFilter(SpatialOperator.BBOX, new ReferencedEnvelope(envelope, srid), valueReference);
        }
    }

}
