package com.fourkites.trucknavigator;

import android.app.Application;

/**
 * Created by Avinash on 04/01/18.
 */

public class Navigator extends Application {

    public static boolean simulate = false;
    public static boolean navigationMode = false;

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
