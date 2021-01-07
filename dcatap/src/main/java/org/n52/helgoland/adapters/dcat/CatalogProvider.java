package org.n52.helgoland.adapters.dcat;

import org.apache.jena.rdf.model.Model;

public interface CatalogProvider {
    Model getModel();
}
