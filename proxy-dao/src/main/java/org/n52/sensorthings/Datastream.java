package org.n52.sensorthings;

import com.google.gson.annotations.SerializedName;

/**
 * @author Jan Schulte
 */
public class Datastream extends SensorThingsElement {

    @SerializedName("unitOfMeasurement")
    public UnitOfMeasurement unitOfMeasurement;

    @SerializedName("observationType")
    public String observationType;

    @SerializedName("Observations@iot.navigationLink")
    public String observationsLink;

    @SerializedName("ObservedProperty@iot.navigationLink")
    public String observedPropertyLink;

    @SerializedName("Sensor@iot.navigationLink")
    public String sensorLink;

    @SerializedName("Thing@iot.navigationLink")
    public String thingLink;

    @SerializedName("Sensor")
    public Sensor sensor;

    @SerializedName("Thing")
    public Thing thing;

    @SerializedName("ObservedProperty")
    public ObservedProperty observedProperty;

}
