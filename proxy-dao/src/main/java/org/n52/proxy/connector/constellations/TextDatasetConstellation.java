package org.n52.proxy.connector.constellations;

import java.util.Date;

import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.ServiceEntity;
import org.n52.series.db.beans.dataset.DatasetType;
import org.n52.series.db.beans.dataset.ObservationType;
import org.n52.series.db.beans.dataset.ValueType;

/**
 * @author Jan Schulte
 */
public class TextDatasetConstellation extends DatasetConstellation {

    public TextDatasetConstellation(String procedure, String offering, String category, String phenomenon,
            String feature, String platform) {
        super(procedure, offering, category, phenomenon, feature, platform);
    }

    @Override
    protected DatasetEntity createDatasetEntity(ServiceEntity service) {
        DatasetEntity dataset = new DatasetEntity();
        dataset.setFirstValueAt(new Date());
        dataset.setLastValueAt(new Date());
        dataset.setDatasetType(DatasetType.timeseries);
        dataset.setObservationType(ObservationType.simple);
        dataset.setValueType(ValueType.text);
        return dataset;
    }

}
