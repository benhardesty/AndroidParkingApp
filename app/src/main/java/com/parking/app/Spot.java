package com.parking.app;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

public class Spot implements Parcelable {

    protected int spotID;
    protected String photos;
    protected String spotAddress;
    protected double spotLatitude;
    protected double spotLongitude;

    Spot(JSONObject spot) {
        try {
            this.spotID = spot.getInt("spotID");
            this.photos = spot.getString("photos");
            this.spotAddress = spot.getString("spotAddress");
            this.spotLatitude = spot.getDouble("spotLatitude");
            this.spotLongitude = spot.getDouble("spotLongitude");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Integer getSpotID() {
        return spotID;
    }

    public String getPhotos() {
        return photos;
    }

    public String getSpotAddress() {
        return spotAddress;
    }

    public Double getSpotLatitude() {
        return spotLatitude;
    }

    public Double getSpotLongitude() {
        return spotLongitude;
    }

    public void setPhotos(String photos) {
        this.photos = photos;
    }

    public void setSpotAddress(String spotAddress) {
        this.spotAddress = spotAddress;
    }

    public void setSpotLatitude(double spotLatitude) {
        this.spotLatitude = spotLatitude;
    }

    public void setSpotLongitude(double spotLongitude) {
        this.spotLongitude = spotLongitude;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel pc, int flags) {
        pc.writeInt(spotID);
        pc.writeString(photos);
        pc.writeString(spotAddress);
        pc.writeDouble(spotLatitude);
        pc.writeDouble(spotLongitude);
    }

    public static final Parcelable.Creator<Spot> CREATOR = new Parcelable.Creator<Spot>() {

        @Override
        public Spot createFromParcel(Parcel pc) {
            return new Spot(pc);
        }

        @Override
        public Spot[] newArray(int size) {
            return new Spot[size];
        }
    };

    public Spot(Parcel pc) {
        spotID = pc.readInt();
        photos = pc.readString();
        spotAddress = pc.readString();
        spotLatitude = pc.readDouble();
        spotLongitude = pc.readDouble();
    }
}
