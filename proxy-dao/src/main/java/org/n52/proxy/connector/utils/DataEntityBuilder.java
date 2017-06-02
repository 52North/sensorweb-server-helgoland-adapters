package org.n52.proxy.connector.utils;

import java.util.Date;
import org.n52.series.db.beans.CountDataEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.QuantityDataEntity;
import org.n52.series.db.beans.TextDataEntity;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.shetland.ogc.gml.time.TimeInstant;
import org.n52.shetland.ogc.gml.time.TimePeriod;
import org.n52.shetland.ogc.om.OmObservation;
import org.n52.shetland.ogc.om.SingleObservationValue;
import org.n52.shetland.ogc.om.values.QuantityValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jan Schulte
 */
public class DataEntityBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataEntityBuilder.class);

    private DataEntityBuilder() {
    }

    public static DataEntity createQuantityDataEntity(OmObservation observation) {
        QuantityDataEntity dataEntity = new QuantityDataEntity();
        SingleObservationValue obsValue = (SingleObservationValue) observation.getValue();
        setPhenomenonTime(obsValue, dataEntity);
        QuantityValue value = (QuantityValue) obsValue.getValue();
        dataEntity.setValue(value.getValue());
        return dataEntity;
    }

    public static DataEntity createCountDataEntity(OmObservation observation) {
        CountDataEntity dataEntity = new CountDataEntity();
        SingleObservationValue obsValue = (SingleObservationValue) observation.getValue();
        setPhenomenonTime(obsValue, dataEntity);
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

    private static void setPhenomenonTime(SingleObservationValue obsValue, DataEntity dataEntity) {
        Time phenomenonTime = obsValue.getPhenomenonTime();
        if (phenomenonTime instanceof TimeInstant) {
            TimeInstant instant = (TimeInstant) phenomenonTime;
            dataEntity.setTimestart(instant.getValue().toDate());
            dataEntity.setTimeend(instant.getValue().toDate());
        } else if (phenomenonTime instanceof TimePeriod) {
            TimePeriod period = (TimePeriod) phenomenonTime;
            dataEntity.setTimestart(period.getStart().toDate());
            dataEntity.setTimeend(period.getEnd().toDate());
        } else {
            LOGGER.warn("No matching time found");
        }
    }
}
