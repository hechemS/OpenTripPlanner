package org.opentripplanner.routing.controller;

import org.opentripplanner.routing.core.RoutingRequest;

public abstract class RequestController {
    public abstract void configure(RoutingRequest request);
}
