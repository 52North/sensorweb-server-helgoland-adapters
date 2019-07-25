package org.n52.proxy.connector.utils;

import java.math.BigDecimal;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.n52.series.db.beans.CountDataEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.GeometryEntity;
import org.n52.series.db.beans.QuantityDataEntity;
import org.n52.series.db.beans.TextDataEntity;
import org.n52.series.db.dao.JTSGeometryConverter;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.shetland.ogc.gml.time.TimeInstant;
import org.n52.shetland.ogc.gml.time.TimePeriod;
import org.n52.shetland.ogc.om.NamedValue;
import org.n52.shetland.ogc.om.OmObservation;
import org.n52.shetland.ogc.om.SingleObservationValue;
import org.n52.shetland.ogc.om.values.Value;

/**
 * @author Jan Schulte
 */
public final class DataEntityBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataEntityBuilder.class);

    private DataEntityBuilder() {
    }

    public static DataEntity<BigDecimal> createQuantityDataEntity(OmObservation observation) {
        QuantityDataEntity dataEntity = new QuantityDataEntity();
        getNumericValue(observation).map(x -> {
            if (x instanceof BigDecimal) {
                return (BigDecimal) x;
            } else {
                return BigDecimal.valueOf(x.doubleValue());
            }
        }).ifPresent(dataEntity::setValue);

        return setCommonValues(observation, dataEntity);

    }

    public static DataEntity<Integer> createCountDataEntity(OmObservation observation) {
        CountDataEntity dataEntity = new CountDataEntity();
        getNumericValue(observation).map(Number::intValue).ifPresent(dataEntity::setValue);
        return setCommonValues(observation, dataEntity);
    }

    public static DataEntity<String> createTextDataEntity(OmObservation observation) {
        TextDataEntity dataEntity = new TextDataEntity();
        getStringValue(observation).ifPresent(dataEntity::setValue);
        return setCommonValues(observation, dataEntity);
    }

    private static Optional<Number> getNumericValue(OmObservation observation) {
        SingleObservationValue<?> singleValue = (SingleObservationValue) observation.getValue();
        Object value = singleValue.getValue().getValue();
        if (value instanceof Number) {
            return Optional.of((Number) value);
        } else {
            return Optional.empty();
        }
    }

    private static Optional<String> getStringValue(OmObservation observation) {
        SingleObservationValue<?> singleValue = (SingleObservationValue) observation.getValue();
        Object value = singleValue.getValue().getValue();
        if (value instanceof String) {
            return Optional.of((String) value);
        } else {
            return Optional.empty();
        }
    }

    private static void setValidTime(OmObservation observation, DataEntity<?> dataEntity) {
        TimePeriod validTime = observation.getValidTime();
        if (validTime != null) {
            if (validTime.isSetStart()) {
                dataEntity.setValidTimeStart(validTime.getStart().toDate());
            }
            if (validTime.isSetEnd()) {
                dataEntity.setValidTimeEnd(validTime.getEnd().toDate());
            }
        }
    }

    private static void setResultTime(OmObservation observation, DataEntity<?> dataEntity) {
        TimeInstant resultTime = observation.getResultTime();
        if (resultTime != null && resultTime.isSetValue()) {
            dataEntity.setResultTime(resultTime.getValue().toDate());
        } else {
            Time phenomenonTime = observation.getPhenomenonTime();
            if (phenomenonTime instanceof TimeInstant) {
                TimeInstant instant = (TimeInstant) phenomenonTime;
                if (instant.isSetValue()) {
                    dataEntity.setResultTime(instant.getValue().toDate());
                }
            } else if (phenomenonTime instanceof TimePeriod) {
                TimePeriod period = (TimePeriod) phenomenonTime;
                if (period.isSetStart()) {
                    dataEntity.setResultTime(period.getStart().toDate());
                } else if (period.isSetEnd()) {
                    dataEntity.setResultTime(period.getEnd().toDate());
                }
            }
        }
    }

    private static void setPhenomenonTime(OmObservation observation, DataEntity<?> dataEntity) {
        Time phenomenonTime = observation.getPhenomenonTime();
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

    private static void setGeometry(OmObservation observation, DataEntity<?> dataEntity) {
        Optional.ofNullable(observation.getSpatialFilteringProfileParameter())
                .filter(NamedValue::isSetValue)
                .map(NamedValue::getValue)
                .filter(Value::isSetValue)
                .map(Value::getValue)
                .map(JTSGeometryConverter::convert)
                .map(GeometryEntity::new)
                .ifPresent(dataEntity::setGeometryEntity);
    }

    private static <T, D extends DataEntity<T>> D setCommonValues(OmObservation observation, D dataEntity) {
        dataEntity.setDeleted(false);
        dataEntity.setChild(false);
        dataEntity.setParent(false);
        setPhenomenonTime(observation, dataEntity);
        setValidTime(observation, dataEntity);
        setResultTime(observation, dataEntity);
        setGeometry(observation, dataEntity);
        return dataEntity;
    }

}
