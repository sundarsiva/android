package com.sundarsiva.primenumber.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.sundarsiva.primenumber.R;

/**
 * Created by Sundar on 3/16/14.
 */
public class InputFragment extends PrimeFragment {

    private static final String TAG = InputFragment.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getPrimeActivity().showHideProgressBar(false);
        View rootView = inflater.inflate(R.layout.fragment_input, container, false);

        final EditText etInputN = (EditText) rootView.findViewById(R.id.input_et_input_n);

        View btFind = rootView.findViewById(R.id.input_bt_find);
        btFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getPrimeActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                getPrimeActivity().showHideProgressBar(true);
                Log.d(TAG, ">find button clicked");
                int inputN = 1;
                try {
                    inputN = Integer.parseInt(etInputN.getText().toString());
                } catch (NumberFormatException e) {
                    getPrimeActivity().showHideProgressBar(false);
                    Toast.makeText(getPrimeActivity(), getString(R.string.input_tst_enter_a_number), Toast.LENGTH_LONG).show();
                    return;
                }
                OutputFragment outputFragment = OutputFragment.newInstance(inputN);
                getPrimeActivity().addFragmentToView(outputFragment, ADD_TO_FRAGMENT_STACK);
            }
        });
        return rootView;
    }

    @Override
    public String getFragmentTag() {
        return TAG;
    }
}
