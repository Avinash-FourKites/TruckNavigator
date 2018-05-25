package com.fourkites.trucknavigator.receiver;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.fourkites.trucknavigator.utils.LocationUtil;

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
        if (LocationUtil.isGpsEnabled(context)) {
            Log.i(TAG, "onReceive: GPS On");
            updateGpsInfo(true, context);
        } else {
            Log.i(TAG, "onReceive: GPS Off");
            updateGpsInfo(false, context);
        }
    }


    private void updateGpsInfo(boolean isOn, Context context) {
        Intent gpsInfoUpdate = new Intent("GPS_INFO_UPDATE_ALERT");
        gpsInfoUpdate.putExtra("isOn", isOn);
        LocalBroadcastManager.getInstance(context).sendBroadcast(gpsInfoUpdate);
    }
}

