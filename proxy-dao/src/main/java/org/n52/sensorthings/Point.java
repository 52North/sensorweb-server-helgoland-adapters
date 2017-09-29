package org.n52.sensorthings;

import java.util.List;

/**
 * @author Jan Schulte
 */
public class Point {

    private List<Double> coordinates;

    private String type;

    /**
     * @return the coordinates
     */
    public List<Double> getCoordinates() {
        return coordinates;
    }

    /**
     * @param coordinates the coordinates to set
     */
    public void setCoordinates(List<Double> coordinates) {
        this.coordinates = coordinates;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

}
