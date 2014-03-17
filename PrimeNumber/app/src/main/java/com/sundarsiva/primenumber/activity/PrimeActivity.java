package com.sundarsiva.primenumber.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.sundarsiva.primenumber.R;
import com.sundarsiva.primenumber.fragment.InputFragment;
import com.sundarsiva.primenumber.fragment.PrimeFragment;

public class PrimeActivity extends FragmentActivity {

    private static final String TAG = PrimeActivity.class.getSimpleName();

    private View mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prime);
        mProgressBar = findViewById(R.id.progress_bar);
        addFragmentToView(new InputFragment(), PrimeFragment.DO_NOT_ADD_TO_FRAGMENT_STACK);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.prime, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void addFragmentToView(PrimeFragment fragment, boolean addToBackStack){
        addFragmentToView(R.id.fragment_container, fragment, addToBackStack);
    }


    public void addFragmentToView(int containerId, PrimeFragment fragment, boolean addToBackStack){
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        String fragmentTag = fragment.getFragmentTag();
        if(!fragment.isAdded() && addToBackStack){
            fragmentTransaction.addToBackStack(fragmentTag);
        }
        fragmentTransaction.replace(containerId, fragment, fragmentTag);
        fragmentTransaction.commit();
    }

    public void showHideProgressBar(boolean show) {
        if(show) {
            mProgressBar.setVisibility(View.VISIBLE);
        } else {
            mProgressBar.setVisibility(View.GONE);
        }
    }


}
