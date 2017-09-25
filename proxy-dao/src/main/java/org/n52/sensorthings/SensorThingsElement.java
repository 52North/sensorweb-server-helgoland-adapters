package org.n52.sensorthings;

import com.google.gson.annotations.SerializedName;

/**
 * @author Jan Schulte
 */
public class SensorThingsElement {

    @SerializedName("@iot.id")
    private int iotID;

    @SerializedName("@iot.selfLink")
    private String selfLink;

    private String description;
    private String name;

    /**
     * @return the iotID
     */
    public int getIotID() {
        return iotID;
    }

    /**
     * @param iotID the iotID to set
     */
    public void setIotID(int iotID) {
        this.iotID = iotID;
    }

    /**
     * @return the selfLink
     */
    public String getSelfLink() {
        return selfLink;
    }

    /**
     * @param selfLink the selfLink to set
     */
    public void setSelfLink(String selfLink) {
        this.selfLink = selfLink;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

}
