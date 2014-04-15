package com.sundarsiva.example.dealer.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Card implements Parcelable {
  public final Rank rank;
  public final Suit suit;

  public Card(Rank rank, Suit suit) {
    this.rank = rank;
    this.suit = suit;
  }

  public Card(Parcel in) {
    this.rank = in.readParcelable(Card.class.getClassLoader());
    this.suit = in.readParcelable(Card.class.getClassLoader());
  }

  @Override
  public int describeContents(){
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeParcelable(rank, 0);
    dest.writeParcelable(suit, 0);
  }

  public static final Parcelable.Creator<Card> CREATOR = new Parcelable.Creator<Card>() {
    public Card createFromParcel(Parcel in) {
      return new Card(in);
    }

    public Card[] newArray(int size) {
      return new Card[size];
    }
  };
}
