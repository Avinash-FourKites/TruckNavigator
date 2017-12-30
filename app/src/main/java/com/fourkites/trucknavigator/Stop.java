package com.fourkites.trucknavigator;

import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.mapping.MapMarker;
import com.here.android.mpa.routing.RouteWaypoint;

/**
 * Created by Avinash on 18/12/17.
 */

public class Stop {
    private String address;
    private RouteWaypoint routeWaypoint;
    private GeoCoordinate geoCoordinate;
    private MapMarker mapMarker;
    private boolean isCurrentLocation;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public MapMarker getMapMarker() {
        return mapMarker;
    }

    public void setMapMarker(MapMarker mapMarker) {
        this.mapMarker = mapMarker;
    }

    public boolean isCurrentLocation() {
        return isCurrentLocation;
    }

    public void setCurrentLocation(boolean currentLocation) {
        isCurrentLocation = currentLocation;
    }
}
