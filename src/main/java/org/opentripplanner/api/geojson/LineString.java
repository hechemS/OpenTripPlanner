package org.opentripplanner.api.geojson;

import java.util.ArrayList;
import java.util.List;

public class LineString {
    String type = "LineString";
    List<List<Float>> coordinates;

    public LineString(List<List<Float>> coordinates) {
        this.coordinates = coordinates;
    }

    public static LineString fromEncodedPolyLine(String polyLine) {

        List<List<Float>> line = new ArrayList<>();
        int index = 0, len = polyLine.length();
        float lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = polyLine.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = polyLine.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            List<Float> l = new ArrayList<>();
            l.add(lng / 100000);
            l.add(lat / 100000);
            line.add(l);
        }

        return new LineString(line);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<List<Float>> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<List<Float>> coordinates) {
        this.coordinates = coordinates;
    }
}
