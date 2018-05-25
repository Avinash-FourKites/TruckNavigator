package com.fourkites.trucknavigator;

import android.app.Application;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

/**
 * Created by Avinash on 04/01/18.
 */

public class Navigator extends Application {

    public static boolean simulate = false;
    public static boolean navigationMode = false;
    public static long simulationSpeed = 0;
    public static boolean isMapLoaded = false;
   /* public static boolean isCurrentLocationAvailable = false;
    public static boolean isSplashActive = true;*/

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric fabric = new Fabric.Builder(this).debuggable(true).kits(new Crashlytics()).build();
        Fabric.with(fabric);


    }


}
