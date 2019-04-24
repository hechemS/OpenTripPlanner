package org.opentripplanner.routing.constraints;

import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.TraverseMode;

import java.util.Map;

public class SimpleState {
    long time;
    SimpleVertex vertex;
    SimpleState backState;
    TraverseMode backMode;
    Double distance = null;

    public SimpleState(State s) {
        this.time = s.getTimeInMillis();
        this.backMode = s.getBackMode();
        this.vertex = new SimpleVertex(s.getVertex());
        if(s.getBackState() != null) this.backState = new SimpleState(s.getBackState());
        if(s.getBackEdge() != null) this.distance = s.backEdge.getDistance();

    }

    public void logDistances(Map<TraverseMode, Double> map) {
        if (getBackMode() != null) {
            double val = map.getOrDefault(getBackMode(), 0.0);
            val += distance;
            map.put(getBackMode(), val);
            backState.logDistances(map);
        }
    }

    public TraverseMode getBackMode() {
        return backMode;
    }

    public void setBackMode(TraverseMode backMode) {
        this.backMode = backMode;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public SimpleVertex getVertex() {
        return vertex;
    }

    public void setVertex(SimpleVertex vertex) {
        this.vertex = vertex;
    }

    public SimpleState getBackState() {
        return backState;
    }

    public void setBackState(SimpleState backState) {
        this.backState = backState;
    }
}
