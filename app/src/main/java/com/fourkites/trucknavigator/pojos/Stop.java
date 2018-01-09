package com.fourkites.trucknavigator.pojos;

import android.os.Parcel;
import android.os.Parcelable;

import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.mapping.MapMarker;
import com.here.android.mpa.routing.RouteWaypoint;

/**
 * Created by Avinash on 18/12/17.
 */

public class Stop implements Parcelable {
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

    public Stop() {

    }

    public Stop(Parcel in) {
        address = in.readString();
        routeWaypoint = (RouteWaypoint) in.readValue(RouteWaypoint.class.getClassLoader());
        geoCoordinate = (GeoCoordinate) in.readValue(GeoCoordinate.class.getClassLoader());
        mapMarker = (MapMarker) in.readValue(MapMarker.class.getClassLoader());
        isCurrentLocation = in.readByte() != 0x00;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        try{
            dest.writeString(address);
            if (routeWaypoint != null)
                dest.writeValue(routeWaypoint);
            if (geoCoordinate != null)
                dest.writeValue(geoCoordinate);
            if (mapMarker != null)
                dest.writeValue(mapMarker);
            dest.writeByte((byte) (isCurrentLocation ? 0x01 : 0x00));
        }catch (RuntimeException e){

        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Stop> CREATOR = new Parcelable.Creator<Stop>() {
        @Override
        public Stop createFromParcel(Parcel in) {
            return new Stop(in);
        }

        @Override
        public Stop[] newArray(int size) {
            return new Stop[size];
        }
    };
}