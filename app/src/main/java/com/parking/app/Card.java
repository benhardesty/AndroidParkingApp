/*
 * Copyright © 2017 Benjamin Hardesty. All Rights Reserved.
*/

package com.parking.app;

import android.os.Parcel;
import android.os.Parcelable;
import org.json.JSONException;
import org.json.JSONObject;

// Class to hold a user's credit card information.
public class Card implements Parcelable {
    private String cardIsDefault;
    private String last2;
    private String expiration;
    private String cardString;
    private String token;

    Card(JSONObject card) {
        try {
            this.last2 = card.getString("last2");
            this.expiration = card.getString("expirationMonth") + "/" + card.getString("expirationYear");
            this.cardIsDefault = card.getString("cardIsDefault");
            this.token = card.getString("token");

            this.cardString = "**" + last2 + " Exp: " + expiration;
            if (cardIsDefault.equals("true") || cardIsDefault.equals(true)) {
                cardString = cardString + " - default";
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getCardString() {
        return cardString;
    }

    public String getToken() {
        return token;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel pc, int flags) {

        pc.writeString(cardIsDefault);
        pc.writeString(last2);
        pc.writeString(expiration);
        pc.writeString(cardString);
        pc.writeString(token);
    }

    public static final Parcelable.Creator<Card> CREATOR = new Parcelable.Creator<Card>() {

        @Override
        public Card createFromParcel(Parcel pc) {
            return new Card(pc);
        }

        @Override
        public Card[] newArray(int size) {
            return new Card[size];
        }
    };

    public Card(Parcel pc) {
        cardIsDefault = pc.readString();
        last2 = pc.readString();
        expiration = pc.readString();
        cardString = pc.readString();
        token = pc.readString();
    }
}
