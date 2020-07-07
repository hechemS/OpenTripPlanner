package org.opentripplanner.routing.impl.PathFinder;

import constraints.context.TransportationMode;
import org.opentripplanner.api.resource.DebugOutput;
import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.core.TraverseModeSet;
import org.opentripplanner.routing.error.PathNotFoundException;
import org.opentripplanner.routing.error.VertexNotFoundException;
import org.opentripplanner.routing.graph.Vertex;
import org.opentripplanner.routing.spt.GraphPath;
import org.opentripplanner.standalone.Router;
import java.util.*;

public class WalkToBikePathFinder extends PathFinder {

    public WalkToBikePathFinder(Router router) { super(router); }

    /**
     * Break up a RoutingRequest with intermediate bike location into two separate requests,
     * before and after the position of the bike.
     *
     * If there is no bike location, issue a single request. Otherwise process the two requests
     *  either from left to right (if {@code request.arriveBy==false})
     * or from right to left (if {@code request.arriveBy==true}). In the latter case the order of
     * the requested paths is (bikeLocation, to), and (from, bikeLocation).
     */

    public List<GraphPath> getGraphPathsConsideringIntermediates (RoutingRequest request) {
        PathFinder minPathFinder = null;
        if (!request.minDistanceToMode.isEmpty()) {
            minPathFinder = new MinimumDistancePathFinder(router);
        }
        Collection<Vertex> temporaryVertices = new ArrayList<>();
        if (request.hasBikeLocation()) {
            List<GraphPath> pathFromBike;
            List<GraphPath> pathToBike;
            if (request.arriveBy) {
                if (request.minDistanceToMode.containsKey(TransportationMode.BIKE)) {
                    RoutingRequest cloneRequest = request.clone();
                    cloneRequest.dateTime = request.dateTime;
                    cloneRequest.from = request.bikeLocation;
                    cloneRequest.to = request.to;
                    cloneRequest.bikeLocation = null;
                    List<GraphPath> paths = minPathFinder.graphPathFinderEntryPoint(cloneRequest);
                    if (paths != null) {
                        pathFromBike = paths;
                    } else {
                        pathFromBike = null;
                    }
                } else {
                    pathFromBike = getGraphPath(request, temporaryVertices, request.dateTime,
                            request.modes, request.bikeLocation, request.to, 3);
                }
                if (pathFromBike == null) {
                    return new ArrayList<>();
                }
                pathToBike = getGraphPath(request, temporaryVertices, pathFromBike.get(0).getStartTime(),
                        new TraverseModeSet("WALK"), request.from, request.bikeLocation, 1);
                if (pathToBike == null) {
                    return new ArrayList<>();
                }
            } else {
                pathToBike = getGraphPath(request, temporaryVertices, request.dateTime,
                        new TraverseModeSet("WALK"), request.from, request.bikeLocation, 1);
                if (pathToBike == null) {
                    return new ArrayList<>();
                }

                if (request.minDistanceToMode.containsKey(TransportationMode.BIKE)) {
                    RoutingRequest cloneRequest = request.clone();
                    cloneRequest.dateTime = pathToBike.get(0).getEndTime();
                    cloneRequest.from = request.bikeLocation;
                    cloneRequest.to = request.to;
                    cloneRequest.bikeLocation = null;
                    List<GraphPath> paths = minPathFinder.graphPathFinderEntryPoint(cloneRequest);
                    if (paths != null) {
                        pathFromBike = paths;
                    } else {
                        pathFromBike = null;
                    }
                } else {
                    pathFromBike = getGraphPath(request, temporaryVertices, pathToBike.get(0).getEndTime(),
                            request.modes, request.bikeLocation, request.to, 3);
                }
                if (pathFromBike == null) {
                    return new ArrayList<>();
                }
            }
            request.setRoutingContext(router.graph);
            request.rctx.debugOutput = debugOutput;
            debugOutput = null;
            TraverseMode switchMode = TraverseMode.BICYCLE;
            return joinParts(pathToBike, pathFromBike, switchMode, false);
        } else {
            return getPaths(request);
        }
    }
}
