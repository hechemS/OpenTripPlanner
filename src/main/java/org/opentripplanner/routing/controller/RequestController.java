package org.opentripplanner.routing.controller;

import constraints.Constraint;
import constraints.HardConstraint;
import constraints.SoftConstraint;
import constraints.conditions.ConstraintCondition;
import constraints.conditions.OperatorType;
import constraints.conditions.ValueCondition;
import constraints.conditions.ValueType;
import constraints.context.ConstraintContext;
import constraints.context.TransportationMode;
import org.opentripplanner.common.model.GenericLocation;
import org.opentripplanner.routing.core.RoutingRequest;

public abstract class RequestController {
    public abstract void configure(RoutingRequest request);

    public Constraint singleBikeUsageConstraint() {
        ConstraintContext context = new ConstraintContext();
        context.setTransportationMode(TransportationMode.BIKE);
        ConstraintCondition condition = new ValueCondition(ValueType.MODE_OCCURRENCES, 1, OperatorType.MaximumValue);
        return new HardConstraint(context, condition);
    }

    public Constraint maximumCyclingDistanceConstraint() {
        ConstraintContext context = new ConstraintContext();
        context.setTransportationMode(TransportationMode.BIKE);
        ConstraintCondition condition = new ValueCondition(ValueType.DISTANCE, 3000, OperatorType.MaximumValue);
        return new HardConstraint(context, condition);
    }

    public Constraint minimumCyclingDistanceConstraint() {
        ConstraintContext context = new ConstraintContext();
        context.setTransportationMode(TransportationMode.BIKE);
        ConstraintCondition condition = new ValueCondition(ValueType.DISTANCE, 1500, OperatorType.MinimumValue);
        return new HardConstraint(context, condition);
    }

    public Constraint maximumWalkingDistanceConstraint() {
        ConstraintContext context = new ConstraintContext();
        context.setTransportationMode(TransportationMode.WALK);
        ConstraintCondition condition = new ValueCondition(ValueType.DISTANCE, 1500, OperatorType.MaximumValue);
        return new HardConstraint(context, condition);
    }

    public Constraint maximumTransfersConstraint() {
        ConstraintContext context = new ConstraintContext();
        context.setTransportationMode(TransportationMode.PUBLIC_TRANSPORT);
        ConstraintCondition condition = new ValueCondition(ValueType.LINE_CHANGES, 2, OperatorType.MaximumValue);
        return new HardConstraint(context, condition);
    }

    public Constraint firstTransferPenalty() {
        ConstraintContext context = new ConstraintContext();
        context.setTransportationMode(TransportationMode.PUBLIC_TRANSPORT);
        ConstraintCondition condition = new ValueCondition(ValueType.LINE_CHANGES, 1, OperatorType.ExactValue);
        return new SoftConstraint(context, condition, 300);
    }

    public Constraint secondTransferPenalty() {
        ConstraintContext context = new ConstraintContext();
        context.setTransportationMode(TransportationMode.PUBLIC_TRANSPORT);
        ConstraintCondition condition = new ValueCondition(ValueType.LINE_CHANGES, 2, OperatorType.ExactValue);
        return new SoftConstraint(context, condition, 600);
    }

    public boolean bikeAccessible(RoutingRequest request, double distance) {
        if (request.hasBikeLocation() && distance(request.from, request.bikeLocation) > distance) {
            return false;
        }
        return true;
    }

    public double distance(GenericLocation start, GenericLocation end) {
        final int R = 6371;
        double latDistance = Math.toRadians(end.lat - start.lat);
        double lonDistance = Math.toRadians(end.lng - start.lng);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(start.lat)) * Math.cos(Math.toRadians(end.lat))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c * 1000;
    }
}
