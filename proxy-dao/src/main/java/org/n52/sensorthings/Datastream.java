package org.n52.sensorthings;

import com.google.gson.annotations.SerializedName;

/**
 * @author Jan Schulte
 */
public class Datastream extends SensorThingsElement {

    @SerializedName("unitOfMeasurement")
    private UnitOfMeasurement unitOfMeasurement;

    @SerializedName("observationType")
    private String observationType;

    @SerializedName("Observations@iot.navigationLink")
    private String observationsLink;

    @SerializedName("ObservedProperty@iot.navigationLink")
    private String observedPropertyLink;

    @SerializedName("Sensor@iot.navigationLink")
    private String sensorLink;

    @SerializedName("Thing@iot.navigationLink")
    private String thingLink;

    @SerializedName("Sensor")
    private Sensor sensor;

    @SerializedName("Thing")
    private Thing thing;

    @SerializedName("ObservedProperty")
    private ObservedProperty observedProperty;

    /**
     * @return the unitOfMeasurement
     */
    public UnitOfMeasurement getUnitOfMeasurement() {
        return unitOfMeasurement;
    }

    /**
     * @param unitOfMeasurement the unitOfMeasurement to set
     */
    public void setUnitOfMeasurement(UnitOfMeasurement unitOfMeasurement) {
        this.unitOfMeasurement = unitOfMeasurement;
    }

    /**
     * @return the observationType
     */
    public String getObservationType() {
        return observationType;
    }

    /**
     * @param observationType the observationType to set
     */
    public void setObservationType(String observationType) {
        this.observationType = observationType;
    }

    /**
     * @return the observationsLink
     */
    public String getObservationsLink() {
        return observationsLink;
    }

    /**
     * @param observationsLink the observationsLink to set
     */
    public void setObservationsLink(String observationsLink) {
        this.observationsLink = observationsLink;
    }

    /**
     * @return the observedPropertyLink
     */
    public String getObservedPropertyLink() {
        return observedPropertyLink;
    }

    /**
     * @param observedPropertyLink the observedPropertyLink to set
     */
    public void setObservedPropertyLink(String observedPropertyLink) {
        this.observedPropertyLink = observedPropertyLink;
    }

    /**
     * @return the sensorLink
     */
    public String getSensorLink() {
        return sensorLink;
    }

    /**
     * @param sensorLink the sensorLink to set
     */
    public void setSensorLink(String sensorLink) {
        this.sensorLink = sensorLink;
    }

    /**
     * @return the thingLink
     */
    public String getThingLink() {
        return thingLink;
    }

    /**
     * @param thingLink the thingLink to set
     */
    public void setThingLink(String thingLink) {
        this.thingLink = thingLink;
    }

    /**
     * @return the sensor
     */
    public Sensor getSensor() {
        return sensor;
    }

    /**
     * @param sensor the sensor to set
     */
    public void setSensor(Sensor sensor) {
        this.sensor = sensor;
    }

    /**
     * @return the thing
     */
    public Thing getThing() {
        return thing;
    }

    /**
     * @param thing the thing to set
     */
    public void setThing(Thing thing) {
        this.thing = thing;
    }

    /**
     * @return the observedProperty
     */
    public ObservedProperty getObservedProperty() {
        return observedProperty;
    }

    /**
     * @param observedProperty the observedProperty to set
     */
    public void setObservedProperty(ObservedProperty observedProperty) {
        this.observedProperty = observedProperty;
    }

}
