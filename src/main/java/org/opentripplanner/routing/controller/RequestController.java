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

    String bus51 = "1__3-51-G-010-1,1__3-51-G-010-2,1__3-51-G-009-10";
    String bus151 = "1__3-151-G-010-1,1__3-151-G-009-9";
    String bus54 = "1__3-54-G-010-2,1__3-54-G-010-1,1__3-54-G-009-11";
    String bus154 = "1__3-154-G-009-5,1__3-154-G-010-2,1__3-154-G-010-1";
    String bus59 = "1__3-59-G-009-8,1__3-59-G-009-9,1__3-59-G-010-1,1__3-59-G-010-2";
    String bus150 = "1__3-150-G-010-1,1__3-150-G-009-5";
    String bus143 = "1__3-143-G-009-11,1__3-143-G-009-12,1__3-143-G-010-2,1__3-143-G-010-1";
    String bus58 = "1__3-58-G-009-11,1__3-58-G-009-12,1__3-58-G-010-1,1__3-58-G-010-2";
    String bus68 = "1__3-68-G-009-16,1__3-68-G-009-17,1__3-68-G-010-2,1__3-68-G-010-1";

    public abstract void configure(RoutingRequest request, String bannedRoutes, String bannedStopsHard, String preferredRoutes, Integer otherThanPreferredRoutesPenalty);

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

    public void setPreferredRoutes(RoutingRequest request, String bannedRoutes) {
        if (bannedRoutes == null) {
            request.setPreferredRoutes(bus51 + "," + bus151 + "," + bus54 + "," + bus154
                    + "," + bus59 + "," + bus150 + "," + bus143 + "," + bus58 + "," + bus68);
        } else {
            request.setPreferredRoutes(bannedRoutes + "," + bus51 + "," + bus151 + "," + bus54
                    + "," + bus154 + "," + bus59 + "," + bus150 + "," + bus143
                    + "," + bus58 + "," + bus68);
        }
    }

    public void minimumCyclingDistance(double distance, RoutingRequest request) {
        if (!request.minDistanceToMode.containsKey(TransportationMode.BIKE) && request.modes.getBicycle()) {
            request.minDistanceToMode.put(TransportationMode.BIKE, distance);
        }
    }

}
