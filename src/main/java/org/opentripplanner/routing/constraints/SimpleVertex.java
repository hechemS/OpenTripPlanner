package org.opentripplanner.routing.constraints;

import org.opentripplanner.routing.graph.Vertex;

public class SimpleVertex {
    double lon;

    double lat;

    public SimpleVertex(double lon, double lat) {
        this.lon = lon;
        this.lat = lat;
    }

    public SimpleVertex(Vertex v) {
        this.lon = v.getLon();
        this.lat = v.getLat();
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }
}
