package org.n52.sensorthings;

import com.google.gson.annotations.SerializedName;

/**
 * @author Jan Schulte
 */
public class Location extends SensorThingsElement {

    private String encodingType;

    private Point location;

    @SerializedName("Things@iot.navigationLink")
    private String thingsLink;

    @SerializedName("HistoricalLocations@iot.navigationLink")
    private String historicalLocationsLink;

    /**
     * @return the encodingType
     */
    public String getEncodingType() {
        return encodingType;
    }

    /**
     * @param encodingType the encodingType to set
     */
    public void setEncodingType(String encodingType) {
        this.encodingType = encodingType;
    }

    /**
     * @return the location
     */
    public Point getLocation() {
        return location;
    }

    /**
     * @param location the location to set
     */
    public void setLocation(Point location) {
        this.location = location;
    }

    /**
     * @return the thingsLink
     */
    public String getThingsLink() {
        return thingsLink;
    }

    /**
     * @param thingsLink the thingsLink to set
     */
    public void setThingsLink(String thingsLink) {
        this.thingsLink = thingsLink;
    }

    /**
     * @return the historicalLocationsLink
     */
    public String getHistoricalLocationsLink() {
        return historicalLocationsLink;
    }

    /**
     * @param historicalLocationsLink the historicalLocationsLink to set
     */
    public void setHistoricalLocationsLink(String historicalLocationsLink) {
        this.historicalLocationsLink = historicalLocationsLink;
    }

}
