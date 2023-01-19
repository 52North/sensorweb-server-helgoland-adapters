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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sensorweb.server.helgoland.adapters.connector.constellations;

import java.util.Date;

import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.ServiceEntity;
import org.n52.series.db.beans.UnitEntity;
import org.n52.series.db.beans.dataset.DatasetType;
import org.n52.series.db.beans.dataset.ObservationType;
import org.n52.series.db.beans.dataset.ValueType;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * @author Jan Schulte
 */
@SuppressFBWarnings({"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
public class ProfileDatasetConstellation extends DatasetConstellation {

    private UnitEntity unit;

    public ProfileDatasetConstellation(String procedure, String offering, String phenomenon,
            String feature) {
        super(procedure, offering, phenomenon, feature);
    }

    public ProfileDatasetConstellation(String procedure, String offering, String category, String phenomenon,
            String feature, String platform) {
        super(procedure, offering, category, phenomenon, feature, platform);
    }

    public UnitEntity getUnit() {
        return unit;
    }

    public void setUnit(UnitEntity unit) {
        this.unit = unit;
    }

    @Override
    protected DatasetEntity createDatasetEntity(ServiceEntity service) {
        DatasetEntity dataset = new DatasetEntity();
        dataset.setUnit(unit);
        dataset.setFirstValueAt(new Date());
        dataset.setLastValueAt(new Date());
        dataset.setDatasetType(DatasetType.timeseries);
        dataset.setObservationType(ObservationType.profile);
        dataset.setValueType(ValueType.quantity);
        return dataset;
    }

}
