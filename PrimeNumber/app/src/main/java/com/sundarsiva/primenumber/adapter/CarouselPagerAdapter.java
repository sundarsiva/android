package com.sundarsiva.primenumber.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sundarsiva.primenumber.R;
import com.sundarsiva.primenumber.database.PrimeDatabaseHelper;
import com.sundarsiva.primenumber.database.PrimeTable;
import com.sundarsiva.primenumber.fragment.CarouselFragment;

import java.util.List;

/**
 * Created by Sundar on 3/16/14.
 */
public class CarouselPagerAdapter extends PagerAdapter {

    private static final String TAG = CarouselPagerAdapter.class.getSimpleName();
    boolean mIsDefaultItemSelected = false;
    private Cursor cursor;

    public CarouselPagerAdapter(Cursor cursor) {
        this.cursor = cursor;
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        LinearLayout numberView = (LinearLayout) View.inflate(container.getContext(), R.layout.carousel_item, null);
        numberView.setId(position);

        cursor.moveToPosition(position);
        Integer number = cursor.getInt(cursor.getColumnIndex(PrimeTable.COLUMN_PRIME_NUMBER));

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
        if(cursor == null) {
            return 0;
        } else {
            return cursor.getCount();
        }
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

    public void swapCursor(Cursor cursor) {
        Log.d(TAG, ">swapCursor");
        this.cursor = cursor;
        notifyDataSetChanged();
    }
}