package org.opentripplanner.routing.controller;

import org.opentripplanner.routing.core.RoutingRequest;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ControllerPolicy {

    public static void selectController(RoutingRequest request) {
        int hours = getHours(request.getDateTime());
        if (hours < 7 || hours > 21) {
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
