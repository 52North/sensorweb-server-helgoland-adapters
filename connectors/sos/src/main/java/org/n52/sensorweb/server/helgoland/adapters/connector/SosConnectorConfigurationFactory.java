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
package org.n52.sensorweb.server.helgoland.adapters.connector;

import javax.inject.Inject;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.sensorweb.server.helgoland.adapters.harvest.DataSourceJobConfiguration;
import org.n52.sensorweb.server.helgoland.adapters.request.GetCapabilitiesGetRequest;
import org.n52.sensorweb.server.helgoland.adapters.web.ProxyHttpClientException;
import org.n52.sensorweb.server.helgoland.adapters.web.SimpleHttpClient;
import org.n52.sensorweb.server.helgoland.adapters.web.response.Response;
import org.n52.shetland.ogc.ows.service.GetCapabilitiesResponse;
import org.n52.svalbard.decode.DecoderRepository;
import org.n52.svalbard.decode.exception.DecodingException;
import org.n52.svalbard.util.CodingHelper;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

@Component
public class SosConnectorConfigurationFactory implements ConnectorConfigurationFactory {

    @Inject
    private DecoderRepository decoderRepository;

    @Override
    public ConnectorConfiguration createConfiguration(DataSourceJobConfiguration dataSource)
            throws JobExecutionException {
        return new SosConnectorConfiguration(dataSource, getCapabilities(dataSource));
    }

    @Override
    public boolean checkDatasource(DataSourceJobConfiguration dataSource) {
        return dataSource.getType().equalsIgnoreCase("SOS");
    }

    private GetCapabilitiesResponse getCapabilities(DataSourceJobConfiguration dataSource)
            throws JobExecutionException {
        try {
            SimpleHttpClient simpleHttpClient = new SimpleHttpClient();
            String url = dataSource.getUrl();
            if (url.contains("?")) {
                url += "&";
            } else {
                url += "?";
            }
            GetCapabilitiesGetRequest request = new GetCapabilitiesGetRequest();
            if (dataSource.isDisableHumanReadableName()) {
                request.withReturnHumanReadableName(false);
            }
            Response response = simpleHttpClient.execute(url, request);
            XmlObject xmlResponse = XmlObject.Factory.parse(response.getEntity());
            return (GetCapabilitiesResponse) decoderRepository.getDecoder(CodingHelper.getDecoderKey(xmlResponse))
                    .decode(xmlResponse);
        } catch (XmlException | DecodingException | ProxyHttpClientException ex) {
            throw new JobExecutionException(ex);
        }
    }
}
