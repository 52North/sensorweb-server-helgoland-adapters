/*
 * Copyright (C) 2015-2020 52°North Initiative for Geospatial Open Source
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
package org.n52.sensorweb.server.helgoland.adapters;

import org.apache.http.HttpHeaders;
import org.n52.janmayen.http.HTTPMethods;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

@EnableWebMvc
@Configuration
// @ImportResource({"classpath*:/spring/dispatcher-servlet.xml"})
@ImportResource({"classpath*:/spring/application-context.xml"})
public class WebConfig implements WebMvcConfigurer {

    private static final String CSV = "csv";

    @Bean
    @SuppressWarnings("EmptyLineSeparator")
    public WebMvcConfigurer createCORSFilter() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                String[] allowedMethods = { HTTPMethods.GET, HTTPMethods.POST, HTTPMethods.PUT, HTTPMethods.DELETE,
                        HTTPMethods.OPTIONS };
                String[] exposedHeaders = { HttpHeaders.CONTENT_TYPE, HttpHeaders.CONTENT_ENCODING };
                String[] allowedHeaders =
                        { HttpHeaders.CONTENT_TYPE, HttpHeaders.CONTENT_ENCODING, HttpHeaders.ACCEPT };
                registry.addMapping("/*")
                        .allowedOrigins("*")
                        .allowedMethods(allowedMethods)
                        .exposedHeaders(exposedHeaders)
                        .allowedHeaders(allowedHeaders);
            };
        };
    }

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer.defaultContentType(MediaType.APPLICATION_JSON)
                  .mediaType("json", MediaType.APPLICATION_JSON)
                  .mediaType("pdf", MediaType.APPLICATION_PDF)
                  .mediaType(CSV, new MediaType("text", CSV))
                  .mediaType("png", MediaType.IMAGE_PNG);
//                  .ignoreUnknownPathExtensions(false)
//                  .favorPathExtension(true)
//                  .ignoreAcceptHeader(false)
//                  .useRegisteredExtensionsOnly(true);
    }

    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        View jsonView = createJsonView();
        registry.enableContentNegotiation(jsonView);
    }

    private View createJsonView() {
        final MappingJackson2JsonView jsonView = new MappingJackson2JsonView();
        jsonView.setExtractValueFromSingleKeyModel(true);
        jsonView.setDisableCaching(false);
        jsonView.setObjectMapper(configureObjectMapper());
        return jsonView;
    }

    private ObjectMapper configureObjectMapper() {
        final ObjectMapper om = new ObjectMapper();
        om.setSerializationInclusion(Include.NON_NULL);
        return om;
    }

}
