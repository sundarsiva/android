package com.sundarsiva.example.dealer.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.sundarsiva.example.dealer.model.Card;
import com.sundarsiva.example.dealer.model.Deck;
import com.sundarsiva.example.dealer.model.IDealer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DealerService extends Service{
    private static final String TAG = DealerService.class.getSimpleName();
    private static Map<Integer, Deck> decks = new HashMap<Integer, Deck>();
    private static int deckId;

    public DealerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new IDealer.Stub() {
            @Override
            public int createDeck() {
                Log.d(TAG, ">createDeck");
                Deck d = new Deck();
                deckId++;
                decks.put(deckId, d);
                return deckId;
            }

            @Override
            public void shuffleCards(int deckId) {
                Log.d(TAG, ">shuffleCards");
                Deck deck = decks.get(deckId);
                if(deck == null) {
                    return;
                }
                deck.shuffle();
            }

            @Override
            public List<Card> dealCards(int deckId, int count) {
                Log.d(TAG, ">dealCards: count: "+count);
                Deck deck = decks.get(deckId);
                List<Card> dealtCards = new ArrayList<Card>();
                if(deck == null) {
                    //return empty list of cards if the deckId does not exist
                    //empty list makes sure that even if the caller doesn't do a null check, it will not crash the app
                    return dealtCards;
                }
                List<Card> cards = deck.getCards();
                //if count is more than available cards, limit to the size of cards.
                count = cards.size() < count ? cards.size() : count;
                for(int i = 0; i < count; i++) {
                    dealtCards.add(cards.get(i));
                }
                //after dealing the cards delete it from the original deck so that
                // the cards are not duplicated in subsequent deals
                for(int i = 0; i < count; i++) {
                    cards.remove(i);
                }
                return dealtCards;
            }

            @Override
            public void returnCards(List<Card> cards, int deckId) {
                Log.d(TAG, ">returnCards");
                Deck deck = decks.get(deckId);
                if(deck == null) {
                    return;
                }
                //put all the cards back in deck
                deck.getCards().addAll(cards);
            }

            @Override
            public void destroyDeck(int deckId) {
                Log.d(TAG, ">destroyDeck");
                decks.remove(deckId);
            }
        };
    }
}
