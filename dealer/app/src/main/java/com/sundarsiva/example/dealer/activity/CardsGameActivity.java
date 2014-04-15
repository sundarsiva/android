package com.sundarsiva.example.dealer.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.sundarsiva.example.dealer.R;
import com.sundarsiva.example.dealer.fragment.CardsFragment;
import com.sundarsiva.example.dealer.model.Card;
import com.sundarsiva.example.dealer.model.IDealer;

import java.util.ArrayList;
import java.util.List;

public class CardsGameActivity extends Activity implements ActionBar.OnNavigationListener {

    private static final String TAG = CardsGameActivity.class.getSimpleName();

    /**
     * The serialization (saved instance state) Bundle key representing the
     * current dropdown position.
     */
    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

    private IDealer mService;
    private DealerServiceConnection mServiceConnection;
    private int mDeckId;
    private List<Card> mCards;

    /**
     * This class represents the actual service connection. It casts the bound
     * stub implementation of the service to the AIDL interface.
     */
    class DealerServiceConnection implements ServiceConnection {

        public void onServiceConnected(ComponentName name, IBinder boundService) {
            mService = IDealer.Stub.asInterface(boundService);
            Log.d(TAG, "onServiceConnected> connected");
            Toast.makeText(CardsGameActivity.this, R.string.msg_service_connected, Toast.LENGTH_LONG)
                    .show();
        }

        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            Log.d(TAG, "onServiceDisconnected> disconnected");
        }
    }

    /** Binds this activity to the mService. */
    private void initService() {
        Log.d(TAG, ">initService");
        mServiceConnection = new DealerServiceConnection();
        Intent i = new Intent();
        i.setClassName("com.sundarsiva.example.dealer.service", "com.sundarsiva.example.dealer.service.DealerService");
        boolean ret = bindService(i, mServiceConnection, Context.BIND_AUTO_CREATE);
        Log.d(TAG, "initService> bound with " + ret);
    }

    /** Unbinds this activity from the mService. */
    private void releaseService() {
        unbindService(mServiceConnection);
        mServiceConnection = null;
        Log.d(TAG, "releaseService> unbound.");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, ">onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cards_game);

        initService();

        // Set up the action bar to show a dropdown list.
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        // Set up the dropdown list navigation in the action bar.
        actionBar.setListNavigationCallbacks(
                // Specify a SpinnerAdapter to populate the dropdown list.
                new ArrayAdapter<String>(
                        actionBar.getThemedContext(),
                        android.R.layout.simple_list_item_1,
                        android.R.id.text1,
                        new String[] {
                                getString(R.string.title_new_deck),
                                getString(R.string.title_shuffle_deck),
                                getString(R.string.title_deal_cards),
                                getString(R.string.title_return_cards),
                                getString(R.string.title_destroy_deck)
                        }),
                this);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Restore the previously serialized current dropdown position.
        if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
            getActionBar().setSelectedNavigationItem(
                    savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Serialize the current dropdown position.
        outState.putInt(STATE_SELECTED_NAVIGATION_ITEM,
                getActionBar().getSelectedNavigationIndex());
    }

    @Override
    public boolean onNavigationItemSelected(int position, long id) {
        if(mService == null) {
            Toast.makeText(this, R.string.msg_service_not_connected, Toast.LENGTH_SHORT).show();
            return false;
        }
        int operationNumber = position + 1;
        try {
            switch (operationNumber) {
                case CardsFragment.OPERATION_NEW_DECK:
                    mDeckId = mService.createDeck();
                    break;
                case CardsFragment.OPERATION_SHUFFLE_DECK:
                    mService.shuffleCards(mDeckId);
                    break;
                case CardsFragment.OPERATION_DEAL_CARDS:
                    mCards = mService.dealCards(mDeckId, 5);//just deal 5 cards
                    break;
                case CardsFragment.OPERATION_RETURN_CARDS:
                    mService.returnCards(mCards, mDeckId);
                    break;
                case CardsFragment.OPERATION_DESTROY_DECK:
                    mService.destroyDeck(mDeckId);
                    break;
            }
        } catch (RemoteException e) {
            Toast.makeText(this, R.string.msg_could_not_connect, Toast.LENGTH_SHORT).show();
        }

        // When the given dropdown item is selected, show its contents in the
        // container view.
        getFragmentManager().beginTransaction()
                .replace(R.id.container, CardsFragment.newInstance(mDeckId, operationNumber, (ArrayList<Card>) mCards))
                .commit();
        return true;
    }

    @Override
    protected void onDestroy() {
        releaseService();
        super.onDestroy();
    }

}
