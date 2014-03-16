package com.sundarsiva.primenumber.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.sundarsiva.primenumber.R;
import com.sundarsiva.primenumber.fragment.InputFragment;
import com.sundarsiva.primenumber.fragment.PrimeFragment;

public class PrimeActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prime);
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
        android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        String fragmentTag = fragment.getFragmentTag();
        if(!fragment.isAdded() && addToBackStack){
            fragmentTransaction.addToBackStack(fragmentTag);
        }
        fragmentTransaction.replace(R.id.fragment_container, fragment, fragmentTag);
        fragmentTransaction.commit();
    }

}
