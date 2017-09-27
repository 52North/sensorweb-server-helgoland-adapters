package org.n52.proxy.connector;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpResponse;
import org.apache.xmlbeans.XmlObject;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.n52.proxy.config.DataSourceConfiguration;
import org.n52.proxy.connector.utils.EntityBuilder;
import org.n52.proxy.connector.utils.ServiceConstellation;
import org.n52.proxy.db.beans.ProxyServiceEntity;
import org.n52.proxy.web.SimpleHttpClient;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.UnitEntity;
import org.n52.series.db.dao.DbQuery;
import org.n52.shetland.ogc.filter.FilterConstants.SpatialOperator;
import org.n52.shetland.ogc.filter.FilterConstants.TimeOperator;
import org.n52.shetland.ogc.filter.SpatialFilter;
import org.n52.shetland.ogc.filter.TemporalFilter;
import org.n52.shetland.ogc.gml.AbstractFeature;
import org.n52.shetland.ogc.gml.CodeType;
import org.n52.shetland.ogc.gml.ReferenceType;
import org.n52.shetland.ogc.gml.time.TimeInstant;
import org.n52.shetland.ogc.gml.time.TimePeriod;
import org.n52.shetland.ogc.om.OmConstants;
import org.n52.shetland.ogc.om.features.FeatureCollection;
import org.n52.shetland.ogc.om.features.samplingFeatures.SamplingFeature;
import org.n52.shetland.ogc.sos.ExtendedIndeterminateTime;
import org.n52.shetland.ogc.sos.Sos2Constants;
import org.n52.shetland.ogc.sos.SosObservationOffering;
import org.n52.shetland.ogc.sos.gda.GetDataAvailabilityResponse.DataAvailability;
import org.n52.shetland.util.ReferencedEnvelope;

import com.google.common.primitives.Ints;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

/**
 * @author Jan Schulte
 */
public abstract class AbstractConnector {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractConnector.class);
    private static final long CONNECTION_TIMEOUT = TimeUnit.SECONDS.toMillis(30);
    private static final long SOCKET_TIMEOUT = TimeUnit.MINUTES.toMillis(30);
    private final SimpleHttpClient httpClient;

    public AbstractConnector() {
        httpClient = new SimpleHttpClient(Ints.checkedCast(CONNECTION_TIMEOUT),
                                          Ints.checkedCast(SOCKET_TIMEOUT));
    }

    public String getConnectorName() {
        return getClass().getName();
    }

    protected HttpResponse sendGetRequest(String uri) throws IOException {
        LOGGER.debug("Executing GET request {}", uri);
        return httpClient.executeGet(uri);
    }

    protected HttpResponse sendPostRequest(XmlObject request, String uri) throws IOException {
        LOGGER.debug("Executing POST request to {}\n{}", uri, request.xmlText());
        return httpClient.executePost(uri, request);
    }

    public abstract List<DataEntity<?>> getObservations(DatasetEntity<?> seriesEntity, DbQuery query);

    public abstract UnitEntity getUom(DatasetEntity<?> seriesEntity);

    public abstract Optional<DataEntity<?>> getFirstObservation(DatasetEntity<?> entity);

    public abstract Optional<DataEntity<?>> getLastObservation(DatasetEntity<?> entity);

    protected void addService(DataSourceConfiguration config, ServiceConstellation serviceConstellation) {
        ProxyServiceEntity service
                = EntityBuilder.createService(config.getItemName(),
                                              "here goes description",
                                              config.getConnector(),
                                              config.getUrl(),
                                              config.getVersion());
        serviceConstellation.setService(service);
    }

    protected String addOffering(SosObservationOffering offering, ServiceConstellation serviceConstellation) {
        String offeringId = offering.getIdentifier();
        CodeType name = offering.getFirstName();
        if (name != null) {
            serviceConstellation.putOffering(offeringId, name.getValue());
        } else {
            serviceConstellation.putOffering(offeringId, offeringId);
        }
        return offeringId;
    }

    protected String addOffering(ReferenceType offering, ServiceConstellation serviceConstellation) {
        String offeringId = offering.getHref();
        String offeringName = offering.getTitle();
        serviceConstellation.putOffering(offeringId, offeringName);
        return offeringId;
    }

    protected String addOffering(String offeringId, String offeringName, ServiceConstellation serviceConstellation) {
        serviceConstellation.putOffering(offeringId, offeringName);
        return offeringId;
    }

    protected String addProcedure(String procedureId, boolean insitu, boolean mobile,
                                  ServiceConstellation serviceConstellation) {
        serviceConstellation.putProcedure(procedureId, procedureId, insitu, mobile);
        return procedureId;
    }

    protected String addProcedure(DataAvailability dataAval, boolean insitu, boolean mobile,
                                  ServiceConstellation serviceConstellation) {
        String procedureId = dataAval.getProcedure().getHref();
        String procedureName = dataAval.getProcedure().getTitle();
        serviceConstellation.putProcedure(procedureId, procedureName, insitu, mobile);
        return procedureId;
    }

    protected String addProcedure(String procedureId, String procedureName, boolean insitu, boolean mobile,
                                  ServiceConstellation serviceConstellation) {
        serviceConstellation.putProcedure(procedureId, procedureName, insitu, mobile);
        return procedureId;
    }

    protected String addPhenomenon(String phenomenonId, ServiceConstellation serviceConstellation) {
        serviceConstellation.putPhenomenon(phenomenonId, phenomenonId);
        return phenomenonId;
    }

    protected String addPhenomenon(String phenomenonId, String phenomenonName,
                                   ServiceConstellation serviceConstellation) {
        serviceConstellation.putPhenomenon(phenomenonId, phenomenonName);
        return phenomenonId;
    }

    protected String addPhenomenon(DataAvailability dataAval, ServiceConstellation serviceConstellation) {
        String phenomenonId = dataAval.getObservedProperty().getHref();
        String phenomenonName = dataAval.getObservedProperty().getTitle();
        serviceConstellation.putPhenomenon(phenomenonId, phenomenonName);
        return phenomenonId;
    }

    protected String addCategory(String categoryId, ServiceConstellation serviceConstellation) {
        serviceConstellation.putCategory(categoryId, categoryId);
        return categoryId;
    }

    protected String addCategory(DataAvailability dataAval, ServiceConstellation serviceConstellation) {
        String categoryId = dataAval.getObservedProperty().getHref();
        String categoryName = dataAval.getObservedProperty().getTitle();
        serviceConstellation.putCategory(categoryId, categoryName);
        return categoryId;
    }

    protected String addCategory(String categoryId, String categoryName, ServiceConstellation serviceConstellation) {
        serviceConstellation.putCategory(categoryId, categoryName);
        return categoryId;
    }

    protected void addFeature(AbstractFeature feature, ServiceConstellation serviceConstellation) {
        if (feature instanceof SamplingFeature) {
            addFeature((SamplingFeature) feature, serviceConstellation);
        } else if (feature instanceof FeatureCollection) {
            ((FeatureCollection) feature)
                    .forEach(featureEntry -> addFeature(featureEntry, serviceConstellation));
        }
    }

    protected String addFeature(SamplingFeature samplingfeature, ServiceConstellation serviceConstellation) {
        String featureId = samplingfeature.getIdentifier();
        String featureDescription = samplingfeature.getDescription();
        String featureName = samplingfeature.getFirstName() != null
                                     ? samplingfeature.getFirstName().getValue()
                                     : featureId;
        if (samplingfeature.getGeometry() != null) {
            serviceConstellation.putFeature(featureId, featureName, featureDescription, samplingfeature.getGeometry());
        } else {
            LOGGER.warn("No geometry found");
        }
        return featureId;
    }

    protected TemporalFilter createFirstTimefilter() {
        return new TemporalFilter(TimeOperator.TM_Equals,
                                  new TimeInstant(ExtendedIndeterminateTime.FIRST),
                                  OmConstants.PHENOMENON_TIME_NAME);
    }

    protected TemporalFilter createLatestTimefilter() {
        return new TemporalFilter(TimeOperator.TM_Equals,
                                  new TimeInstant(ExtendedIndeterminateTime.LATEST),
                                  OmConstants.PHENOMENON_TIME_NAME);
    }

    protected TemporalFilter createTimeFilter(DbQuery query) {
        return createTimeFilter(query.getTimespan());
    }

    protected TemporalFilter createTimeFilter(DateTime dateTime) {
        if (dateTime == null) {
            return null;
        } else {
            TimeInstant instant = new TimeInstant(dateTime);
            TimeOperator operator = TimeOperator.TM_Equals;
            String valueReference = OmConstants.PHENOMENON_TIME_NAME;
            return new TemporalFilter(operator, instant, valueReference);
        }
    }

    protected TemporalFilter createTimeFilter(Interval timespan) {
        if (timespan == null) {
            return null;
        } else if (timespan.toDurationMillis() == 0) {
            return createTimeFilter(timespan.getStart());
        } else {
            String valueReference = OmConstants.PHENOMENON_TIME_NAME;
            TimeOperator operator = TimeOperator.TM_During;
            TimePeriod period = new TimePeriod(timespan);
            return new TemporalFilter(operator, period, valueReference);
        }
    }

    protected SpatialFilter createSpatialFilter(DbQuery query) {
        return createSpatialFilter(query.getSpatialFilter());
    }

    protected SpatialFilter createSpatialFilter(Envelope envelope) {
        return createSpatialFilter(envelope, 4326);
    }

    protected SpatialFilter createSpatialFilter(Envelope envelope, int srid) {
        if (envelope == null) {
            return null;
        }
        Geometry geom = new ReferencedEnvelope(envelope, srid).toGeometry();
        String valueReference = Sos2Constants.VALUE_REFERENCE_SPATIAL_FILTERING_PROFILE;
        return new SpatialFilter(SpatialOperator.BBOX, geom, valueReference);
    }
}
