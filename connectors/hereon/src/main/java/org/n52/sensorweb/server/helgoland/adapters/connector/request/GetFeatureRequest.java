package org.n52.sensorweb.server.helgoland.adapters.connector.request;

import org.n52.sensorweb.server.helgoland.adapters.connector.HereonConstants;

public class GetFeatureRequest extends AbstractHereonRequest implements HereonConstants {
    
    public GetFeatureRequest() {
        withGeometry(false);
    }

}
