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
package org.n52.sensorweb.server.helgoland.adapters;

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

import org.apache.http.HttpResponse;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.n52.sensorweb.server.helgoland.adapters.web.SimpleHttpClient;
import org.n52.shetland.ogc.ows.service.GetCapabilitiesRequest;
import org.n52.shetland.ogc.ows.service.GetCapabilitiesResponse;
import org.n52.shetland.ogc.sos.SosCapabilities;
import org.n52.svalbard.decode.Decoder;
import org.n52.svalbard.decode.DecoderKey;
import org.n52.svalbard.decode.DecoderRepository;
import org.n52.svalbard.decode.exception.DecodingException;
import org.n52.svalbard.encode.Encoder;
import org.n52.svalbard.encode.EncoderKey;
import org.n52.svalbard.encode.EncoderRepository;
import org.n52.svalbard.encode.exception.EncodingException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;

import static org.apache.xmlbeans.XmlObject.Factory.parse;
import static org.n52.shetland.ogc.sos.Sos2Constants.NS_SOS_20;
import static org.n52.svalbard.util.CodingHelper.getDecoderKey;
import static org.n52.svalbard.util.CodingHelper.getEncoderKey;
import static org.slf4j.LoggerFactory.getLogger;

@Disabled
@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = {"classpath:arctic-sea-test.xml"})
public class GetCapabilitiesTest {

    private static final Logger LOGGER = getLogger(GetCapabilitiesTest.class);

    private String uri = "http://sensorweb.demo.52north.org/sensorwebtestbed/service";
//    private String uri = "http://sensorweb.demo.52north.org/52n-sos-webapp/service";

    @Inject
    private EncoderRepository encoderRepository;

    @Inject
    private DecoderRepository decoderRepository;

    @Test
    public void sendGetCapabilitiesRequest() throws EncodingException, IOException, XmlException, DecodingException {
        SimpleHttpClient client = new SimpleHttpClient();
        XmlObject getCapabilitiesDocument = createGetCapabilitiesDocument();
        // send getCapabilities
        HttpResponse response = client.executePost(uri, getCapabilitiesDocument);
        // handle response
        GetCapabilitiesResponse capabilitiesResponse = createGetCapabilitiesResponse(response.getEntity().getContent());
        LOGGER.info("Service: " + capabilitiesResponse.getService());
        LOGGER.info("Version: " + capabilitiesResponse.getVersion());
        LOGGER.info("XML-String: " + capabilitiesResponse.getXmlString());
        SosCapabilities sosCapabilities = (SosCapabilities) capabilitiesResponse.getCapabilities();
        sosCapabilities.getContents().get().forEach(elem -> {
            LOGGER.info("Contents-Element: " + elem);
        });
        LOGGER.info("FilterCapabilities: " + sosCapabilities.getFilterCapabilities());
    }

    private GetCapabilitiesResponse createGetCapabilitiesResponse(InputStream responseStream) {
        try {
            XmlObject response = parse(responseStream);
            DecoderKey decoderKey = getDecoderKey(response);
            Decoder<Object, Object> decoder = decoderRepository.getDecoder(decoderKey);
            GetCapabilitiesResponse temp = (GetCapabilitiesResponse) decoder.decode(response);
            return temp;
        } catch (DecodingException | XmlException | IOException ex) {
            LOGGER.error(ex.getLocalizedMessage(), ex);
        }
        return null;
    }

    private XmlObject createGetCapabilitiesDocument() throws EncodingException {
        GetCapabilitiesRequest request = new GetCapabilitiesRequest("SOS");
        EncoderKey encoderKey = getEncoderKey(NS_SOS_20, request);
        Encoder<Object, Object> encoder = encoderRepository.getEncoder(encoderKey);
        return (XmlObject) encoder.encode(request);
    }

}
