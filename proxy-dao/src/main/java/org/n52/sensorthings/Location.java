package org.n52.sensorthings;

import com.google.gson.annotations.SerializedName;

/**
 * @author Jan Schulte
 */
public class Location extends SensorThingsElement {

    public String encodingType;

    public Point location;

    @SerializedName("Things@iot.navigationLink")
    public String thingsLink;

    @SerializedName("HistoricalLocations@iot.navigationLink")
    public String historicalLocationsLink;

}
