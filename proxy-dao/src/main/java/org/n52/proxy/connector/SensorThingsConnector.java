package org.n52.proxy.connector;

import com.github.filosganga.geogson.gson.GeometryAdapterFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static java.util.Optional.of;
import org.apache.http.HttpResponse;
import org.joda.time.DateTime;
import static org.joda.time.format.DateTimeFormat.forPattern;
import org.joda.time.format.DateTimeFormatter;
import org.n52.proxy.config.DataSourceConfiguration;
import org.n52.proxy.connector.constellations.QuantityDatasetConstellation;
import org.n52.proxy.connector.utils.ConnectorHelper;
import static org.n52.proxy.connector.utils.ConnectorHelper.addService;
import static org.n52.proxy.connector.utils.EntityBuilder.createUnit;
import org.n52.proxy.connector.utils.ServiceConstellation;
import org.n52.sensorthings.Datastream;
import org.n52.sensorthings.Datastreams;
import org.n52.sensorthings.Location;
import org.n52.sensorthings.Locations;
import org.n52.sensorthings.Observation;
import org.n52.sensorthings.Observations;
import org.n52.sensorthings.ObservedProperty;
import org.n52.sensorthings.Sensor;
import org.n52.sensorthings.Thing;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.QuantityDataEntity;
import org.n52.series.db.beans.UnitEntity;
import org.n52.series.db.dao.DbQuery;
import static org.slf4j.LoggerFactory.getLogger;

public class SensorThingsConnector extends AbstractConnector {

    private static final org.slf4j.Logger LOGGER = getLogger(SensorThingsConnector.class);

    private Gson gson = new GsonBuilder().registerTypeAdapterFactory(new GeometryAdapterFactory()).create();

    private DateTimeFormatter formatter = forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z");

    @Override
    public List<DataEntity> getObservations(DatasetEntity seriesEntity, DbQuery query) {
        return createObservations(seriesEntity, query.getTimespan().getStart(), query.getTimespan().getEnd());
    }

    @Override
    public UnitEntity getUom(DatasetEntity seriesEntity) {
        return seriesEntity.getUnit();
    }

    @Override
    public Optional<DataEntity> getFirstObservation(DatasetEntity entity) {
        return of(createObservationBounds(entity, "asc"));
    }

    @Override
    public Optional<DataEntity> getLastObservation(DatasetEntity entity) {
        return of(createObservationBounds(entity, "desc"));
    }

    public ServiceConstellation getConstellation(DataSourceConfiguration config) {
        ServiceConstellation serviceConstellation = new ServiceConstellation();
        config.setConnector(getConnectorName());
        addService(config, serviceConstellation);
        createDatasets(serviceConstellation, config.getUrl());
        return serviceConstellation;
    }

    private void createDatasets(ServiceConstellation serviceConstellation, String url) {
        Datastreams datastreams = getDatastreams(url);
        doForDatastreams(datastreams, serviceConstellation);
        while (datastreams.nextLink != null) {
            datastreams = (Datastreams) doGetRequest(datastreams.nextLink, Datastreams.class);
            doForDatastreams(datastreams, serviceConstellation);
        }
    }

    private void doForDatastreams(Datastreams datastreams, ServiceConstellation serviceConstellation) {
        datastreams.value.forEach((Datastream datastream) -> {
            doForDatastream(datastream, serviceConstellation);
        });
    }

    private void doForDatastream(Datastream datastream, ServiceConstellation serviceConstellation) {
        String offeringId = addOffering(datastream.thing, serviceConstellation);
        String phenomenonId = addPhenomenon(datastream.observedProperty, serviceConstellation);
        String procedureId = addProcedure(datastream.sensor, serviceConstellation);
        String categoryId = addCategory(datastream.observedProperty, serviceConstellation);
        Locations locations = (Locations) doGetRequest(datastream.thing.locationsLink, Locations.class);
        if (locations != null) {
            String featureId = addFeature(locations.value.get(0), serviceConstellation);
            QuantityDatasetConstellation constellation = new QuantityDatasetConstellation(procedureId,
                    offeringId,
                    categoryId,
                    phenomenonId,
                    featureId);
            constellation.setDomainId(Integer.toString(datastream.iotID));
            constellation.setUnit(createUnit(datastream.unitOfMeasurement.symbol,
                    serviceConstellation.getService()));
            serviceConstellation.add(constellation);
        }
    }

    private Object doGetRequest(String urlString, Class clazz) {
        try {
            HttpResponse response = sendGetRequest(urlString);
            return gson.fromJson(new InputStreamReader(response.getEntity().getContent()), clazz);
        } catch (MalformedURLException ex) {
            LOGGER.error(ex.getLocalizedMessage(), ex);
        } catch (JsonSyntaxException | IOException ex) {
            LOGGER.error(ex.getLocalizedMessage(), ex);
        }
        return null;
    }

    private Object doGetRequest(String url, String entity, Class clazz) {
        return doGetRequest(url + entity, clazz);
    }

    private Datastreams getDatastreams(String url) {
        return (Datastreams) doGetRequest(url, "Datastreams?$expand=Sensor,Thing,ObservedProperty", Datastreams.class);
    }

    private List<DataEntity> createObservations(DatasetEntity seriesEntity, DateTime start, DateTime end) {
        Observations observations = (Observations) doGetRequest(
                seriesEntity.getService().getUrl(),
                "Datastreams(" + seriesEntity.getDomainId() + ")/Observations?$filter=phenomenonTime%20gt%20'" + start.toString(
                formatter) + "'%20and%20phenomenonTime%20lt%20'" + end.toString(formatter) + "'",
                Observations.class
        );
        ArrayList<DataEntity> list = new ArrayList<>();
        addObservationsToList(observations, list);
        while (observations.nextLink != null) {
            observations = (Observations) doGetRequest(observations.nextLink, Observations.class);
            addObservationsToList(observations, list);
        }
        return list;
    }

    private void addObservationsToList(Observations observations, ArrayList<DataEntity> list) {
        observations.value.forEach((observation) -> {
            list.add(createObservation(observation));
        });
    }

    private DataEntity createObservationBounds(DatasetEntity entity, String order) {
        Observations observations = (Observations) doGetRequest(entity.getService().getUrl(),
                "Datastreams(" + entity.getDomainId() + ")/Observations?$orderby=phenomenonTime%20" + order + "&$top=1",
                Observations.class);
        if (observations.value.size() == 1) {
            return createObservation(observations.value.get(0));
        }
        return null;
    }

    private String addOffering(Thing thing, ServiceConstellation serviceConstellation) {
        return ConnectorHelper.addOffering(Integer.toString(thing.iotID), thing.name, serviceConstellation);
    }

    private String addPhenomenon(ObservedProperty obsProp, ServiceConstellation serviceConstellation) {
        return ConnectorHelper.addPhenomenon(Integer.toString(obsProp.iotID), obsProp.name, serviceConstellation);
    }

    private String addProcedure(Sensor sensor, ServiceConstellation serviceConstellation) {
        return ConnectorHelper.addProcedure(Integer.toString(sensor.iotID), sensor.name, true, false,
                serviceConstellation);
    }

    private String addCategory(ObservedProperty obsProp, ServiceConstellation serviceConstellation) {
        return ConnectorHelper.addCategory(Integer.toString(obsProp.iotID), obsProp.name, serviceConstellation);
    }

    private String addFeature(Location location, ServiceConstellation serviceConstellation) {
        String featureId = Integer.toString(location.iotID);
        serviceConstellation.putFeature(featureId, location.name, location.location.coordinates.get(1),
                location.location.coordinates.get(0), 4326);
        return featureId;
    }

    private DataEntity createObservation(Observation observation) {
        QuantityDataEntity dataEntity = new QuantityDataEntity();
        dataEntity.setTimestart(observation.phenomenonTime);
        dataEntity.setTimeend(observation.phenomenonTime);
        dataEntity.setValue(observation.result);
        return dataEntity;
    }

}
