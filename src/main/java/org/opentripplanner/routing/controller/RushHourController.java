package org.opentripplanner.routing.controller;

import constraints.*;
import org.opentripplanner.routing.core.RoutingRequest;

import java.util.ArrayList;
import java.util.List;

public class RushHourController extends RequestController {

    @Override
    public void configure(RoutingRequest request) {
        // avoid Marienplatz and Hauptbahnhof.
        request.setBannedStopsHard("1:de:09162:2:52:52,1:de:09162:2:51:51,1:de:09162:2:31:71" +
                "1:de:09162:6:51:53,1:de:09162:6:51:51,1:de:09162:6:55:56,1:de:09162:6:55:55," +
                "1:de:09162:6:2:2,1:de:09162:6:2:1,1:de:09162:6:52:54,1:de:09162:6:52:52,1:de:09162:6:32:72");
        // don't use the U6.
        request.setBannedRoutes("1__1-U6-G-009-19,1__1-U6-G-010-3,1__1-U6-G-010-2,1__1-U6-G-010-1");
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
        request.constraintController = new ConstraintController(new ConstraintWrapper(constraint));
    }
}
