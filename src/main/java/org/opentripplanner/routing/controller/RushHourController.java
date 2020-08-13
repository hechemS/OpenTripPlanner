package org.opentripplanner.routing.controller;

import constraints.*;
import constraints.context.TransportationMode;
import org.opentripplanner.routing.core.RoutingRequest;
import java.util.ArrayList;
import java.util.List;

public class RushHourController extends RequestController {

    @Override
    public void configure(RoutingRequest request, String bannedRoutes, String bannedStops, String preferredRoutes, Integer otherThanPreferredRoutesPenalty) {
        request.setWalkReluctance(3);
        // set preferred routes
        setPreferredRoutes(request, preferredRoutes);
        request.setOtherThanPreferredRoutesPenalty(900);
        // avoid Sendlinger Tor and Hauptbahnhof.
        if (bannedStops == null) {
            request.setBannedStops(request.rushHourBannedStops);
        } else {
            request.setBannedStops(bannedStops + "," + request.rushHourBannedStops);
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
