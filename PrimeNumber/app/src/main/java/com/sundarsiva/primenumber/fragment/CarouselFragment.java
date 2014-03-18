package com.sundarsiva.primenumber.fragment;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.sundarsiva.primenumber.PrimeNumbers;
import com.sundarsiva.primenumber.R;
import com.sundarsiva.primenumber.adapter.CarouselPagerAdapter;
import com.sundarsiva.primenumber.contentprovider.PrimeNumberProvider;
import com.sundarsiva.primenumber.database.PrimeTable;

import java.util.List;

/**
 * Created by Sundar on 3/16/14.
 */
public class CarouselFragment extends PrimeFragment implements ViewPager.OnPageChangeListener,
        android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = CarouselFragment.class.getSimpleName();

    public static final float MIN_SCALE = 1f - 1f / 7f;
    public static final float MAX_SCALE = 1f;

    private static OnCarouselScrolledListener mScrolledListener;
    private ViewPager mVpCarousel;
    private Context mContext;
    private CarouselPagerAdapter mCarouselPagerAdapter;
    private static final String INPUT_N = "input_n";
    private int mInputN;

    public static CarouselFragment newInstance(OnCarouselScrolledListener scrolledListener, int inputN) {
        Bundle args = new Bundle();
        args.putInt(INPUT_N, inputN);
        CarouselFragment cardCarouselFragment = new CarouselFragment();
        cardCarouselFragment.setArguments(args);
        mScrolledListener = scrolledListener;
        return cardCarouselFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Bundle args = getArguments();
        mInputN = args.getInt(INPUT_N);
        super.onCreate(savedInstanceState);
        mContext = getPrimeActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, ">onCreateView");
        final int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 172, getResources().getDisplayMetrics());
        mCarouselPagerAdapter = new CarouselPagerAdapter(null);
        mVpCarousel = (ViewPager) getPrimeActivity().findViewById(R.id.vp_carousel_container);
        mVpCarousel.removeAllViews();
        mVpCarousel.setAdapter(mCarouselPagerAdapter);
        getLoaderManager().initLoader(-1, null, this);
        mVpCarousel.setOffscreenPageLimit(5);
        mVpCarousel.setPageMargin(-margin);
        mVpCarousel.setOnPageChangeListener(this);
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
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        String[] projection = { PrimeTable.COLUMN_ID, PrimeTable.COLUMN_PRIME_NUMBER };
        return new CursorLoader(mContext, Uri.parse(PrimeNumberProvider.CONTENT_URI + "/" + mInputN), projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
        Log.d(TAG, "onLoadFinished()");
        int nPrimes = cursor.getCount();

        if(nPrimes < mInputN) {
            cursor.moveToLast();
            int lastPrime = 0;
            if(nPrimes > 0 && cursor.getColumnIndex(PrimeTable.COLUMN_PRIME_NUMBER) > 0) {
                lastPrime = cursor.getInt(cursor.getColumnIndex(PrimeTable.COLUMN_PRIME_NUMBER));
            }

            int numPrimesToFind = mInputN - nPrimes;
            List<Integer> primes = PrimeNumbers.getNPrimeNumber(lastPrime, numPrimesToFind);
            int numFoundPrimes = primes.size();
            ContentValues[] bulkValues = new ContentValues[numFoundPrimes];
            for(int i = 0; i < numFoundPrimes; i++) {
                ContentValues values = new ContentValues();
                values.put(PrimeTable.COLUMN_PRIME_NUMBER, primes.get(i));
                bulkValues[i] = values;
            }
            getPrimeActivity().getContentResolver().bulkInsert(PrimeNumberProvider.CONTENT_URI, bulkValues);
            getLoaderManager().restartLoader(-1, null, this);
            return;
        }

        mCarouselPagerAdapter.swapCursor(cursor);
        int lastIndex = mCarouselPagerAdapter.getCount()-1;
        mVpCarousel.setCurrentItem(lastIndex);
        this.onPageSelected(lastIndex);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
        mCarouselPagerAdapter.swapCursor(null);
    }

    @Override
    public String getFragmentTag() {
        return TAG;
    }
}
