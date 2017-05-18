package org.n52.sensorthings;

import com.google.gson.annotations.SerializedName;

/**
 * @author Jan Schulte
 */
public class Sensor extends SensorThingsElement {

    String encodingType;

    String metadata;

    @SerializedName("Datastreams@iot.navigationLink")
    String datastreamsLink;

}
