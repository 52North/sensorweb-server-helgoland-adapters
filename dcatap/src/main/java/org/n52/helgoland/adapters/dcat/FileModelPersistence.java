/*
 * Copyright (C) 2015-2023 52°North Spatial Information Research GmbH
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class FileModelPersistence implements ModelPersistence {
    private static final Logger LOG = LoggerFactory.getLogger(FileModelPersistence.class);
    private final Object persistenceLock = new Object();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Path modelPath;

    public FileModelPersistence(Path modelPath) {
        this.modelPath = Objects.requireNonNull(modelPath);
    }

    @Override
    public Optional<Model> read() {
        this.lock.readLock().lock();
        try {
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
        } finally {
            this.lock.readLock().unlock();
        }
    }

    @Override
    public void write(Model model) {
        this.lock.writeLock().lock();
        try (OutputStream output = Files.newOutputStream(modelPath)) {
            model.write(output);
        } catch (IOException e) {
            LOG.error("Error writing model to disk", e);
        } finally {
            this.lock.writeLock().unlock();
        }
    }
}
