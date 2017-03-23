package org.n52.sensorthings;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;

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
