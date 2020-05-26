package org.opentripplanner.routing.controller;

import org.opentripplanner.routing.core.RoutingRequest;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ControllerPolicy {

    /** Determine the default parametrization of the routing request.
     * If the current time is later 21:00 or earlier than 7:00 no constraints are set.
     * If the time is between 7:00 and 21:00 a set of default constraints are set .
     * If the current time is between 7:00 and 9:00 or 16:00 and 18:00 (rush hour) avoid
     * Marienplatz and Hauptbahnhof and don't use the U6 (all default constraints are also
     * set).
     */

    public static void selectController(RoutingRequest request) {
        int hours = getHours(request.getDateTime());
        if (!request.minDistanceToMode.isEmpty() || hours < 7 || hours > 21) {
            request.selectController(new EmptyController());
        } else if (hours >= 7 && hours <= 9 || hours >= 16 && hours <= 18) {
            request.selectController(new RushHourController());
        } else {
            request.selectController(new DefaultController());
        }
    }

    private static int getHours(Date date) {
        String time = new SimpleDateFormat("HH:mm:ss").format(date);
        return Integer.parseInt(time.split(":")[0]);
    }

    public static int getMinutes(Date date) {
        String time = new SimpleDateFormat("HH:mm:ss").format(date);
        return Integer.parseInt(time.split(":")[1]);
    }

}
