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
    public CatalogResource(CatalogProvider catalogProvider) {this.catalogProvider = catalogProvider;}

    @GetMapping(value = "/catalog", produces = {RDFMediaTypes.APPLICATION_RDF_XML,
                                                MediaType.APPLICATION_JSON_VALUE,
                                                RDFMediaTypes.APPLICATION_LD_JSON,
                                                MediaType.APPLICATION_XML_VALUE,
                                                RDFMediaTypes.APPLICATION_N_TRIPLES,
                                                RDFMediaTypes.TEXT_TURTLE,
                                                RDFMediaTypes.APPLICATION_N_QUADS,
                                                RDFMediaTypes.APPLICATION_TRIG,
                                                RDFMediaTypes.APPLICATION_RDF_JSON,
                                                RDFMediaTypes.TEXT_N3})
    public Model getCatalog() {
        return catalogProvider.getModel();
    }

    @GetMapping(value = "/catalog.json", produces = {RDFMediaTypes.APPLICATION_LD_JSON})
    public Model getCatalogAsJson() {
        return getCatalog();
    }

    @GetMapping(value = "/catalog.xml", produces = {RDFMediaTypes.APPLICATION_RDF_XML})
    public Model getCatalogAsXml() {
        return getCatalog();
    }

    @GetMapping(value = "/catalog.ttl", produces = {RDFMediaTypes.TEXT_TURTLE})
    public Model getCatalogAsTtl() {
        return getCatalog();
    }

}
