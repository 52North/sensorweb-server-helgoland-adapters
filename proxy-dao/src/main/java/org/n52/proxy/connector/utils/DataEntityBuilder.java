package org.n52.proxy.connector.utils;

import org.n52.series.db.beans.CountDataEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.MeasurementDataEntity;
import org.n52.shetland.ogc.gml.time.TimeInstant;
import org.n52.shetland.ogc.om.OmObservation;
import org.n52.shetland.ogc.om.SingleObservationValue;
import org.n52.shetland.ogc.om.values.QuantityValue;

/**
 * @author Jan Schulte
 */
public class DataEntityBuilder {

    public static DataEntity createMeasurementDataEntity(OmObservation observation) {
        MeasurementDataEntity dataEntity = new MeasurementDataEntity();
        SingleObservationValue obsValue = (SingleObservationValue) observation.getValue();
        TimeInstant instant = (TimeInstant) obsValue.getPhenomenonTime();
        dataEntity.setTimestart(instant.getValue().toDate());
        dataEntity.setTimeend(instant.getValue().toDate());
        QuantityValue value = (QuantityValue) obsValue.getValue();
        dataEntity.setValue(value.getValue());
        return dataEntity;
    }

    public static DataEntity createCountDataEntity(OmObservation observation) {
        CountDataEntity dataEntity = new CountDataEntity();
        SingleObservationValue obsValue = (SingleObservationValue) observation.getValue();
        TimeInstant instant = (TimeInstant) obsValue.getPhenomenonTime();
        dataEntity.setTimestart(instant.getValue().toDate());
        dataEntity.setTimeend(instant.getValue().toDate());
        QuantityValue value = (QuantityValue) obsValue.getValue();
        dataEntity.setValue(value.getValue().intValue());
        return dataEntity;
    }
}
