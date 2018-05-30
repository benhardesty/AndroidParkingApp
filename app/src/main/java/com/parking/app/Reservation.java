/*
 * Copyright © 2017 Benjamin Hardesty. All Rights Reserved.
*/

package com.parking.app;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

public class Reservation implements Parcelable {

    private int reservationID;
    private int spotID;
    private String spotAddress;
    private int spotOwnerID;
    private String renterName;
    private String renterVehicle;
    private String start;
    private String end;
    private String reservationType;
    private String access;
    private String hours;
    private double totalPrice;
    private String timeStamp;
    private int reservationCancelledByRentor;
    private int reservationCancelledByOwner;
    private String cancelledTimeStamp;
    private String spotOwnerDetails;

    Reservation(JSONObject reservation) {
        try {
            this.reservationID = reservation.getInt("reservationID");
            this.spotID = reservation.getInt("spotID");
            this.spotAddress = reservation.getString("spotAddress");
            this.renterName = reservation.getString("requestorName");
            this.renterVehicle = reservation.getString("userVehicle");
            this.start = reservation.getString("start");
            this.end = reservation.getString("end");
            this.reservationType = reservation.getString("reservationType");
            this.access = reservation.getString("access");
            this.hours = reservation.getString("hours");
            this.totalPrice = reservation.getDouble("totalPrice");
            this.reservationCancelledByRentor = reservation.getInt("reservationCancelledByRentor");
            this.reservationCancelledByOwner = reservation.getInt("reservationCancelledByOwner");
            this.cancelledTimeStamp = reservation.getString("cancelledTimeStamp");
            this.spotOwnerDetails = reservation.getString("spotOwnerDetails");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public int getReservationID() {
        return reservationID;
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

    public String getRenterName() {
        return renterName;
    }

    public String getRenterVehicle() {
        return renterVehicle;
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

    public int getReservationCancelledByRentor() {
        return reservationCancelledByRentor;
    }

    public int getReservationCancelledByOwner() {
        return reservationCancelledByOwner;
    }

    public String getCancelledTimeStamp() {
        return cancelledTimeStamp;
    }

    public String getSpotOwnerDetails() {
        return spotOwnerDetails;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel pc, int flags) {
        pc.writeInt(reservationID);
        pc.writeInt(spotID);
        pc.writeString(spotAddress);
        pc.writeInt(spotOwnerID);
        pc.writeString(renterName);
        pc.writeString(renterVehicle);
        pc.writeString(start);
        pc.writeString(end);
        pc.writeString(reservationType);
        pc.writeString(access);
        pc.writeString(hours);
        pc.writeDouble(totalPrice);
        pc.writeString(timeStamp);
        pc.writeInt(reservationCancelledByRentor);
        pc.writeInt(reservationCancelledByOwner);
        pc.writeString(cancelledTimeStamp);
        pc.writeString(spotOwnerDetails);
    }

    public static final Parcelable.Creator<Reservation> CREATOR = new Parcelable.Creator<Reservation>() {

        @Override
        public Reservation createFromParcel(Parcel pc) {
            return new Reservation(pc);
        }

        @Override
        public Reservation[] newArray(int size) {
            return new Reservation[size];
        }
    };

    public Reservation(Parcel pc) {
        reservationID = pc.readInt();
        spotID = pc.readInt();
        spotAddress = pc.readString();
        spotOwnerID = pc.readInt();
        renterName = pc.readString();
        renterVehicle = pc.readString();
        start = pc.readString();
        end = pc.readString();
        reservationType = pc.readString();
        access = pc.readString();
        hours = pc.readString();
        totalPrice = pc.readDouble();
        timeStamp = pc.readString();
        reservationCancelledByRentor = pc.readInt();
        reservationCancelledByOwner = pc.readInt();
        cancelledTimeStamp = pc.readString();
        spotOwnerDetails = pc.readString();
    }
}
