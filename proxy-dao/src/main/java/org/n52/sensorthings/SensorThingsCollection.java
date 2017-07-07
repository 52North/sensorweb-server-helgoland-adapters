package org.n52.sensorthings;

import java.util.ArrayList;

import com.google.gson.annotations.SerializedName;

/**
 * @author Jan Schulte
 */
public class SensorThingsCollection<T> {

    @SerializedName("@iot.count")
    public int observations;

    @SerializedName("@iot.nextLink")
    public String nextLink;

    public ArrayList<T> value;

}
