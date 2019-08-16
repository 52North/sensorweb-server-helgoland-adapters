package org.n52.proxy.connector.constellations;


import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.ServiceEntity;
import org.n52.series.db.beans.UnitEntity;
import org.n52.series.db.beans.dataset.DatasetType;
import org.n52.series.db.beans.dataset.ObservationType;
import org.n52.series.db.beans.dataset.ValueType;

/**
 * @author Jan Schulte
 */
public class QuantityDatasetConstellation extends DatasetConstellation {

    private UnitEntity unit;

    public QuantityDatasetConstellation(String procedure, String offering, String category, String phenomenon,
            String feature, String platform) {
        super(procedure, offering, category, phenomenon, feature, platform);
    }

    public UnitEntity getUnit() {
        return unit;
    }

    public QuantityDatasetConstellation setUnit(UnitEntity unit) {
        this.unit = unit;
        return this;
    }

    @Override
    protected DatasetEntity createDatasetEntity(ServiceEntity service) {
        DatasetEntity dataset = new DatasetEntity();
        dataset.setUnit(unit);
        dataset.setFirstValueAt(getSamplingTimeStart());
        dataset.setLastValueAt(getSamplingTimeEnd());
        dataset.setDatasetType(DatasetType.timeseries);
        dataset.setObservationType(ObservationType.simple);
        dataset.setValueType(ValueType.quantity);
        return dataset;
    }

}
