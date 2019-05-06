package org.opentripplanner.api.geojson;

import org.opentripplanner.api.model.Leg;

import java.util.HashMap;
import java.util.Map;

public class Feature {
    String type = "Feature";
    Map<String, String> properties;
    LineString geometry;

    public Feature(Map<String, String> properties, LineString geometry) {
        this.properties = properties;
        this.geometry = geometry;
    }

    public static Feature fromLeg(Leg leg) {
        Map<String, String> properties = new HashMap<>();
        properties.put("modeOfTransport", leg.mode);
        LineString geometry = LineString.fromEncodedPolyLine(leg.legGeometry.getPoints());
        return new Feature(properties, geometry);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public LineString getGeometry() {
        return geometry;
    }

    public void setGeometry(LineString geometry) {
        this.geometry = geometry;
    }
}
