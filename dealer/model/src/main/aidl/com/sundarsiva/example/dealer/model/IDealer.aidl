package com.sundarsiva.example.dealer.model;

import java.util.List;
import com.sundarsiva.example.dealer.model.Card;

interface IDealer {
  int createDeck();
  void shuffleCards(int deckId);
  List<Card> dealCards(int deckId, int count);
  void returnCards(in List<Card> cards, int deckId);
  void destroyDeck(int deckId);
}