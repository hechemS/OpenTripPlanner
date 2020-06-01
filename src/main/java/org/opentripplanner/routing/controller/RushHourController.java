package org.opentripplanner.routing.controller;

import constraints.*;
import org.opentripplanner.routing.core.RoutingRequest;
import java.util.ArrayList;
import java.util.List;

public class RushHourController extends RequestController {

    @Override
    public void configure(RoutingRequest request, String bannedRoutes, String bannedStopsHard) {
        String Sendlinger_Tor = "1:de:09162:50:32:42,1:de:09162:50:53:53,1:de:09162:50:1:1,1:de:09162:50:1:8," +
                "1:de:09162:50:3:5,1:de:09162:50:3:3,1:de:09162:50:54:54,1:de:09162:50:10:10,1:de:09162:50:2:2," +
                "1:de:09162:50:4:4,1:de:09162:50:11:11,1:de:09162:50:51:51,1:de:09162:50:51:52";
        String Hauptbahnhof =  "1:de:09162:6:51:53,1:de:09162:6:51:51,1:de:09162:6:55:56,1:de:09162:6:55:55,1:de:09162:6:2:2,1:de:09162:6:2:1," +
                "1:de:09162:6:52:54, 1:de:09162:6:52:52,1:de:09162:6:32:72";
        String Marienplatz = "1:de:09162:2:52:52,1:de:09162:2:51:51,1:de:09162:2:31:71";
        String U6 = "1__1-U6-G-009-19,1__1-U6-G-010-3,1__1-U6-G-010-2,1__1-U6-G-010-1";
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
        constraints.add(maximumCyclingDistanceConstraint());
        //constraints.add(minimumCyclingDistanceConstraint());
        constraints.add(maximumWalkingDistanceConstraint());
        constraints.add(maximumTransfersConstraint());
        constraints.add(firstTransferPenalty());
        constraints.add(secondTransferPenalty());
        NestedConstraint constraint = new NestedConstraint(constraints, true);
        request.constraintController.addConstraint(constraint);
    }
}
