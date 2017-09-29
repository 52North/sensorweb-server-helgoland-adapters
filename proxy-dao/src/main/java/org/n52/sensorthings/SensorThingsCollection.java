package org.n52.sensorthings;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * @author Jan Schulte
 */
public class SensorThingsCollection<T> {

    @SerializedName("@iot.count")
    private int observations;

    @SerializedName("@iot.nextLink")
    private String nextLink;

    private List<T> value;

    /**
     * @return the observations
     */
    public int getObservations() {
        return observations;
    }

    /**
     * @param observations the observations to set
     */
    public void setObservations(int observations) {
        this.observations = observations;
    }

    /**
     * @return the nextLink
     */
    public String getNextLink() {
        return nextLink;
    }

    /**
     * @param nextLink the nextLink to set
     */
    public void setNextLink(String nextLink) {
        this.nextLink = nextLink;
    }

    /**
     * @return the value
     */
    public List<T> getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(List<T> value) {
        this.value = value;
    }

}
