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
import org.opentripplanner.routing.core.RoutingRequest;

public abstract class RequestController {
    public abstract void configure(RoutingRequest request, String bannedRoutes, String bannedStopsHard);

    public Constraint singleBikeUsageConstraint() {
        ConstraintContext context = new ConstraintContext();
        context.setTransportationMode(TransportationMode.BIKE);
        ConstraintCondition condition = new ValueCondition(ValueType.MODE_OCCURRENCES, 1, OperatorType.MaximumValue);
        return new HardConstraint(context, condition);
    }

    public Constraint maximumWalkingDistanceConstraint(int distance) {
        ConstraintContext context = new ConstraintContext();
        context.setTransportationMode(TransportationMode.WALK);
        ConstraintCondition condition = new ValueCondition(ValueType.DISTANCE, distance, OperatorType.MaximumValue);
        return new HardConstraint(context, condition);
    }

    public Constraint maximumCyclingDistanceConstraint(int distance) {
        ConstraintContext context = new ConstraintContext();
        context.setTransportationMode(TransportationMode.BIKE);
        ConstraintCondition condition = new ValueCondition(ValueType.DISTANCE, distance, OperatorType.MaximumValue);
        return new HardConstraint(context, condition);
    }

    public Constraint transferPenalty(int transfer, int penalty) {
        ConstraintContext context = new ConstraintContext();
        context.setTransportationMode(TransportationMode.PUBLIC_TRANSPORT);
        ConstraintCondition condition = new ValueCondition(ValueType.LINE_CHANGES, transfer, OperatorType.ExactValue);
        return new SoftConstraint(context, condition, penalty);
    }

    public Constraint thirdTransferPenalty(int transfer, int penalty) {
        ConstraintContext context = new ConstraintContext();
        context.setTransportationMode(TransportationMode.PUBLIC_TRANSPORT);
        ConstraintCondition condition = new ValueCondition(ValueType.LINE_CHANGES, transfer, OperatorType.ExactValue);
        return new SoftConstraint(context, condition, penalty);
    }

    public Constraint maximumTransfersConstraint(int max) {
        ConstraintContext context = new ConstraintContext();
        context.setTransportationMode(TransportationMode.PUBLIC_TRANSPORT);
        ConstraintCondition condition = new ValueCondition(ValueType.LINE_CHANGES, max, OperatorType.MaximumValue);
        return new HardConstraint(context, condition);
    }

}
