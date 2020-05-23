package org.opentripplanner.routing.impl.PathFinder;

import org.opentripplanner.common.model.GenericLocation;
import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.standalone.Router;

public class PathFinderPolicy {

    public static PathFinder selectPathFinder(RoutingRequest request, Router router) {
        if(request.hasBikeLocation()) {
            double distanceToOrigin = distance(request.from, request.bikeLocation);
            double distanceToDestination = distance(request.bikeLocation, request.to);
            if (request.modes.getBicycle() && distanceToOrigin < 500) {
                System.out.println("walk to bike");
                return new WalkToBikePathFinder(router);
            } else if (request.modes.getBicycle() && distanceToDestination < 3000) {
                System.out.println("bike to destination");
                return new BikeToDestinationPathFinder(router);
            } else {
                request.modes.setBicycle(false);
                return new GraphPathFinder(router);
            }
        }
        return new GraphPathFinder(router);
    }

    public static double distance(GenericLocation start, GenericLocation end) {
        final int R = 6371;
        double latDistance = Math.toRadians(end.lat - start.lat);
        double lonDistance = Math.toRadians(end.lng - start.lng);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(start.lat)) * Math.cos(Math.toRadians(end.lat))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c * 1000;
    }
}
