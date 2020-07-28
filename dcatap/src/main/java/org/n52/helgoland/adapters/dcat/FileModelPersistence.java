package org.n52.helgoland.adapters.dcat;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

public class FileModelPersistence implements ModelPersistence {
    private static final Logger LOG = LoggerFactory.getLogger(FileModelPersistence.class);
    private final Object persistenceLock = new Object();
    private final Path modelPath;

    public FileModelPersistence(Path modelPath) {
        this.modelPath = Objects.requireNonNull(modelPath);
    }

    @Override
    public Optional<Model> read() {
        synchronized (persistenceLock) {
            if (Files.isReadable(modelPath)) {
                try (InputStream input = Files.newInputStream(modelPath)) {
                    Model model = ModelFactory.createDefaultModel();
                    model.read(input, "");
                    return Optional.of(model);
                } catch (IOException e) {
                    LOG.error("Error reading model from disk", e);
                }
            }
            return Optional.empty();
        }
    }

    @Override
    public void write(Model model) {
        synchronized (persistenceLock) {
            try (OutputStream output = Files.newOutputStream(modelPath)) {
                model.write(output);
            } catch (IOException e) {
                LOG.error("Error writing model to disk", e);
            }
        }
    }
}
