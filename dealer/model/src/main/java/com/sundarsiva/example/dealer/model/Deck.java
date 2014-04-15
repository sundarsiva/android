package com.sundarsiva.example.dealer.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck implements Parcelable {
    private final List<Card> cards = new ArrayList<Card>();

    public Deck() {
        for (Rank rank: Rank.values()) {
            for (Suit suit: Suit.values()) {
                cards.add(new Card(rank, suit));
            }
        }
    }

    public Deck(Parcel in) {
        in.readList(cards, Deck.class.getClassLoader());
    }

    @Override
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(cards);
    }

    public static final Parcelable.Creator<Card> CREATOR = new Parcelable.Creator<Card>() {
        public Card createFromParcel(Parcel in) {
            return new Card(in);
        }

        public Card[] newArray(int size) {
            return new Card[size];
        }
    };

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public List<Card> getCards(){
        return cards;
    }



}
