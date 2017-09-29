package org.n52.sensorthings;

import com.google.gson.annotations.SerializedName;

/**
 * @author Jan Schulte
 */
public class Sensor extends SensorThingsElement {

    private String encodingType;

    private String metadata;

    @SerializedName("Datastreams@iot.navigationLink")
    private String datastreamsLink;

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
     * @return the metadata
     */
    public String getMetadata() {
        return metadata;
    }

    /**
     * @param metadata the metadata to set
     */
    public void setMetadata(String metadata) {
        this.metadata = metadata;
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

}
