package org.opentripplanner.routing.impl.PathFinder;

import org.opentripplanner.api.resource.DebugOutput;
import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.routing.core.TraverseModeSet;
import org.opentripplanner.routing.error.PathNotFoundException;
import org.opentripplanner.routing.error.VertexNotFoundException;
import org.opentripplanner.routing.graph.Vertex;
import org.opentripplanner.routing.spt.GraphPath;
import org.opentripplanner.standalone.Router;
import java.util.*;

public class WalkToBikePathFinder extends PathFinder {

    public WalkToBikePathFinder(Router router) { super(router); }

    /* Try to find N paths through the Graph */
    public List<GraphPath> graphPathFinderEntryPoint (RoutingRequest request) {

        // We used to perform a protective clone of the RoutingRequest here.
        // There is no reason to do this if we don't modify the request.
        // Any code that changes them should be performing the copy!

        List<GraphPath> paths = null;
        try {
            paths = getGraphPathConsideringBikeLocation(request);
            if (paths == null && request.wheelchairAccessible) {
                // There are no paths that meet the user's slope restrictions.
                // Try again without slope restrictions, and warn the user in the response.
                RoutingRequest relaxedRequest = request.clone();
                relaxedRequest.maxSlope = Double.MAX_VALUE;
                request.rctx.slopeRestrictionRemoved = true;
                paths = getGraphPathConsideringBikeLocation(relaxedRequest);
            }
            request.rctx.debugOutput.finishedCalculating();

        } catch (VertexNotFoundException e) {
            LOG.info("Vertex not found: " + request.from + " : " + request.to);
            throw e;
        }

        // Detect and report that most obnoxious of bugs: path reversal asymmetry.
        // Removing paths might result in an empty list, so do this check before the empty list check.
        if (paths != null) {
            Iterator<GraphPath> gpi = paths.iterator();
            while (gpi.hasNext()) {
                GraphPath graphPath = gpi.next();
                // TODO check, is it possible that arriveBy and time are modifed in-place by the search?
                if (request.arriveBy) {
                    if (graphPath.states.getLast().getTimeSeconds() > request.dateTime) {
                        LOG.error("A graph path arrives after the requested time. This implies a bug.");
                        gpi.remove();
                    }
                } else {
                    if (graphPath.states.getFirst().getTimeSeconds() < request.dateTime) {
                        LOG.error("A graph path leaves before the requested time. This implies a bug.");
                        gpi.remove();
                    }
                }
            }
        }

        if (paths == null || paths.size() == 0) {
            LOG.debug("Path not found: " + request.from + " : " + request.to);
            request.rctx.debugOutput.finishedRendering(); // make sure we still report full search time
            throw new PathNotFoundException();
        }

        return paths;
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
    DebugOutput debugOutput = null;

    private List<GraphPath> getGraphPathConsideringBikeLocation (RoutingRequest request) {
        Collection<Vertex> temporaryVertices = new ArrayList<>();
        if (request.hasBikeLocation()) {
            GraphPath pathAfterBike;
            GraphPath pathBeforeBike;
            if (request.arriveBy) {
                pathAfterBike = getGraphPathFromBike(request, temporaryVertices, request.dateTime);
                if (pathAfterBike == null) {
                    return new ArrayList<GraphPath>();
                }
                pathBeforeBike = getGraphPathToBike(request, temporaryVertices, pathAfterBike.getStartTime());
                if (pathBeforeBike == null) {
                    return new ArrayList<GraphPath>();
                }
            } else {
                pathBeforeBike = getGraphPathToBike(request, temporaryVertices, request.dateTime);
                if (pathBeforeBike == null) {
                    return new ArrayList<GraphPath>();
                }
                pathAfterBike = getGraphPathFromBike(request, temporaryVertices, pathBeforeBike.getEndTime());
                if (pathAfterBike == null) {
                    return new ArrayList<GraphPath>();
                }
            }
            request.setRoutingContext(router.graph);
            request.rctx.debugOutput = debugOutput;
            debugOutput = null;
            return Collections.singletonList(joinParts(pathBeforeBike, pathAfterBike));
        } else {
            return getPaths(request);
        }
    }

    private GraphPath getGraphPathToBike (RoutingRequest request, Collection<Vertex> temporaryVertices, long time) {
        RoutingRequest intermediateRequest = request.clone();
        intermediateRequest.setNumItineraries(1);
        intermediateRequest.dateTime = time;
        intermediateRequest.from = request.from;
        intermediateRequest.to = request.bikeLocation;
        intermediateRequest.rctx = null;
        intermediateRequest.setRoutingContext(router.graph, temporaryVertices);
        intermediateRequest.setModes(new TraverseModeSet("WALK"));
        if (debugOutput != null) {
            intermediateRequest.rctx.debugOutput = debugOutput;
        } else {
            debugOutput = intermediateRequest.rctx.debugOutput;
        }
        List<GraphPath> paths = getPaths(intermediateRequest);
        if (paths.size() == 0) {
            return null;
        } else {
            return paths.get(0);
        }
    }

    private GraphPath getGraphPathFromBike (RoutingRequest request, Collection<Vertex> temporaryVertices, long time) {
        RoutingRequest intermediateRequest = request.clone();
        intermediateRequest.setNumItineraries(1);
        intermediateRequest.dateTime = time;
        intermediateRequest.from = request.bikeLocation;
        intermediateRequest.to = request.to;
        intermediateRequest.rctx = null;
        intermediateRequest.setRoutingContext(router.graph, temporaryVertices);
        intermediateRequest.setModes(request.modes);
        if (debugOutput != null) {
            intermediateRequest.rctx.debugOutput = debugOutput;
        } else {
            debugOutput = intermediateRequest.rctx.debugOutput;
        }
        List<GraphPath> paths = getPaths(intermediateRequest);
        if (paths.size() == 0) {
            return null;
        } else {
            return paths.get(0);
        }
    }



}
