package org.opentripplanner.api.geojson;

import org.opentripplanner.api.model.Itinerary;
import org.opentripplanner.api.model.Leg;
import org.opentripplanner.api.model.TripPlan;

import java.util.ArrayList;
import java.util.List;

public class FeatureCollection {
    String type = "FeatureCollection";
    List<Feature> features;

    public FeatureCollection(List<Feature> features) {
        this.features = features;
    }

    public static FeatureCollection fromitinerary(Itinerary itinerary) {
        List<Feature> features = new ArrayList<>();
        for(Leg leg : itinerary.legs) {
            features.add(Feature.fromLeg(leg));
        }

        return new FeatureCollection(features);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Feature> getFeatures() {
        return features;
    }

    public void setFeatures(List<Feature> features) {
        this.features = features;
    }
}
