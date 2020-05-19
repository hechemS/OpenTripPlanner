package org.opentripplanner.routing.controller;

import constraints.*;
import constraints.conditions.*;
import constraints.context.ConstraintContext;
import constraints.context.TransportationMode;
import org.opentripplanner.routing.core.RoutingRequest;

import java.util.ArrayList;
import java.util.List;

public class DefaultController extends RequestController {

    @Override
    public void configure(RoutingRequest request) {
        // bike available at the start of the trip
        if (bikeAccessible(request, 20)) {
            request.bikeLocation = null;
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
        request.constraintController = new ConstraintController(new ConstraintWrapper(constraint));
    }
}
