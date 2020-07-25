package org.opentripplanner.routing.controller;

import constraints.*;
import constraints.context.TransportationMode;
import org.opentripplanner.routing.core.RoutingRequest;
import java.util.ArrayList;
import java.util.List;

public class RushHourController extends RequestController {

    String Sendlinger_Tor = "1:de:09162:50:35:75,1:de:09162:50:53:53,1:de:09162:50:1:1,1:de:09162:50:1:8," +
            "1:de:09162:50:3:5,1:de:09162:50:3:3,1:de:09162:50:54:54,1:de:09162:50:10:10,1:de:09162:50:2:2," +
            "1:de:09162:50:4:4,1:de:09162:50:11:11,1:de:09162:50:51:51,1:de:09162:50:51:52";
    String Hauptbahnhof =  "1:de:09162:6:51:53,1:de:09162:6:51:51,1:de:09162:6:55:56,1:de:09162:6:55:55,1:de:09162:6:2:2,1:de:09162:6:2:1," +
            "1:de:09162:6:52:54,1:de:09162:6:52:52,1:de:09162:6:32:72";

    @Override
    public void configure(RoutingRequest request, String bannedRoutes, String bannedStopsHard, String preferredRoutes, Integer otherThanPreferredRoutesPenalty) {
        request.setWalkReluctance(3);
        // set preferred routes
        setPreferredRoutes(request, bannedRoutes);
        request.setOtherThanPreferredRoutesPenalty(900);
        // avoid Sendlinger Tor and Hauptbahnhof.
        if (bannedStopsHard == null) {
            request.setBannedStopsHard(Sendlinger_Tor + "," + Hauptbahnhof);
        } else {
            request.setBannedStopsHard(bannedStopsHard + "," + Sendlinger_Tor + "," + Hauptbahnhof);
        }

        List<Constraint> constraints = new ArrayList<>();
        constraints.add(singleBikeUsageConstraint());
        if (!request.minDistanceToMode.containsKey(TransportationMode.WALK) || request.minDistanceToMode.get(TransportationMode.WALK) < 3000) {
            constraints.add(maximumWalkingDistanceConstraint(3000));
        }
        if (!request.minDistanceToMode.containsKey(TransportationMode.BIKE) || request.minDistanceToMode.get(TransportationMode.BIKE) < 5000) {
            constraints.add(maximumCyclingDistanceConstraint(5000));
        }
        constraints.add(transferPenalty(2, 300));
        constraints.add(transferPenalty(3, 600));
        constraints.add(maximumTransfersConstraint(3));
        NestedConstraint constraint = new NestedConstraint(constraints, true);
        request.constraintController.addConstraint(constraint);
    }
}
