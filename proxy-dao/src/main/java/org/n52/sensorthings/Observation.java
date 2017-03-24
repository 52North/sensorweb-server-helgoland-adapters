package org.n52.sensorthings;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

/**
 * @author Jan Schulte
 */
public class Observation extends SensorThingsElement {

    public Date phenomenonTime;

    public double result;

    public String resultTime;

    @SerializedName("Datastream@iot.navigationLink")
    public String datastreamLink;

    @SerializedName("FeatureOfInterest@iot.navigationLink")
    public String featureOfInterestLink;

}
