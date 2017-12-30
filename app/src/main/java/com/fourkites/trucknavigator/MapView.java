/*
 * Copyright (c) 2011-2017 HERE Europe B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fourkites.trucknavigator;

import android.annotation.TargetApi;
import android.app.Activity;
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
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.here.android.mpa.common.ApplicationContext;
import com.here.android.mpa.common.GeoBoundingBox;
import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.GeoPosition;
import com.here.android.mpa.common.IconCategory;
import com.here.android.mpa.common.LocationDataSourceHERE;
import com.here.android.mpa.common.OnEngineInitListener;
import com.here.android.mpa.common.PositioningManager;
import com.here.android.mpa.guidance.AudioPlayerDelegate;
import com.here.android.mpa.guidance.NavigationManager;
import com.here.android.mpa.guidance.VoiceCatalog;
import com.here.android.mpa.guidance.VoicePackage;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapFragment;
import com.here.android.mpa.mapping.MapMarker;
import com.here.android.mpa.mapping.MapObject;
import com.here.android.mpa.mapping.MapRoute;
import com.here.android.mpa.mapping.MapState;
import com.here.android.mpa.mapping.MapTrafficLayer;
import com.here.android.mpa.mapping.MapTransitLayer;
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
import java.util.ArrayList;
import java.util.List;

/**
 * This class encapsulates the properties and functionality of the Map view.It also triggers a
 * turn-by-turn navigation from HERE Burnaby office to Langley BC.There is a sample voice skin
 * bundled within the SDK package to be used out-of-box, please refer to the Developer's guide for
 * the usage.
 */
public class MapView implements Map.OnTransformListener {
    private MapFragment m_mapFragment;
    private Activity activity;
    private Button m_naviControlButton;
    private static Map map;
    private NavigationManager m_navigationManager;
    private GeoBoundingBox m_geoBoundingBox;
    private Route m_route;
    private MapRoute mapRoute;
    private boolean isInitialized;
    TextToSpeech tts;
    private long voiceSkinId;
    private TextView duration;
    private RelativeLayout mapFragmentContainer;
    // map embedded in the map fragment
    //private Map map;

    // map fragment embedded in this activity
    private MapFragment mapFragment;

    // positioning manager instance
    private PositioningManager mPositioningManager;

    // HERE location data source instance
    private LocationDataSourceHERE mHereLocation;

    // flag that indicates whether maps is being transformed
    private boolean mTransforming;

    // callback that is called when transforming ends
    private Runnable mPendingUpdate;
    // Nuance TTS

    // using to decide to use Nuance or no, by default TTS engine on device will be used
    // private boolean useNuance = false;

    private static List<Stop> waypoints;
    private static List<Result> searchResults;
    private final String TAG = "MAPVIEW";
    private RecyclerView stopsView;
    private RecyclerView suggestionView;
    // private FloatingActionButton createRoute;

    private StopsAdapter stopsAdapter;
    private RouteWaypoint currentPosition;
    private GeoCoordinate currentGeo;
    private FloatingActionButton start;
    private FloatingActionButton stop;
    private FloatingActionButton createRoute;

    //private RelativeLayout toolbarLayout;
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
    //private AppBarLayout appBar;
    //private android.support.v7.widget.Toolbar mToolbar;
    private TextView distanceCovered;
    private TextView totalDistance;
    private TextView eta;
    private ImageView manuverIcon;
    private TextView distanceOfManeuver;
    private RelativeLayout navigationBar;
    private int totalDistanceVal;
    private LinearLayout searchLayout;
    private AutoCompleteTextView searchView;
    private Handler handler = new Handler();
    private Runnable run;
    private Stop selectedStopForSuggestion;
    private int selectedStopPosition;
    RequestQueue queue;
    private AutoSuggestAdapter autoSuggestAdapter;
    private final int MAX_HEIGHT = 180;
    private int stopsViewHeight;
    private RelativeLayout toolbar;
    private Stop currentPositionStop;


    public MapView(Activity activity, boolean isTtsEnabled, TextToSpeech tts) {
        this.activity = activity;
        this.tts = tts;
        isInitialized = isTtsEnabled;
        waypoints = new ArrayList<>();
        mapMarkers = new ArrayList<>();
        searchResults = new ArrayList<>();
        queue = Volley.newRequestQueue(activity);
        initMapFragment();
        // initNaviControlButton();
    }

    private void initMapFragment() {
        m_mapFragment = new MapFragment();
        activity.getFragmentManager().beginTransaction().add(R.id.mapFragmentContainer, m_mapFragment, "MAP_TAG").commit();
        m_mapFragment.setRetainInstance(true);
        //mToolbar = (Toolbar) activity.findViewById(R.id.toolbar);
        toolbar = (RelativeLayout) activity.findViewById(R.id.toolbar);

       /* appBar = (AppBarLayout) activity.findViewById(R.id.app_bar);
        appBar.setExpanded(true);*/
        // createRoute = (FloatingActionButton) activity.findViewById(R.id.createRoute);
        start = (FloatingActionButton) activity.findViewById(R.id.startNavigation);
        createRoute = (FloatingActionButton) activity.findViewById(R.id.createRoute);
        stop = (FloatingActionButton) activity.findViewById(R.id.stopNavigation);
        // toolbarLayout = (RelativeLayout) activity.findViewById(R.id.toolbarLayout);
        schemeSwitch = (ImageView) activity.findViewById(R.id.schemeSwitch);
        popUpLayout = (LinearLayout) activity.findViewById(R.id.popUpLayout);
        mapView = (TextView) activity.findViewById(R.id.mapView);
        satellite = (TextView) activity.findViewById(R.id.satellite);
        terrain = (TextView) activity.findViewById(R.id.terrain);
        trafficConditions = (TextView) activity.findViewById(R.id.trafficConditions);
        publicTransport = (TextView) activity.findViewById(R.id.publicTransport);
        showTrafficIncidents = (TextView) activity.findViewById(R.id.showTrafficIncidents);
        currentLocation = (ImageView) activity.findViewById(R.id.currentLocation);


        distanceCovered = (TextView) activity.findViewById(R.id.distanceCovered);
        totalDistance = (TextView) activity.findViewById(R.id.totalDistance);
        eta = (TextView) activity.findViewById(R.id.eta);
        navigationBar = (RelativeLayout) activity.findViewById(R.id.navigationBar);
        manuverIcon = (ImageView) activity.findViewById(R.id.manuverIcon);
        distanceOfManeuver = (TextView) activity.findViewById(R.id.distanceOfManeuver);
        stopsView = (RecyclerView) activity.findViewById(R.id.stopsView);

        stopsAdapter = new StopsAdapter(this, activity, waypoints);
        LinearLayoutManager layoutManager = new LinearLayoutManager(activity);
        stopsView.setLayoutManager(layoutManager);
        stopsView.setAdapter(stopsAdapter);


        searchLayout = (LinearLayout) activity.findViewById(R.id.searchLayout);

        ImageView addStop = (ImageView) activity.findViewById(R.id.addStop);
        ImageView back = (ImageView) activity.findViewById(R.id.back);
        ImageView close = (ImageView) activity.findViewById(R.id.remove);
        searchView = (AutoCompleteTextView) activity.findViewById(R.id.searchView);

        suggestionView = (RecyclerView) activity.findViewById(R.id.suggestionView);

        addStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (searchResults != null && autoSuggestAdapter != null) {
                    searchResults.clear();
                    autoSuggestAdapter.notifyDataSetChanged();
                }
                Stop stop = new Stop();
                addWaypoint(stop);
                selectedStopForSuggestion = stop;
                if (waypoints != null)
                    selectedStopPosition = waypoints.size() - 1;
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchLayout.setVisibility(View.GONE);
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.setText("");
            }
        });


       /* createRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (map != null) {
                    createRoute();
                }
            }
        });*/

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startNavigation();
                //toolbarLayout.setVisibility(View.GONE);
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopNavigation();

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
                getCurrentPosition();
            }
        });

       /* duration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mapFragmentContainer.getVisibility() == View.VISIBLE) {
                    mapFragmentContainer.setVisibility(View.GONE);
                } else {
                    mapFragmentContainer.setVisibility(View.VISIBLE);
                }
            }
        });*/


        //search view auto suggest
        searchView.setThreshold(3);

        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchView.isFocused()) {
                    if (!searchView.getText().toString().equals("")) {

                        if (searchView.getText().toString().length() >= 3) {
                            String text = searchView.getText().toString();
                            final String txt = text;
                            handler.removeCallbacks(run);

                            run = new Runnable() {
                                public void run() {
                                    //stopViewHolder.loadingCircle.setVisibility(View.VISIBLE);
                                    suggesstionsRequest(selectedStopForSuggestion, txt.trim().toString());
                                }
                            };
                            handler.postDelayed(run, 0);
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




        /* Initialize the MapFragment, results will be given via the called back. */
        if (m_mapFragment != null) {
            ApplicationContext context = new ApplicationContext(activity);
            m_mapFragment.init(context, new OnEngineInitListener() {
                @Override
                public void onEngineInitializationCompleted(Error error) {

                    if (error == Error.NONE) {
                        map = m_mapFragment.getMap();
                        positioningManager = PositioningManager.getInstance();
                        getCurrentPosition();

                       /* m_navigationManager = NavigationManager.getInstance();
                        //map.getPositionIndicator().setVisible(true);
                        m_navigationManager.setMap(map);
                        createRoute();*/

                        m_navigationManager = NavigationManager.getInstance();
                        m_navigationManager.setMap(map);
                        /*
                        * NavigationManager contains a number of listeners which we can use to monitor the
                        * navigation status and getting relevant instructions.In this example, we will add 2
                        * listeners for demo purpose,please refer to HERE Android SDK API documentation for details
                        */
                        addNavigationListeners();


                        // more map settings
                        map.setProjectionMode(Map.Projection.GLOBE);  // globe projection
                        map.setExtrudedBuildingsVisible(true);  // enable 3D building footprints
                        map.setLandmarksVisible(true);  // 3D Landmarks visible
                        map.setCartoMarkersVisible(IconCategory.ALL, true);  // show embedded map markers
                        map.setSafetySpotsVisible(true); // show speed cameras as embedded markers on the map

                        map.setMapScheme(Map.Scheme.NORMAL_DAY);   // normal day mapscheme

                        // traffic options
                        map.setTrafficInfoVisible(false);
                        mapSchemeSwitcher();
                        //geocodingRequest();
                    } else {
                        Toast.makeText(activity,
                                "ERROR: Cannot initialize Map with error " + error,
                                Toast.LENGTH_LONG).show();
                    }
                }
            });
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
            waypoints.set(pos, stop);
            map.setCenter(stop.getGeoCoordinate(), Map.Animation.BOW);

            stopsAdapter.notifyDataSetChanged();
            refreshRoute();
        }
    }

    public void refreshRoute() {
        if (map != null) {
            if (mapMarkers != null) {
                map.removeMapObjects(mapMarkers);
            }

            if (mapRoute != null)
                map.removeMapObject(mapRoute);

            if (waypoints != null) {
                addMarkerToMap(waypoints, false);

                if (waypoints.size() > 1) {
                    //navigationBar.setVisibility(View.VISIBLE);
                    createRoute.setVisibility(View.VISIBLE);
                } else {
                    //navigationBar.setVisibility(View.GONE);
                    createRoute.setVisibility(View.GONE);
                }
            }

            if(start.getVisibility() == View.VISIBLE)
                start.setVisibility(View.GONE);
        }
    }


    private void suggesstionsRequest(final Stop stop, String query) {
        if (stop.getAddress() != null && !stop.getAddress().isEmpty())
            query = stop.getAddress();

        String url = "https://places.api.here.com/places/v1/autosuggest?X-Map-Viewport=-76.5633,81.2945,102.7335,90.0000&app_code=K2Cpd_EKDzrZb1tz0zdpeQ&app_id=bC4fb9WQfCCZfkxspD4z&q=" + query + "&result_types=address,place,category,chain&size=10";
        StringRequest getAutoSuggestRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response != null && !response.isEmpty()) {
                    Gson gson = new Gson();
                    SearchResults results = gson.fromJson(response, SearchResults.class);
                    if (results.getResults() != null && results.getResults().size() > 0) {
                        //we have results
                        if (searchResults != null)
                            searchResults.clear();

                        //Adding current location in suggestions
                        if(currentPositionStop != null){
                            Result result = new Result();
                            List<Double> position = new ArrayList<>();
                            position.add(currentPositionStop.getGeoCoordinate().getLatitude());
                            position.add(currentPositionStop.getGeoCoordinate().getLongitude());
                            result.setPosition(position);
                            result.setHighlightedTitle("Your Location");
                            result.setHighlightedVicinity("\n"+currentPositionStop.getAddress());
                            result.setCurrentLocation(true);
                            searchResults.add(result);
                        }


                        searchResults.addAll(results.getResults());

                        if (autoSuggestAdapter == null) {
                            autoSuggestAdapter = new AutoSuggestAdapter(MapView.this, activity, R.layout.suggesstion_item, searchResults, stop, selectedStopPosition);
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
                        Toast.makeText(activity, "No suggestions", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(activity, "No suggestions", Toast.LENGTH_SHORT).show();
            }
        });

        queue.add(getAutoSuggestRequest);

    }

    protected void editSuggestion(Stop stop, int position) {
        if (stop != null) {
            String query = "";
            if (searchLayout.getVisibility() != View.VISIBLE)
                searchLayout.setVisibility(View.VISIBLE);

            selectedStopForSuggestion = stop;
            selectedStopPosition = position;

            if (stop.getAddress() != null) {
                query = stop.getAddress();
            }
            searchView.setText(query);
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

                        //mapMarker.setDraggable(true);
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

    private MapMarker.OnDragListener onDragListener = new MapMarker.OnDragListener() {
        @Override
        public void onMarkerDrag(MapMarker mapMarker) {
            mapMarker.getCoordinate().getLatitude();
        }

        @Override
        public void onMarkerDragEnd(MapMarker mapMarker) {
            mapMarker.getCoordinate().getLatitude();
        }

        @Override
        public void onMarkerDragStart(MapMarker mapMarker) {
            mapMarker.getCoordinate().getLatitude();
        }
    };


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

                        mapView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //addMapScheme(Map.Scheme.NORMAL_DAY);
                                map.setMapScheme(Map.Scheme.NORMAL_DAY);
                                mapView.setBackground(activity.getResources().getDrawable(R.drawable.map_scheme_highlighter));
                                satellite.setBackground(null);
                                terrain.setBackground(null);
                            }
                        });
                        satellite.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // addMapScheme(Map.Scheme.SATELLITE_DAY);
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
                                //addMapScheme(Map.Scheme.TERRAIN_DAY);
                                map.setMapScheme(Map.Scheme.TERRAIN_DAY);
                                terrain.setBackground(activity.getResources().getDrawable(R.drawable.map_scheme_highlighter));
                                satellite.setBackground(null);
                                mapView.setBackground(null);
                            }
                        });
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
        if (m_mapFragment != null) {
            PositionIndicator positionIndicator = m_mapFragment.getPositionIndicator();
            if (!positionIndicator.isVisible())
                positionIndicator.setVisible(true);
            if (!positionIndicator.isAccuracyIndicatorVisible())
                positionIndicator.setAccuracyIndicatorVisible(true);
        }
    }


    private void getCurrentPosition() {

        LocationDataSourceHERE m_hereDataSource = LocationDataSourceHERE.getInstance();
        /*if (m_hereDataSource != null)*/
        {

            positioningManager.addListener(new WeakReference<PositioningManager.OnPositionChangedListener>(positionChangedListener));
            //positioningManager.setDataSource(m_hereDataSource);
            if (positioningManager.start(PositioningManager.LocationMethod.GPS_NETWORK)) {

                if (positioningManager.hasValidPosition()) {
                    currentGeo = positioningManager.getPosition().getCoordinate();
                    currentPosition = new RouteWaypoint(currentGeo);
                    getCurrentLocation(currentGeo);
                    positioningManager.removeListener(positionChangedListener);
                }/* else {
                currentGeo = positioningManager.getLastKnownPosition().getCoordinate();
                currentPosition = new RouteWaypoint(positioningManager.getLastKnownPosition().getCoordinate());
            }*/


           /* ResultListener<Location> listener = new ReverseGeocodeListener();
            ReverseGeocodeRequest2 request = new ReverseGeocodeRequest2(currentPosition);
            if (request.execute(listener) != ErrorCode.NONE) {
                Log.d(TAG, "Error in getCurrentPosition");
            }*/
            } else {
                Toast.makeText(activity, "PositioningManager.start: failed, exiting", Toast.LENGTH_LONG).show();
            }
        }

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
                    showCurrentPositionMarker();
                } else {
                    Log.i("MapView", "ERROR:RevGeocode Request returned error code:" + errorCode);

                }
            }
        });
    }

    private Stop buildStop(GeoCoordinate geoCoordinate, String Address) {
        if (geoCoordinate != null) {
            Stop stop = new Stop();
            stop.setAddress(Address);
            stop.setGeoCoordinate(geoCoordinate);
            stop.setRouteWaypoint(new RouteWaypoint(geoCoordinate));
            return stop;
        }
        return null;
    }

    public void addWaypoint(Stop stop) {
        if (waypoints == null)
            waypoints = new ArrayList<>();

        waypoints.add(stop);
        stopsAdapter.notifyDataSetChanged();

        if(waypoints.size() > 1)
            createRoute.setVisibility(View.VISIBLE);

        if (waypoints.size() > 2) {
            int max = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 120, activity.getResources().getDisplayMetrics());

            stopsView.getLayoutParams().height = max;
        }
        if (waypoints != null)
            stopsView.smoothScrollToPosition(waypoints.size() - 1);
       /* stopsViewHeight = toolbar.getHeight();
        stopsViewHeight = stopsViewHeight + 30;
        ViewGroup.LayoutParams layoutParams = toolbar.getLayoutParams();

        if (stopsViewHeight > MAX_HEIGHT) {
            layoutParams.height = MAX_HEIGHT;
        } else {
            layoutParams.height = stopsViewHeight;
        }

        toolbar.setLayoutParams(layoutParams);*/
    }


    private void createRoute() {

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
        /* Disable highway in this route. */
        //routeOptions.setHighwaysAllowed(false);
        /* Calculate the shortest route available. */
        routeOptions.setRouteType(RouteOptions.Type.FASTEST);
        /* Calculate 1 route. */
        routeOptions.setRouteCount(3);
        /* Finally set the route option */
        routePlan.setRouteOptions(routeOptions);


        /* Define waypoints for the route */
        /* START: 4350 Still Creek Dr */
        //RouteWaypoint startPoint = new RouteWaypoint(currentPosition);
        //routePlan.addWaypoint(currentPosition);
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
                                if (routeResults.get(0).getRoute() != null) {

                                    m_route = routeResults.get(0).getRoute();
                                    //navigationBar.setVisibility(View.VISIBLE);
                                    totalDistanceVal = m_route.getLength();
                                    updateNavigationBar(m_route.getTta(Route.TrafficPenaltyMode.OPTIMAL, Route.WHOLE_ROUTE).getDuration(), totalDistanceVal, 0);

                                   // updateNavigationBar(m_route.getFirstManeuver().getIcon().value(), m_route.getFirstManeuver().getDistanceFromStart());
                                    /* Create a MapRoute so that it can be placed on the map */
                                    mapRoute = new MapRoute(routeResults.get(0).getRoute());
                                    mapRoute.setColor(activity.getResources().getColor(R.color.carrier_link_blue));

                                /* Show the maneuver number on top of the route */
                                    mapRoute.setManeuverNumberVisible(true);

                                /* Add the MapRoute to the map */
                                    map.addMapObject(mapRoute);

                                /*
                                 * We may also want to make sure the map view is orientated properly
                                 * so the entire route can be easily seen.
                                 */
                                    m_geoBoundingBox = routeResults.get(0).getRoute().getBoundingBox();
                                    map.zoomTo(m_geoBoundingBox, Map.Animation.NONE,
                                            Map.MOVE_PRESERVE_ORIENTATION);

                                    map.getPositionIndicator().setVisible(true);

                                    createRoute.setVisibility(View.GONE);

                                    start.setVisibility(View.VISIBLE);
                                    //createRoute.setVisibility(View.GONE);

                                } else {
                                    Toast.makeText(activity,
                                            "Error:route results returned is not valid",
                                            Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(activity,
                                        "Error:route calculation returned error code: " + routingError,
                                        Toast.LENGTH_LONG).show();

                            }
                        }
                    });
        }
    }

    protected void startNavigation() {
        if (m_route != null) {
            start.setVisibility(View.GONE);
            stop.setVisibility(View.VISIBLE);
            schemeSwitch.setVisibility(View.GONE);
            popUpLayout.setVisibility(View.GONE);
            navigationBar.setVisibility(View.VISIBLE);
            toolbar.setVisibility(View.GONE);

            // appBar.setVisibility(View.GONE);
            hideAppBar();

            startGuidance(m_route);
        }
    }

    private void updateNavigationBar(int duration, int meters, int covered) {
        if (duration > 0) {
            eta.setText(timeConversion(duration));
        }

        if (covered > 0 && covered > 1000)
            distanceCovered.setText((covered / 1000) + " km");
        else if (covered > 0 && covered < 1000)
            distanceCovered.setText(covered + " m");

        if (meters > 0 && meters > 1000)
            totalDistance.setText((meters / 1000) + "km");
        else if (meters > 0 && meters < 1000)
            totalDistance.setText(meters + " m");
      /*  if(icon != null)
            manuverIcon.setImageBitmap(icon);*/

    }

    private void updateNavigationBar(int icon, int meters) {
        setManuverIcon(icon);
        if (meters > 0 && meters > 1000)
            distanceOfManeuver.setText("In" + (meters / 1000) + " km");
        else if (meters > 0 && meters < 1000)
            distanceOfManeuver.setText("In" + meters + " m");

    }

    private void setManuverIcon(int id) {

        switch (id) {
            case 0:
                manuverIcon.setBackground(activity.getDrawable(R.drawable.maneuver_icon_0));
                break;
            case 1:
                manuverIcon.setBackground(activity.getDrawable(R.drawable.maneuver_icon_1));
                break;
            case 2:
                manuverIcon.setBackground(activity.getDrawable(R.drawable.maneuver_icon_2));
                break;
            case 3:
                manuverIcon.setBackground(activity.getDrawable(R.drawable.maneuver_icon_3));
                break;
            case 4:
                manuverIcon.setBackground(activity.getDrawable(R.drawable.maneuver_icon_4));
                break;
            case 5:
                manuverIcon.setBackground(activity.getDrawable(R.drawable.maneuver_icon_5));
                break;
            case 6:
                manuverIcon.setBackground(activity.getDrawable(R.drawable.maneuver_icon_6));
                break;
            case 7:
                manuverIcon.setBackground(activity.getDrawable(R.drawable.maneuver_icon_7));
                break;
            case 8:
                manuverIcon.setBackground(activity.getDrawable(R.drawable.maneuver_icon_8));
                break;
            case 9:
                manuverIcon.setBackground(activity.getDrawable(R.drawable.maneuver_icon_9));
                break;
            case 10:
                manuverIcon.setBackground(activity.getDrawable(R.drawable.maneuver_icon_10));
                break;
            case 11:
                manuverIcon.setBackground(activity.getDrawable(R.drawable.maneuver_icon_11));
                break;
            case 12:
                manuverIcon.setBackground(activity.getDrawable(R.drawable.maneuver_icon_12));
                break;
            case 13:
                manuverIcon.setBackground(activity.getDrawable(R.drawable.maneuver_icon_13));
                break;
            case 14:
                manuverIcon.setBackground(activity.getDrawable(R.drawable.maneuver_icon_14));
                break;
            case 15:
                manuverIcon.setBackground(activity.getDrawable(R.drawable.maneuver_icon_15));
                break;
            case 16:
                manuverIcon.setBackground(activity.getDrawable(R.drawable.maneuver_icon_16));
                break;
            case 17:
                manuverIcon.setBackground(activity.getDrawable(R.drawable.maneuver_icon_17));
                break;
            case 18:
                manuverIcon.setBackground(activity.getDrawable(R.drawable.maneuver_icon_18));
                break;
            case 19:
                manuverIcon.setBackground(activity.getDrawable(R.drawable.maneuver_icon_19));
                break;
            case 20:
                manuverIcon.setBackground(activity.getDrawable(R.drawable.maneuver_icon_20));
                break;
            case 21:
                manuverIcon.setBackground(activity.getDrawable(R.drawable.maneuver_icon_21));
                break;
            case 22:
                manuverIcon.setBackground(activity.getDrawable(R.drawable.maneuver_icon_22));
                break;
            case 23:
                manuverIcon.setBackground(activity.getDrawable(R.drawable.maneuver_icon_23));
                break;
            case 24:
                manuverIcon.setBackground(activity.getDrawable(R.drawable.maneuver_icon_24));
                break;
            case 25:
                manuverIcon.setBackground(activity.getDrawable(R.drawable.maneuver_icon_25));
                break;
            case 26:
                manuverIcon.setBackground(activity.getDrawable(R.drawable.maneuver_icon_26));
                break;
            case 27:
                manuverIcon.setBackground(activity.getDrawable(R.drawable.maneuver_icon_27));
                break;
            case 28:
                manuverIcon.setBackground(activity.getDrawable(R.drawable.maneuver_icon_28));
                break;
            case 29:
                manuverIcon.setBackground(activity.getDrawable(R.drawable.maneuver_icon_29));
                break;
            case 30:
                manuverIcon.setBackground(activity.getDrawable(R.drawable.maneuver_icon_30));
                break;
            case 31:
                manuverIcon.setBackground(activity.getDrawable(R.drawable.maneuver_icon_31));
                break;
            case 32:
                manuverIcon.setBackground(activity.getDrawable(R.drawable.maneuver_icon_32));
                break;
            case 33:
                manuverIcon.setBackground(activity.getDrawable(R.drawable.maneuver_icon_33));
                break;
            case 34:
                manuverIcon.setBackground(activity.getDrawable(R.drawable.maneuver_icon_34));
                break;
            case 35:
                manuverIcon.setBackground(activity.getDrawable(R.drawable.maneuver_icon_35));
                break;
            case 36:
                manuverIcon.setBackground(activity.getDrawable(R.drawable.maneuver_icon_36));
                break;
            case 37:
                manuverIcon.setBackground(activity.getDrawable(R.drawable.maneuver_icon_37));
                break;
            case 38:
                manuverIcon.setBackground(activity.getDrawable(R.drawable.maneuver_icon_38));
                break;
            case 39:
                manuverIcon.setBackground(activity.getDrawable(R.drawable.maneuver_icon_39));
                break;
            case 40:
                manuverIcon.setBackground(activity.getDrawable(R.drawable.maneuver_icon_40));
                break;
            case 41:
                manuverIcon.setBackground(activity.getDrawable(R.drawable.maneuver_icon_41));
                break;
            case 42:
                manuverIcon.setBackground(activity.getDrawable(R.drawable.maneuver_icon_42));
                break;
            case 43:
                manuverIcon.setBackground(activity.getDrawable(R.drawable.maneuver_icon_43));
                break;
            case 44:
                manuverIcon.setBackground(activity.getDrawable(R.drawable.maneuver_icon_44));
                break;
            case 45:
                manuverIcon.setBackground(activity.getDrawable(R.drawable.maneuver_icon_45));
                break;
            case 46:
                manuverIcon.setBackground(activity.getDrawable(R.drawable.maneuver_icon_46));
                break;
            case 47:
                manuverIcon.setBackground(activity.getDrawable(R.drawable.maneuver_icon_47));
                break;
            case 48:
                manuverIcon.setBackground(activity.getDrawable(R.drawable.maneuver_icon_48));
                break;
        }

    }


    protected String getTimeEstimated(int duration) {
        double hours = duration / 3600;
        double minutes = (duration % 3600) / 60;
        double seconds = duration % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private static String timeConversion(int totalSeconds) {

        final int MINUTES_IN_AN_HOUR = 60;
        final int SECONDS_IN_A_MINUTE = 60;

        int seconds = totalSeconds % SECONDS_IN_A_MINUTE;
        int totalMinutes = totalSeconds / SECONDS_IN_A_MINUTE;
        int minutes = totalMinutes % MINUTES_IN_AN_HOUR;
        int hours = totalMinutes / MINUTES_IN_AN_HOUR;

        return hours + " hours " + minutes + " minutes " + seconds + " seconds";
    }


    private void stopNavigation() {
        if (mapMarkers != null) {
            map.removeMapObjects(mapMarkers);
        }

        if (mapRoute != null)
            map.removeMapObject(mapRoute);

        tts.speak("Navigation Completed", TextToSpeech.QUEUE_FLUSH, null);
        m_navigationManager.stop();
        stop.setVisibility(View.GONE);
        start.setVisibility(View.GONE);
        createRoute.setVisibility(View.GONE);
        schemeSwitch.setVisibility(View.VISIBLE);
        waypoints.clear();
        stopsAdapter.notifyDataSetChanged();
        navigationBar.setVisibility(View.GONE);

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.BELOW, R.id.relativeLayout2);
        stopsView.setLayoutParams(layoutParams);
        toolbar.setVisibility(View.VISIBLE);

        restoreAppBar();
        getCurrentPosition();
    }

    private void initNaviControlButton() {
        m_naviControlButton = (Button) activity.findViewById(R.id.startNavigation);
        m_naviControlButton.setText(R.string.start_navi);
        m_naviControlButton.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {
                /*
                 * To start a turn-by-turn navigation, a concrete route object is required.We use
                 * the same steps from Routing sample app to create a route from 4350 Still Creek Dr
                 * to Langley BC without going on HWY.
                 *
                 * The route calculation requires local map data.Unless there is pre-downloaded map
                 * data on device by utilizing MapLoader APIs,it's not recommended to trigger the
                 * route calculation immediately after the MapEngine is initialized.The
                 * INSUFFICIENT_MAP_DATA error code may be returned by CoreRouter in this case.
                 *
                 */

                if (m_naviControlButton.getText().toString().contains("Start")) {
                    startGuidance(m_route);
                } else {
                    m_navigationManager.stop();
                    /*
                     * Restore the map orientation to show entire route on screen
                     */
                    map.zoomTo(m_geoBoundingBox, Map.Animation.NONE, 0f);
                    m_naviControlButton.setText(R.string.start_navi);
                    m_route = null;
                }
            }
        });
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

        m_navigationManager.startNavigation(route); // navigate realtime
        //m_navigationManager.simulate(route, 30); // simulate with 30km speed

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


    private NavigationManager.PositionListener m_positionListener = new NavigationManager.PositionListener() {
        @Override
        public void onPositionUpdated(GeoPosition geoPosition) {
            /* Current position information can be retrieved in this callback */
            // the position we get in this callback can be used
            // to reposition the map and change orientation.
            geoPosition.getCoordinate();
            geoPosition.getHeading();
            geoPosition.getSpeed();

            // also remaining time and distance can be
            // fetched from navigation manager
          /*  m_navigationManager.getTta(Route.TrafficPenaltyMode.OPTIMAL, true);
            m_navigationManager.getDestinationDistance();*/
            try {
                int distanceCovered = totalDistanceVal - (int) m_navigationManager.getDestinationDistance();
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
            Maneuver maneuver = m_navigationManager.getNextManeuver();
            maneuver.getIcon().value();
            maneuver.getDistanceToNextManeuver();
            maneuver.getAction().name();
            if (maneuver.getIcon() != null)
                updateNavigationBar(maneuver.getIcon().value(), maneuver.getDistanceToNextManeuver());
            else
                updateNavigationBar(-1, maneuver.getDistanceToNextManeuver());
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
            stopNavigation();
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

    public void onDestroy() {
        /* Stop the navigation when app is destroyed */
        if (m_navigationManager != null) {
            m_navigationManager.stop();
        }
    }


    private void getCurrentLocation(final GeoCoordinate coordinate) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //toolbarLayout.setVisibility(View.VISIBLE);
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
                getCurrentLocation(coordinate);

            }
        }

        @Override
        public void onPositionFixChanged(PositioningManager.LocationMethod locationMethod, PositioningManager.LocationStatus locationStatus) {

        }
    };

    private void hideAppBar() {
        // mToolbar.setVisibility(View.GONE);
      /*  ViewGroup.LayoutParams params = appBar.getLayoutParams();
        params.height = 0;
        appBar.setLayoutParams(params);*/
    }

    private void restoreAppBar() {
        // mToolbar.setVisibility(View.VISIBLE);

       /* ViewGroup.LayoutParams params = appBar.getLayoutParams();
        params.height = 80;
        appBar.setLayoutParams(params);*/
    }
}
