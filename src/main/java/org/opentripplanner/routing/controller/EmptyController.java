package org.opentripplanner.routing.controller;

import org.opentripplanner.routing.core.RoutingRequest;

public class EmptyController extends RequestController {

    @Override
    public void configure(RoutingRequest request) {
        //Nothing to do.
    }
}
