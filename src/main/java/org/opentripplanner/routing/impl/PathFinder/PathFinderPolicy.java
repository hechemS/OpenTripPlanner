package org.opentripplanner.routing.impl.PathFinder;

import org.opentripplanner.common.model.GenericLocation;
import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.standalone.Router;

public class PathFinderPolicy {

    /** Decide how the bike will be used in the routing based on the distance to the origin
     * and destination of the trip.
     * If there is a bike location and the distance to the origin is less than 500 meters,
     * walk to the bike and use it immediately after finding it.
     * If the bike is withing 2000 meters from the destination, use it in the last part of the trip.
     * If the bike is out of reach from both origin and destination, don't use the bike in the trip.
     * If the bike location is not specified, assume the bike is at the origin.
     */
    public static PathFinder selectPathFinder(RoutingRequest request, Router router) {
        if(!request.minDistanceToMode.isEmpty()) {
            return new MinimumDistancePathFinder(router);
        }
        if(request.hasBikeLocation()) {
            double distanceToOrigin = haversineDistance(request.from, request.bikeLocation);
            double distanceToDestination = haversineDistance(request.bikeLocation, request.to);
            if (request.modes.getBicycle() && distanceToOrigin < 500) {
                return new WalkToBikePathFinder(router);
            } else if (request.modes.getBicycle() && distanceToDestination < 2000) {
                return new BikeToDestinationPathFinder(router);
            } else {
                request.modes.setBicycle(false);
                return new GraphPathFinder(router);
            }
        }
        return new GraphPathFinder(router);
    }

    /** Determine the great-circle distance between two points on a sphere given their longitudes and latitudes.
     */
    public static double haversineDistance(GenericLocation start, GenericLocation end) {
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
