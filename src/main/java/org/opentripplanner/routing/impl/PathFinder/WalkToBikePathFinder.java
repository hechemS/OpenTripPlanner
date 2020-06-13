package org.opentripplanner.routing.impl.PathFinder;

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
    //private List<GraphPath> getGraphPathConsideringBikeLocation (RoutingRequest request) {
    public List<GraphPath> getGraphPathsConsideringIntermediates (RoutingRequest request) {
        Collection<Vertex> temporaryVertices = new ArrayList<>();
        if (request.hasBikeLocation()) {
            GraphPath pathFromBike;
            GraphPath pathToBike;
            if (request.arriveBy) {
                pathFromBike = getGraphPath(request, temporaryVertices, request.dateTime,
                        request.modes, request.bikeLocation, request.to);
                if (pathFromBike == null) {
                    return new ArrayList<>();
                }
                pathToBike = getGraphPath(request, temporaryVertices, pathFromBike.getStartTime(),
                        new TraverseModeSet("WALK"), request.from, request.bikeLocation);
                if (pathToBike == null) {
                    return new ArrayList<>();
                }
            } else {
                pathToBike = getGraphPath(request, temporaryVertices, request.dateTime,
                        new TraverseModeSet("WALK"), request.from, request.bikeLocation);
                if (pathToBike == null) {
                    return new ArrayList<>();
                }
                pathFromBike = getGraphPath(request, temporaryVertices, pathToBike.getEndTime(),
                        request.modes, request.bikeLocation, request.to);
                if (pathFromBike == null) {
                    return new ArrayList<>();
                }
            }
            request.setRoutingContext(router.graph);
            request.rctx.debugOutput = debugOutput;
            debugOutput = null;
            TraverseMode switchMode = TraverseMode.BICYCLE;
            return Collections.singletonList(joinParts(pathToBike, pathFromBike, switchMode));
        } else {
            return getPaths(request);
        }
    }
}
