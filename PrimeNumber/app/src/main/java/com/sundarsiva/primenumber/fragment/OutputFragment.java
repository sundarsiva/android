package com.sundarsiva.primenumber.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sundarsiva.primenumber.R;

/**
 * Created by ssivasub on 3/16/14.
 */
public class OutputFragment extends PrimeFragment {

    private static final String TAG = OutputFragment.class.getSimpleName();
    private static final String INPUT_N = "input_n";


    public static OutputFragment newInstance(int n) {
        Bundle args = new Bundle();
        args.putInt(INPUT_N, n);
        OutputFragment outputFragment = new OutputFragment();
        outputFragment.setArguments(args);
        return outputFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_output, container, false);
        return rootView;
    }

    @Override
    public String getFragmentTag() {
        return TAG;
    }
}
