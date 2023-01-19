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
package org.n52.sensorweb.server.helgoland.adapters.web;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.n52.janmayen.http.MediaType;
import org.n52.janmayen.http.MediaTypes;
import org.n52.sensorweb.server.helgoland.adapters.web.request.AbstractDeleteRequest;
import org.n52.sensorweb.server.helgoland.adapters.web.request.AbstractGetRequest;
import org.n52.sensorweb.server.helgoland.adapters.web.request.AbstractPostRequest;
import org.n52.sensorweb.server.helgoland.adapters.web.request.AbstractRequest;
import org.n52.sensorweb.server.helgoland.adapters.web.response.Response;
import org.n52.shetland.util.CollectionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;

public class SimpleHttpClient implements HttpClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleHttpClient.class);
    private static final int DEFAULT_CONNECTION_TIMEOUT = 30000;
    private static final int DEFAULT_SOCKET_TIMEOUT = 30000;
    private static final RetryPolicy<HttpResponse> RETRY_POLICY =
            new RetryPolicy<HttpResponse>().withDelay(10, 900, ChronoUnit.SECONDS).handle(ConnectException.class);
    private CloseableHttpClient httpclient;
    private int connectionTimeout;
    private int socketTimeout;
    private String proxyHost;
    private String proxyPort;
    private boolean proxySslKey;

    /**
     * Creates an instance with
     * <code>timeout = {@value #DEFAULT_CONNECTION_TIMEOUT}</code> ms.
     */
    public SimpleHttpClient() {
        this(DEFAULT_CONNECTION_TIMEOUT);
    }

    /**
     * Creates an instance with a given connection timeout.
     *
     * @param connectionTimeout
     *            the connection timeout in milliseconds.
     */
    public SimpleHttpClient(int connectionTimeout) {
        this(connectionTimeout, DEFAULT_SOCKET_TIMEOUT);
    }

    /**
     * Creates an instance with the given timeouts.
     *
     * @param connectionTimeout
     *            the connection timeout in milliseconds.
     * @param socketTimeout
     *            the socket timeout in milliseconds.
     */
    public SimpleHttpClient(int connectionTimeout, int socketTimeout) {
        this.socketTimeout = socketTimeout;
        this.connectionTimeout = connectionTimeout;
        recreateClient();
    }

    protected SimpleHttpClient(CloseableHttpClient httpclient) {
        this.httpclient = httpclient;
    }

    @Override
    public Response execute(String url, AbstractRequest request) throws ProxyHttpClientException {
        return execute(URI.create(url), request);
    }

    @Override
    public Response execute(URI url, AbstractRequest request) throws ProxyHttpClientException {
        if (request instanceof AbstractGetRequest) {
            return doGet(url, (AbstractGetRequest) request);
        } else if (request instanceof AbstractPostRequest) {
            return doPost(url, (AbstractPostRequest<?>) request);
        } else if (request instanceof AbstractDeleteRequest) {
            return doDelete(url, (AbstractDeleteRequest) request);
        }
        throw new ProxyHttpClientException("The request type '%s' is unknown!", request.getClass().getTypeName());
    }

    @Override
    public HttpResponse executeGet(String uri) throws IOException {
        LOGGER.debug("executing GET method '{}'", uri);
        return executeMethod(new HttpGet(uri));
    }

    @Override
    public HttpResponse executePost(String uri, String payloadToSend) throws IOException {
        return executePost(uri, payloadToSend, MediaTypes.TEXT_XML);
    }

    @Override
    public HttpResponse executePost(String uri, String payloadToSend, MediaType contentType) throws IOException {
        StringEntity requestEntity = new StringEntity(payloadToSend, getContentType(contentType));
        LOGGER.trace("payload to send: {}", payloadToSend);
        return executePost(uri, requestEntity);
    }

    @Override
    public HttpResponse executePost(String uri, HttpEntity payloadToSend) throws IOException {
        LOGGER.debug("executing POST method to '{}'.", uri);
        HttpPost post = new HttpPost(uri);
        post.setEntity(payloadToSend);
        return executeMethod(post);
    }

    @Override
    public HttpResponse executeMethod(HttpRequestBase method) throws IOException {
        return executeHttpRequest(method);
    }

    public SimpleHttpClient setConnectionTimout(int timeout) {
        this.connectionTimeout = timeout;
        recreateClient();
        return this;
    }

    public SimpleHttpClient setSocketTimout(int timeout) {
        this.socketTimeout = timeout;
        recreateClient();
        return this;
    }

    public SimpleHttpClient setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
        return this;
    }

    public SimpleHttpClient setProxyPort(String proxyPort) {
        this.proxyPort = proxyPort;
        return this;
    }

    public SimpleHttpClient setProxySslKey(boolean proxySslKey) {
        this.proxySslKey = proxySslKey;
        return this;
    }

    protected Response doGet(URI url, AbstractGetRequest request) throws ProxyHttpClientException {
        try {
            HttpGet httpGet = new HttpGet(getGetUrl(url, request.getPath(), request.getQueryParameters()));
            if (request.hasHeader()) {
                for (Entry<String, String> entry : request.getHeader().entrySet()) {
                    httpGet.addHeader(entry.getKey(), entry.getValue());
                }
            }
            logRequest(getGetUrl(url, request.getPath(), request.getQueryParameters()));
            return getContent(executeHttpRequest(httpGet));
        } catch (URISyntaxException | IOException e) {
            throw new ProxyHttpClientException().causedBy(e);
        }
    }

    protected Response doGet(URI url, String path, Map<String, String> header, Map<String, String> parameter)
            throws ProxyHttpClientException {
        try {
            HttpGet httpGet = new HttpGet(getGetUrl(url, path, parameter));
            if (CollectionHelper.isNotEmpty(header)) {
                for (Entry<String, String> entry : header.entrySet()) {
                    httpGet.addHeader(entry.getKey(), entry.getValue());
                }
            }
            logRequest(getGetUrl(url, path, parameter));
            return getContent(executeHttpRequest(httpGet));
        } catch (URISyntaxException | IOException e) {
            throw new ProxyHttpClientException().causedBy(e);
        }
    }

    protected Response doPost(URI url, AbstractPostRequest<?> request) throws ProxyHttpClientException {
        try {
            HttpPost httpPost = new HttpPost(getPathUrl(url, request.getPath()));
            if (request.hasHeader()) {
                for (Entry<String, String> entry : request.getHeader().entrySet()) {
                    httpPost.addHeader(entry.getKey(), entry.getValue());
                }
            }
            String content = request.getContent();
            logRequest(content);
            httpPost.setEntity(new StringEntity(content));
            return getContent(executeHttpRequest(httpPost));
        } catch (IOException | URISyntaxException e) {
            throw new ProxyHttpClientException().causedBy(e);
        }
    }

    protected Response doPost(URI url, String content, MediaType contentType) throws ProxyHttpClientException {
        try {
            HttpPost httpPost = new HttpPost(url);
            httpPost.addHeader(HttpHeaders.CONTENT_TYPE, contentType.toString());
            logRequest(content);
            httpPost.setEntity(new StringEntity(content));
            return getContent(executeHttpRequest(httpPost));
        } catch (IOException e) {
            throw new ProxyHttpClientException().causedBy(e);
        }
    }

    protected Response doDelete(URI url, AbstractDeleteRequest request) throws ProxyHttpClientException {
        try {
            HttpDelete httpDelete = new HttpDelete(getPathUrl(url, request.getPath()));
            logRequest(getPathUrl(url, request.getPath()));
            return getContent(executeHttpRequest(httpDelete));
        } catch (URISyntaxException | IOException e) {
            throw new ProxyHttpClientException().causedBy(e);
        }
    }

    private CloseableHttpResponse executeHttpRequest(HttpRequestBase request) throws IOException {
        int counter = 4;
        CloseableHttpResponse response = null;
        long start = System.currentTimeMillis();
        do {
            response =
                    Failsafe.with(RETRY_POLICY).onFailure(ex -> LOGGER.warn("Could not connect to host; retrying", ex))
                            .get(() -> getClient().execute(request));
        } while (response == null && counter >= 0);

        LOGGER.trace("Querying took {} ms!", System.currentTimeMillis() - start);
        return response;
    }

    protected CloseableHttpClient getClient() {
        return httpclient;
    }

    private Response getContent(CloseableHttpResponse response) throws IOException {
        try {
            return response != null
                    ? new Response(response.getStatusLine().getStatusCode(),
                            response.getEntity() != null ? EntityUtils.toString(response.getEntity(), "UTF-8") : null)
                    : new Response(200, null);
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    private URI getGetUrl(URI url, Map<String, String> parameters) throws URISyntaxException {
        URIBuilder uriBuilder = new URIBuilder(url, StandardCharsets.UTF_8);
        if (CollectionHelper.isNotEmpty(parameters)) {
            for (Entry<String, String> entry : parameters.entrySet()) {
                uriBuilder.addParameter(entry.getKey(), entry.getValue());
            }
        }
        URI uri = uriBuilder.build();
        return uri;
    }

    private URI getGetUrl(URI url, String path, Map<String, String> parameters) throws URISyntaxException {
        return getGetUrl(getPathUrl(url, path), parameters);
    }

    private URI getPathUrl(URI url, String path) throws URISyntaxException {
        if (path != null && !path.isEmpty()) {
            URIBuilder uriBuilder = new URIBuilder(url.toString() + path, StandardCharsets.UTF_8);
            return uriBuilder.build();
        }
        return url;
    }

    private ContentType getContentType(MediaType contentType) {
        return ContentType.create(contentType.toString());
    }

    protected void recreateClient() {
        if (this.httpclient != null) {
            try {
                this.httpclient.close();
            } catch (IOException ex) {
                LOGGER.warn("Error closing client", ex);
            }
            this.httpclient = null;
        }
        this.httpclient = configureClient().build();
    }

    /**
     * May be overwritten by subclasses that e.g. require specialized Headers
     * @return builder for the client instance
     */
    protected HttpClientBuilder configureClient() {
        return HttpClientBuilder.create()
                .setDefaultRequestConfig(getRequestConfig())
                .setSSLSocketFactory(getSSLSocketFactory())
                .setDefaultSocketConfig(getSocketConfig())
                .setMaxConnTotal(200)
                .setMaxConnPerRoute(50)
                .useSystemProperties();
    }

    private SSLConnectionSocketFactory getSSLSocketFactory() {
        if (isIgnoreSSLHostnameValidation()) {
            LOGGER.debug("Noop hostname verifier enabled!");
            try {
                return new SSLConnectionSocketFactory(
                        SSLContexts.custom().loadTrustMaterial(null, new TrustSelfSignedStrategy()).build(),
                        NoopHostnameVerifier.INSTANCE);
            } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
                LOGGER.error("Error creating SSL connnection socket factory!", e);
            }
        }
        return null;
    }

    private boolean isIgnoreSSLHostnameValidation() {
        return proxySslKey;
    }

    private RequestConfig getRequestConfig() {
        Builder builder =
                RequestConfig.custom().setConnectTimeout(this.connectionTimeout).setSocketTimeout(this.socketTimeout);
        if (isSetProxy()) {
            builder.setProxy(getProxy());
        }
        return builder.build();
    }

    private SocketConfig getSocketConfig() {
        return SocketConfig.custom().setSoTimeout(this.socketTimeout).build();
    }

    private HttpHost getProxy() {
        if (proxyPort != null && !proxyPort.isEmpty()) {
            return new HttpHost(proxyHost, Integer.parseInt(proxyPort));
        }
        return new HttpHost(proxyHost);
    }

    private boolean isSetProxy() {
        return proxyHost != null && !proxyHost.isEmpty();
    }

    private void logRequest(URI request) {
        logRequest(request.toString());
    }

    private void logRequest(String request) {
        LOGGER.debug("Request: {}", request);
    }

}
