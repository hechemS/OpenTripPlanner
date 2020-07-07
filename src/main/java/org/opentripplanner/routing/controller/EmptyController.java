package org.opentripplanner.routing.controller;

import constraints.Constraint;
import constraints.NestedConstraint;
import org.opentripplanner.routing.core.RoutingRequest;
import java.util.ArrayList;
import java.util.List;

public class EmptyController extends RequestController {

    @Override
    public void configure(RoutingRequest request, String bannedRoutes, String bannedStopsHard, String preferredRoutes, Integer otherThanPreferredRoutesPenalty) {
        // set constraints.
        List<Constraint> constraints = new ArrayList<>();
        constraints.add(singleBikeUsageConstraint());
        NestedConstraint constraint = new NestedConstraint(constraints, true);
        request.constraintController.addConstraint(constraint);
    }
}
