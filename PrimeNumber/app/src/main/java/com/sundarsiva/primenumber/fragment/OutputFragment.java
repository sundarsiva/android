package com.sundarsiva.primenumber.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sundarsiva.primenumber.PrimeNumbers;
import com.sundarsiva.primenumber.R;
import com.sundarsiva.primenumber.Util;

import java.util.List;

/**
 * Created by Sundar on 3/16/14.
 */
public class OutputFragment extends PrimeFragment implements OnCarouselScrolledListener{

    private static final String TAG = OutputFragment.class.getSimpleName();
    private static final String INPUT_N = "input_n";
    private TextView mTvNthPrime;
    private int mInputN;

    public static OutputFragment newInstance(int n) {
        Bundle args = new Bundle();
        args.putInt(INPUT_N, n);
        OutputFragment outputFragment = new OutputFragment();
        outputFragment.setArguments(args);
        return outputFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Bundle args = getArguments();
        mInputN = args.getInt(INPUT_N);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_output, null);
        assert rootView != null;
        mTvNthPrime = (TextView) rootView.findViewById(R.id.output_nth_prime);
        super.onCreateView(inflater, container, savedInstanceState);

        CarouselFragment carouselFragment = CarouselFragment.newInstance(OutputFragment.this, mInputN);
        getPrimeActivity().addFragmentToView(R.id.vp_carousel_container, carouselFragment, DO_NOT_ADD_TO_FRAGMENT_STACK);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public String getFragmentTag() {
        return TAG;
    }

    @Override
    public void onCarouselPositionChanged(int position) {
        String nth = Util.addNumberSuffix(position + 1);
        String nthPrime = getString(R.string.output_nth_prime, nth);
        mTvNthPrime.setText(nthPrime);
    }

}
