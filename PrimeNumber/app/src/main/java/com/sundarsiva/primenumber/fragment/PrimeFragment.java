package com.sundarsiva.primenumber.fragment;

import android.support.v4.app.Fragment;

import com.sundarsiva.primenumber.activity.PrimeActivity;

/**
 * Created by Sundar on 3/16/14.
 */
public abstract class PrimeFragment extends Fragment {

    public static final boolean ADD_TO_FRAGMENT_STACK = true;
    public static final boolean DO_NOT_ADD_TO_FRAGMENT_STACK = false;

    public PrimeActivity getPrimeActivity() {
        return  (PrimeActivity)getActivity();
    }

    public abstract String getFragmentTag();
}
