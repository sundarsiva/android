package com.sundarsiva.example.dealer.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sundarsiva.example.dealer.R;
import com.sundarsiva.example.dealer.model.Card;

import java.util.ArrayList;
import java.util.List;

public class CardsFragment extends Fragment {

    public static final int OPERATION_NEW_DECK = 1, OPERATION_SHUFFLE_DECK = 2, OPERATION_DEAL_CARDS = 3,
            OPERATION_RETURN_CARDS = 4, OPERATION_DESTROY_DECK = 5;

    private static final String ARG_OPERATION_NUMBER = "operation_number";
    private static final String ARG_DECK_ID = "deck_id";
    private static final String ARG_DECK_CARDS = "deck_cards";

    /**
     * Returns a new instance of this fragment for the given operation
     */
    public static CardsFragment newInstance(int deckId, int operationNumber, ArrayList<Card> cards) {
        CardsFragment fragment = new CardsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_DECK_ID, deckId);
        args.putInt(ARG_OPERATION_NUMBER, operationNumber);
        args.putParcelableArrayList(ARG_DECK_CARDS, cards);
        fragment.setArguments(args);
        return fragment;
    }

    public CardsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_cards_game, container, false);
        TextView tvOperationMsg = (TextView) rootView.findViewById(R.id.fragment_cards_operation_msg);

        int deckId = getArguments().getInt(ARG_DECK_ID);
        int operationNumber = getArguments().getInt(ARG_OPERATION_NUMBER);
        List<Card> cards = getArguments().getParcelableArrayList(ARG_DECK_CARDS);

        String display = "";
        switch (operationNumber) {
            case OPERATION_NEW_DECK:
                display = getString(R.string.fragment_cards_created_deck, deckId);
                break;
            case OPERATION_SHUFFLE_DECK:
                display = getString(R.string.fragment_cards_shuffle_deck, deckId);
                break;
            case OPERATION_DEAL_CARDS:
                display = getString(R.string.fragment_cards_deal_cards, deckId);
                for(Card card : cards) {
                    display = display + "<br/>" + card.rank + " : " + card.suit;
                }
                break;
            case OPERATION_RETURN_CARDS:
                display = getString(R.string.fragment_cards_return_cards, deckId);
                break;
            case OPERATION_DESTROY_DECK:
                display = getString(R.string.fragment_cards_destroy_deck, deckId);
                break;
        }

        tvOperationMsg.setText(Html.fromHtml(display));
        return rootView;
    }
}
