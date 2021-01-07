package org.n52.helgoland.adapters.dcat;

import org.apache.jena.rdf.model.Model;

import java.util.Optional;

public interface ModelPersistence {
    Optional<Model> read();

    void write(Model model);
}
