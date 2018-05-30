/*
 * Copyright © 2017 Benjamin Hardesty. All Rights Reserved.
*/

package com.parking.app;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

public class ParkingSpot extends Spot implements Parcelable {

    protected int active;
    protected String spotType;
    protected String accessType;
    protected int spotRatings;
    protected String spotRating;
    protected int availableMonthly;
    protected int monthlyAvailabilitiesSelected;
    protected int validMonthlyAvailabilities;
    protected double monthlyPrice;
    protected String monthlyHours;
    protected int availableDaily;
    protected int dailyAvailabilitiesSelected;
    protected int validDailyAvailabilities;
    protected double dailyPrice;
    protected String dailyHours;
    protected int availableHourly;
    protected int hourlyAvailabilitiesSelected;
    protected int validHourlyAvailabilities;
    protected double hourlyPrice;
    protected String hourlyHours;
    protected int temp;
    protected String tempTimeStamp;
    protected String reservedDates;
    protected String spotNumber;

    ParkingSpot(JSONObject spot) {
        super(spot);

        try {
            this.active = spot.getInt("active");
            this.spotType = spot.getString("spotType");
            this.accessType = spot.getString("accessType");
            this.availableMonthly = spot.getInt("availableMonthly");
            this.monthlyPrice = spot.getDouble("monthlyPrice");
            this.monthlyHours = spot.getString("monthlyHours");
            this.availableDaily = spot.getInt("availableDaily");
            this.dailyPrice = spot.getDouble("dailyPrice");
            this.dailyHours = spot.getString("dailyHours");
            this.availableHourly = spot.getInt("availableHourly");
            this.hourlyPrice = spot.getDouble("hourlyPrice");
            this.hourlyHours = spot.getString("hourlyHours");
            this.spotNumber = spot.getString("spotNumber");

            if (this.availableMonthly == 1) {
                this.monthlyAvailabilitiesSelected = 1;
                this.validMonthlyAvailabilities = 1;
            } else {
                this.monthlyAvailabilitiesSelected = 0;
                this.validMonthlyAvailabilities = 0;
            }
            if (this.availableDaily == 1) {
                this.dailyAvailabilitiesSelected = 1;
                this.validDailyAvailabilities = 1;
            } else {
                this.dailyAvailabilitiesSelected = 0;
                this.validDailyAvailabilities = 0;
            }
            if (this.availableHourly == 1) {
                this.hourlyAvailabilitiesSelected = 1;
                this.validHourlyAvailabilities = 1;
            } else {
                this.hourlyAvailabilitiesSelected = 0;
                this.validHourlyAvailabilities = 0;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setSpotType(String spotType) {
        this.spotType = spotType;
    }

    public void setActive(int active) {
        this.active = active;
    }

    public void setAccessType(String accessType) {
        this.accessType = accessType;
    }

    public void setAvailableMonthly(Integer availableMonthly) {
        this.availableMonthly = availableMonthly;
    }

    public void setMonthlyAvailabilitiesSelected(Integer monthlyAvailabilitiesSelected) {
        this.monthlyAvailabilitiesSelected = monthlyAvailabilitiesSelected;
    }

    public void setValidMonthlyAvailabilities(Integer validMonthlyAvailabilities) {
        this.validMonthlyAvailabilities = validMonthlyAvailabilities;
    }

    public void setMonthlyPrice(Double monthlyPrice) {
        this.monthlyPrice = monthlyPrice;
    }

    public void setMonthlyHours(String monthlyHours) {
        this.monthlyHours = monthlyHours;
    }

    public void setAvailableDaily(Integer availableDaily) {
        this.availableDaily = availableDaily;
    }

    public void setDailyAvailabilitiesSelected(Integer dailyAvailabilitiesSelected) {
        this.dailyAvailabilitiesSelected = dailyAvailabilitiesSelected;
    }

    public void setValidDailyAvailabilities(Integer validDailyAvailabilities) {
        this.validDailyAvailabilities = validDailyAvailabilities;
    }

    public void setDailyPrice(Double dailyPrice) {
        this.dailyPrice = dailyPrice;
    }

    public void setDailyHours(String dailyHours) {
        this.dailyHours = dailyHours;
    }

    public void setAvailableHourly(Integer availableHourly) {
        this.availableHourly = availableHourly;
    }

    public void setHourlyAvailabilitiesSelected(Integer hourlyAvailabilitiesSelected) {
        this.hourlyAvailabilitiesSelected = hourlyAvailabilitiesSelected;
    }

    public void setValidHourlyAvailabilities(Integer validHourlyAvailabilities) {
        this.validHourlyAvailabilities = validHourlyAvailabilities;
    }

    public void setHourlyPrice(Double hourlyPrice) {
        this.hourlyPrice = hourlyPrice;
    }

    public void setHourlyHours(String hourlyHours) {
        this.hourlyHours = hourlyHours;
    }

    public int getActive() {
        return active;
    }

    public String getSpotType() {
        return this.spotType;
    }

    public String getAccessType() {
        return accessType;
    }

    public String getSpotRating() {
        return spotRating;
    }

    public Integer getAvailableMonthly() {
        return availableMonthly;
    }

    public Integer getMonthlyAvailabilitiesSelected() {
        return monthlyAvailabilitiesSelected;
    }

    public Integer getValidMonthlyAvailabilities() {
        return validMonthlyAvailabilities;
    }

    public Double getMonthlyPrice() {
        return monthlyPrice;
    }

    public String getMonthlyHours() {
        return monthlyHours;
    }

    public Integer getAvailableDaily() {
        return availableDaily;
    }

    public Integer getDailyAvailabilitiesSelected() { return dailyAvailabilitiesSelected; }

    public Integer getValidDailyAvailabilities() {
        return validDailyAvailabilities;
    }

    public Double getDailyPrice() {
        return dailyPrice;
    }

    public String getDailyHours() {
        return dailyHours;
    }

    public Integer getAvailableHourly() {
        return availableHourly;
    }

    public Integer getHourlyAvailabilitiesSelected() { return hourlyAvailabilitiesSelected; }

    public Integer getValidHourlyAvailabilities() {
        return validHourlyAvailabilities;
    }

    public Double getHourlyPrice() {
        return hourlyPrice;
    }

    public String getHourlyHours() {
        return hourlyHours;
    }

    public String getSpotNumber() {
        return spotNumber;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel pc, int flags) {

        pc.writeInt(active);
        pc.writeString(spotType);
        pc.writeString(accessType);
        pc.writeInt(spotRatings);
        pc.writeString(spotRating);
        pc.writeInt(availableMonthly);
        pc.writeInt(monthlyAvailabilitiesSelected);
        pc.writeInt(validMonthlyAvailabilities);
        pc.writeDouble(monthlyPrice);
        pc.writeString(monthlyHours);
        pc.writeInt(availableDaily);
        pc.writeInt(dailyAvailabilitiesSelected);
        pc.writeInt(validDailyAvailabilities);
        pc.writeDouble(dailyPrice);
        pc.writeString(dailyHours);
        pc.writeInt(availableHourly);
        pc.writeInt(hourlyAvailabilitiesSelected);
        pc.writeInt(validHourlyAvailabilities);
        pc.writeDouble(hourlyPrice);
        pc.writeString(hourlyHours);
        pc.writeInt(temp);
        pc.writeString(tempTimeStamp);
        pc.writeString(reservedDates);
        pc.writeString(spotNumber);
    }

    public static final Parcelable.Creator<ParkingSpot> CREATOR = new Parcelable.Creator<ParkingSpot>() {

        @Override
        public ParkingSpot createFromParcel(Parcel pc) {
            return new ParkingSpot(pc);
        }

        @Override
        public ParkingSpot[] newArray(int size) {
            return new ParkingSpot[size];
        }
    };

    public ParkingSpot(Parcel pc) {
        super(pc);

        active = pc.readInt();
        spotType = pc.readString();
        accessType = pc.readString();
        spotRatings = pc.readInt();
        spotRating = pc.readString();
        availableMonthly = pc.readInt();
        monthlyAvailabilitiesSelected = pc.readInt();
        validMonthlyAvailabilities = pc.readInt();
        monthlyPrice = pc.readDouble();
        monthlyHours = pc.readString();
        availableDaily = pc.readInt();
        dailyAvailabilitiesSelected = pc.readInt();
        validDailyAvailabilities = pc.readInt();
        dailyPrice = pc.readDouble();
        dailyHours = pc.readString();
        availableHourly = pc.readInt();
        hourlyAvailabilitiesSelected = pc.readInt();
        validHourlyAvailabilities = pc.readInt();
        hourlyPrice = pc.readDouble();
        hourlyHours = pc.readString();
        temp = pc.readInt();
        tempTimeStamp = pc.readString();
        reservedDates = pc.readString();
        spotNumber = pc.readString();
    }
}