package com.fourkites.trucknavigator;

import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.routing.RouteWaypoint;

/**
 * Created by Avinash on 19/12/17.
 */


public class Suggestion {

    private String name;

    private RouteWaypoint routeWaypoint;

    private GeoCoordinate geoCoordinate;

    public Suggestion() {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RouteWaypoint getRouteWaypoint() {
        return routeWaypoint;
    }

    public void setRouteWaypoint(RouteWaypoint routeWaypoint) {
        this.routeWaypoint = routeWaypoint;
    }

    public GeoCoordinate getGeoCoordinate() {
        return geoCoordinate;
    }

    public void setGeoCoordinate(GeoCoordinate geoCoordinate) {
        this.geoCoordinate = geoCoordinate;
    }
}
