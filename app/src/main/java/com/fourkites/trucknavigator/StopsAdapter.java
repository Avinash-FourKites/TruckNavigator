package com.fourkites.trucknavigator;

/**
 * Created by Avinash on 19/12/17.
 */
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;

import com.here.android.mpa.routing.RouteWaypoint;
import com.here.android.mpa.search.Address;
import com.here.android.mpa.search.DiscoveryRequest;
import com.here.android.mpa.search.DiscoveryResult;
import com.here.android.mpa.search.DiscoveryResultPage;
import com.here.android.mpa.search.ErrorCode;
import com.here.android.mpa.search.GeocodeRequest;
import com.here.android.mpa.search.Location;
import com.here.android.mpa.search.Place;
import com.here.android.mpa.search.PlaceLink;
import com.here.android.mpa.search.PlaceRequest;
import com.here.android.mpa.search.ResultListener;
import com.here.android.mpa.search.SearchRequest;
import org.jsoup.Jsoup;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Avinash on 04/08/16.
 */


public class StopsAdapter extends RecyclerView.Adapter {

    private final List<Stop> stops;
    private final Context context;
    private final Handler handler = new Handler();
    private Runnable run;
    private ArrayList<Suggestion> suggestions;
    private SuggestionAdapter adapter;
    private int count;
    private Set<Suggestion> addressSet;
    private MapView mapView;

    public StopsAdapter(MapView mapView,Context context, List<Stop> stops) {
        this.mapView = mapView;
        this.context = context;
        this.stops = stops;
        suggestions = new ArrayList<>();
        addressSet = new HashSet<>();
    }

    public static class StopViewHolder extends RecyclerView.ViewHolder {
        View view;
        final TextView address;
        final ImageView remove;
        //final ProgressBar loadingCircle;

        public StopViewHolder(View view) {
            super(view);
            this.view = view;
            address = (TextView) view.findViewById(R.id.address);
            remove = (ImageView) view.findViewById(R.id.remove);
            // loadingCircle = (ProgressBar) view.findViewById(R.id.loadingCircle);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rootView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.stop_item, parent, false);
        return new StopViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {

        final Stop stop = stops.get(holder.getAdapterPosition());

        final StopViewHolder stopViewHolder = (StopViewHolder) holder;
        if (stop.getAddress() != null && !stop.getAddress().isEmpty())
            stopViewHolder.address.setText(Jsoup.parse(stop.getAddress()).text());
        else
            stopViewHolder.address.setText("");

        //stopViewHolder.address.setThreshold(3);

       /* stopViewHolder.address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showKeyboard(context, stopViewHolder.address);
            }
        });
*/
        /*topViewHolder.address.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (stopViewHolder.address.isFocused()) {
                    if (!stopViewHolder.address.getText().toString().equals("")) {

                        if (stopViewHolder.address.getText().toString().length() >= 3) {
                            String text = stopViewHolder.address.getText().toString();
                            final String txt = text;
                            handler.removeCallbacks(run);

                            run = new Runnable() {
                                public void run() {
                                    //stopViewHolder.loadingCircle.setVisibility(View.VISIBLE);
                                    suggesstionsRequest(txt.trim(), stopViewHolder);
                                }
                            };
                            handler.postDelayed(run, 0);
                        }
                    } else {
                        stopViewHolder.address.clearFocus();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });*/

       /* stopViewHolder.address.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long rowId) {
                hideKeyboard(context, stopViewHolder.address);
                stop.setAddress(suggestions.get(position).getName());
                stop.setGeoCoordinate(suggestions.get(position).getGeoCoordinate());
                stop.setRouteWaypoint(suggestions.get(position).getRouteWaypoint());
                mapView.addMarkerToMap(stop, false);
                notifyDataSetChanged();
                stopViewHolder.address.clearFocus();
            }
        });*/

      /*  stopViewHolder.address.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (imm != null)
                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                }
            }
        });*/

        stopViewHolder.remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // Log.i("Stops size before", stops.size()+"");
                stops.remove(holder.getAdapterPosition());
                //Log.i("Stops size after", stops.size()+"");
                notifyDataSetChanged();
                mapView.refreshRoute();
            }
        });

        stopViewHolder.address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Stop stop = stops.get(holder.getAdapterPosition());
                mapView.editSuggestion(stop,holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return stops.size();
    }

    /*private void suggesstionsRequest(String query, StopViewHolder stopViewHolder) {
        try {
            addressSet.clear();

            DiscoveryRequest request = new SearchRequest(query);

            //DiscoveryRequest request1 = new DiscoveryRequest();

            // limit number of items in each result page to 10
            request.setCollectionSize(10);

            ErrorCode error = request.execute(new SearchRequestListener(stopViewHolder));
            if (error != ErrorCode.NONE) {
                // Handle request error

            }
        } catch (IllegalArgumentException ex) {
            // Handle invalid create search request parameters

        }
    }

    class SearchRequestListener implements ResultListener<DiscoveryResultPage> {
        private StopViewHolder stopViewHolder;

        SearchRequestListener(StopViewHolder stopViewHolder) {
            this.stopViewHolder = stopViewHolder;
        }

        @Override
        public void onCompleted(final DiscoveryResultPage data, ErrorCode error) {
            if (error == ErrorCode.NONE) {
                // Handle error
                count = 1;

                for (DiscoveryResult item : data.getItems()) {

                    if (item.getResultType() == DiscoveryResult.ResultType.PLACE) {
                        PlaceLink placeLink = (PlaceLink) item;
                        PlaceRequest placeRequest = placeLink.getDetailsRequest();

                        placeRequest.execute(new ResultListener<Place>() {
                            @Override
                            public void onCompleted(Place place, ErrorCode errorCode) {

                                addSuggestions(place.getLocation(), data.getItems().size(), stopViewHolder);
                            }
                        });
                    }

                }
            }
        }
    }

    private void  addSuggestions(Location location, int totalCount, StopViewHolder stopViewHolder) {
        Address address = location.getAddress();
      *//*  StringBuilder sb = new StringBuilder();

        if (!address.getHouseNumber().isEmpty())
            sb.append(address.getHouseNumber()).append(",");
        if (!address.getFloorNumber().isEmpty())
            sb.append(address.getFloorNumber()).append(",");
        if (!address.getStreet().isEmpty())
            sb.append(address.getStreet()).append(",");
        if (!address.getDistrict().isEmpty())
            sb.append(address.getDistrict()).append(",");
        if (!address.getCity().isEmpty())
            sb.append(address.getCity()).append(",");
        if (!address.getState().isEmpty())
            sb.append(address.getState()).append(",");
        if (!address.getPostalCode().isEmpty())
            sb.append(address.getPostalCode()).append(",");
        if (!address.getCountryName().isEmpty())
            sb.append(address.getCountryName());
*//*
        suggestions.clear();
        Suggestion sugObj = new Suggestion();
        sugObj.setName(Jsoup.parse(address.toString()).text());
        sugObj.setGeoCoordinate(location.getCoordinate());
        sugObj.setRouteWaypoint(new RouteWaypoint(location.getCoordinate()));
        addressSet.add(sugObj);
        suggestions.addAll(addressSet);

        adapter = new SuggestionAdapter(context, R.layout.suggesstion_item, suggestions);
        stopViewHolder.address.setAdapter(adapter);
        stopViewHolder.address.showDropDown();
        //stopViewHolder.loadingCircle.setVisibility(View.INVISIBLE);

        Log.d("addressSet size", addressSet.size() + "");
    }

    private void triggerGeocodeRequest(final StopViewHolder stopViewHolder, final String query, final Stop stop) {
        *//*
         * Create a GeocodeRequest object with the desired query string, then set the search area by
         * providing a GeoCoordinate and radius before executing the request.
         *//*
        GeocodeRequest geocodeRequest = new GeocodeRequest(query);
        geocodeRequest.execute(new ResultListener<List<Location>>() {
            @Override
            public void onCompleted(List<Location> locations, ErrorCode errorCode) {
                if (errorCode == ErrorCode.NONE) {
                    *//*
                     * From the location object, we retrieve the coordinate and display to the
                     * screen. Please refer to HERE Android SDK doc for other supported APIs.
                     *//*
                    StringBuilder sb = new StringBuilder();
                    for (Location loc : locations) {
                        sb.append(loc.getCoordinate().toString());
                        sb.append("\n");
                    }

                    *//*stop.setAddress(suggestions.get(position).getName());
                    notifyDataSetChanged();
                    stopViewHolder.address.dismissDropDown();
                    stopViewHolder.address.clearFocus();*//*

                }
            }
        });
    }

    public void hideKeyboard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void showKeyboard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.showSoftInputFromInputMethod(view.getWindowToken(), 0);
    }*/


}


