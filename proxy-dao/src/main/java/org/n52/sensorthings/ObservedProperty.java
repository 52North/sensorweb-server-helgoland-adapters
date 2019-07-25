package org.n52.sensorthings;

import com.google.gson.annotations.SerializedName;

/**
 * @author Jan Schulte
 */
public class ObservedProperty extends SensorThingsElement {

    private String definition;

    @SerializedName("Datastreams@iot.navigationLink")
    private String datastreamsLink;

    /**
     * @return the definition
     */
    public String getDefinition() {
        return definition;
    }

    /**
     * @param definition the definition to set
     */
    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getDatastreamsLink() {
        return datastreamsLink;
    }

    public void setDatastreamsLink(String datastreamsLink) {
        this.datastreamsLink = datastreamsLink;
    }

}
