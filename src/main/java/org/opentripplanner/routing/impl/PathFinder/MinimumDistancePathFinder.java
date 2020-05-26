package org.opentripplanner.routing.impl.PathFinder;

import constraints.context.TransportationMode;
import org.opentripplanner.api.resource.DebugOutput;
import org.opentripplanner.common.model.GenericLocation;
import org.opentripplanner.routing.algorithm.strategies.MinimumDistanceTerminationStrategy;
import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.core.TraverseModeSet;
import org.opentripplanner.routing.graph.Vertex;
import org.opentripplanner.routing.spt.GraphPath;
import org.opentripplanner.standalone.Router;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class MinimumDistancePathFinder extends PathFinder {

    public MinimumDistancePathFinder(Router router) {
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

    public List<GraphPath> getGraphPathsConsideringIntermediates(RoutingRequest request) {
        Collection<Vertex> temporaryVertices = new ArrayList<>();
        GraphPath minDistancePath = getMinimumDistancePath(request, temporaryVertices, request.dateTime);
        if (minDistancePath == null) {
            return new ArrayList<>();
        }
        GenericLocation midLocation = new GenericLocation(minDistancePath.getEndVertex().getCoordinate());
        GraphPath pathToDestination = getPathToDestination(request, temporaryVertices, minDistancePath.getEndTime(), midLocation);
        if (pathToDestination == null) {
            return new ArrayList<>();
        }
        request.setRoutingContext(router.graph);
        request.rctx.debugOutput = debugOutput;
        debugOutput = null;
        return Collections.singletonList(joinParts(minDistancePath, pathToDestination, minDistancePath.states.getLast().getBackMode()));
    }

    private GraphPath getMinimumDistancePath (RoutingRequest request, Collection<Vertex> temporaryVertices, long time) {
        GraphPath lastPath = null;
        GraphPath currentPath = null;
        GenericLocation startLocation = request.from;
        for (TransportationMode mode : request.minDistanceToMode.keySet()) {
            TraverseMode traverseMode = TraverseMode.transportationToTraverseMode(mode);
            RoutingRequest intermediateRequest = request.clone();
            intermediateRequest.setNumItineraries(1);
            intermediateRequest.dateTime = time;
            intermediateRequest.from = startLocation;
            intermediateRequest.to = request.to;
            intermediateRequest.rctx = null;
            intermediateRequest.setRoutingContext(router.graph, temporaryVertices);
            intermediateRequest.setModes(new TraverseModeSet(traverseMode));
            if (debugOutput != null) {
                intermediateRequest.rctx.debugOutput = debugOutput;
            } else {
                debugOutput = intermediateRequest.rctx.debugOutput;
            }
            strategy = new MinimumDistanceTerminationStrategy(traverseMode, request.minDistanceToMode.get(mode));
            List<GraphPath> paths = getPaths(intermediateRequest);
            if (paths.size() == 0) {
                return null;
            }
            if (lastPath != null) {
                currentPath = joinParts(lastPath, paths.get(0), lastPath.states.getLast().getBackMode());
            } else {
                currentPath = paths.get(0);
            }
            lastPath = currentPath;
            startLocation = new GenericLocation(paths.get(0).getEndVertex().getCoordinate());
            time = paths.get(0).getEndTime();
        }
        return currentPath;
    }

    private GraphPath getPathToDestination (RoutingRequest request, Collection<Vertex> temporaryVertices, long time, GenericLocation startLocation) {
        RoutingRequest intermediateRequest = request.clone();
        intermediateRequest.setNumItineraries(1);
        intermediateRequest.dateTime = time;
        intermediateRequest.from = startLocation;
        intermediateRequest.to = request.to;
        intermediateRequest.rctx = null;
        intermediateRequest.setRoutingContext(router.graph, temporaryVertices);
        intermediateRequest.setModes(request.modes);
        intermediateRequest.modes.setBicycle(false);
        if (debugOutput != null) {
            intermediateRequest.rctx.debugOutput = debugOutput;
        } else {
            debugOutput = intermediateRequest.rctx.debugOutput;
        }
        strategy = null;
        List<GraphPath> paths = getPaths(intermediateRequest);
        if (paths.size() == 0) {
            return null;
        } else {
            return paths.get(0);
        }
    }

}
