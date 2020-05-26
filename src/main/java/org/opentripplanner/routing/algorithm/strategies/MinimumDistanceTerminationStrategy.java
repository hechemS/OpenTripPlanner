package org.opentripplanner.routing.algorithm.strategies;

import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.graph.Vertex;
import org.opentripplanner.routing.spt.ShortestPathTree;

public class MinimumDistanceTerminationStrategy implements SearchTerminationStrategy {
    TraverseMode traverseMode;
    double minDistance;

    public MinimumDistanceTerminationStrategy(TraverseMode traverseMode, double minDistance) {
        this.traverseMode = traverseMode;
        this.minDistance = minDistance;
    }

    public boolean shouldSearchTerminate(State current) {
        return shouldSearchTerminate(null, null, current, null, null);
    }

    @Override
    public boolean shouldSearchTerminate(Vertex origin, Vertex target, State current, ShortestPathTree spt, RoutingRequest traverseOptions) {
        return getDistance(current) > minDistance;
    }

    public double getDistance(State s) {
        if (s.getBackState() == null) {
            return 0;
        }
        if (s.getBackMode() == traverseMode) {
            return s.getBackEdge().getDistance() + getDistance(s.getBackState());
        } else {
            return getDistance(s.getBackState());
        }
    }
}
