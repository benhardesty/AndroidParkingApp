/*
 * Copyright © 2017 Benjamin Hardesty. All Rights Reserved.
*/

package com.parking.app;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import org.json.JSONObject;

public class SpotMarker extends ParkingSpot {

    private Integer ID;
    private LatLng latLng;
    private Marker marker;

    public SpotMarker(JSONObject spot){
        super(spot);

        this.ID = this.getSpotID();
        this.latLng = new LatLng(this.getSpotLatitude(), this.getSpotLongitude());
    }

    public Integer getID() {
        return getSpotID();
    }

    public LatLng getLatLng() {
        return this.latLng;
    }

    public void setMarker(Marker newMarker) {
        this.marker = newMarker;
    }

    public void setMarkerTag(SpotMarker spotMarker) {
        this.marker.setTag(spotMarker);
    }

    public void removeMarker(SpotMarker spotMarker) {
        spotMarker.marker.remove();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SpotMarker that = (SpotMarker) o;

        return this.ID.equals(that.ID);
    }

    @Override
    public int describeContents() {
        return 0;
    }

}
