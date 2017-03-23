package org.n52.sensorthings;

import com.google.gson.annotations.SerializedName;

/**
 * @author Jan Schulte
 */
public class Thing extends SensorThingsElement {

    public ThingProperties properties;

    @SerializedName("Datastreams@iot.navigationLink")
    public String datastreamsLink;

    @SerializedName("HistoricalLocations@iot.navigationLink")
    public String historicalLocationsLink;

    @SerializedName("Locations@iot.navigationLink")
    public String locationsLink;

}

class ThingProperties {

    String displayName;

}
