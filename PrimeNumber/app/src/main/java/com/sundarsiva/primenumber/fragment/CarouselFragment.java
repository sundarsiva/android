package com.sundarsiva.primenumber.fragment;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.sundarsiva.primenumber.R;
import com.sundarsiva.primenumber.adapter.CarouselPagerAdapter;

import java.util.List;

/**
 * Created by Sundar on 3/16/14.
 */
public class CarouselFragment extends PrimeFragment implements ViewPager.OnPageChangeListener {

    private static final String TAG = CarouselFragment.class.getSimpleName();

    public static final float MIN_SCALE = 1f - 1f / 7f;
    public static final float MAX_SCALE = 1f;

    private static OnCarouselScrolledListener mScrolledListener;
    private ViewPager mVpCarousel;
    private static List<Integer> mPrimeNumbers;

    public static CarouselFragment newInstance(OnCarouselScrolledListener scrolledListener, List<Integer> primeNumbers) {
        CarouselFragment cardCarouselFragment = new CarouselFragment();
        mPrimeNumbers = primeNumbers;
        mScrolledListener = scrolledListener;
        return cardCarouselFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, ">onCreateView");
        final int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 172, getResources().getDisplayMetrics());
        CarouselPagerAdapter carouselPagerAdapter = new CarouselPagerAdapter(mPrimeNumbers);
        mVpCarousel = (ViewPager) getPrimeActivity().findViewById(R.id.vp_carousel_container);
        mVpCarousel.removeAllViews();
        mVpCarousel.setAdapter(carouselPagerAdapter);
        mVpCarousel.setOffscreenPageLimit(5);
        mVpCarousel.setPageMargin(-margin);
        mVpCarousel.setOnPageChangeListener(this);
        final int primeNumberSize = mPrimeNumbers.size();

        if(primeNumberSize > 0){
            mVpCarousel.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mVpCarousel.setCurrentItem(primeNumberSize - 1);
                    getPrimeActivity().showHideProgressBar(false);
                }
            }, 100);
            this.onPageSelected(primeNumberSize - 1);
       }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        for (int i = 0; i < mVpCarousel.getChildCount(); i++) {
            LinearLayout addressView = (LinearLayout) mVpCarousel.getChildAt(i);
            assert addressView != null;
            int itemPosition = addressView.getId();

            if (itemPosition == position) {
                addressView.setSelected(true);
                addressView.setScaleX(MAX_SCALE - positionOffset / 7f);
                addressView.setScaleY(MAX_SCALE - positionOffset / 7f);
            }

            if (itemPosition == (position + 1)) {
                addressView.setScaleX(MIN_SCALE + positionOffset / 7f);
                addressView.setScaleY(MIN_SCALE + positionOffset / 7f);
            }
        }
    }

    @Override
    public void onPageSelected(int position) {
        mScrolledListener.onCarouselPositionChanged(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public String getFragmentTag() {
        return TAG;
    }
}
