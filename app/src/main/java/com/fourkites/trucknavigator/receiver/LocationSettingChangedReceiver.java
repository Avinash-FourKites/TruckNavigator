package com.fourkites.trucknavigator.receiver;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

/**
 * Created by Avinash on 17/01/18.
 */


/**
 * Created by rohitrai on 17/03/15.
 */
public class LocationSettingChangedReceiver extends WakefulBroadcastReceiver {
    private String TAG = "LocationSetting";
    private static boolean firstOn = true;
    private static boolean firstOff = true;


    @Override
    public void onReceive(Context context, Intent intent) {

        if(isGpsEnabled(context)){
            Log.i(TAG, "onReceive: GPS On");
            updateGpsInfo(true,context);
        } else {
            Log.i(TAG, "onReceive: GPS Off");
            updateGpsInfo(false,context);
        }

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

    private void updateGpsInfo(boolean isOn, Context context) {
        Intent gpsInfoUpdate = new Intent("GPS_INFO_UPDATE_ALERT");
        gpsInfoUpdate.putExtra("isOn", isOn);
        LocalBroadcastManager.getInstance(context).sendBroadcast(gpsInfoUpdate);
    }
}

