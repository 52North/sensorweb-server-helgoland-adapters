/*
 * Copyright (C) 2015-2021 52°North Initiative for Geospatial Open Source
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
package org.n52.helgoland.adapters.dcat;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.RDFWriter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public class ModelHttpMessageConverter extends AbstractHttpMessageConverter<Model> {

    private static final Map<MediaType, Lang> LANG_BY_MEDIA_TYPE;

    static {
        Map<MediaType, Lang> langByMediaType = new LinkedHashMap<>();
        langByMediaType.put(MediaTypes.APPLICATION_LD_JSON, Lang.JSONLD);
        langByMediaType.put(MediaType.APPLICATION_JSON, Lang.JSONLD);
        langByMediaType.put(MediaTypes.APPLICATION_RDF_XML, Lang.RDFXML);
        langByMediaType.put(MediaType.APPLICATION_XML, Lang.RDFXML);
        langByMediaType.put(MediaTypes.APPLICATION_N_TRIPLES, Lang.NTRIPLES);
        langByMediaType.put(MediaTypes.TEXT_TURTLE, Lang.TURTLE);
        langByMediaType.put(MediaTypes.APPLICATION_N_QUADS, Lang.NQUADS);
        langByMediaType.put(MediaTypes.APPLICATION_TRIG, Lang.TRIG);
        langByMediaType.put(MediaTypes.APPLICATION_RDF_JSON, Lang.RDFJSON);
        langByMediaType.put(MediaTypes.TEXT_N3, Lang.TURTLE);
        LANG_BY_MEDIA_TYPE = Collections.unmodifiableMap(langByMediaType);
    }

    public ModelHttpMessageConverter() {
        super(StandardCharsets.UTF_8, LANG_BY_MEDIA_TYPE.keySet().toArray(new MediaType[0]));
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return Model.class.isAssignableFrom(clazz);
    }

    @Override
    protected Model readInternal(Class<? extends Model> clazz, HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {
        if (!supports(clazz)) {
            throw new HttpMessageNotReadableException("unsupported class " + clazz, inputMessage);
        }
        MediaType contentType = getContentType(inputMessage);
        Model model = ModelFactory.createDefaultModel();
        read(model,
             inputMessage.getBody(),
             getLang(contentType, msg -> new HttpMessageNotReadableException(msg, inputMessage)),
             getContentTypeCharset(contentType));
        return model;
    }

    private Charset getContentTypeCharset(MediaType contentType) {
        if (contentType.getCharset() != null) {
            return contentType.getCharset();
        } else if (contentType.isCompatibleWith(MediaType.APPLICATION_JSON)) {
            return StandardCharsets.UTF_8;
        } else {
            return getDefaultCharset();
        }
    }

    @Override
    protected void writeInternal(Model model, HttpOutputMessage outputMessage)
            throws IOException, HttpMessageNotWritableException {
        MediaType contentType = getContentType(outputMessage);
        write(model,
              outputMessage.getBody(),
              getLang(contentType, HttpMessageNotWritableException::new),
              getContentTypeCharset(contentType));
    }

    @SuppressWarnings("deprecation")
    private void read(Model model, InputStream stream, Lang lang, Charset charset) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(stream, charset)) {
            RDFParser.create().source(reader).lang(lang).parse(model);
        }
    }

    @SuppressWarnings("deprecation")
    private void write(Model model, OutputStream stream, Lang lang, Charset charset) throws IOException {
        try (OutputStreamWriter writer = new OutputStreamWriter(stream, charset)) {
            RDFWriter.create().lang(lang).source(model).build().output(writer);
        }
    }

    private MediaType getContentType(HttpMessage outputMessage) {
        MediaType contentType = outputMessage.getHeaders().getContentType();
        if (contentType == null) {
            outputMessage.getHeaders().setContentType(contentType = MediaTypes.APPLICATION_RDF_XML);
        }
        return contentType;
    }

    private <T extends Throwable> Lang getLang(MediaType mediaType, Function<String, T> exSupplier) throws T {
        if (LANG_BY_MEDIA_TYPE.containsKey(mediaType)) {
            return LANG_BY_MEDIA_TYPE.get(mediaType);
        }
        return LANG_BY_MEDIA_TYPE.keySet().stream().filter(mt -> mt.includes(mediaType)).findFirst()
                                 .map(LANG_BY_MEDIA_TYPE::get)
                                 .orElseThrow(() -> exSupplier.apply("no lang found for mediaType " + mediaType));
    }
}
