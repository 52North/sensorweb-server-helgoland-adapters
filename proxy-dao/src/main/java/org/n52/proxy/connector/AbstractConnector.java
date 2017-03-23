package org.n52.proxy.connector;

import java.util.List;
import java.util.Optional;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.UnitEntity;
import org.n52.series.db.dao.DbQuery;
import org.n52.svalbard.decode.DecoderRepository;
import org.n52.svalbard.encode.EncoderRepository;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Jan Schulte
 */
public abstract class AbstractConnector {

    @Autowired
    protected DecoderRepository decoderRepository;

    @Autowired
    protected EncoderRepository encoderRepository;

    public String getConnectorName() {
        return getClass().getName();
    }

    public abstract List<DataEntity> getObservations(DatasetEntity seriesEntity, DbQuery query);

    public abstract UnitEntity getUom(DatasetEntity seriesEntity);

    public abstract Optional<DataEntity> getFirstObservation(DatasetEntity entity);

    public abstract Optional<DataEntity> getLastObservation(DatasetEntity entity);
}
