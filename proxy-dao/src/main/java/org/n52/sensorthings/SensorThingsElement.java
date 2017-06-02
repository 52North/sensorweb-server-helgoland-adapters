package org.n52.sensorthings;

import com.google.gson.annotations.SerializedName;

/**
 * @author Jan Schulte
 */
public class SensorThingsElement {

    @SerializedName("@iot.id")
    public int iotID;

    @SerializedName("@iot.selfLink")
    public String selfLink;

    public String description;
    public String name;

}
