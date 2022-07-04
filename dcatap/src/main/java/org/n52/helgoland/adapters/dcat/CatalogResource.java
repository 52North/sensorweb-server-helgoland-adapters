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
package org.n52.helgoland.adapters.dcat;

import org.apache.jena.rdf.model.Model;
import org.n52.shetland.rdf.RDFMediaTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CatalogResource {
    private final CatalogProvider catalogProvider;

    @Autowired
    public CatalogResource(CatalogProvider catalogProvider) {
        this.catalogProvider = catalogProvider;
    }

    @GetMapping(value = "/catalog",
            produces = { RDFMediaTypes.APPLICATION_RDF_XML, MediaType.APPLICATION_JSON_VALUE,
                    RDFMediaTypes.APPLICATION_LD_JSON, MediaType.APPLICATION_XML_VALUE,
                    RDFMediaTypes.APPLICATION_N_TRIPLES, RDFMediaTypes.TEXT_TURTLE, RDFMediaTypes.APPLICATION_N_QUADS,
                    RDFMediaTypes.APPLICATION_TRIG, RDFMediaTypes.APPLICATION_RDF_JSON, RDFMediaTypes.TEXT_N3 })
    public Model getCatalog() {
        return catalogProvider.getModel();
    }

    @GetMapping(value = "/catalog.json", produces = { RDFMediaTypes.APPLICATION_LD_JSON })
    public Model getCatalogAsJson() {
        return getCatalog();
    }

    @GetMapping(value = "/catalog.xml", produces = { RDFMediaTypes.APPLICATION_RDF_XML })
    public Model getCatalogAsXml() {
        return getCatalog();
    }

    @GetMapping(value = "/catalog.ttl", produces = { RDFMediaTypes.TEXT_TURTLE })
    public Model getCatalogAsTtl() {
        return getCatalog();
    }

}
