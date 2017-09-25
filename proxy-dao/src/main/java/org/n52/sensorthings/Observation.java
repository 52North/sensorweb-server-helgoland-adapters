package org.n52.sensorthings;

import java.util.Date;

import com.google.gson.annotations.SerializedName;

/**
 * @author Jan Schulte
 */
public class Observation extends SensorThingsElement {

    private Date phenomenonTime;

    private double result;

    private String resultTime;

    @SerializedName("Datastream@iot.navigationLink")
    private String datastreamLink;

    @SerializedName("FeatureOfInterest@iot.navigationLink")
    private String featureOfInterestLink;

    /**
     * @return the phenomenonTime
     */
    public Date getPhenomenonTime() {
        return phenomenonTime;
    }

    /**
     * @param phenomenonTime the phenomenonTime to set
     */
    public void setPhenomenonTime(Date phenomenonTime) {
        this.phenomenonTime = phenomenonTime;
    }

    /**
     * @return the result
     */
    public double getResult() {
        return result;
    }

    /**
     * @param result the result to set
     */
    public void setResult(double result) {
        this.result = result;
    }

    /**
     * @return the resultTime
     */
    public String getResultTime() {
        return resultTime;
    }

    /**
     * @param resultTime the resultTime to set
     */
    public void setResultTime(String resultTime) {
        this.resultTime = resultTime;
    }

    /**
     * @return the datastreamLink
     */
    public String getDatastreamLink() {
        return datastreamLink;
    }

    /**
     * @param datastreamLink the datastreamLink to set
     */
    public void setDatastreamLink(String datastreamLink) {
        this.datastreamLink = datastreamLink;
    }

    /**
     * @return the featureOfInterestLink
     */
    public String getFeatureOfInterestLink() {
        return featureOfInterestLink;
    }

    /**
     * @param featureOfInterestLink the featureOfInterestLink to set
     */
    public void setFeatureOfInterestLink(String featureOfInterestLink) {
        this.featureOfInterestLink = featureOfInterestLink;
    }

}
