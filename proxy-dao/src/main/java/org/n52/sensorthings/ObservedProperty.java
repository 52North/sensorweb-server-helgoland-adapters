package org.n52.sensorthings;

import com.google.gson.annotations.SerializedName;

/**
 * @author Jan Schulte
 */
public class ObservedProperty extends SensorThingsElement {

    public String definition;

    @SerializedName("Datastreams@iot.navigationLink")
    String datastreamsLink;

}
