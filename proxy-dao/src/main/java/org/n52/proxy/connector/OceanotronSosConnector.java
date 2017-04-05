/*
 * Copyright (C) 2013-2017 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public License
 * version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 */
package org.n52.proxy.connector;

import com.sun.javafx.scene.control.skin.VirtualFlow;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.n52.proxy.config.DataSourceConfiguration;
import org.n52.proxy.connector.constellations.MeasurementDatasetConstellation;
import static org.n52.proxy.connector.utils.ConnectorHelper.addService;
import org.n52.proxy.connector.utils.ServiceConstellation;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.UnitEntity;
import org.n52.shetland.ogc.sensorML.System;
import org.n52.series.db.dao.DbQuery;
import org.n52.shetland.ogc.ows.OwsCapabilities;
import org.n52.shetland.ogc.ows.OwsServiceProvider;
import org.n52.shetland.ogc.ows.service.GetCapabilitiesResponse;
import org.n52.shetland.ogc.sensorML.AbstractProcess;
import org.n52.shetland.ogc.sensorML.HasComponents;
import org.n52.shetland.ogc.sensorML.SensorML;
import org.n52.shetland.ogc.sensorML.elements.SmlComponent;
import org.n52.shetland.ogc.sos.Sos2Constants;
import org.n52.shetland.ogc.sos.SosCapabilities;
import static org.n52.shetland.ogc.sos.SosConstants.SOS;
import org.n52.shetland.ogc.sos.SosObservationOffering;
import org.n52.shetland.ogc.sos.request.DescribeSensorRequest;
import org.n52.shetland.ogc.sos.request.GetFeatureOfInterestRequest;
import org.n52.shetland.ogc.sos.response.GetFeatureOfInterestResponse;
import org.n52.shetland.ogc.swes.SwesConstants;
import org.slf4j.Logger;
import static org.slf4j.LoggerFactory.getLogger;

public class OceanotronSosConnector extends SOS2Connector {

    private static final Logger LOGGER = getLogger(OceanotronSosConnector.class);

    /**
     * Matches when the provider name is equal "Geomatys"
     */
    @Override
    public boolean canHandle(DataSourceConfiguration config, GetCapabilitiesResponse capabilities) {
        OwsCapabilities owsCaps = capabilities.getCapabilities();
        if (owsCaps.getServiceProvider().isPresent()) {
            OwsServiceProvider servProvider = owsCaps.getServiceProvider().get();
            if (servProvider.getProviderName().equals("Geomatys")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<DataEntity> getObservations(DatasetEntity seriesEntity, DbQuery query) {
        // TODO implement
        throw new UnsupportedOperationException("getObservations not supported yet.");
    }

    @Override
    public UnitEntity getUom(DatasetEntity seriesEntity) {
        // TODO implement
        throw new UnsupportedOperationException("getUom not supported yet.");
    }

    @Override
    public Optional<DataEntity> getFirstObservation(DatasetEntity entity) {
        // TODO implement
        throw new UnsupportedOperationException("getFirstObservation not supported yet.");
    }

    @Override
    public Optional<DataEntity> getLastObservation(DatasetEntity entity) {
        // TODO implement
        throw new UnsupportedOperationException("getLastObservation not supported yet.");
    }

    protected void addDatasets(ServiceConstellation serviceConstellation, SosCapabilities sosCaps, String url) {
        if (sosCaps != null) {
            sosCaps.getContents().ifPresent((obsOffs) -> {
                obsOffs.forEach((SosObservationOffering obsOff) -> {
                    // TODO remove if
                    if (obsOff.getIdentifier().equals("CORIOLIS-RECOPESCA")) {
                        addElem(obsOff, serviceConstellation, url);
                    }
                });
            });
        }
    }

    private void addElem(SosObservationOffering obsOff, ServiceConstellation serviceConstellation, String url) {

        String offeringId = obsOff.getOffering().getIdentifier();
        serviceConstellation.putOffering(offeringId, offeringId);

        obsOff.getProcedures().forEach((String procedureId) -> {
            obsOff.getObservableProperties().forEach((obsProp) -> {
                SensorML sensorML = getDescribeSensorResponse(procedureId, url);
                sensorML.getMembers().forEach((AbstractProcess member) -> {
                    if (member instanceof System) {
                        ((HasComponents<System>) member).getComponents().forEach((SmlComponent component) -> {
                            final String procedureComponentId = component.getName();
                            serviceConstellation.putProcedure(procedureComponentId, procedureComponentId, true, false);
                            serviceConstellation.putPhenomenon(obsProp, obsProp);
                            serviceConstellation.putCategory(obsProp, obsProp);

                            GetFeatureOfInterestResponse featureOfInterestResponse = getFeatureOfInterestResponse(procedureComponentId, obsProp, url);



                            final String foiId = "foiId";
                            serviceConstellation.putFeature(foiId, "foiName", 0, 0, 0);
                            // TODO maybe not only MeasurementDatasetConstellation
                            serviceConstellation.add(
                                    new MeasurementDatasetConstellation(procedureComponentId, offeringId,
                                            obsProp,
                                            obsProp,
                                            foiId));
                        });
                    };
                });
            });
        });
    }

    private SensorML getDescribeSensorResponse(String procedureId, String url) {
        DescribeSensorRequest request = new DescribeSensorRequest(SOS, Sos2Constants.SERVICEVERSION);
        request.setProcedure(procedureId);
        request.setProcedureDescriptionFormat("http://www.opengis.net/sensorML/1.0.0");
        return (SensorML) getSosResponseFor(request, SwesConstants.NS_SWES_20, url);
    }

    private GetFeatureOfInterestResponse getFeatureOfInterestResponse(String procedureId, String obsProp, String url) {
        GetFeatureOfInterestRequest request = new GetFeatureOfInterestRequest(SOS, Sos2Constants.SERVICEVERSION);
        request.setProcedures(new ArrayList(Arrays.asList(procedureId)));
        request.setObservedProperties(new ArrayList(Arrays.asList(obsProp)));
        return (GetFeatureOfInterestResponse) getSosResponseFor(request, Sos2Constants.NS_SOS_20, url);
    }
}
