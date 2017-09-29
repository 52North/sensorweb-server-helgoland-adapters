package org.n52.proxy.connector.constellations;

import static org.n52.proxy.connector.utils.EntityBuilder.createUnit;

import java.util.Date;

import org.n52.proxy.db.beans.ProxyServiceEntity;
import org.n52.series.db.beans.QuantityDatasetEntity;
import org.n52.series.db.beans.UnitEntity;

/**
 * @author Jan Schulte
 */
public class QuantityDatasetConstellation extends DatasetConstellation<QuantityDatasetEntity> {

    private UnitEntity unit;

    public QuantityDatasetConstellation(String procedure, String offering, String category, String phenomenon,
            String feature) {
        super(procedure, offering, category, phenomenon, feature);
    }

    public UnitEntity getUnit() {
        return unit;
    }

    public void setUnit(UnitEntity unit) {
        this.unit = unit;
    }

    @Override
    protected QuantityDatasetEntity createDatasetEntity(ProxyServiceEntity service) {
        QuantityDatasetEntity quantityDataset = new QuantityDatasetEntity();
        // add empty unit entity, will be replaced later in the repositories
        if (unit == null) {
            // create empty unit
            unit = createUnit("", null, service);
        }
        quantityDataset.setUnit(unit);
        quantityDataset.setFirstValueAt(new Date());
        quantityDataset.setLastValueAt(new Date());
        return quantityDataset;
    }

}
