package org.opentripplanner.routing.impl.PathFinder;

import org.opentripplanner.common.geometry.SphericalDistanceLibrary;
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
        if(request.hasBikeLocation()) {
            double distanceToOrigin = SphericalDistanceLibrary.distance(request.from.getCoordinate(), request.bikeLocation.getCoordinate());
            if (request.modes.getBicycle() && distanceToOrigin < 500) {
                return new WalkToBikePathFinder(router);
            } else if (request.modes.getBicycle() && bikeReachableFromDestination(request.from, request.to, request.bikeLocation)) {
                return new BikeToDestinationPathFinder(router);
            } else {
                request.modes.setBicycle(false);
                return new GraphPathFinder(router);
            }
        }
        if(!request.minDistanceToMode.isEmpty()) {
            return new MinimumDistancePathFinder(router);
        } else {
            return new GraphPathFinder(router);
        }
    }

    /** Determine the latitude and longitude of the middle location between @param start and @param end.
     * Inspired by https://stackoverflow.com/a/4656937.
     */
    public static GenericLocation midLocation(GenericLocation start, GenericLocation end){
        double differenceLon = Math.toRadians(end.lng - start.lng);
        double startLat = Math.toRadians(start.lat);
        double endLat = Math.toRadians(end.lat);
        double startLon = Math.toRadians(start.lng);
        double x = Math.cos(endLat) * Math.cos(differenceLon);
        double y = Math.cos(endLat) * Math.sin(differenceLon);
        double midLat = Math.atan2(Math.sin(startLat) + Math.sin(endLat), Math.sqrt((Math.cos(startLat) + x) * (Math.cos(startLat) + x) + y * y));
        double midLon = startLon + Math.atan2(y, Math.cos(startLat) + x);
        return new GenericLocation(Math.toDegrees(midLat), Math.toDegrees(midLon));
    }

    public static boolean bikeReachableFromDestination(GenericLocation start, GenericLocation end, GenericLocation bikeLocation) {
        GenericLocation middle = midLocation(start, end);
        double r = SphericalDistanceLibrary.distance(middle.getCoordinate(), end.getCoordinate()) + 500;
        double bikeToMid = SphericalDistanceLibrary.distance(bikeLocation.getCoordinate(), middle.getCoordinate());
        double bikeToDestination = SphericalDistanceLibrary.distance(bikeLocation.getCoordinate(), end.getCoordinate());
        return bikeToMid < r && bikeToDestination < 3000;
    }
}
