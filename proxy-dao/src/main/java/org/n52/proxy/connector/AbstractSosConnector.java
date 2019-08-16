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
package org.n52.proxy.connector;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.apache.http.HttpResponse;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.joda.time.DateTime;
import org.n52.janmayen.http.QueryBuilder;
import org.n52.proxy.config.DataSourceConfiguration;
import org.n52.proxy.connector.constellations.DatasetConstellation;
import org.n52.proxy.connector.constellations.QuantityDatasetConstellation;
import org.n52.proxy.connector.utils.DataEntityBuilder;
import org.n52.proxy.connector.utils.EntityBuilder;
import org.n52.proxy.connector.utils.ServiceConstellation;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.OfferingEntity;
import org.n52.series.db.beans.UnitEntity;
import org.n52.series.db.beans.dataset.ValueType;
import org.n52.shetland.ogc.filter.SpatialFilter;
import org.n52.shetland.ogc.filter.TemporalFilter;
import org.n52.shetland.ogc.om.OmObservation;
import org.n52.shetland.ogc.ows.OWSConstants;
import org.n52.shetland.ogc.ows.OwsCapabilities;
import org.n52.shetland.ogc.ows.OwsOperation;
import org.n52.shetland.ogc.ows.OwsOperationsMetadata;
import org.n52.shetland.ogc.ows.exception.OwsExceptionReport;
import org.n52.shetland.ogc.ows.service.GetCapabilitiesResponse;
import org.n52.shetland.ogc.ows.service.OwsServiceRequest;
import org.n52.shetland.ogc.sos.Sos2Constants;
import org.n52.shetland.ogc.sos.SosConstants;
import org.n52.shetland.ogc.sos.SosObservationOffering;
import org.n52.shetland.ogc.sos.gda.GetDataAvailabilityConstants;
import org.n52.shetland.ogc.sos.gda.GetDataAvailabilityResponse;
import org.n52.shetland.ogc.sos.gda.GetDataAvailabilityResponse.DataAvailability;
import org.n52.shetland.ogc.sos.request.DescribeSensorRequest;
import org.n52.shetland.ogc.sos.request.GetFeatureOfInterestRequest;
import org.n52.shetland.ogc.sos.request.GetObservationRequest;
import org.n52.shetland.ogc.sos.response.DescribeSensorResponse;
import org.n52.shetland.ogc.sos.response.GetFeatureOfInterestResponse;
import org.n52.shetland.ogc.sos.response.GetObservationResponse;
import org.n52.shetland.ogc.swes.SwesConstants;
import org.n52.svalbard.decode.Decoder;
import org.n52.svalbard.decode.DecoderKey;
import org.n52.svalbard.decode.DecoderRepository;
import org.n52.svalbard.decode.exception.DecodingException;
import org.n52.svalbard.decode.exception.NoDecoderForKeyException;
import org.n52.svalbard.encode.Encoder;
import org.n52.svalbard.encode.EncoderKey;
import org.n52.svalbard.encode.EncoderRepository;
import org.n52.svalbard.encode.exception.EncodingException;
import org.n52.svalbard.encode.exception.NoEncoderForKeyException;
import org.n52.svalbard.util.CodingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractSosConnector extends AbstractConnector {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractSosConnector.class);
    private static final String COULD_NOT_RETRIEVE_RESPONSE = "Could not retrieve response";

    protected int counter;

    private DecoderRepository decoderRepository;
    private EncoderRepository encoderRepository;

    public DecoderRepository getDecoderRepository() {
        return decoderRepository;
    }

    @Inject
    public void setDecoderRepository(DecoderRepository decoderRepository) {
        this.decoderRepository = decoderRepository;
    }

    public EncoderRepository getEncoderRepository() {
        return encoderRepository;
    }

    @Inject
    public void setEncoderRepository(EncoderRepository encoderRepository) {
        this.encoderRepository = encoderRepository;
    }

    public boolean matches(DataSourceConfiguration config, GetCapabilitiesResponse capabilities) {
        if (config.getConnector() != null) {
            return getClass().getSimpleName().equals(config.getConnector()) ||
                   getClass().getName().equals(config.getConnector());
        } else {
            return canHandle(config, capabilities);
        }
    }

    public UnitEntity getUom(String procedure, String offering, String phenomenon, String feature,
            boolean supportsFirstLast, DateTime lastTimestamp, String serviceURL) {
        GetObservationResponse response = getObservation(procedure, offering, phenomenon, feature,
                createFirstTimefilter(supportsFirstLast, lastTimestamp), serviceURL);
        return response.getObservationCollection().toStream().findFirst().map(o -> o.getValue().getValue().getUnit())
                .map(unit -> EntityBuilder.createUnit(unit, null)).orElse(null);
    }

    protected Object getSosResponseFor(String uri) {
        try {
            return decodeResponse(sendGetRequest(uri));
        } catch (IOException ex) {
            LOGGER.error(COULD_NOT_RETRIEVE_RESPONSE, ex);
            throw new ConnectorRequestFailedException(ex);
        }
    }

    protected Object getSosResponseFor(URL uri) {
        return getSosResponseFor(uri.toString());
    }

    protected Object getSosResponseFor(OwsServiceRequest request, String namespace, String serviceUrl) {
        counter++;
        try {
            EncoderKey encoderKey = CodingHelper.getEncoderKey(namespace, request);
            Encoder<XmlObject, OwsServiceRequest> encoder = getEncoderRepository().getEncoder(encoderKey);
            if (encoder == null) {
                throw new NoEncoderForKeyException(encoderKey);
            }
            XmlObject xmlRequest = encoder.encode(request);
            return decodeResponse(sendPostRequest(xmlRequest, serviceUrl));
        } catch (IOException ex) {
            LOGGER.error(COULD_NOT_RETRIEVE_RESPONSE, ex);
            throw new ConnectorRequestFailedException(ex);
        } catch (EncodingException ex) {
            LOGGER.error("Could not encode request : " + request, ex);
            throw new ConnectorRequestFailedException(ex);
        }
    }

    protected Object decodeResponse(HttpResponse response) {
        try (InputStream content = response.getEntity().getContent()) {
            XmlObject xmlResponse = XmlObject.Factory.parse(content);
            DecoderKey decoderKey = CodingHelper.getDecoderKey(xmlResponse);
            Decoder<Object, Object> decoder = getDecoderRepository().getDecoder(decoderKey);
            if (decoder == null) {
                throw new NoDecoderForKeyException(decoderKey);
            }
            Object decode = decoder.decode(xmlResponse);
            if (decode instanceof OwsExceptionReport) {
                throw new ConnectorRequestFailedException((OwsExceptionReport) decode);
            }
            return decode;
        } catch (IOException ex) {
            LOGGER.error(COULD_NOT_RETRIEVE_RESPONSE, ex);
            throw new ConnectorRequestFailedException(ex);
        } catch (XmlException ex) {
            LOGGER.error("Could not parse response XML", ex);
            throw new ConnectorRequestFailedException(ex);
        } catch (DecodingException ex) {
            LOGGER.error("Could not decode response", ex);
            throw new ConnectorRequestFailedException(ex);
        }
    }

    protected abstract boolean canHandle(DataSourceConfiguration config, GetCapabilitiesResponse capabilities);

    public abstract ServiceConstellation getConstellation(DataSourceConfiguration config,
                                                          GetCapabilitiesResponse capabilities);

    protected DataEntity<?> createDataEntity(OmObservation observation, DatasetEntity seriesEntity) {
        if (ValueType.quantity.equals(seriesEntity.getValueType())) {
            return DataEntityBuilder.createQuantityDataEntity(observation);
        } else if (ValueType.count.equals(seriesEntity.getValueType())) {
            return DataEntityBuilder.createCountDataEntity(observation);
        } else if (ValueType.text.equals(seriesEntity.getValueType())) {
            return DataEntityBuilder.createTextDataEntity(observation);
        } else {
            LOGGER.error("No supported datasetEntity for ", seriesEntity);
            return null;
        }
    }

    protected GetFeatureOfInterestResponse getFeatureOfInterestByProcedure(String procedureId, String serviceUri) {
        return getFeatureOfInterest(null, procedureId, null, serviceUri);
    }

    protected GetFeatureOfInterestResponse getFeatureOfInterestById(String featureId, String serviceUri) {
        return getFeatureOfInterest(featureId, null, null, serviceUri);
    }

    protected GetFeatureOfInterestResponse getFeatureOfInterest(String featureId, String procedureId, String obsProp,
                                                                String url) {
        GetFeatureOfInterestRequest request
                = new GetFeatureOfInterestRequest(SosConstants.SOS, Sos2Constants.SERVICEVERSION);
        Optional.ofNullable(featureId).map(Arrays::asList).ifPresent(request::setFeatureIdentifiers);
        Optional.ofNullable(procedureId).map(Arrays::asList).ifPresent(request::setProcedures);
        Optional.ofNullable(obsProp).map(Arrays::asList).ifPresent(request::setObservedProperties);
        return (GetFeatureOfInterestResponse) getSosResponseFor(request, Sos2Constants.NS_SOS_20, url);
    }

    protected DescribeSensorResponse describeSensor(String procedureId, String url, String format) {
        DescribeSensorRequest request = new DescribeSensorRequest(SosConstants.SOS, Sos2Constants.SERVICEVERSION);
        request.setProcedure(procedureId);
        request.setProcedureDescriptionFormat(format);
        return (DescribeSensorResponse) getSosResponseFor(request, SwesConstants.NS_SWES_20, url);
    }

    protected boolean supportsGDA(OwsCapabilities owsCaps) {
        return owsCaps.getOperationsMetadata()
                .map(OwsOperationsMetadata::getOperations)
                .map(Set::stream)
                .orElseGet(Stream::empty)
                .map(OwsOperation::getName)
                .anyMatch(name -> name.equals(GetDataAvailabilityConstants.EN_GET_DATA_AVAILABILITY));
    }

    protected GetDataAvailabilityResponse getDataAvailabilityByProcedure(String procedureId, String serviceURL) {
        return getDataAvailability(procedureId, null, null, null, serviceURL);
    }

    protected GetDataAvailabilityResponse getDataAvailability(DatasetEntity seriesEntity) {
        return getDataAvailability(seriesEntity.getProcedure().getIdentifier(),
                                   seriesEntity.getOffering().getIdentifier(),
                                   seriesEntity.getPhenomenon().getIdentifier(),
                                   seriesEntity.getFeature().getIdentifier(),
                                   seriesEntity.getService().getUrl());
    }

    protected GetDataAvailabilityResponse getDataAvailability(QuantityDatasetConstellation dataset,
                                                              String serviceURL) {
        return getDataAvailability(dataset.getProcedure(),
                                   dataset.getOffering(),
                                   dataset.getPhenomenon(),
                                   dataset.getFeature(),
                                   serviceURL);
    }

    protected GetDataAvailabilityResponse getDataAvailability(String procedure,
                                                              String offering,
                                                              String phenomenon,
                                                              String feature,
                                                              String url) {
        try {
            QueryBuilder builder = new QueryBuilder(url);
            builder.add(OWSConstants.RequestParams.service, SosConstants.SOS);
            builder.add(OWSConstants.RequestParams.version, Sos2Constants.SERVICEVERSION);
            builder.add(OWSConstants.RequestParams.request, GetDataAvailabilityConstants.EN_GET_DATA_AVAILABILITY);
            builder.add(GetDataAvailabilityConstants.GetDataAvailabilityParams.procedure, procedure);
            builder.add(GetDataAvailabilityConstants.GetDataAvailabilityParams.offering, offering);
            builder.add(GetDataAvailabilityConstants.GetDataAvailabilityParams.observedProperty, phenomenon);
            builder.add(GetDataAvailabilityConstants.GetDataAvailabilityParams.featureOfInterest, feature);
            return (GetDataAvailabilityResponse) getSosResponseFor(builder.build());
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    protected GetObservationResponse getObservation(DatasetEntity seriesEntity,
                                                    TemporalFilter temporalFilter) {
        return getObservation(seriesEntity, temporalFilter, null, null);
    }

    protected GetObservationResponse getObservation(DatasetEntity seriesEntity,
                                                    TemporalFilter temporalFilter,
                                                    SpatialFilter spatialFilter) {
        return getObservation(seriesEntity, temporalFilter, spatialFilter, null);
    }

    protected GetObservationResponse getObservation(DatasetEntity seriesEntity,
                                                    TemporalFilter temporalFilter,
                                                    String responseFormat) {
        return getObservation(seriesEntity, temporalFilter, null, responseFormat);
    }

    protected GetObservationResponse getObservation(DatasetEntity seriesEntity,
                                                    TemporalFilter temporalFilter,
                                                    SpatialFilter spatialFilter,
                                                    String responseFormat) {
        return getObservation(seriesEntity,
                              Optional.ofNullable(temporalFilter).map(Arrays::asList).orElseGet(Collections::emptyList),
                              spatialFilter,
                              responseFormat);
    }

    protected GetObservationResponse getObservation(DatasetEntity seriesEntity,
                                                    List<TemporalFilter> temporalFilter,
                                                    SpatialFilter spatialFilter,
                                                    String responseFormat) {
        GetObservationRequest request = new GetObservationRequest(SosConstants.SOS, Sos2Constants.SERVICEVERSION);
        request.addProcedure(seriesEntity.getProcedure().getIdentifier());
        request.addOffering(seriesEntity.getOffering().getIdentifier());
        request.addObservedProperty(seriesEntity.getPhenomenon().getIdentifier());
        request.addFeatureIdentifier(seriesEntity.getFeature().getIdentifier());
        Optional.ofNullable(temporalFilter).ifPresent(request::setTemporalFilters);
        Optional.ofNullable(spatialFilter).ifPresent(request::setSpatialFilter);
        Optional.ofNullable(responseFormat).ifPresent(request::setResponseFormat);
        return (GetObservationResponse) getSosResponseFor(request, Sos2Constants.NS_SOS_20,
                                                          seriesEntity.getService().getUrl());
    }

    private GetObservationResponse getObservation(String procedure, String offering, String phenomenon, String feature,
            TemporalFilter temporalFilter, String serviceURL) {
        GetObservationRequest request = new GetObservationRequest(SosConstants.SOS, Sos2Constants.SERVICEVERSION);
        request.addProcedure(procedure);
        request.addOffering(offering);
        request.addObservedProperty(phenomenon);
        request.addFeatureIdentifier(feature);
        Optional.ofNullable(temporalFilter).ifPresent(request::addTemporalFilter);
        return (GetObservationResponse) getSosResponseFor(request, Sos2Constants.NS_SOS_20,
                serviceURL);
    }
}
