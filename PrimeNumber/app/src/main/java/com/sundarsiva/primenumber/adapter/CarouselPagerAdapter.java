package com.sundarsiva.primenumber.adapter;

import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sundarsiva.primenumber.R;
import com.sundarsiva.primenumber.fragment.CarouselFragment;

import java.util.List;

/**
 * Created by ssivasub on 3/16/14.
 */
public class CarouselPagerAdapter extends PagerAdapter {

    private static final String TAG = CarouselPagerAdapter.class.getSimpleName();
    private List<Integer> mNumberAdapterList;
    boolean mIsDefaultItemSelected = false;

    public CarouselPagerAdapter(List<Integer> primeNumbers){
        Log.d(TAG, "Loading new pager adapter");
        mNumberAdapterList = primeNumbers;
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        LinearLayout numberView = (LinearLayout) View.inflate(container.getContext(), R.layout.carousel_item, null);
        numberView.setId(position);
        Integer number = mNumberAdapterList.get(position);

        TextView tvNumber = (TextView) numberView.findViewById(R.id.carousel_tv_number);
        tvNumber.setText(number.toString());

        if (!mIsDefaultItemSelected) {
            numberView.setScaleX(CarouselFragment.MAX_SCALE);
            numberView.setScaleY(CarouselFragment.MAX_SCALE);
            mIsDefaultItemSelected = true;
        } else {
            numberView.setScaleX(CarouselFragment.MIN_SCALE);
            numberView.setScaleY(CarouselFragment.MIN_SCALE);
        }

        container.addView(numberView);
        return numberView;
    }

    @Override
    public int getCount() {
        return mNumberAdapterList.size();
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
    @Override
    public boolean isViewFromObject(View view, Object o) {
        return view == o;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }
}