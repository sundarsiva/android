package com.sundarsiva.example.dealer.model;

import android.os.Parcel;
import android.os.Parcelable;

public enum Rank implements Parcelable {
  TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING, ACE;

  @Override
  public int describeContents(){
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(name());
  }

  public static final Parcelable.Creator<Rank> CREATOR = new Parcelable.Creator<Rank>() {
    public Rank createFromParcel(Parcel in) {
      return Rank.valueOf(in.readString());
    }

    public Rank[] newArray(int size) {
      return new Rank[size];
    }
  };
}
