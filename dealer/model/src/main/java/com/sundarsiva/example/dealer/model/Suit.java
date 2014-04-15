package com.sundarsiva.example.dealer.model;

import android.os.Parcel;
import android.os.Parcelable;

public enum Suit implements Parcelable {
  HEARTS, DIAMONDS, SPADES, CLUBS;


  @Override
  public int describeContents(){
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(name());
  }

  public static final Parcelable.Creator<Suit> CREATOR = new Parcelable.Creator<Suit>() {
    public Suit createFromParcel(Parcel in) {
      return Suit.valueOf(in.readString());
    }

    public Suit[] newArray(int size) {
      return new Suit[size];
    }
  };
}
