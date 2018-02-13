package com.fourkites.trucknavigator.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fourkites.trucknavigator.NavigationView;
import com.fourkites.trucknavigator.R;
import com.fourkites.trucknavigator.pojos.Result;
import com.fourkites.trucknavigator.pojos.Stop;
import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.routing.RouteWaypoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Avinash on 29/12/17.
 */

public class AutoSuggestAdapter extends RecyclerView.Adapter {

    private final Context context;
    private final int layoutResourceId;
    private final List<Result> data;
    private NavigationView navigationView;
    private Stop stop;
    private int stopPosition;


    public AutoSuggestAdapter(NavigationView navigationView, Context context, int layoutResourceId, List<Result> data, Stop stop, int stopPosition) {
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
        this.navigationView = navigationView;
        this.stop = stop;
        this.stopPosition = stopPosition;
    }

    public static class SuggestionViewHolder extends RecyclerView.ViewHolder {

        final TextView address;
        View view;
        ImageView icon;

        public SuggestionViewHolder(View view) {
            super(view);
            this.view = view;

            address = (TextView) view.findViewById(R.id.address);
            icon = (ImageView) view.findViewById(R.id.icon);
        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rootView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.suggesstion_item, parent, false);
        return new AutoSuggestAdapter.SuggestionViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {

        final Result suggestion = data.get(holder.getAdapterPosition());

        final AutoSuggestAdapter.SuggestionViewHolder suggestionViewHolder = (AutoSuggestAdapter.SuggestionViewHolder) holder;
        String addr = suggestion.getHighlightedTitle() + "\n " + suggestion.getHighlightedVicinity();
        if (addr != null)
            suggestionViewHolder.address.setText(Html.fromHtml(addr));

        if (suggestion.isCurrentLocation()) {
            suggestionViewHolder.icon.setVisibility(View.VISIBLE);
            suggestionViewHolder.address.setTextColor(context.getResources().getColor(R.color.carrier_link_blue));
        } else {
            suggestionViewHolder.icon.setVisibility(View.INVISIBLE);
        }

        ((SuggestionViewHolder) holder).view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isDuplicate = false;
                String addr = "";
                Result suggestion = data.get(holder.getAdapterPosition());
                if (suggestion.isCurrentLocation())
                    addr = suggestion.getHighlightedVicinity();
                else
                    addr = suggestion.getHighlightedTitle() + " " + suggestion.getHighlightedVicinity();

                ArrayList<Stop> waypoints = navigationView.getWaypoints();
                if (waypoints != null) {
                    for (int i = 0; i < waypoints.size(); i++) /*(Stop sp : waypoints)*/ {
                        Stop sp = waypoints.get(i);
                        if (sp != null && sp.getGeoCoordinate() != null) {
                            if ((sp.getGeoCoordinate().getLatitude() == suggestion.getPosition().get(0)) && (sp.getGeoCoordinate().getLongitude() == suggestion.getPosition().get(1))) {
                                if (waypoints.size() > 2 && i == 0)
                                    isDuplicate = true;
                                break;
                            }
                        }
                    }
                }

                if (!isDuplicate) {
                    stop.setAddress(addr);
                    GeoCoordinate geoCoordinate = new GeoCoordinate(suggestion.getPosition().get(0), suggestion.getPosition().get(1));
                    stop.setGeoCoordinate(geoCoordinate);
                    stop.setRouteWaypoint(new RouteWaypoint(geoCoordinate));
                    navigationView.addOrUpdateWaypoint(stop, stopPosition);
                } else {
                    navigationView.discardDuplicateStop();
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setStop(Stop stop) {
        this.stop = stop;
    }

    public void setStopPosition(int stopPosition) {
        this.stopPosition = stopPosition;
    }
}
