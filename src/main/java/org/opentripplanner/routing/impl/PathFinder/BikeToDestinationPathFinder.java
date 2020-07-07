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

public class BikeToDestinationPathFinder extends PathFinder {

    public BikeToDestinationPathFinder(Router router) {
        super(router);
    }

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
            List<GraphPath> pathFromBike = null;
            List<GraphPath> pathToBike = null;
            TraverseModeSet modeSet = request.modes.clone();
            modeSet.setBicycle(false);
            if (request.arriveBy) {
                if (request.minDistanceToMode.containsKey(TransportationMode.BIKE)) {
                    RoutingRequest cloneRequest = request.clone();
                    cloneRequest.dateTime = request.dateTime;
                    cloneRequest.from = request.bikeLocation;
                    cloneRequest.to = request.to;
                    cloneRequest.modes = new TraverseModeSet("BICYCLE");
                    cloneRequest.bikeLocation = null;
                    List<GraphPath> paths = minPathFinder.graphPathFinderEntryPoint(cloneRequest);
                    if (paths != null) {
                        pathFromBike = paths;
                    } else {
                        pathFromBike = null;
                    }
                } else {
                    pathFromBike = getGraphPath(request, temporaryVertices, request.dateTime,
                            new TraverseModeSet("BICYCLE"), request.bikeLocation, request.to, 1);
                }
                if (pathFromBike == null) {
                    return new ArrayList<>();
                }
                for (GraphPath pathFrom : pathFromBike) {
                    List<GraphPath> pathTo = getGraphPath(request, temporaryVertices, pathFrom.getStartTime(),
                            modeSet, request.from, request.bikeLocation, 1);
                    if (pathTo != null) {
                        if (pathToBike == null) {
                            pathToBike = new ArrayList<>();
                        }
                        pathToBike.add(pathTo.get(0));
                    }

                }
                if (pathToBike == null) {
                    return new ArrayList<>();
                }
            } else {
                pathToBike = getGraphPath(request, temporaryVertices, request.dateTime,
                        modeSet ,request.from, request.bikeLocation, 3);
                if (pathToBike == null) {
                    return new ArrayList<>();
                }
                if (request.minDistanceToMode.containsKey(TransportationMode.BIKE)) {
                    for (GraphPath pathTo : pathToBike) {
                        RoutingRequest cloneRequest = request.clone();
                        cloneRequest.dateTime = pathTo.getEndTime();
                        cloneRequest.from = request.bikeLocation;
                        cloneRequest.to = request.to;
                        cloneRequest.modes = new TraverseModeSet("BICYCLE");
                        cloneRequest.bikeLocation = null;
                        cloneRequest.setNumItineraries(1);
                        List<GraphPath> paths = minPathFinder.graphPathFinderEntryPoint(cloneRequest);
                        if (paths != null) {
                            if (pathFromBike == null) {
                                pathFromBike = new ArrayList<>();
                            }
                            pathFromBike.add(paths.get(0));
                        } else {
                            pathFromBike = null;
                        }
                    }
                } else {
                    for (GraphPath pathTo : pathToBike) {
                        List<GraphPath> pathFrom = getGraphPath(request, temporaryVertices, pathTo.getEndTime(),
                                new TraverseModeSet("BICYCLE"), request.bikeLocation, request.to, 1);
                        if (pathFrom != null) {
                            if (pathFromBike == null) {
                                pathFromBike = new ArrayList<>();
                            }
                            pathFromBike.add(pathFrom.get(0));
                        }
                    }
                }
                if (pathFromBike == null) {
                    return new ArrayList<>();
                }
            }
            request.setRoutingContext(router.graph);
            request.rctx.debugOutput = debugOutput;
            debugOutput = null;
            TraverseMode switchMode = pathToBike.get(0).states.getLast().getBackMode();
            return joinParts(pathToBike, pathFromBike, switchMode, false);
        } else {
            return getPaths(request);
        }
    }
}
