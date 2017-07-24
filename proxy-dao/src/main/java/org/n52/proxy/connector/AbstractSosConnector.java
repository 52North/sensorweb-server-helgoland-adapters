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
import org.apache.http.HttpResponse;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import static org.apache.xmlbeans.XmlObject.Factory.parse;
import org.n52.proxy.config.DataSourceConfiguration;
import org.n52.proxy.connector.utils.ServiceConstellation;
import org.n52.shetland.ogc.ows.service.GetCapabilitiesResponse;
import org.n52.shetland.ogc.ows.service.OwsServiceRequest;
import org.n52.svalbard.decode.DecoderKey;
import org.n52.svalbard.decode.exception.DecodingException;
import org.n52.svalbard.encode.EncoderKey;
import org.n52.svalbard.encode.exception.EncodingException;
import static org.n52.svalbard.util.CodingHelper.getDecoderKey;
import static org.n52.svalbard.util.CodingHelper.getEncoderKey;
import org.slf4j.Logger;
import static org.slf4j.LoggerFactory.getLogger;

public abstract class AbstractSosConnector extends AbstractConnector {

    protected int counter = 0;

    private static final Logger LOGGER = getLogger(AbstractSosConnector.class);

    public boolean matches(DataSourceConfiguration config, GetCapabilitiesResponse capabilities) {
        if (config.getConnector() != null) {
            return this.getClass().getSimpleName().equals(config.getConnector())
                    || this.getClass().getName().equals(config.getConnector());
        } else {
            return canHandle(config, capabilities);
        }
    }

    protected Object getSosResponseFor(String uri) {
        try {
            HttpResponse response = sendGetRequest(uri);
            return decodeResponse(response);
        } catch (XmlException | IOException | DecodingException ex) {
            LOGGER.error(ex.getLocalizedMessage(), ex);
            return null;
        }
    }

    protected Object getSosResponseFor(OwsServiceRequest request, String namespace, String serviceUrl) {
        XmlObject xmlRequest = null;
        try {
            counter++;
            EncoderKey encoderKey = getEncoderKey(namespace, request);
            xmlRequest = (XmlObject) encoderRepository.getEncoder(encoderKey).encode(request);
            HttpResponse response = sendPostRequest(xmlRequest, serviceUrl);
            return decodeResponse(response);
        } catch (EncodingException | IOException | XmlException | DecodingException ex) {
            if (xmlRequest != null) {
                LOGGER.info(xmlRequest.xmlText());
            }
            LOGGER.error(ex.getLocalizedMessage());
            return null;
        }
    }

    private Object decodeResponse(HttpResponse response) throws XmlException, IOException, DecodingException {
        XmlObject xmlResponse = parse(response.getEntity().getContent());
        DecoderKey decoderKey = getDecoderKey(xmlResponse);
        return decoderRepository.getDecoder(decoderKey).decode(xmlResponse);
    }

    protected abstract boolean canHandle(DataSourceConfiguration config, GetCapabilitiesResponse capabilities);

    public abstract ServiceConstellation getConstellation(DataSourceConfiguration config,
            GetCapabilitiesResponse capabilities);

}
