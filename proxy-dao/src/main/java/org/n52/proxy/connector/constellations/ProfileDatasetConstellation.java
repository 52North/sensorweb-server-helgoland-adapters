/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.proxy.connector.constellations;

import java.util.Date;

import org.n52.proxy.connector.utils.EntityBuilder;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.ServiceEntity;
import org.n52.series.db.beans.UnitEntity;
import org.n52.series.db.beans.dataset.DatasetType;
import org.n52.series.db.beans.dataset.ObservationType;
import org.n52.series.db.beans.dataset.ValueType;

/**
 * @author Jan Schulte
 */
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
