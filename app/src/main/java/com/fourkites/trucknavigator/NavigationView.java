package com.fourkites.trucknavigator;

/**
 * Created by Avinash on 09/01/18.
 */

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.fourkites.trucknavigator.adapters.AutoSuggestAdapter;
import com.fourkites.trucknavigator.adapters.RoutesAdapter;
import com.fourkites.trucknavigator.adapters.StopsAdapter;
import com.fourkites.trucknavigator.pojos.Result;
import com.fourkites.trucknavigator.pojos.SearchResults;
import com.fourkites.trucknavigator.pojos.SelectedRoute;
import com.fourkites.trucknavigator.pojos.Stop;
import com.fourkites.trucknavigator.utils.LocationUtil;
import com.google.gson.Gson;
import com.here.android.mpa.common.ApplicationContext;
import com.here.android.mpa.common.GeoBoundingBox;
import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.GeoPosition;
import com.here.android.mpa.common.IconCategory;
import com.here.android.mpa.common.OnEngineInitListener;
import com.here.android.mpa.common.PositioningManager;
import com.here.android.mpa.common.ViewObject;
import com.here.android.mpa.guidance.AudioPlayerDelegate;
import com.here.android.mpa.guidance.NavigationManager;
import com.here.android.mpa.guidance.VoiceCatalog;
import com.here.android.mpa.guidance.VoicePackage;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapFragment;
import com.here.android.mpa.mapping.MapGesture;
import com.here.android.mpa.mapping.MapMarker;
import com.here.android.mpa.mapping.MapObject;
import com.here.android.mpa.mapping.MapRoute;
import com.here.android.mpa.mapping.MapState;
import com.here.android.mpa.mapping.MapTrafficLayer;
import com.here.android.mpa.mapping.MapTransitLayer;
import com.here.android.mpa.mapping.OnMapRenderListener;
import com.here.android.mpa.mapping.PositionIndicator;
import com.here.android.mpa.routing.CoreRouter;
import com.here.android.mpa.routing.Maneuver;
import com.here.android.mpa.routing.Route;
import com.here.android.mpa.routing.RouteOptions;
import com.here.android.mpa.routing.RoutePlan;
import com.here.android.mpa.routing.RouteResult;
import com.here.android.mpa.routing.RouteWaypoint;
import com.here.android.mpa.routing.Router;
import com.here.android.mpa.routing.RoutingError;
import com.here.android.mpa.search.ErrorCode;
import com.here.android.mpa.search.Location;
import com.here.android.mpa.search.ResultListener;
import com.here.android.mpa.search.ReverseGeocodeRequest2;

import org.jsoup.Jsoup;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * This class encapsulates the properties and functionality of the Map view.It also triggers a
 * turn-by-turn navigation from HERE Maps
 */
public class NavigationView implements Map.OnTransformListener {

    private MapFragment mapFragment;
    private Activity activity;
    private Button m_naviControlButton;
    private static Map map;
    private NavigationManager m_navigationManager;
    private GeoBoundingBox m_geoBoundingBox;
    private List<Route> routes;
    private SelectedRoute selectedRoute;
    private Route m_route;
    private MapRoute mapRoute;
    private boolean isInitialized;
    private TextToSpeech tts;
    private long voiceSkinId;
    // flag that indicates whether maps is being transformed
    private boolean mTransforming;
    // callback that is called when transforming ends
    private Runnable mPendingUpdate;
    private static ArrayList<Stop> waypoints;
    private static List<Result> searchResults;
    private RecyclerView stopsView;
    private RecyclerView suggestionView;
    private StopsAdapter stopsAdapter;
    private RouteWaypoint currentPosition;
    private GeoCoordinate currentGeo;
    private FloatingActionButton start;
    private FloatingActionButton stop;
    private FloatingActionButton createRoute;
    private ImageView schemeSwitch;
    private LinearLayout popUpLayout;
    private TextView mapView;
    private TextView satellite;
    private TextView terrain;
    private TextView trafficConditions;
    private TextView publicTransport;
    private TextView showTrafficIncidents;
    private static List<MapObject> mapMarkers;
    private PositioningManager positioningManager;
    private ImageView currentLocation;
    private TextView distanceCovered;
    private TextView totalDistance;
    private TextView eta;
    private ImageView manuverIcon;
    private TextView distanceOfManeuver;
    private RelativeLayout navigationBar;
    private int totalDistanceVal;
    private RelativeLayout searchLayout;
    private AutoCompleteTextView searchView;
    private Handler handler = new Handler();
    private Runnable run;
    private Stop selectedStopForSuggestion;
    private int selectedStopPosition;
    private RequestQueue queue;
    private AutoSuggestAdapter autoSuggestAdapter;
    private RoutesAdapter routesAdapter;
    private RelativeLayout toolbar;
    private Stop currentPositionStop;
    private ProgressDialog progressBar;
    private final String LOCATION_TAG = "locationTag";
    private ProgressBar suggestLoader;
    private TextView toolbarTitle;
    private RecyclerView routesView;
    private FrameLayout mapFragmentContainer;
    private LinearLayout controls;
    private LinearLayout routesLayout;
    private ImageView routesBack;
    private boolean directionsState;
    private boolean startState;
    private boolean stopState;
    private boolean toolbarState;
    private boolean navigationBarState;
    private boolean controlsState;
    private boolean schemeSwitchState;
    private boolean popUpLayoutState;
    private ImageView addStop;
    private ImageView back;
    private ImageView close;
    private Toast toast;
    private LinearLayout routeDetailsLayout;
    private TextView selectedRouteEta;
    private TextView selectedRouteDistance;
    private RelativeLayout parent;
    private EditText speedForSimulation;
    private final double MILES_CONVERSION = 1609.344;


    public NavigationView(Activity activity, boolean isTtsEnabled, TextToSpeech tts) {
        this.activity = activity;
        this.tts = tts;
        isInitialized = isTtsEnabled;
        waypoints = new ArrayList<>();
        mapMarkers = new ArrayList<>();
        searchResults = new ArrayList<>();
        queue = Volley.newRequestQueue(activity);
        selectedRoute = new SelectedRoute();
        settingUpUI();
    }

    public NavigationView(Activity activity, boolean isTtsEnabled, TextToSpeech tts, ArrayList<Stop> points, SelectedRoute selectedRoute) {
        this.activity = activity;
        this.tts = tts;
        isInitialized = isTtsEnabled;
        waypoints = points;
        mapMarkers = new ArrayList<>();
        searchResults = new ArrayList<>();
        queue = Volley.newRequestQueue(activity);
        this.selectedRoute = selectedRoute;
        m_route = selectedRoute.getM_route();
        settingUpUI();
    }

    public ArrayList<Stop> getWaypoints() {
        return waypoints;
    }

    public void setSelectedRoute(SelectedRoute selectedRoute) {
        this.selectedRoute = selectedRoute;
    }

    public SelectedRoute getSelectedRoute() {
        return selectedRoute;
    }

    private void settingUpUI() {
        //showProgress("loading...");

        initializeView();
        //Setting up stops list view
        setStopsAdapter();

        //Show Simulation (Title in blue -simulation | in white - realtime)
        showSimulationHint();

        addListeners();

        initializeMapFragment();
    }


    private void initializeView() {

        //Initializing MAPVIEW
        mapFragment = new MapFragment();
        mapFragmentContainer = (FrameLayout) activity.findViewById(R.id.mapFragmentContainer);
        activity.getFragmentManager().beginTransaction().add(mapFragmentContainer.getId(), mapFragment, "MAP_TAG1").commit();
        mapFragment.setRetainInstance(true);

        toolbar = (RelativeLayout) activity.findViewById(R.id.toolbar);
        start = (FloatingActionButton) activity.findViewById(R.id.startNavigation);
        createRoute = (FloatingActionButton) activity.findViewById(R.id.createRoute);
        stop = (FloatingActionButton) activity.findViewById(R.id.stopNavigation);
        schemeSwitch = (ImageView) activity.findViewById(R.id.schemeSwitch);
        popUpLayout = (LinearLayout) activity.findViewById(R.id.popUpLayout);
        mapView = (TextView) activity.findViewById(R.id.navigationView);
        satellite = (TextView) activity.findViewById(R.id.satellite);
        terrain = (TextView) activity.findViewById(R.id.terrain);
        trafficConditions = (TextView) activity.findViewById(R.id.trafficConditions);
        publicTransport = (TextView) activity.findViewById(R.id.publicTransport);
        showTrafficIncidents = (TextView) activity.findViewById(R.id.showTrafficIncidents);
        currentLocation = (ImageView) activity.findViewById(R.id.currentLocation);
        suggestLoader = (ProgressBar) activity.findViewById(R.id.suggestLoader);
        distanceCovered = (TextView) activity.findViewById(R.id.distanceCovered);
        totalDistance = (TextView) activity.findViewById(R.id.totalDistance);
        eta = (TextView) activity.findViewById(R.id.eta);
        navigationBar = (RelativeLayout) activity.findViewById(R.id.navigationBar);
        manuverIcon = (ImageView) activity.findViewById(R.id.manuverIcon);
        distanceOfManeuver = (TextView) activity.findViewById(R.id.distanceOfManeuver);
        stopsView = (RecyclerView) activity.findViewById(R.id.stopsView);
        controls = (LinearLayout) activity.findViewById(R.id.controls);
        searchLayout = (RelativeLayout) activity.findViewById(R.id.searchLayout);
        addStop = (ImageView) activity.findViewById(R.id.addStop);
        back = (ImageView) activity.findViewById(R.id.back);
        close = (ImageView) activity.findViewById(R.id.remove);
        searchView = (AutoCompleteTextView) activity.findViewById(R.id.searchView);
        suggestionView = (RecyclerView) activity.findViewById(R.id.suggestionView);
        toolbarTitle = (TextView) activity.findViewById(R.id.toolbar_title);
        routesLayout = (LinearLayout) activity.findViewById(R.id.routesLayout);
        routesView = (RecyclerView) activity.findViewById(R.id.routesView);
        routesBack = (ImageView) activity.findViewById(R.id.routesBack);
        routeDetailsLayout = (LinearLayout) activity.findViewById(R.id.routeDetailsLayout);
        selectedRouteDistance = (TextView) activity.findViewById(R.id.selectedRouteDistance);
        selectedRouteEta = (TextView) activity.findViewById(R.id.selectedRouteEta);
        parent = (RelativeLayout) activity.findViewById(R.id.parent);
        speedForSimulation = (EditText) activity.findViewById(R.id.speedForSimulation);
    }

    private void addListeners() {

        /*toolbarTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigator.simulate = !Navigator.simulate;
                showSimulationHint();

            }
        });*/

        routesBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapFragmentContainer.setVisibility(View.VISIBLE);
                routesLayout.setVisibility(View.GONE);
                createRoute.setVisibility(View.VISIBLE);
                start.setVisibility(View.GONE);
                toolbar.setVisibility(View.VISIBLE);
                controls.setVisibility(View.VISIBLE);
            }
        });

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startNavigation();
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigator.navigationMode = true;
                ((NavigationActivity) activity).showLogoutWarning(true, "Are you sure you want to exit the current navigation?.", false);
            }
        });

        createRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createRoute();
            }
        });

        currentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (LocationUtil.isGpsEnabled(activity))
                        getCurrentTrack();
                    else
                        showToast("Please turn on the location services.");
                } catch (NullPointerException e) {
                }
            }
        });

        addStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (searchResults != null && autoSuggestAdapter != null) {
                    searchResults.clear();
                }
                if (!checkForEmptyStop()) {
                    Stop stop = new Stop();
                    addWaypoint(stop, false);
                    selectedStopForSuggestion = stop;
                    if (waypoints != null)
                        selectedStopPosition = waypoints.size() - 1;
                } else {
                    showToast("Please Enter Stop.");
                }
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchLayout.setVisibility(View.GONE);
                softCloseKeyboard();
                showDirections();
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.setText("");
            }
        });

        //SearchView
        searchView.setThreshold(3);

        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(final CharSequence s, int start, int before, int count) {
                if (searchView.isFocused()) {
                    if (!searchView.getText().toString().equals("")) {

                        if (searchView.getText().toString().length() >= 3) {
                            suggestLoader.setVisibility(View.VISIBLE);
                            handler.removeCallbacks(run);

                            run = new Runnable() {
                                public void run() {
                                    //stopViewHolder.loadingCircle.setVisibility(View.VISIBLE);
                                    suggesstionsRequest(selectedStopForSuggestion, s.toString().trim());
                                }
                            };
                            handler.postDelayed(run, 2000);
                        }
                    } else {
                        searchView.clearFocus();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

    }

    private void softCloseKeyboard() {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null)
            imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
    }

    public void getCurrentTrack() {
        if (m_navigationManager != null)
            m_navigationManager.setMapUpdateMode(NavigationManager.MapUpdateMode.ROADVIEW);

        if (!Navigator.navigationMode)
            getCurrentPosition();

    }

    private void setStopsAdapter() {
        stopsAdapter = new StopsAdapter(this, activity, waypoints);
        LinearLayoutManager layoutManager = new LinearLayoutManager(activity);
        stopsView.setLayoutManager(layoutManager);
        stopsView.setAdapter(stopsAdapter);
    }

    private void initializeMapFragment() {
          /* Initialize the MapFragment, results will be given via the called back. */
        if (mapFragment != null) {
            final ApplicationContext context = new ApplicationContext(activity);
            mapFragment.init(context, new OnEngineInitListener() {

                @Override
                public void onEngineInitializationCompleted(Error error) {

                    if (error == Error.NONE) {
                        map = mapFragment.getMap();

                        mapFragment.addOnMapRenderListener(new OnMapRenderListener() {
                            @Override
                            public void onPreDraw() {

                            }

                            @Override
                            public void onPostDraw(boolean b, long l) {
                                Log.d("mapView", "onPostDraw: " + b + "  long " + l);
                            }

                            @Override
                            public void onSizeChanged(int i, int i1) {

                            }

                            @Override
                            public void onGraphicsDetached() {

                            }

                            @Override
                            public void onRenderBufferCreated() {
                            }
                        });

                        mapFragment.getMapGesture().addOnGestureListener(new MapGesture.OnGestureListener() {
                            @Override
                            public void onPanStart() {
                                allowZoomOnMapGesture();
                            }

                            @Override
                            public void onPanEnd() {
                                allowZoomOnMapGesture();
                            }

                            @Override
                            public void onMultiFingerManipulationStart() {
                                allowZoomOnMapGesture();
                            }

                            @Override
                            public void onMultiFingerManipulationEnd() {
                                allowZoomOnMapGesture();
                            }

                            @Override
                            public boolean onMapObjectsSelected(List<ViewObject> list) {
                                allowZoomOnMapGesture();
                                return false;
                            }

                            @Override
                            public boolean onTapEvent(PointF pointF) {
                                allowZoomOnMapGesture();
                                return false;
                            }

                            @Override
                            public boolean onDoubleTapEvent(PointF pointF) {
                                allowZoomOnMapGesture();
                                return false;
                            }

                            @Override
                            public void onPinchLocked() {
                                allowZoomOnMapGesture();
                            }

                            @Override
                            public boolean onPinchZoomEvent(float v, PointF pointF) {
                                allowZoomOnMapGesture();
                                return false;
                            }

                            @Override
                            public void onRotateLocked() {
                                allowZoomOnMapGesture();
                            }

                            @Override
                            public boolean onRotateEvent(float v) {
                                allowZoomOnMapGesture();
                                return false;
                            }

                            @Override
                            public boolean onTiltEvent(float v) {
                                allowZoomOnMapGesture();
                                return false;
                            }

                            @Override
                            public boolean onLongPressEvent(PointF pointF) {
                                allowZoomOnMapGesture();
                                return false;
                            }

                            @Override
                            public void onLongPressRelease() {
                                allowZoomOnMapGesture();

                            }

                            @Override
                            public boolean onTwoFingerTapEvent(PointF pointF) {
                                allowZoomOnMapGesture();
                                return false;
                            }
                        });

                        map.setCenter(new GeoCoordinate(41.878332, -87.629789), Map.Animation.BOW);
                        positioningManager = PositioningManager.getInstance();

                        if (waypoints.size() < 1)
                            getCurrentPosition();

                        if (m_navigationManager == null)
                            m_navigationManager = NavigationManager.getInstance();

                        m_navigationManager.setMap(map);

                        /*
                        * NavigationManager contains a number of listeners which we can use to monitor the
                        * navigation status and getting relevant instructions.In this example, we will add 2
                        * listeners for demo purpose,please refer to HERE Android SDK API documentation for details
                        */
                        addNavigationListeners();

                        addingMapSettings();

                        restoreViewsDuringRestart();


                    } else {
                        showToast("ERROR: Cannot initialize Map with error " + error);
                    }
                }
            });
        }
    }

    private void addingMapSettings() {
        EnumSet<Map.PedestrianFeature> set = EnumSet.of(Map.PedestrianFeature.TUNNEL, Map.PedestrianFeature.BRIDGE, Map.PedestrianFeature.CROSSWALK);
        map.setPedestrianFeaturesVisible(set);
        map.setStreetLevelCoverageVisible(true);
        map.setProjectionMode(Map.Projection.MERCATOR);  // globe projection
        map.setExtrudedBuildingsVisible(false);  // enable 3D building footprints
        map.setLandmarksVisible(false);  // 3D Landmarks visible
        map.setCartoMarkersVisible(IconCategory.ALL, true);  // show embedded map markers
        map.setSafetySpotsVisible(true); // show speed cameras as embedded markers on the map
        map.setMapScheme(Map.Scheme.NORMAL_DAY);   // normal day mapscheme
        map.setTrafficInfoVisible(false);
        mapSchemeSwitcher();
    }

    private void restoreViewsDuringRestart() {
        if (waypoints.size() > 1) {

            refreshRoute();

            if (getSelectedRoute() != null)
                selectRoute(getSelectedRoute().getM_route());

            directionsState = ((NavigationActivity) activity).isDirectionsState();
            startState = ((NavigationActivity) activity).isStartState();
            stopState = ((NavigationActivity) activity).isStopState();
            toolbarState = ((NavigationActivity) activity).isToolbarState();
            navigationBarState = ((NavigationActivity) activity).isNavigationBarState();
            controlsState = ((NavigationActivity) activity).isControlsState();
            schemeSwitchState = ((NavigationActivity) activity).isSchemeSwitchState();
            popUpLayoutState = ((NavigationActivity) activity).isPopUpLayoutState();
            if (directionsState)
                getCreateRoute().setVisibility(View.VISIBLE);
            else
                getCreateRoute().setVisibility(View.GONE);

            if (startState)
                getStart().setVisibility(View.VISIBLE);
            else
                getStart().setVisibility(View.GONE);

            if (stopState)
                getStop().setVisibility(View.VISIBLE);
            else
                getStop().setVisibility(View.GONE);


            if (toolbarState)
                getToolbar().setVisibility(View.VISIBLE);
            else
                getToolbar().setVisibility(View.GONE);


            if (navigationBarState)
                getNavigationBar().setVisibility(View.VISIBLE);
            else
                getNavigationBar().setVisibility(View.GONE);


            if (controlsState)
                getControls().setVisibility(View.VISIBLE);
            else
                getControls().setVisibility(View.GONE);


            if (schemeSwitchState)
                getSchemeSwitch().setVisibility(View.VISIBLE);
            else
                getSchemeSwitch().setVisibility(View.GONE);


            if (popUpLayoutState)
                getPopUpLayout().setVisibility(View.VISIBLE);
            else
                getPopUpLayout().setVisibility(View.GONE);
        }
    }

    private void showSimulationHint() {
        if (Navigator.simulate) {
            toolbarTitle.setTextColor(activity.getResources().getColor(R.color.carrier_link_blue));
            speedForSimulation.setVisibility(View.VISIBLE);
        } else {
            toolbarTitle.setTextColor(activity.getResources().getColor(android.R.color.white));
            speedForSimulation.setVisibility(View.GONE);
        }
    }

    public void addOrUpdateWaypoint(Stop stop, int pos) {
        if (searchLayout.getVisibility() == View.VISIBLE)
            searchLayout.setVisibility(View.GONE);

        if (searchView != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null)
                imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
        }

        if (waypoints != null) {
            try {
                if (waypoints.size() < 1)
                    waypoints.add(stop);
                else
                    waypoints.set(pos, stop);
            } catch (IndexOutOfBoundsException e) {
                if (waypoints.size() == 1)
                    waypoints.set(0, stop);
            }


            map.setCenter(stop.getGeoCoordinate(), Map.Animation.BOW);

            stopsAdapter.notifyDataSetChanged();
            refreshRoute();
        }
    }

    public void discardDuplicateStop() {
        if (searchLayout.getVisibility() == View.VISIBLE)
            searchLayout.setVisibility(View.GONE);

        if (searchView != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null)
                imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
        }
    }

    private boolean avoidDuplicateStop(Stop stop) {

        for (Stop sp : waypoints) {
            if ((stop.getGeoCoordinate().getLongitude() == sp.getGeoCoordinate().getLongitude()) && (stop.getGeoCoordinate().getLatitude() == sp.getGeoCoordinate().getLatitude())) {
                return true;
            }
        }
        return false;
    }

    public void refreshRoute() {
        if (map != null) {
            if (mapMarkers != null) {
                map.removeMapObjects(mapMarkers);
            }

            if (mapRoute != null)
                map.removeMapObject(mapRoute);

            if (routeDetailsLayout.getVisibility() == View.VISIBLE)
                routeDetailsLayout.setVisibility(View.GONE);

            if (waypoints != null) {

                addMarkerToMap(waypoints, false);


                if (waypoints.size() > 1) {
                    createRoute.setVisibility(View.VISIBLE);

                } else {
                    createRoute.setVisibility(View.GONE);
                }
                zoomToFit();
            }

            if (start.getVisibility() == View.VISIBLE)
                start.setVisibility(View.GONE);
        }
    }

    private void suggesstionsRequest(final Stop stop, String query) {
       /* if (stop.getAddress() != null && !stop.getAddress().isEmpty())
            query = stop.getAddress();*/


        String url = "https://places.api.here.com/places/v1/autosuggest?X-Map-Viewport=-76.5633,81.2945,102.7335,90.0000&app_code=K2Cpd_EKDzrZb1tz0zdpeQ&app_id=bC4fb9WQfCCZfkxspD4z&q=" + query + "&result_types=address,place,category,chain&size=10";
        StringRequest getAutoSuggestRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                suggestLoader.setVisibility(View.GONE);
                if (response != null && !response.isEmpty()) {
                    Gson gson = new Gson();
                    SearchResults results = gson.fromJson(response, SearchResults.class);
                    if (results.getResults() != null && results.getResults().size() > 0) {
                        //we have results
                        if (searchResults != null)
                            searchResults.clear();

                        //Adding current location in suggestions
                        if (currentPositionStop != null) {
                            Result result = new Result();
                            List<Double> position = new ArrayList<>();
                            position.add(currentPositionStop.getGeoCoordinate().getLatitude());
                            position.add(currentPositionStop.getGeoCoordinate().getLongitude());
                            result.setPosition(position);
                            result.setHighlightedTitle("Your Location");
                            result.setHighlightedVicinity("\n" + currentPositionStop.getAddress());
                            result.setCurrentLocation(true);
                            searchResults.add(result);
                        }


                        searchResults.addAll(results.getResults());

                        if (autoSuggestAdapter == null) {
                            autoSuggestAdapter = new AutoSuggestAdapter(NavigationView.this, activity, R.layout.suggesstion_item, searchResults, stop, selectedStopPosition);
                            LinearLayoutManager layoutManager = new LinearLayoutManager(activity);
                            suggestionView.setLayoutManager(layoutManager);
                            suggestionView.setAdapter(autoSuggestAdapter);
                        } else {
                            autoSuggestAdapter.setStop(selectedStopForSuggestion);
                            autoSuggestAdapter.setStopPosition(selectedStopPosition);
                            autoSuggestAdapter.notifyDataSetChanged();
                        }

                    } else {
                        //no search results
                        showToast("No suggestions");
                    }

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if ((error instanceof NetworkError) || (error instanceof NoConnectionError))
                    showToast("Please check your network connection.");
                suggestLoader.setVisibility(View.GONE);
            }
        });

        queue.add(getAutoSuggestRequest);

    }

    private void addCurrentLocationTosuggestions(Stop stop) {
        if (searchResults != null)
            searchResults.clear();

        //Adding current location in suggestions
        if (currentPositionStop != null) {
            Result result = new Result();
            List<Double> position = new ArrayList<>();
            position.add(currentPositionStop.getGeoCoordinate().getLatitude());
            position.add(currentPositionStop.getGeoCoordinate().getLongitude());
            result.setPosition(position);
            result.setHighlightedTitle("<b>Your Location</b></br>");
            result.setHighlightedVicinity(currentPositionStop.getAddress());
            result.setCurrentLocation(true);
            if (searchResults.size() > 0)
                searchResults.set(0, result);
            else
                searchResults.add(result);
        }

        if (autoSuggestAdapter == null) {
            autoSuggestAdapter = new AutoSuggestAdapter(NavigationView.this, activity, R.layout.suggesstion_item, searchResults, stop, selectedStopPosition);
            LinearLayoutManager layoutManager = new LinearLayoutManager(activity);
            suggestionView.setLayoutManager(layoutManager);
            suggestionView.setAdapter(autoSuggestAdapter);
        } else {
            autoSuggestAdapter.setStop(selectedStopForSuggestion);
            autoSuggestAdapter.setStopPosition(selectedStopPosition);
            autoSuggestAdapter.notifyDataSetChanged();
        }
    }

    public void editSuggestion(Stop stop, int position) {

        if (currentPositionStop != null)
            addCurrentLocationTosuggestions(currentPositionStop);
        if (stop != null) {
            String query = "";
            if (searchLayout.getVisibility() != View.VISIBLE)
                searchLayout.setVisibility(View.VISIBLE);

            selectedStopForSuggestion = stop;
            selectedStopPosition = position;

            if (stop.getAddress() != null) {
                query = stop.getAddress();
            }
            searchView.setText(Jsoup.parse(query).text());
            searchView.requestFocus();
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

        }
    }

    public void addMarkerToMap(List<Stop> stops, boolean currentLocation) {
        if (map != null) {
            if (stops != null) {

                for (int count = 0; count < stops.size(); count++) {
                    Stop stop = stops.get(count);
                    if (stop.getAddress() != null && stop.getGeoCoordinate() != null) {
                        com.here.android.mpa.common.Image markerImage = new com.here.android.mpa.common.Image();
                        if (count == 0)
                            markerImage.setBitmap(getBitmap(activity, R.drawable.current_position));
                        else if (count == stops.size() - 1)
                            markerImage.setBitmap(getBitmap(activity, R.drawable.destination_marker));
                        else
                            markerImage.setBitmap(getBitmap(activity, R.drawable.way_points_marker));


                        MapMarker mapMarker = new MapMarker(stop.getGeoCoordinate(), markerImage);

                        PointF anchor = new PointF();
                        anchor.set(markerImage.getWidth() / 2, markerImage.getHeight() / 2);
                        mapMarker.setAnchorPoint(anchor);
                        if (stop.getAddress() != null) {
                            mapMarker.setTitle(Jsoup.parse(stop.getAddress()).text());
                        }

                        map.addMapObject(mapMarker);
                        mapMarker.showInfoBubble();
                        mapMarker.setDraggable(false);
                        mapMarkers.add(mapMarker);
                        stop.setMapMarker(mapMarker);
                        positioningManager.removeListener(positionChangedListener);
                    }
                }




/*                if (waypoints != null && waypoints.size() > 1) {
                    createRoute();
                    zoomToFit();
                } else if (waypoints != null && waypoints.size() == 1) {
                    map.setCenter(waypoints.get(0).getGeoCoordinate(), Map.Animation.LINEAR);
                    map.setMapScheme(Map.Scheme.NORMAL_DAY);
                    map.setZoomLevel(13.2);
                    positioningManager.removeListener(positionChangedListener);
                }*/
            }

        }
    }

    private static void zoomToFit() {
        if (waypoints != null) {
            if (waypoints.size() > 0) {
                List<GeoCoordinate> coordinates = new ArrayList<>();
                for (Stop stop : waypoints) {
                    if (stop.getGeoCoordinate() != null)
                        coordinates.add(stop.getGeoCoordinate());
                }
                if (coordinates.size() > 1) {
                    GeoBoundingBox geoBoundingBox = GeoBoundingBox.getBoundingBoxContainingGeoCoordinates(coordinates);
                    map.zoomTo(geoBoundingBox, Map.Animation.BOW, Map.MOVE_PRESERVE_ORIENTATION);
                }
            }
        }
    }

    private void mapSchemeSwitcher() {
        schemeSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    if (popUpLayout.getVisibility() != View.VISIBLE) {
                        popUpLayout.setVisibility(View.VISIBLE);

                       /* mapView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                map.setMapScheme(Map.Scheme.NORMAL_DAY);
                                mapView.setBackground(activity.getResources().getDrawable(R.drawable.map_scheme_highlighter));
                                satellite.setBackground(null);
                                terrain.setBackground(null);
                            }
                        });*/
                        /*satellite.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                map.setMapScheme(Map.Scheme.SATELLITE_DAY);
                                map.setTrafficInfoVisible(true);
                                satellite.setBackground(activity.getResources().getDrawable(R.drawable.map_scheme_highlighter));
                                mapView.setBackground(null);
                                terrain.setBackground(null);


                            }
                        });
                        terrain.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                map.setMapScheme(Map.Scheme.TERRAIN_DAY);
                                terrain.setBackground(activity.getResources().getDrawable(R.drawable.map_scheme_highlighter));
                                satellite.setBackground(null);
                                mapView.setBackground(null);
                            }
                        });*/

                        trafficConditions.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (trafficConditions.getBackground() == null) {
                                    map.setTrafficInfoVisible(true);
                                    trafficConditions.setBackground(activity.getResources().getDrawable(R.drawable.map_scheme_highlighter));
                                } else {
                                    map.setTrafficInfoVisible(false);
                                    trafficConditions.setBackground(null);
                                }
                            }
                        });

                        publicTransport.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (publicTransport.getBackground() == null) {
                                    map.getMapTransitLayer().setMode(MapTransitLayer.Mode.EVERYTHING);
                                    publicTransport.setBackground(activity.getResources().getDrawable(R.drawable.map_scheme_highlighter));
                                } else {
                                    map.getMapTransitLayer().setMode(MapTransitLayer.Mode.NOTHING);
                                    publicTransport.setBackground(null);
                                }
                            }
                        });

                        showTrafficIncidents.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (showTrafficIncidents.getBackground() == null) {
                                    map.getMapTrafficLayer().setEnabled(MapTrafficLayer.RenderLayer.INCIDENT, true);

                                    showTrafficIncidents.setBackground(activity.getResources().getDrawable(R.drawable.map_scheme_highlighter));
                                } else {
                                    map.getMapTrafficLayer().setEnabled(MapTrafficLayer.RenderLayer.INCIDENT, false);

                                    showTrafficIncidents.setBackground(null);
                                }
                            }
                        });

                    } else {
                        popUpLayout.setVisibility(View.INVISIBLE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private static Bitmap getBitmap(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (drawable instanceof BitmapDrawable) {
            return BitmapFactory.decodeResource(context.getResources(), drawableId);
        } else if (drawable instanceof VectorDrawable) {
            return getBitmap((VectorDrawable) drawable);
        } else {
            return null;
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static Bitmap getBitmap(VectorDrawable vectorDrawable) {
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        return bitmap;
    }

    private void showCurrentPositionMarker() {
        try {
            if (mapFragment != null) {
                PositionIndicator positionIndicator = mapFragment.getPositionIndicator();
                if (!positionIndicator.isVisible())
                    positionIndicator.setVisible(true);
                if (!positionIndicator.isAccuracyIndicatorVisible())
                    positionIndicator.setAccuracyIndicatorVisible(true);
            }
        } catch (NullPointerException e) {

        }

    }

    private void getCurrentPosition() {
        positioningManager.addListener(new WeakReference<PositioningManager.OnPositionChangedListener>(positionChangedListener));
        if (positioningManager.start(PositioningManager.LocationMethod.GPS_NETWORK_INDOOR)) {
            //showProgress("Getting Current Location...");
            if (positioningManager.hasValidPosition()) {
                currentGeo = positioningManager.getPosition().getCoordinate();
                currentPosition = new RouteWaypoint(currentGeo);
                getCurrentLocation(currentGeo);
            }
        } else {
            Toast.makeText(activity, "PositioningManager.start: failed, exiting", Toast.LENGTH_LONG).show();
        }
    }

    private boolean showDirections() {
        int count = 0;
        if (waypoints != null) {
            for (int i = 0; i < waypoints.size(); i++) {
                if (waypoints.get(i).getAddress() != null && !waypoints.get(i).getAddress().isEmpty())
                    count++;
            }

            if (count >= 2 && !Navigator.navigationMode && m_route == null && routesLayout.getVisibility() != View.VISIBLE)
                return true;
        }
        return false;
    }

    private boolean checkForEmptyStop() {

        if (waypoints != null) {
            for (int i = 0; i < waypoints.size(); i++) {
                if ((waypoints.get(i).getAddress() == null) || ((waypoints.get(i).getAddress() == null) && (waypoints.get(i).getAddress().isEmpty())))
                    return true;
            }
        }
        return false;
    }

    private void triggerRevGeocodeRequest(final Stop stop) {
        /* Create a ReverseGeocodeRequest object with a GeoCoordinate. */
        ReverseGeocodeRequest2 revGecodeRequest = new ReverseGeocodeRequest2(stop.getGeoCoordinate());
        revGecodeRequest.execute(new ResultListener<Location>() {
            @Override
            public void onCompleted(Location location, ErrorCode errorCode) {
                if (errorCode == ErrorCode.NONE) {
                    /*
                     * From the location object, we retrieve the address and display to the screen.
                     * Please refer to HERE Android SDK doc for other supported APIs.
                     */
                    stop.setAddress(location.getAddress().toString());
                    stop.setCurrentLocation(true);
                    currentPositionStop = stop;

                    addWaypoint(currentPositionStop, true);

                    if ((waypoints != null && waypoints.size() == 1)) {
                        Stop stop = new Stop();
                        addWaypoint(stop, false);
                    }

                    if (showDirections())
                        createRoute.setVisibility(View.VISIBLE);
                    else
                        createRoute.setVisibility(View.GONE);

                    showCurrentPositionMarker();
                    zoomToCurrentPosition(currentPositionStop);
                    positioningManager.removeListener(positionChangedListener);
                    positioningManager.stop();

                } else {
                    Log.i("NavigationView", "ERROR:RevGeocode Request returned error code:" + errorCode);

                }
                Navigator.isMapLoaded = true;
                hideProgress();
            }
        });
    }

    private void zoomToCurrentPosition(Stop stop) {
        map.zoomTo(new GeoBoundingBox(stop.getGeoCoordinate(), 1000, 1000), Map.Animation.BOW, Map.MOVE_PRESERVE_ORIENTATION);
    }

    public void addWaypoint(Stop stop, boolean isCurrent) {
        if (waypoints == null)
            waypoints = new ArrayList<>();

        if (isCurrent && waypoints.size() > 0) {
            waypoints.set(0, stop);
        } else
            waypoints.add(stop);

        stopsAdapter.notifyDataSetChanged();

        if (waypoints.size() > 2) {
            int max = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 120, activity.getResources().getDisplayMetrics());

            stopsView.getLayoutParams().height = max;
        }
        if (waypoints != null)
            stopsView.smoothScrollToPosition(waypoints.size() - 1);
    }

    private void createRoute() {

        showProgress("Creating the truck route...");

        if (map != null)
            map.removeMapObject(mapRoute);
        /* Initialize a CoreRouter */
        CoreRouter coreRouter = new CoreRouter();

        /* Initialize a RoutePlan */
        RoutePlan routePlan = new RoutePlan();

        /*
         * Initialize a RouteOption.HERE SDK allow users to define their own parameters for the
         * route calculation,including transport modes,route types and route restrictions etc.Please
         * refer to API doc for full list of APIs
         */
        RouteOptions routeOptions = new RouteOptions();
        /* Other transport modes are also available e.g Pedestrian */
        routeOptions.setTransportMode(RouteOptions.TransportMode.TRUCK);
        /* Calculate the shortest route available. */
        routeOptions.setRouteType(RouteOptions.Type.FASTEST);
        /* Calculate 3 route. */
        routeOptions.setRouteCount(3);
        /* Finally set the route option */
        routePlan.setRouteOptions(routeOptions);


        /* Define waypoints for the route */

        if (waypoints.size() > 0) {
            for (Stop stop : waypoints) {
                if (stop.getRouteWaypoint() != null)
                    routePlan.addWaypoint(stop.getRouteWaypoint());
            }

        /* Trigger the route calculation,results will be called back via the listener */
            coreRouter.calculateRoute(routePlan,
                    new Router.Listener<List<RouteResult>, RoutingError>() {

                        @Override
                        public void onProgress(int i) {
                        /* The calculation progress can be retrieved in this callback. */
                        }

                        @Override
                        public void onCalculateRouteFinished(List<RouteResult> routeResults,
                                                             RoutingError routingError) {
                        /* Calculation is done.Let's handle the result */
                            if (routingError == RoutingError.NONE) {

                                if (routes != null)
                                    routes.clear();
                                else
                                    routes = new ArrayList<>();
                                for (RouteResult result : routeResults) {
                                    routes.add(result.getRoute());
                                }

                                if (routes.size() == 1) {
                                    selectRoute(routes.get(0));
                                } else if (routes.size() > 1) {
                                    mapFragmentContainer.setVisibility(View.GONE);
                                    routesLayout.setVisibility(View.VISIBLE);
                                    toolbar.setVisibility(View.GONE);
                                    controls.setVisibility(View.GONE);
                                    if (routesAdapter == null) {
                                        routesAdapter = new RoutesAdapter(NavigationView.this, activity, routes);
                                        LinearLayoutManager layoutManager = new LinearLayoutManager(activity);
                                        routesView.setLayoutManager(layoutManager);
                                        routesView.setAdapter(routesAdapter);
                                    } else {

                                        routesAdapter.notifyDataSetChanged();
                                    }
                                    createRoute.setVisibility(View.GONE);
                                } else {
                                    Toast.makeText(activity,
                                            "Truck route is not available for these stops",
                                            Toast.LENGTH_LONG).show();
                                }

                            } else {
                               /* Toast.makeText(activity,
                                        "Error:route calculation returned error code: " + routingError,
                                        Toast.LENGTH_LONG).show();*/
                                showRouteErrorMessage(routingError);

                            }
                            hideProgress();
                        }
                    });
        }
    }

    private void showRouteErrorMessage(RoutingError routingError) {
        String msg = null;

        if (routingError == RoutingError.GRAPH_DISCONNECTED) {
            msg = "No route was found.";
        } else if (routingError == RoutingError.NO_CONNECTIVITY || routingError == RoutingError.NETWORK_COMMUNICATION || routingError == RoutingError.INVALID_OPERATION) {
            msg = "Please check your network connection.";
        } else {
            msg = "Unable to contact our servers . Please try again later.";
        }

        if (msg != null)
            showToast(msg);
    }

    public FloatingActionButton getCreateRoute() {
        return createRoute;
    }

    public FloatingActionButton getStart() {
        return start;
    }

    public FloatingActionButton getStop() {
        return stop;
    }

    public ImageView getSchemeSwitch() {
        return schemeSwitch;
    }

    public LinearLayout getPopUpLayout() {
        return popUpLayout;
    }

    public RelativeLayout getNavigationBar() {
        return navigationBar;
    }

    public RelativeLayout getToolbar() {
        return toolbar;
    }

    public LinearLayout getControls() {
        return controls;
    }

    protected void startNavigation() {
        if (m_route != null) {
            start.setVisibility(View.GONE);
            stop.setVisibility(View.VISIBLE);
            routeDetailsLayout.setVisibility(View.GONE);
            schemeSwitch.setVisibility(View.GONE);
            popUpLayout.setVisibility(View.GONE);
            navigationBar.setVisibility(View.VISIBLE);
            toolbar.setVisibility(View.GONE);

            if (currentPositionStop != null)
                zoomToCurrentPosition(currentPositionStop);
            Navigator.navigationMode = true;
            startGuidance(m_route);
        }
    }

    private String getRouteDistance(int meters) {
        String info = "";
        if (meters > 0 && meters > 1000)
            info = (getDecimalFormatter().format(meters / MILES_CONVERSION) + " miles");
        else if (meters > 0 && meters <= 1000)
            info = (int) meters + " m";
        return "Total Distance : " + info;
    }

    private DecimalFormat getDecimalFormatter() {
        return new DecimalFormat("0.0");
    }


    private void updateNavigationBar(int duration, int meters, double covered) {
        if (duration > 0) {
            eta.setText(timeConversion(duration));
        }

        if (covered > 0 && covered > 1000)
            distanceCovered.setText(getDecimalFormatter().format(covered / MILES_CONVERSION) + " miles");
        else if (covered > 0 && covered <= 1000)
            distanceCovered.setText((int) covered + " m");

        if (meters > 0 && meters > 1000)
            totalDistance.setText(getDecimalFormatter().format(meters / MILES_CONVERSION) + " miles");
        else if (meters > 0 && meters <= 1000)
            totalDistance.setText((int) meters + " m");
    }

    private void updateNavigationBar(int icon, int meters) {
        setManuverIcon(icon);
        if (meters > 0 && meters > 1000)
            distanceOfManeuver.setText("In" + getDecimalFormatter().format(meters / MILES_CONVERSION) + " miles");
        else if (meters > 0 && meters <= 1000)
            distanceOfManeuver.setText("In" + (int) meters + " m");

    }

    private void setManuverIcon(int id) {

        switch (id) {
            case 0:
                manuverIcon.setBackground(activity.getResources().getDrawable(R.drawable.maneuver_icon_0));
                break;
            case 1:
                manuverIcon.setBackground(activity.getResources().getDrawable(R.drawable.maneuver_icon_1));
                break;
            case 2:
                manuverIcon.setBackground(activity.getResources().getDrawable(R.drawable.maneuver_icon_2));
                break;
            case 3:
                manuverIcon.setBackground(activity.getResources().getDrawable(R.drawable.maneuver_icon_3));
                break;
            case 4:
                manuverIcon.setBackground(activity.getResources().getDrawable(R.drawable.maneuver_icon_4));
                break;
            case 5:
                manuverIcon.setBackground(activity.getResources().getDrawable(R.drawable.maneuver_icon_5));
                break;
            case 6:
                manuverIcon.setBackground(activity.getResources().getDrawable(R.drawable.maneuver_icon_6));
                break;
            case 7:
                manuverIcon.setBackground(activity.getResources().getDrawable(R.drawable.maneuver_icon_7));
                break;
            case 8:
                manuverIcon.setBackground(activity.getResources().getDrawable(R.drawable.maneuver_icon_8));
                break;
            case 9:
                manuverIcon.setBackground(activity.getResources().getDrawable(R.drawable.maneuver_icon_9));
                break;
            case 10:
                manuverIcon.setBackground(activity.getResources().getDrawable(R.drawable.maneuver_icon_10));
                break;
            case 11:
                manuverIcon.setBackground(activity.getResources().getDrawable(R.drawable.maneuver_icon_11));
                break;
            case 12:
                manuverIcon.setBackground(activity.getResources().getDrawable(R.drawable.maneuver_icon_12));
                break;
            case 13:
                manuverIcon.setBackground(activity.getResources().getDrawable(R.drawable.maneuver_icon_13));
                break;
            case 14:
                manuverIcon.setBackground(activity.getResources().getDrawable(R.drawable.maneuver_icon_14));
                break;
            case 15:
                manuverIcon.setBackground(activity.getResources().getDrawable(R.drawable.maneuver_icon_15));
                break;
            case 16:
                manuverIcon.setBackground(activity.getResources().getDrawable(R.drawable.maneuver_icon_16));
                break;
            case 17:
                manuverIcon.setBackground(activity.getResources().getDrawable(R.drawable.maneuver_icon_17));
                break;
            case 18:
                manuverIcon.setBackground(activity.getResources().getDrawable(R.drawable.maneuver_icon_18));
                break;
            case 19:
                manuverIcon.setBackground(activity.getResources().getDrawable(R.drawable.maneuver_icon_19));
                break;
            case 20:
                manuverIcon.setBackground(activity.getResources().getDrawable(R.drawable.maneuver_icon_20));
                break;
            case 21:
                manuverIcon.setBackground(activity.getResources().getDrawable(R.drawable.maneuver_icon_21));
                break;
            case 22:
                manuverIcon.setBackground(activity.getResources().getDrawable(R.drawable.maneuver_icon_22));
                break;
            case 23:
                manuverIcon.setBackground(activity.getResources().getDrawable(R.drawable.maneuver_icon_23));
                break;
            case 24:
                manuverIcon.setBackground(activity.getResources().getDrawable(R.drawable.maneuver_icon_24));
                break;
            case 25:
                manuverIcon.setBackground(activity.getResources().getDrawable(R.drawable.maneuver_icon_25));
                break;
            case 26:
                manuverIcon.setBackground(activity.getResources().getDrawable(R.drawable.maneuver_icon_26));
                break;
            case 27:
                manuverIcon.setBackground(activity.getResources().getDrawable(R.drawable.maneuver_icon_27));
                break;
            case 28:
                manuverIcon.setBackground(activity.getResources().getDrawable(R.drawable.maneuver_icon_28));
                break;
            case 29:
                manuverIcon.setBackground(activity.getResources().getDrawable(R.drawable.maneuver_icon_29));
                break;
            case 30:
                manuverIcon.setBackground(activity.getResources().getDrawable(R.drawable.maneuver_icon_30));
                break;
            case 31:
                manuverIcon.setBackground(activity.getResources().getDrawable(R.drawable.maneuver_icon_31));
                break;
            case 32:
                manuverIcon.setBackground(activity.getResources().getDrawable(R.drawable.maneuver_icon_32));
                break;
            case 33:
                manuverIcon.setBackground(activity.getResources().getDrawable(R.drawable.maneuver_icon_33));
                break;
            case 34:
                manuverIcon.setBackground(activity.getResources().getDrawable(R.drawable.maneuver_icon_34));
                break;
            case 35:
                manuverIcon.setBackground(activity.getResources().getDrawable(R.drawable.maneuver_icon_35));
                break;
            case 36:
                manuverIcon.setBackground(activity.getResources().getDrawable(R.drawable.maneuver_icon_36));
                break;
            case 37:
                manuverIcon.setBackground(activity.getResources().getDrawable(R.drawable.maneuver_icon_37));
                break;
            case 38:
                manuverIcon.setBackground(activity.getResources().getDrawable(R.drawable.maneuver_icon_38));
                break;
            case 39:
                manuverIcon.setBackground(activity.getResources().getDrawable(R.drawable.maneuver_icon_39));
                break;
            case 40:
                manuverIcon.setBackground(activity.getResources().getDrawable(R.drawable.maneuver_icon_40));
                break;
            case 41:
                manuverIcon.setBackground(activity.getResources().getDrawable(R.drawable.maneuver_icon_41));
                break;
            case 42:
                manuverIcon.setBackground(activity.getResources().getDrawable(R.drawable.maneuver_icon_42));
                break;
            case 43:
                manuverIcon.setBackground(activity.getResources().getDrawable(R.drawable.maneuver_icon_43));
                break;
            case 44:
                manuverIcon.setBackground(activity.getResources().getDrawable(R.drawable.maneuver_icon_44));
                break;
            case 45:
                manuverIcon.setBackground(activity.getResources().getDrawable(R.drawable.maneuver_icon_45));
                break;
            case 46:
                manuverIcon.setBackground(activity.getResources().getDrawable(R.drawable.maneuver_icon_46));
                break;
            case 47:
                manuverIcon.setBackground(activity.getResources().getDrawable(R.drawable.maneuver_icon_47));
                break;
            case 48:
                manuverIcon.setBackground(activity.getResources().getDrawable(R.drawable.maneuver_icon_48));
                break;
        }
    }

    public void selectRoute(Route route) {

        if (route != null) {
            if (mapRoute != null)
                map.removeMapObject(mapRoute);

            m_route = route;

            selectedRoute.setM_route(route);

            totalDistanceVal = route.getLength();
            updateNavigationBar(route.getTta(Route.TrafficPenaltyMode.OPTIMAL, Route.WHOLE_ROUTE).getDuration(), totalDistanceVal, 0);

            routeDetailsLayout.setVisibility(View.VISIBLE);
            selectedRouteDistance.setText(getRouteDistance(totalDistanceVal));
            selectedRouteEta.setText("Estimated Time : " + timeConversion(route.getTta(Route.TrafficPenaltyMode.OPTIMAL, Route.WHOLE_ROUTE).getDuration()));


            mapRoute = new MapRoute(route);
            mapRoute.setColor(activity.getResources().getColor(R.color.carrier_link_blue));

            /* Show the maneuver number on top of the route */
            mapRoute.setManeuverNumberVisible(true);

            /* Add the MapRoute to the map */
            map.addMapObject(mapRoute);

            /*
             * We may also want to make sure the map view is orientated properly
             * so the entire route can be easily seen.
             */
            m_geoBoundingBox = route.getBoundingBox();
            map.zoomTo(m_geoBoundingBox, Map.Animation.NONE,
                    Map.MOVE_PRESERVE_ORIENTATION);

            map.getPositionIndicator().setVisible(true);

            mapFragmentContainer.setVisibility(View.VISIBLE);
            routesLayout.setVisibility(View.GONE);
            createRoute.setVisibility(View.GONE);
            start.setVisibility(View.VISIBLE);
            toolbar.setVisibility(View.VISIBLE);
            controls.setVisibility(View.VISIBLE);
        }
    }

    public static String timeConversion(int totalSeconds) {

        final int MINUTES_IN_AN_HOUR = 60;
        final int SECONDS_IN_A_MINUTE = 60;

        int seconds = totalSeconds % SECONDS_IN_A_MINUTE;
        int totalMinutes = totalSeconds / SECONDS_IN_A_MINUTE;
        int minutes = totalMinutes % MINUTES_IN_AN_HOUR;
        int hours = totalMinutes / MINUTES_IN_AN_HOUR;

        StringBuilder sb = new StringBuilder();
        if (hours > 0)
            sb.append(hours).append("h ");
        if (minutes > 0)
            sb.append(minutes).append("m ");
        if (seconds > 0)
            sb.append(seconds).append("s");

        return sb.toString();
    }

    protected void stopNavigation(boolean getCurrentLocation, boolean secondTime) {
        removeNavigationListeners();
        if (mapMarkers != null) {
            map.removeMapObjects(mapMarkers);
        }

        if (mapRoute != null)
            map.removeMapObject(mapRoute);
        Navigator.navigationMode = false;
        if (!secondTime)
            tts.speak("Navigation ended", TextToSpeech.QUEUE_FLUSH, null);
        m_navigationManager.stop();

        selectedRoute.setM_route(null);
        m_route = null;

        stop.setVisibility(View.GONE);
        start.setVisibility(View.GONE);
        createRoute.setVisibility(View.GONE);
        schemeSwitch.setVisibility(View.VISIBLE);
        waypoints.clear();
        stopsAdapter.notifyDataSetChanged();
        eta.setText("");
        totalDistance.setText("");
        distanceCovered.setText("0");
        navigationBar.setVisibility(View.GONE);

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.BELOW, R.id.relativeLayout2);
        stopsView.setLayoutParams(layoutParams);
        toolbar.setVisibility(View.VISIBLE);
        currentPositionStop = null;
        // if (getCurrentLocation)
        getCurrentPosition();
    }

    private AudioPlayerDelegate player = new AudioPlayerDelegate() {
        @Override
        public boolean playText(final String s) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    tts.speak(s, TextToSpeech.QUEUE_FLUSH, null);
                }
            });
            return true;
        }

        @Override
        public boolean playFiles(String[] files) {
            // we don't want to play audio files
            return false;
        }
    };

    private void allowZoomOnMapGesture() {
        if (m_navigationManager != null)
            m_navigationManager.setMapUpdateMode(NavigationManager.MapUpdateMode.ROADVIEW_NOZOOM);
    }

    private void startGuidance(final Route route) {


        final VoiceCatalog voiceCatalog = VoiceCatalog.getInstance();
        // Get the list of voice packages from the voice catalog list
        List<VoicePackage> voicePackages = VoiceCatalog.getInstance().getCatalogList();

        voiceSkinId = -1;

// select
        for (VoicePackage vPackage : voicePackages) {
            if (vPackage.getMarcCode().compareToIgnoreCase("eng") == 0) {
                if (vPackage.isTts()) {
                    voiceSkinId = vPackage.getId();
                    break;
                }
            }
        }

        if (!voiceCatalog.isLocalVoiceSkin(voiceSkinId)) {
            voiceCatalog.downloadVoice(voiceSkinId, new VoiceCatalog.OnDownloadDoneListener() {
                @Override
                public void onDownloadDone(VoiceCatalog.Error error) {
                    if (error == VoiceCatalog.Error.NONE) {
                        // catalog download successful
                        m_navigationManager.setVoiceSkin(voiceCatalog.getLocalVoiceSkin(voiceSkinId));
                        // better visuals when switching to special car navigation map scheme
                        map.setTilt(45);

                        // set guidance view to position with road ahead, tilt and zoomlevel was setup before manually
                        // choose other update modes for different position and zoom behavior
                        m_navigationManager.setMapUpdateMode(NavigationManager.MapUpdateMode.ROADVIEW);

                        // get new guidance instructions
                        m_navigationManager.addNewInstructionEventListener(new WeakReference<>(instructListener));

                        // set usage of Nuance TTS engine if specified
                        if (isInitialized) {
                            m_navigationManager.getAudioPlayer().setDelegate(player);
                        } else {
                            // passing null delete any custom audio player that was set earlier
                            m_navigationManager.getAudioPlayer().setDelegate(null);
                        }

                        // start simulation with speed of 10 m/s
                        //NavigationManager.Error e = NavigationManager.getInstance().simulate(route, 10);

                        // start real guidance
                        startNavigation(route);
                    }
                }
            });
        } else {
            m_navigationManager.setVoiceSkin(voiceCatalog.getLocalVoiceSkin(voiceSkinId));
            // better visuals when switching to special car navigation map scheme
            map.setTilt(45);

            // set guidance view to position with road ahead, tilt and zoomlevel was setup before manually
            // choose other update modes for different position and zoom behavior
            m_navigationManager.setMapUpdateMode(NavigationManager.MapUpdateMode.POSITION_ANIMATION);

            // get new guidance instructions
            m_navigationManager.addNewInstructionEventListener(new WeakReference<>(instructListener));

            // set usage of Nuance TTS engine if specified
            if (isInitialized) {
                m_navigationManager.getAudioPlayer().setDelegate(player);
            } else {
                // passing null delete any custom audio player that was set earlier
                m_navigationManager.getAudioPlayer().setDelegate(null);
            }

            // start simulation with speed of 10 m/s
            //NavigationManager.Error e = NavigationManager.getInstance().simulate(route, 10);

            // start real guidance
            startNavigation(route);
        }


        // NavigationManager.Error e = NavigationManager.getInstance().startNavigation(route);

    }


    private void startNavigation(Route route) {
        /*
         * Start the turn-by-turn navigation.Please note if the transport mode of the passed-in
         * route is pedestrian, the NavigationManager automatically triggers the guidance which is
         * suitable for walking. Simulation and tracking modes can also be launched at this moment
         * by calling either simulate() or startTracking()
         */

        if (Navigator.simulate) {
            long speed;
            String userInput = speedForSimulation.getText().toString().trim();

            if (userInput != null && !userInput.isEmpty() && !userInput.equalsIgnoreCase("0")) {
                double sp = Long.parseLong(userInput) * 0.448;
                speed = (long) sp;
            } else
                speed = 30;

            m_navigationManager.simulate(route, speed);
        } else {
            m_navigationManager.startNavigation(route);
        }

        /*
         * Set the map update mode to ROADVIEW.This will enable the automatic map movement based on
         * the current location.If user gestures are expected during the navigation, it's
         * recommended to set the map update mode to NONE first. Other supported update mode can be
         * found in HERE Android SDK API doc
         */
        m_navigationManager.setMapUpdateMode(NavigationManager.MapUpdateMode.ROADVIEW);

    }

    private void addNavigationListeners() {
        /*
         * Register a NavigationManagerEventListener to monitor the status change on
         * NavigationManager
         */
        m_navigationManager.addNavigationManagerEventListener(
                new WeakReference<NavigationManager.NavigationManagerEventListener>(
                        m_navigationManagerEventListener));

        /* Register a PositionListener to monitor the position updates */
        m_navigationManager.addPositionListener(
                new WeakReference<NavigationManager.PositionListener>(m_positionListener));

        // start listening to navigation events
        m_navigationManager.addNewInstructionEventListener(
                new WeakReference<NavigationManager.NewInstructionEventListener>(instructListener));

        // rerouting the path when user goes out of the calculated route
        m_navigationManager.addRerouteListener(
                new WeakReference<NavigationManager.RerouteListener>(rerouteListener));

        // receive audio feedbacks
        m_navigationManager.addAudioFeedbackListener(
                new WeakReference<NavigationManager.AudioFeedbackListener>(audioFeedbackListener));

    }

    private void removeNavigationListeners() {
        try {
            m_navigationManager.removeNavigationManagerEventListener(m_navigationManagerEventListener);
            m_navigationManager.removePositionListener(m_positionListener);
            m_navigationManager.removeNewInstructionEventListener(instructListener);
            m_navigationManager.removeRerouteListener(rerouteListener);
            m_navigationManager.removeAudioFeedbackListener(audioFeedbackListener);
        } catch (Exception e) {

        }
    }


    private NavigationManager.PositionListener m_positionListener = new NavigationManager.PositionListener() {
        @Override
        public void onPositionUpdated(GeoPosition geoPosition) {
            /* Current position information can be retrieved in this callback
             the position we get in this callback can be used
            to reposition the map and change orientation.*/
            geoPosition.getCoordinate();
            geoPosition.getHeading();
            geoPosition.getSpeed();

            try {
                double distanceCovered = totalDistanceVal - m_navigationManager.getDestinationDistance();
                updateNavigationBar(m_navigationManager.getTta(Route.TrafficPenaltyMode.OPTIMAL, true).getDuration(), 0, distanceCovered);
            } catch (NullPointerException e) {
            }
        }
    };

    private NavigationManager.NewInstructionEventListener instructListener
            = new NavigationManager.NewInstructionEventListener() {

        @Override
        public void onNewInstructionEvent() {
            // Interpret and present the Maneuver object as it contains
            // turn by turn navigation instructions for the user.
            try {
                Maneuver maneuver = m_navigationManager.getNextManeuver();
                maneuver.getIcon().value();
                maneuver.getDistanceToNextManeuver();
                maneuver.getAction().name();
                if (maneuver.getIcon() != null)
                    updateNavigationBar(maneuver.getIcon().value(), maneuver.getDistanceToNextManeuver());
                else
                    updateNavigationBar(-1, maneuver.getDistanceToNextManeuver());
            } catch (NullPointerException e) {
            }
        }
    };


    private NavigationManager.RerouteListener rerouteListener = new NavigationManager.RerouteListener() {
        @Override
        public void onRerouteBegin() {
            super.onRerouteBegin();

        }

        @Override
        public void onRerouteEnd(Route route) {
            super.onRerouteEnd(route);

            if (mapRoute != null) {
                map.removeMapObject(mapRoute);
            }

            m_route = route;
            /* Create a MapRoute so that it can be placed on the map */
            mapRoute = new MapRoute(m_route);

            /* Show the maneuver number on top of the route */
            mapRoute.setManeuverNumberVisible(true);

            /* Add the MapRoute to the map */
            map.addMapObject(mapRoute);

            /*
             * We may also want to make sure the map view is orientated properly
             * so the entire route can be easily seen.
             */
            m_geoBoundingBox = m_route.getBoundingBox();
            map.zoomTo(m_geoBoundingBox, Map.Animation.NONE,
                    Map.MOVE_PRESERVE_ORIENTATION);

            map.getPositionIndicator().setVisible(true);
        }

        @Override
        public void onRerouteFailed() {
            super.onRerouteFailed();
        }
    };

    private NavigationManager.AudioFeedbackListener audioFeedbackListener = new NavigationManager.AudioFeedbackListener() {
        @Override
        public void onAudioStart() {
            super.onAudioStart();
        }

        @Override
        public void onAudioEnd() {
            super.onAudioEnd();
        }

        @Override
        public void onVibrationStart() {
            super.onVibrationStart();
        }

        @Override
        public void onVibrationEnd() {
            super.onVibrationEnd();
        }
    };

    private NavigationManager.NavigationManagerEventListener m_navigationManagerEventListener = new NavigationManager.NavigationManagerEventListener() {
        @Override
        public void onRunningStateChanged() {
            //Toast.makeText(activity, "Running state changed", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onNavigationModeChanged() {
            //Toast.makeText(activity, "Navigation mode changed", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onEnded(NavigationManager.NavigationMode navigationMode) {
            //Toast.makeText(activity, navigationMode + " was ended", Toast.LENGTH_SHORT).show();
            stopNavigation(true, true);
        }

        @Override
        public void onMapUpdateModeChanged(NavigationManager.MapUpdateMode mapUpdateMode) {
            //Toast.makeText(activity, "Map update mode is changed to " + mapUpdateMode,Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onRouteUpdated(Route route) {
            Toast.makeText(activity, "Route updated", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCountryInfo(String s, String s1) {
            //Toast.makeText(activity, "Country info updated from " + s + " to " + s1,Toast.LENGTH_SHORT).show();
        }
    };


    @Override
    public void onMapTransformStart() {
        mTransforming = true;
    }

    @Override
    public void onMapTransformEnd(MapState mapState) {
        mTransforming = false;
        if (mPendingUpdate != null) {
            mPendingUpdate.run();
            mPendingUpdate = null;
        }
    }

    private void getCurrentLocation(final GeoCoordinate coordinate) {
        positioningManager.removeListener(positionChangedListener);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Stop stop = new Stop();
                stop.setGeoCoordinate(coordinate);
                stop.setRouteWaypoint(new RouteWaypoint(coordinate));
                triggerRevGeocodeRequest(stop);

            }
        }, 0);
    }

    private PositioningManager.OnPositionChangedListener positionChangedListener = new PositioningManager.OnPositionChangedListener() {
        @Override
        public void onPositionUpdated(final PositioningManager.LocationMethod locationMethod, final GeoPosition geoPosition, final boolean mapMatched) {
            final GeoCoordinate coordinate = geoPosition.getCoordinate();
            if (mTransforming) {
                mPendingUpdate = new Runnable() {
                    @Override
                    public void run() {
                        onPositionUpdated(locationMethod, geoPosition, mapMatched);
                    }
                };
            } else {
                map.setCenter(coordinate, Map.Animation.BOW);
                positioningManager.removeListener(positionChangedListener);
                getCurrentLocation(coordinate);
            }
        }

        @Override
        public void onPositionFixChanged(PositioningManager.LocationMethod locationMethod, PositioningManager.LocationStatus locationStatus) {

        }
    };

    public void showProgressBasedOnMapAndPosition() {
        showProgress("App is getting ready...");
    }


    private void showProgress(String message) {
        hideProgress();
        try {
            if (activity != null) {
                if (progressBar == null) {
                    progressBar = new ProgressDialog(activity);
                    progressBar.setCancelable(false);
                    progressBar.setMessage(message);
                    progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                }

                if (progressBar != null && !progressBar.isShowing() && !activity.isFinishing()) {
                    progressBar.show();
                }
            }
        } catch (IllegalArgumentException e) {

        }
    }

    private void hideProgress() {
        try {
            if (progressBar != null && progressBar.isShowing()) {
                progressBar.dismiss();
                progressBar = null;
            }
        } catch (IllegalArgumentException e) {

        }
    }

    public void showToast(String message) {
        if (toast != null)
            toast.cancel();

        toast = Toast.makeText(activity, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM, 0, 0);
        toast.show();
    }
}
