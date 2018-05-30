/*
 * Copyright © 2017 Benjamin Hardesty. All Rights Reserved.
*/

package com.parking.app;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

public class PendingRequest implements Parcelable {

    private int requestID;
    private int spotID;
    private String spotAddress;
    private int spotOwnerID;
    private int userID;
    private String start;
    private String end;
    private String reservationType;
    private String access;
    private String hours;
    private double totalPrice;
    private String timeStamp;
    private String spotOwnerDetails;
    private String renterDetails;

    PendingRequest(JSONObject request) {
        try {
            this.requestID = request.getInt("requestID");
            this.spotID = request.getInt("spotID");
            this.spotAddress = request.getString("spotAddress");
            //this.spotOwnerID = request.getInt("spotOwnerID");
            //this.userID = request.getInt("userID");
            this.start = request.getString("start");
            this.end = request.getString("end");
            this.reservationType = request.getString("reservationType");
            this.access = request.getString("access");
            this.hours = request.getString("hours");
            this.totalPrice = request.getDouble("totalPrice");
            //this.timeStamp = request.getString("timeStamp");
            this.spotOwnerDetails = request.getString("spotOwnerDetails");
            this.renterDetails = request.getString("renterDetails");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public int getRequestID() {
        return requestID;
    }

    public int getSpotID() {
        return spotID;
    }

    public String getSpotAddress() {
        return spotAddress;
    }

    public int getSpotOwnerID() {
        return spotOwnerID;
    }

    public int getUserID() {
        return userID;
    }

    public String getStart() {
        return start;
    }

    public String getEnd() {
        return end;
    }

    public String getReservationType() {
        return reservationType;
    }

    public String getAccess() {
        return access;
    }

    public String getHours() {
        return hours;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public String getSpotOwnerDetails() {
        return spotOwnerDetails;
    }

    public String getRenterDetails() {
        return renterDetails;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel pc, int flags) {
        pc.writeInt(requestID);
        pc.writeInt(spotID);
        pc.writeString(spotAddress);
        pc.writeInt(spotOwnerID);
        pc.writeInt(userID);
        pc.writeString(start);
        pc.writeString(end);
        pc.writeString(reservationType);
        pc.writeString(access);
        pc.writeString(hours);
        pc.writeDouble(totalPrice);
        pc.writeString(timeStamp);
        pc.writeString(spotOwnerDetails);
        pc.writeString(renterDetails);
    }

    public static final Parcelable.Creator<PendingRequest> CREATOR = new Parcelable.Creator<PendingRequest>() {

        @Override
        public PendingRequest createFromParcel(Parcel pc) {
            return new PendingRequest(pc);
        }

        @Override
        public PendingRequest[] newArray(int size) {
            return new PendingRequest[size];
        }
    };

    public PendingRequest(Parcel pc) {

        requestID = pc.readInt();
        spotID = pc.readInt();
        spotAddress = pc.readString();
        spotOwnerID = pc.readInt();
        userID = pc.readInt();
        start = pc.readString();
        end = pc.readString();
        reservationType = pc.readString();
        access = pc.readString();
        hours = pc.readString();
        totalPrice = pc.readDouble();
        timeStamp = pc.readString();
        spotOwnerDetails = pc.readString();
        renterDetails = pc.readString();
    }
}
