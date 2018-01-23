package com.fourkites.trucknavigator.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fourkites.trucknavigator.NavigationView;
import com.fourkites.trucknavigator.R;
import com.here.android.mpa.routing.Route;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by Avinash on 29/12/17.
 */

public class RoutesAdapter extends RecyclerView.Adapter {

    private final Context context;
    private final List<Route> data;
    private NavigationView navigationView;
    private final double MILES_CONVERSION = 1609.344;
    private DecimalFormat df;


    public RoutesAdapter(NavigationView navigationView, Context context, List<Route> data) {
        this.context = context;
        this.data = data;
        this.navigationView = navigationView;
        df = new DecimalFormat("0.0");
    }

    public static class RouteViewHolder extends RecyclerView.ViewHolder {

        final TextView eta;
        final TextView distance;
        View view;
        ImageView icon;

        public RouteViewHolder(View view) {
            super(view);
            this.view = view;
            distance = (TextView) view.findViewById(R.id.distance);
            eta = (TextView) view.findViewById(R.id.eta);
            icon = (ImageView) view.findViewById(R.id.icon);
        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rootView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.route_item, parent, false);
        return new RouteViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {

        final Route route = data.get(holder.getAdapterPosition());
        final RouteViewHolder routeHolder = (RouteViewHolder) holder;

        if (route != null) {
            String distance;
            if (route.getLength() < 1000) {
                distance = "In " + route.getLength() + " meters";
            } else {
                distance = "In " + df.format(route.getLength() / MILES_CONVERSION) + " miles";
            }
            routeHolder.distance.setText(distance);

            String eta = navigationView.timeConversion(route.getTta(Route.TrafficPenaltyMode.OPTIMAL, Route.WHOLE_ROUTE).getDuration());
            if (eta != null) {
                routeHolder.eta.setText(eta);
            }
        }

        routeHolder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigationView.selectRoute(route);
            }
        });

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

}
