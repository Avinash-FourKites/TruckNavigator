package com.fourkites.trucknavigator.pojos;

import android.os.Parcel;
import android.os.Parcelable;

import com.here.android.mpa.routing.Route;

/**
 * Created by Avinash on 09/01/18.
 */

public class SelectedRoute implements Parcelable {

    private Route m_route;

    public Route getM_route() {
        return m_route;
    }

    public void setM_route(Route m_route) {
        this.m_route = m_route;
    }

    public SelectedRoute() {
    }

    protected SelectedRoute(Parcel in) {
        m_route = (Route) in.readValue(Route.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        try{
            if (m_route != null)
                dest.writeValue(m_route);
        }catch (RuntimeException e){

        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<SelectedRoute> CREATOR = new Parcelable.Creator<SelectedRoute>() {
        @Override
        public SelectedRoute createFromParcel(Parcel in) {
            return new SelectedRoute(in);
        }

        @Override
        public SelectedRoute[] newArray(int size) {
            return new SelectedRoute[size];
        }
    };
}
