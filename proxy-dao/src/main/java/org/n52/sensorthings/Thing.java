package org.n52.sensorthings;

import com.google.gson.annotations.SerializedName;

/**
 * @author Jan Schulte
 */
public class Thing extends SensorThingsElement {

    private ThingProperties properties;

    @SerializedName("Datastreams@iot.navigationLink")
    private String datastreamsLink;

    @SerializedName("HistoricalLocations@iot.navigationLink")
    private String historicalLocationsLink;

    @SerializedName("Locations@iot.navigationLink")
    private String locationsLink;


    /**
     * @return the properties
     */
    public ThingProperties getProperties() {
        return properties;
    }

    /**
     * @param properties the properties to set
     */
    public void setProperties(ThingProperties properties) {
        this.properties = properties;
    }

    /**
     * @return the datastreamsLink
     */
    public String getDatastreamsLink() {
        return datastreamsLink;
    }

    /**
     * @param datastreamsLink the datastreamsLink to set
     */
    public void setDatastreamsLink(String datastreamsLink) {
        this.datastreamsLink = datastreamsLink;
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

    /**
     * @return the locationsLink
     */
    public String getLocationsLink() {
        return locationsLink;
    }

    /**
     * @param locationsLink the locationsLink to set
     */
    public void setLocationsLink(String locationsLink) {
        this.locationsLink = locationsLink;
    }
}
