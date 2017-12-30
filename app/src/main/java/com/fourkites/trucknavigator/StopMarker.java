package com.fourkites.trucknavigator;

import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapMarker;
import com.here.android.mpa.mapping.MapMarker;

/**
 * Created by Avinash on 27/12/17.
 */

public class StopMarker {

    private MapMarker mapMarker;
    private MapMarker.OnDragListener onDragListener;

    public MapMarker getMapMarker() {
        return mapMarker;
    }

    public void setMapMarker(MapMarker mapMarker) {
        this.mapMarker = mapMarker;
    }

    public MapMarker.OnDragListener getOnDragListener() {
        return onDragListener;
    }

    public void setOnDragListener(MapMarker.OnDragListener onDragListener) {
        this.onDragListener = onDragListener;
    }
}
