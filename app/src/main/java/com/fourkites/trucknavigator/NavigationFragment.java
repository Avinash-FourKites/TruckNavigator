package com.fourkites.trucknavigator;


import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.fourkites.trucknavigator.pojos.SelectedRoute;
import com.fourkites.trucknavigator.pojos.Stop;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class NavigationFragment extends Fragment {

    private NavigationView navigationView;
    private Button getStarted;
    private LinearLayout appLayout;
    private RelativeLayout splashLayout;
    private TextToSpeech tts;
    private boolean isTtsEnabled = false;
    private final static int REQUEST_CODE_ASK_PERMISSIONS = 1;
    private GpsInfoReceiver gpsInfoReceiver;
    private ArrayList<Stop> points;
    private SelectedRoute selectedRoute;
    private boolean directionsState;
    private boolean startState;
    private boolean stopState;
    private boolean toolbarState;
    private boolean navigationBarState;
    private boolean controlsState;
    private boolean schemeSwitchState;
    private boolean popUpLayoutState;
    private boolean mainViewState;
    private boolean routeViewState;

    private final String TAG = "LOCATION MODE";


    public NavigationFragment() {
        // Required empty public constructor
    }


    public static NavigationFragment newInstance() {
        NavigationFragment fragment = new NavigationFragment();
      /*  Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);*/
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        // Initialize Text to Speech Engine
        initTTS();


        //Restoring the data during Activity Restart
        if (savedInstanceState != null) {
            //savedState = savedInstanceState.getBundle("savedState");
            restoreDataWhenActivityRestarts(savedInstanceState);
        }
        if (getArguments() != null) {
           /* mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);*/
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_main, container, false);

        appLayout = view.findViewById(R.id.appLayout);
        splashLayout = view.findViewById(R.id.splashLayout);
        getStarted = view.findViewById(R.id.start_button);
        gpsInfoReceiver = new GpsInfoReceiver();


        //showHomeScreen();

        return view;
    }

    private void restoreDataWhenActivityRestarts(Bundle savedInstanceState) {
        points = savedInstanceState.getParcelableArrayList("waypoints");
        selectedRoute = savedInstanceState.getParcelable("route");
        directionsState = savedInstanceState.getBoolean("directionsState");
        startState = savedInstanceState.getBoolean("startState");
        stopState = savedInstanceState.getBoolean("stopState");
        toolbarState = savedInstanceState.getBoolean("toolbarState");
        navigationBarState = savedInstanceState.getBoolean("navigationBarState");
        controlsState = savedInstanceState.getBoolean("controlsState");
        schemeSwitchState = savedInstanceState.getBoolean("schemeSwitchState");
        popUpLayoutState = savedInstanceState.getBoolean("popUpLayoutState");
        mainViewState = savedInstanceState.getBoolean("mainViewState");
        routeViewState = savedInstanceState.getBoolean("routeViewState");

        if (mainViewState) {
            appLayout.setVisibility(View.VISIBLE);
            splashLayout.setVisibility(View.GONE);
        } else {
            appLayout.setVisibility(View.GONE);
            splashLayout.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (navigationView != null) {
            if (navigationView.getWaypoints() != null)
                outState.putParcelableArrayList("waypoints", navigationView.getWaypoints());
            if (navigationView.getSelectedRoute() != null)
                outState.putParcelable("route", navigationView.getSelectedRoute());
            if (navigationView.getCreateRoute() != null)
                outState.putBoolean("directionsState", navigationView.getCreateRoute().getVisibility() == View.VISIBLE);
            if (navigationView.getStart() != null)
                outState.putBoolean("startState", navigationView.getStart().getVisibility() == View.VISIBLE);
            if (navigationView.getStop() != null)
                outState.putBoolean("stopState", navigationView.getStop().getVisibility() == View.VISIBLE);
            if (navigationView.getToolbar() != null)
                outState.putBoolean("toolbarState", navigationView.getToolbar().getVisibility() == View.VISIBLE);
            if (navigationView.getNavigationBar() != null)
                outState.putBoolean("navigationBarState", navigationView.getNavigationBar().getVisibility() == View.VISIBLE);
            if (navigationView.getControls() != null)
                outState.putBoolean("controlsState", navigationView.getControls().getVisibility() == View.VISIBLE);
            if (navigationView.getSchemeSwitch() != null)
                outState.putBoolean("schemeSwitchState", navigationView.getSchemeSwitch().getVisibility() == View.VISIBLE);
            if (navigationView.getPopUpLayout() != null)
                outState.putBoolean("popUpLayoutState", navigationView.getPopUpLayout().getVisibility() == View.VISIBLE);
            if (appLayout != null)
                outState.putBoolean("mainViewState", appLayout.getVisibility() == View.VISIBLE);
            if (navigationView.getRouteDetailsLayout() != null)
                outState.putBoolean("routeViewState", navigationView.getRouteDetailsLayout().getVisibility() == View.VISIBLE);
            //outState.putBundle("savedState", savedState);
        }
    }

    private void initTTS() {
        tts = new TextToSpeech(getActivity(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {

                    int result = tts.setLanguage(Locale.US);

                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "This Language is not supported");
                    } else {
                        isTtsEnabled = true;
                    }

                } else {
                    Log.e("TTS", "Initilization Failed!");
                }
                requestPermissions();
            }
        });
    }

    private void showHomeScreen() {

        getStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                splashLayout.setVisibility(View.GONE);
                appLayout.setVisibility(View.VISIBLE);
                if (!Navigator.isMapLoaded) {
                    if (navigationView != null) {
                        navigationView.showProgressBasedOnMapAndPosition();
                    } else {
                        Log.e(TAG, "showHomeScreen onClick: Inialization error");
                    }
                }

            }
        });
    }

    /**
     * Only when the app's target SDK is 23 or higher, it requests each dangerous permissions it
     * needs when the app is running.
     */
    private void requestPermissions() {

        final List<String> requiredSDKPermissions = new ArrayList<String>();
        requiredSDKPermissions.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        requiredSDKPermissions.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        requiredSDKPermissions.add(android.Manifest.permission.INTERNET);
        requiredSDKPermissions.add(android.Manifest.permission.ACCESS_WIFI_STATE);
        requiredSDKPermissions.add(android.Manifest.permission.ACCESS_NETWORK_STATE);

        if (Build.VERSION.SDK_INT < 23) {
            ActivityCompat.requestPermissions(getActivity(), requiredSDKPermissions.toArray(new String[requiredSDKPermissions.size()]), REQUEST_CODE_ASK_PERMISSIONS);
        } else
            requestPermissions(requiredSDKPermissions.toArray(new String[requiredSDKPermissions.size()]), REQUEST_CODE_ASK_PERMISSIONS);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS: {
                for (int index = 0; index < permissions.length; index++) {
                    if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {

                        /**
                         * If the user turned down the permission request in the past and chose the
                         * Don't ask again option in the permission request system dialog.
                         */
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                                permissions[index])) {
                            Toast.makeText(getActivity(),
                                    "Required permission " + permissions[index] + " not granted. "
                                            + "Please go to settings and turn on for sample app",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getActivity(),
                                    "Required permission " + permissions[index] + " not granted",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                }
                displayLocationSettingsRequest(getActivity());


                break;
            }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void displayLocationSettingsRequest(Context context) {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.i(TAG, "All location settings are satisfied.");
                        /**
                         * All permission requests are being handled.Create map fragment view.Please note
                         * the HERE SDK requires all permissions defined above to operate properly.
                         */
                        initMap();

                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");

                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().
                            status.startResolutionForResult(getActivity(), 100);
                        } catch (IntentSender.SendIntentException e) {
                            Log.i(TAG, "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                        break;
                }
            }
        });
    }

    private void initMap() {
      /*  if (points != null && points.size() > 0) {
            navigationView = new NavigationView(getActivity(), isTtsEnabled, tts, points, selectedRoute);
        } else */
        {
            navigationView = new NavigationView(getActivity(), isTtsEnabled, tts);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(gpsInfoReceiver, new IntentFilter("GPS_INFO_UPDATE_ALERT"));
        if (navigationView != null && Navigator.navigationMode)
            navigationView.keepScreenOn();
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(gpsInfoReceiver);

        super.onPause();
    }

    /**
     * Update Ui regarding the Location Status
     */

    public class GpsInfoReceiver extends BroadcastReceiver {
        protected static final String TAG = "gps-receiver";

        @Override
        public void onReceive(Context context, Intent intent) {

            try {
                if (navigationView == null)
                    initMap();
                else if (intent.getBooleanExtra("isOn", false))
                    navigationView.getCurrentTrack();
                else
                    navigationView.showToast("Please turn on the location.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(tts != null)
            tts.shutdown();
    }
}
