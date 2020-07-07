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
    String Marienplatz = "1:de:09162:2:52:52,1:de:09162:2:51:51";
    String U6 = "1__1-U6-G-010-10,1__1-U6-G-010-12,1__1-U6-G-010-11,1__1-U6-G-010-14,1__1-U6-G-010-13," +
            "1__1-U6-G-010-16,1__1-U6-G-010-15,1__1-U6-G-010-17,1__1-U6-G-010-7,1__1-U6-G-010-5," +
            "1__1-U6-G-010-9,1__1-U6-G-010-8,1__3-506-G-010-100,1__3-506-G-010-105,1__1-U6-G-010-1";

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
        // don't use the U6.
        if (bannedRoutes == null) {
            request.setBannedRoutes(U6);
        } else {
            request.setBannedRoutes(bannedRoutes + "," + U6);
        }
        // set constraints.
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
