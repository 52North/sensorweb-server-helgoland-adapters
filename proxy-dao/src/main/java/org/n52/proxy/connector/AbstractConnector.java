package org.n52.proxy.connector;

import java.util.List;
import java.util.Optional;
import org.apache.http.HttpResponse;
import org.apache.xmlbeans.XmlObject;
import org.n52.proxy.web.SimpleHttpClient;
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

    private final int CONNECTION_TIMEOUT = 30000;

    public String getConnectorName() {
        return getClass().getName();
    }

    protected HttpResponse sendGetRequest(String uri) {
        return new SimpleHttpClient(CONNECTION_TIMEOUT, CONNECTION_TIMEOUT).executeGet(uri);
    }

    protected HttpResponse sendPostRequest(XmlObject request, String uri) {
        return new SimpleHttpClient(CONNECTION_TIMEOUT, CONNECTION_TIMEOUT).executePost(uri, request);
    }

    public abstract List<DataEntity> getObservations(DatasetEntity seriesEntity, DbQuery query);

    public abstract UnitEntity getUom(DatasetEntity seriesEntity);

    public abstract Optional<DataEntity> getFirstObservation(DatasetEntity entity);

    public abstract Optional<DataEntity> getLastObservation(DatasetEntity entity);
}
