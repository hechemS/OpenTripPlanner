package org.opentripplanner.routing.controller;

import constraints.*;
import org.opentripplanner.routing.core.RoutingRequest;
import java.util.ArrayList;
import java.util.List;

public class DefaultController extends RequestController {

    @Override
    public void configure(RoutingRequest request) {
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
