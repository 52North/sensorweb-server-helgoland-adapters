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

    private final String username;
    private final String password;
    private final String tokenUrl;

    private Date tokenEndOfLife = Date.from(Instant.EPOCH);

    public ArcgisRestHttpClient(String username,
                                String password,
                                String tokenUrl) {
        super();
        this.username = username;
        this.password = password;
        this.tokenUrl = tokenUrl;
    }

    public ArcgisRestHttpClient(int connectionTimeout,
                                int socketTimeout,
                                String username,
                                String password,
                                String tokenUrl) {
        super(connectionTimeout, socketTimeout);
        this.username = username;
        this.password = password;
        this.tokenUrl = tokenUrl;
    }

    /**
     * Creates a new client if token is expired. Returns the existing client otherwise.
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
            this.tokenEndOfLife = accessToken.expires;
            baseClient.setDefaultHeaders(Collections.singletonList(
                    new BasicHeader("Authorization", "Bearer " + accessToken.token)));

        } catch (IOException e) {
            LOGGER.error("Error getting AccessToken", e);
        }
        return baseClient;
    }

    /**
     * Gets an auth token.
     * Based on: https://developers.arcgis.com/rest/users-groups-and-items/generate-token.htm
     *
     * @return ArcGISToken
     */
    private ArcGISToken getAccessToken() throws IOException {
        CloseableHttpClient tokenClient = HttpClientBuilder.create().build();

        HttpPost request = new HttpPost(tokenUrl);

        NameValuePair[] params = new NameValuePair[]{
                new BasicNameValuePair("username", this.username),
                new BasicNameValuePair("password", this.password),
                new BasicNameValuePair("client", "referer"),
                new BasicNameValuePair("referer", "www.something.com"),
                new BasicNameValuePair("f", "json")
        };

        request.setEntity(new UrlEncodedFormEntity(Arrays.asList(params)));
        CloseableHttpResponse response = tokenClient.execute(request);

        if (response.getStatusLine().getStatusCode() != 200) {
            throw new IOException("Unable to retrieve token. status was: " + response.getStatusLine().getStatusCode());
        }
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(response.getEntity().getContent(), ArcGISToken.class);
    }

    private static class ArcGISToken {

        public String token;

        public Date expires;

        public boolean ssl;

    }
}
