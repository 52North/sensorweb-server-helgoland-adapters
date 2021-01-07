package org.n52.helgoland.adapters.dcat;

import org.n52.shetland.rdf.RDFMediaTypes;
import org.springframework.http.MediaType;

public interface MediaTypes {
    MediaType APPLICATION_RDF_XML = MediaType.valueOf(RDFMediaTypes.APPLICATION_RDF_XML);
    MediaType APPLICATION_N_TRIPLES = MediaType.valueOf(RDFMediaTypes.APPLICATION_N_TRIPLES);
    MediaType TEXT_TURTLE = MediaType.valueOf(RDFMediaTypes.TEXT_TURTLE);
    MediaType TEXT_N3 = MediaType.valueOf(RDFMediaTypes.TEXT_N3);
    MediaType APPLICATION_LD_JSON = MediaType.valueOf(RDFMediaTypes.APPLICATION_LD_JSON);
    MediaType APPLICATION_RDF_JSON = MediaType.valueOf(RDFMediaTypes.APPLICATION_RDF_JSON);
    MediaType APPLICATION_N_QUADS = MediaType.valueOf(RDFMediaTypes.APPLICATION_N_QUADS);
    MediaType APPLICATION_TRIG = MediaType.valueOf(RDFMediaTypes.APPLICATION_TRIG);

}
