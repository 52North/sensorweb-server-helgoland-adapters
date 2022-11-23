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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.n52.sensorweb.server.helgoland.adapters.config.Credentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

/**
 * Client for retrieving Data from ArcGIS Rest API.
 *
 * @author j.speckamp@52north.org
 */
public class ArcgisRestHttpClient extends SimpleHttpClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArcgisRestHttpClient.class);
    private static final String REFERER = "referer";

    private final String username;
    private final String password;
    private final String tokenUrl;

    private Date tokenEndOfLife = Date.from(Instant.EPOCH);

    public ArcgisRestHttpClient(Credentials credentials) {
        this(credentials.getUsername(), credentials.getPassword(), credentials.getTokenUrl());
    }

    public ArcgisRestHttpClient(int connectionTimeout, int socketTimeout, Credentials credentials) {
        this(connectionTimeout, socketTimeout, credentials.getUsername(), credentials.getPassword(),
                credentials.getTokenUrl());
    }

    public ArcgisRestHttpClient(String username, String password, String tokenUrl) {
        super();
        this.username = username;
        this.password = password;
        this.tokenUrl = tokenUrl;
    }

    public ArcgisRestHttpClient(int connectionTimeout, int socketTimeout, String username, String password,
            String tokenUrl) {
        super(connectionTimeout, socketTimeout);
        this.username = username;
        this.password = password;
        this.tokenUrl = tokenUrl;
    }

    /**
     * Creates a new client if token is expired. Returns the existing client
     * otherwise.
     *
     * @return client
     */
    @Override
    protected CloseableHttpClient getClient() {
        if (new Date().after(tokenEndOfLife)) {
            recreateClient();
        }
        return super.getClient();
    }

    @Override
    protected HttpClientBuilder configureClient() {
        HttpClientBuilder baseClient = super.configureClient();
        if (tokenUrl == null) {
            return baseClient;
        }

        try {
            ArcGISToken accessToken = getAccessToken();
            this.tokenEndOfLife = accessToken.getExpires();
            baseClient.setDefaultHeaders(
                    Collections.singletonList(new BasicHeader("Authorization", "Bearer " + accessToken.getToken())));

        } catch (IOException e) {
            LOGGER.error("Error getting AccessToken", e);
        }
        return baseClient;
    }

    /**
     * Gets an auth token. Based on:
     * https://developers.arcgis.com/rest/users-groups-and-items/generate-token.htm
     *
     * @return ArcGISToken
     */
    private ArcGISToken getAccessToken() throws IOException {
        CloseableHttpClient tokenClient = HttpClientBuilder.create().build();

        HttpPost request = new HttpPost(tokenUrl);

        NameValuePair[] params = new NameValuePair[] { new BasicNameValuePair("username", this.username),
                new BasicNameValuePair("password", this.password), new BasicNameValuePair("client", REFERER),
                new BasicNameValuePair(REFERER, "www.something.com"), new BasicNameValuePair("f", "json") };

        request.setEntity(new UrlEncodedFormEntity(Arrays.asList(params)));
        CloseableHttpResponse response = tokenClient.execute(request);

        if (response.getStatusLine().getStatusCode() != 200) {
            throw new IOException("Unable to retrieve token. status was: " + response.getStatusLine().getStatusCode());
        }
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(response.getEntity().getContent(), ArcGISToken.class);
    }

    private static class ArcGISToken {

        private String token;

        private Date expires;

        private boolean ssl;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public Date getExpires() {
            return expires;
        }

        public void setExpires(Date expires) {
            this.expires = expires;
        }

        public boolean isSsl() {
            return ssl;
        }

        public void setSsl(boolean ssl) {
            this.ssl = ssl;
        }

    }
}
