package com.fourkites.trucknavigator.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.support.v4.content.ContextCompat;

/**
 * Created by Avinash on 24/01/18.
 */

public class LocationUtil {

    public LocationUtil() {

    }

    public static boolean isGpsOn(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            return true;

        return false;
    }

    private static boolean checkLocationPermissionState(Context context) {
        int locationPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
        return locationPermission == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isGpsEnabled(Context context) {
        return checkLocationPermissionState(context) && isGpsOn(context);
    }

}
