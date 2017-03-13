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
import java.util.List;
import java.util.Optional;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.proxy.config.DataSourceConfiguration;
import org.n52.proxy.connector.constellations.MeasurementDatasetConstellation;
import org.n52.proxy.connector.utils.ConnectorHelper;
import org.n52.proxy.connector.utils.ServiceConstellation;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.UnitEntity;
import org.n52.series.db.dao.DbQuery;
import org.n52.shetland.ogc.ows.OwsCapabilities;
import org.n52.shetland.ogc.ows.OwsServiceProvider;
import org.n52.shetland.ogc.ows.service.GetCapabilitiesResponse;
import org.n52.shetland.ogc.sos.Sos1Constants;
import org.n52.shetland.ogc.sos.SosCapabilities;
import org.n52.shetland.ogc.sos.SosConstants;
import org.n52.shetland.ogc.sos.SosObservationOffering;
import org.n52.shetland.ogc.sos.request.DescribeSensorRequest;
import org.n52.shetland.ogc.sos.response.DescribeSensorResponse;
import org.n52.svalbard.decode.Decoder;
import org.n52.svalbard.decode.DecoderKey;
import org.n52.svalbard.decode.exception.DecodingException;
import org.n52.svalbard.encode.EncoderKey;
import org.n52.svalbard.encode.exception.EncodingException;
import org.n52.svalbard.util.CodingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OceanotronSosConnector extends AbstractSosConnector {

    private static final Logger LOGGER = LoggerFactory.getLogger(OceanotronSosConnector.class);

    @Override
    public ServiceConstellation getConstellation(DataSourceConfiguration config, GetCapabilitiesResponse capabilities) {
        ServiceConstellation serviceConstellation = new ServiceConstellation();
        config.setVersion(Sos1Constants.SERVICEVERSION);
        config.setConnector(getConnectorName());
        ConnectorHelper.addService(config, serviceConstellation);
        SosCapabilities sosCaps = (SosCapabilities) capabilities.getCapabilities();
        addDatasets(serviceConstellation, sosCaps, config.getUrl());
        return serviceConstellation;
    }

    /**
     * Matches when the provider name is equal "Geomatys"
     */
    @Override
    public boolean canHandle(DataSourceConfiguration config, GetCapabilitiesResponse capabilities) {
        OwsCapabilities owsCaps = capabilities.getCapabilities();
        if (owsCaps.getServiceProvider().isPresent()) {
            OwsServiceProvider servProvider = owsCaps.getServiceProvider().get();
            if (servProvider.getProviderName().equals("Geomatys")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<DataEntity> getObservations(DatasetEntity seriesEntity, DbQuery query) {
        // TODO implement
        throw new UnsupportedOperationException("getObservations not supported yet.");
    }

    @Override
    public UnitEntity getUom(DatasetEntity seriesEntity) {
        // TODO implement
        throw new UnsupportedOperationException("getUom not supported yet.");
    }

    @Override
    public Optional<DataEntity> getFirstObservation(DatasetEntity entity) {
        // TODO implement
        throw new UnsupportedOperationException("getFirstObservation not supported yet.");
    }

    @Override
    public Optional<DataEntity> getLastObservation(DatasetEntity entity) {
        // TODO implement
        throw new UnsupportedOperationException("getLastObservation not supported yet.");
    }

    private void addDatasets(ServiceConstellation serviceConstellation, SosCapabilities sosCaps, String url) {
        if (sosCaps != null) {
            sosCaps.getContents().ifPresent((obsOffs) -> {
                obsOffs.forEach((obsOff) -> {
                    addElem(obsOff, serviceConstellation, url);
                });
//                addElem(obsOffs.first(), serviceConstellation, url);
            });
        }
    }

    private void addElem(SosObservationOffering obsOff, ServiceConstellation serviceConstellation, String url) {

        String offeringId = obsOff.getOffering().getIdentifier();
        serviceConstellation.putOffering(offeringId, offeringId);

        obsOff.getProcedures().forEach((procedureId) -> {
            serviceConstellation.putProcedure(procedureId, procedureId, true, false);

            obsOff.getObservableProperties().forEach((obsProp) -> {
                serviceConstellation.putPhenomenon(obsProp, obsProp);
                serviceConstellation.putCategory(obsProp, obsProp);
                final String foiId = "foiId";
                serviceConstellation.putFeature(foiId, "foiName", 0, 0, 0);
                // TODO maybe not only MeasurementDatasetConstellation
                serviceConstellation.add(new MeasurementDatasetConstellation(procedureId, offeringId, obsProp, obsProp,
                        foiId));
            });
//                HttpResponse response = this.sendRequest(createDescribeSensorRequest(procedureId), url);
//                DescribeSensorResponse descSensResp = createDescSensResponse(response.getEntity().getContent());
        });
    }

    private XmlObject createDescribeSensorRequest(String procedureId) throws EncodingException {
        DescribeSensorRequest request = new DescribeSensorRequest(SosConstants.SOS, Sos1Constants.SERVICEVERSION);
        request.setProcedure(procedureId);
        request.setProcedureDescriptionFormat("text/xml;subtype=\"sensorML/1.0.0\"");
        EncoderKey encoderKey = CodingHelper.getEncoderKey(Sos1Constants.NS_SOS, request);
        return (XmlObject) encoderRepository.getEncoder(encoderKey).encode(request);
    }

    private DescribeSensorResponse createDescSensResponse(InputStream responseStream) {
        try {
            XmlObject response = XmlObject.Factory.parse(responseStream);
            DecoderKey decoderKey = CodingHelper.getDecoderKey(response);
            Decoder<Object, Object> decoder = decoderRepository.getDecoder(decoderKey);
            return (DescribeSensorResponse) decoder.decode(response);
        } catch (IOException | DecodingException | XmlException ex) {
            LOGGER.error(ex.getLocalizedMessage(), ex);
        }
        return null;
    }

}
