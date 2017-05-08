package org.n52.proxy.connector.utils;

import java.util.Date;
import org.n52.series.db.beans.CountDataEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.QuantityDataEntity;
import org.n52.series.db.beans.TextDataEntity;
import org.n52.shetland.ogc.gml.time.TimeInstant;
import org.n52.shetland.ogc.om.OmObservation;
import org.n52.shetland.ogc.om.SingleObservationValue;
import org.n52.shetland.ogc.om.values.QuantityValue;

/**
 * @author Jan Schulte
 */
public class DataEntityBuilder {

    private DataEntityBuilder() {
    }

    public static DataEntity createQuantityDataEntity(OmObservation observation) {
        QuantityDataEntity dataEntity = new QuantityDataEntity();
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

    public static DataEntity createTextDataEntity(OmObservation observation) {
        TextDataEntity dataEntity = new TextDataEntity();
        dataEntity.setTimestart(new Date());
        dataEntity.setTimeend(new Date());
        dataEntity.setValue("Text-Text-value");
        return dataEntity;
    }
}
