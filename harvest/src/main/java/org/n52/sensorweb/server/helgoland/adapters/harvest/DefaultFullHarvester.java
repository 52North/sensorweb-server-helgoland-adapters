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
package org.n52.sensorweb.server.helgoland.adapters.harvest;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.n52.sensorweb.server.helgoland.adapters.connector.utils.ServiceConstellation;
import org.n52.series.db.beans.CategoryEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.beans.FeatureEntity;
import org.n52.series.db.beans.OfferingEntity;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.beans.ServiceEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

public class DefaultFullHarvester extends AbstractDefaultHarvester implements FullHarvester {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultFullHarvester.class);

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HarvesterResponse process(HarvestContext context) {
        if (context.getConstellation() instanceof ServiceConstellation) {
            ServiceConstellation constellation = (ServiceConstellation) context.getConstellation();
            // serviceEntity
            ServiceEntity service = getCRUDRepository().insertService(constellation.getService());
            Set<Long> datasetIds = getCRUDRepository().getIdsForService(service);
            int datasetCount = datasetIds.size();

            // save all constellations
            constellation.getDatasets().forEach(dataset -> {
                ProcedureEntity procedure = constellation.getProcedures().get(dataset.getProcedure());
                CategoryEntity category = constellation.getCategories().get(dataset.getCategory());
                FeatureEntity feature = constellation.getFeatures().get(dataset.getFeature());
                OfferingEntity offering = constellation.getOfferings().get(dataset.getOffering());
                PhenomenonEntity phenomenon = constellation.getPhenomena().get(dataset.getPhenomenon());
                PlatformEntity platform = constellation.getPlatforms().get(dataset.getPlatform());

                List<DescribableEntity> entities =
                        Arrays.asList(procedure, category, feature, offering, phenomenon, platform);
                if (entities.stream().allMatch(Objects::nonNull)) {
                    entities.stream().forEach(x -> x.setService(service));
                    DatasetEntity ds =
                            getCRUDRepository().insertDataset(dataset.createDatasetEntity(procedure,
                                    category, feature, offering, phenomenon, platform, service));
                    if (ds != null) {
                        datasetIds.remove(ds.getId());

                        dataset.getFirst().ifPresent(data -> getCRUDRepository().insertData(ds, data));
                        dataset.getLatest().ifPresent(data -> getCRUDRepository().insertData(ds, data));
                        LOGGER.info("Added dataset: {}", dataset);
                    } else {
                        LOGGER.warn("Can't save dataset: {}", dataset);
                    }
                } else {
                    LOGGER.warn("Can't add dataset: {}", dataset);
                }
            });

            getCRUDRepository().cleanUp(service, datasetIds,
                    datasetCount > 0 && datasetIds.size() == datasetCount);
            return new FullHarvesterResponse();
        }
        return new FullHarvesterResponse(false);
    }

}
