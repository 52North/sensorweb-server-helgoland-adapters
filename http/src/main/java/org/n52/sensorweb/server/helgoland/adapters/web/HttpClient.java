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
package org.n52.sensorweb.server.helgoland.adapters.web;

import java.io.IOException;
import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.n52.janmayen.http.MediaType;
import org.n52.sensorweb.server.helgoland.adapters.web.request.AbstractRequest;
import org.n52.sensorweb.server.helgoland.adapters.web.response.Response;

public interface HttpClient {

    HttpResponse executeGet(String uri) throws IOException;

    /**
     * Sends the given payload as content-type text/xml with UTF-8 encoding to the determined URI.
     * <strong>Callees are responsible for ensuring that the contents are actually encoded as UTF-8</strong>. If not
     * UTF-8, use {@link #executePost(String, String, MediaType)} instead.
     *
     * @param uri           the target to send the POST request to.
     * @param payloadToSend the POST payload as XML encoded as UTF-8.
     *
     * @return the HTTP response returned by the target.
     *
     * @throws IOException if sending the request fails.
     */
    HttpResponse executePost(String uri, String payloadToSend) throws IOException;

    /**
     * Sends the given payload (marked to be of a specific content-type) to the determined URI.
     *
     * @param uri           the target to send the POST request to.
     * @param payloadToSend the POST payload as XML.
     * @param contentType   the content-type of the payload.
     *
     * @return the HTTP response returned by the target.
     *
     * @throws IOException if sending the request fails.
     */
    HttpResponse executePost(String uri, String payloadToSend, MediaType contentType) throws IOException;

    /**
     * Sends the given payload to the determined URI. Refer to the <a
     * href="http://hc.apache.org/httpcomponents-core-ga/httpcore/apidocs/index.html">HTTP components docs</a>
     * to get more information which entity types are possible.
     *
     * @param uri           the target to send the POST request to.
     * @param payloadToSend a more generic way to send arbitrary content.
     *
     * @return the HTTP response returned by the target.
     *
     * @throws IOException if sending the request fails.
     */
    HttpResponse executePost(String uri, HttpEntity payloadToSend) throws IOException;

    /**
     * @param method the HTTP method to execute.
     *
     * @return the HTTP response returned by the target.
     *
     * @throws IOException if sending the request fails
     */
    HttpResponse executeMethod(HttpRequestBase method) throws IOException;

    Response execute(URI url, AbstractRequest request) throws ProxyHttpClientException;

    Response execute(String url, AbstractRequest request) throws ProxyHttpClientException;

}
