package org.opentripplanner.routing.impl.PathFinder;

import com.google.common.collect.Lists;
import org.opentripplanner.api.resource.DebugOutput;
import org.opentripplanner.common.model.GenericLocation;
import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.routing.error.PathNotFoundException;
import org.opentripplanner.routing.error.VertexNotFoundException;
import org.opentripplanner.routing.graph.Vertex;
import org.opentripplanner.routing.spt.GraphPath;
import org.opentripplanner.standalone.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * This class contains the logic for repeatedly building shortest path trees and accumulating paths through
 * the graph until the requested number of them have been found.
 * It is used in point-to-point (i.e. not one-to-many / analyst) routing.
 *
 * Its exact behavior will depend on whether the routing request allows transit.
 *
 * When using transit it will incorporate techniques from what we called "long distance" mode, which is designed to
 * provide reasonable response times when routing over large graphs (e.g. the entire Netherlands or New York State).
 * In this case it only uses the street network at the first and last legs of the trip, and all other transfers
 * between transit vehicles will occur via SimpleTransfer edges which are pre-computed by the graph builder.
 * 
 * More information is available on the OTP wiki at:
 * https://github.com/openplans/OpenTripPlanner/wiki/LargeGraphs
 *
 * One instance of this class should be constructed per search (i.e. per RoutingRequest: it is request-scoped).
 * Its behavior is undefined if it is reused for more than one search.
 *
 * It is very close to being an abstract library class with only static functions. However it turns out to be convenient
 * and harmless to have the OTPServer object etc. in fields, to avoid passing context around in function parameters.
 */
public class GraphPathFinder extends PathFinder {

    private static final Logger LOG = LoggerFactory.getLogger(GraphPathFinder.class);
    private static final double DEFAULT_MAX_WALK = 2000;
    private static final double CLAMP_MAX_WALK = 15000;

    Router router;

    public GraphPathFinder(Router router) { super(router); }

    /**
     * Break up a RoutingRequest with intermediate places into separate requests, in the given order.
     *
     * If there are no intermediate places, issue a single request. Otherwise process the places
     * list [from, i1, i2, ..., to] either from left to right (if {@code request.arriveBy==false})
     * or from right to left (if {@code request.arriveBy==true}). In the latter case the order of
     * the requested subpaths is (i2, to), (i1, i2), and (from, i1) which has to be reversed at
     * the end.
     */
    public List<GraphPath> getGraphPathsConsideringIntermediates(RoutingRequest request) {
        Collection<Vertex> temporaryVertices = new ArrayList<>();
        if (request.hasIntermediatePlaces()) {
            List<GenericLocation> places = Lists.newArrayList(request.from);
            places.addAll(request.intermediatePlaces);
            places.add(request.to);
            long time = request.dateTime;

            List<GraphPath> paths = new ArrayList<>();
            DebugOutput debugOutput = null;
            int placeIndex = (request.arriveBy ? places.size() - 1 : 1);

            while (0 < placeIndex && placeIndex < places.size()) {
                RoutingRequest intermediateRequest = request.clone();
                intermediateRequest.setNumItineraries(1);
                intermediateRequest.dateTime = time;
                intermediateRequest.from = places.get(placeIndex - 1);
                intermediateRequest.to = places.get(placeIndex);
                intermediateRequest.rctx = null;
                intermediateRequest.setRoutingContext(router.graph, temporaryVertices);

                if (debugOutput != null) {// Restore the previous debug info accumulator
                    intermediateRequest.rctx.debugOutput = debugOutput;
                } else {// Store the debug info accumulator
                    debugOutput = intermediateRequest.rctx.debugOutput;
                }

                List<GraphPath> partialPaths = getPaths(intermediateRequest);
                if (partialPaths.size() == 0) {
                    return partialPaths;
                }

                GraphPath path = partialPaths.get(0);
                paths.add(path);
                time = (request.arriveBy ? path.getStartTime() : path.getEndTime());
                placeIndex += (request.arriveBy ? -1 : +1);
            }
            request.setRoutingContext(router.graph);
            request.rctx.debugOutput = debugOutput;
            if (request.arriveBy) {
                Collections.reverse(paths);
            }
            return Collections.singletonList(joinPaths(paths));
        } else {
            return getPaths(request);
        }
    }

/*
    TODO reimplement
    This should probably be done with a special value in the departure/arrival time.

    public static TripPlan generateFirstTrip(RoutingRequest request) {
        request.setArriveBy(false);

        TimeZone tz = graph.getTimeZone();

        GregorianCalendar calendar = new GregorianCalendar(tz);
        calendar.setTimeInMillis(request.dateTime * 1000);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.AM_PM, 0);
        calendar.set(Calendar.SECOND, graph.index.overnightBreak);

        request.dateTime = calendar.getTimeInMillis() / 1000;
        return generate(request);
    }

    public static TripPlan generateLastTrip(RoutingRequest request) {
        request.setArriveBy(true);

        TimeZone tz = graph.getTimeZone();

        GregorianCalendar calendar = new GregorianCalendar(tz);
        calendar.setTimeInMillis(request.dateTime * 1000);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.AM_PM, 0);
        calendar.set(Calendar.SECOND, graph.index.overnightBreak);
        calendar.add(Calendar.DAY_OF_YEAR, 1);

        request.dateTime = calendar.getTimeInMillis() / 1000;

        return generate(request);
    }
*/

}
