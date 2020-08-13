package org.opentripplanner.routing.controller;

import org.opentripplanner.routing.core.RoutingRequest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class ControllerPolicy {

    /** Determine the default parametrization of the routing request.
     * If the current time is later 21:00 or earlier than 7:00 no constraints are set.
     * If the time is between 7:00 and 21:00 a set of default constraints are set .
     * If the current time is between 7:00 and 9:00 (rush hour) avoid Marienplatz and
     * Hauptbahnhof and don't use the U6 (all default constraints are also set).
     */

    public static void selectController(RoutingRequest request) {
        int hours = getHours(request.getDateTime());
        if (hours < 7 || hours > 21) {
            request.selectController(new EmptyController());
        } else if (request.getRushHouAvoidance() && (isRushHour(request))) {
            request.selectController(new RushHourController());
        } else {
            request.selectController(new DefaultController());
        }
    }

    private static DateFormat createFormatter() {
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("GMT+2"));
        return formatter;
    }

    private static int getHours(Date date) {
        DateFormat formatter = createFormatter();
        String time = formatter.format(date);
        return Integer.parseInt(time.split(":")[0]);
    }

    public static int getMinutes(Date date) {
        DateFormat formatter = createFormatter();
        String time = new SimpleDateFormat("HH:mm:ss").format(date);
        return Integer.parseInt(time.split(":")[1]);
    }

    public static boolean isRushHour(RoutingRequest request) {
        int hours = getHours(request.getDateTime());
        String[] periods = request.rushHourPeriods.split(",");
        for (String period : periods) {
            String[] times = period.split(":");
            int start = Integer.parseInt(times[0]);
            int end = Integer.parseInt(times[1]);
            if (start <= hours && end > hours) {
                return true;
            }
        }
        return false;
    }

}
