package org.n52.proxy.connector;

import com.github.filosganga.geogson.gson.GeometryAdapterFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import org.n52.proxy.config.DataSourceConfiguration;
import org.n52.proxy.connector.constellations.MeasurementDatasetConstellation;
import org.n52.proxy.connector.utils.ConnectorHelper;
import org.n52.proxy.connector.utils.EntityBuilder;
import org.n52.proxy.connector.utils.ServiceConstellation;
import org.n52.sensorthings.Datastream;
import org.n52.sensorthings.Datastreams;
import org.n52.sensorthings.Location;
import org.n52.sensorthings.Locations;
import org.n52.sensorthings.ObservedProperty;
import org.n52.sensorthings.Sensor;
import org.n52.sensorthings.Thing;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.UnitEntity;
import org.n52.series.db.dao.DbQuery;
import org.slf4j.LoggerFactory;

public class SensorThingsConnector extends AbstractConnector {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(SensorThingsConnector.class);

    private Gson gson = new GsonBuilder().registerTypeAdapterFactory(new GeometryAdapterFactory()).create();

    @Override
    public List<DataEntity> getObservations(DatasetEntity seriesEntity, DbQuery query) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public UnitEntity getUom(DatasetEntity seriesEntity) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Optional<DataEntity> getFirstObservation(DatasetEntity entity) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Optional<DataEntity> getLastObservation(DatasetEntity entity) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public ServiceConstellation getConstellation(DataSourceConfiguration config) {
        ServiceConstellation serviceConstellation = new ServiceConstellation();
        config.setConnector(getConnectorName());
        ConnectorHelper.addService(config, serviceConstellation);
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
//        datastreams.value.forEach((Datastream datastream) -> {
//            doForDatastream(datastream, serviceConstellation);
//        });
        doForDatastream(datastreams.value.get(0), serviceConstellation);
    }

    private void doForDatastream(Datastream datastream, ServiceConstellation serviceConstellation) {
        String offeringId = addOffering(datastream.thing, serviceConstellation);
        String phenomenonId = addPhenomenon(datastream.observedProperty, serviceConstellation);
        String procedureId = addProcedure(datastream.sensor, serviceConstellation);
        String categoryId = addCategory(datastream.observedProperty, serviceConstellation);
        Locations locations = (Locations) doGetRequest(datastream.thing.locationsLink, Locations.class);
        if (locations != null) {
            String featureId = addFeature(locations.value.get(0), serviceConstellation);
            MeasurementDatasetConstellation constellation = new MeasurementDatasetConstellation(procedureId,
                    offeringId,
                    categoryId,
                    phenomenonId,
                    featureId);
            constellation.setUnit(EntityBuilder.createUnit(datastream.unitOfMeasurement.symbol,
                    serviceConstellation.getService()));
            serviceConstellation.add(constellation);
        }
    }

    private Object doGetRequest(String urlString, Class clazz) {
        try {
            LOGGER.info(urlString);
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            return gson.fromJson(new InputStreamReader(connection.getInputStream()), clazz);
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

}
