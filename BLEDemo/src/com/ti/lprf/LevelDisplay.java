package com.ti.lprf;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.SeekBar;


/** Custom widget to display accelerometer data (like a level tool). 
 * Note that vertical mode doesn't really work. */
public class LevelDisplay extends SeekBar {
	private Boolean m_is_vertical = false;
	
	public LevelDisplay(Context context) {
		super(context);
		__init(context, null);
	}

	public LevelDisplay(Context context, AttributeSet attrs) {
		super(context, attrs);
		__init(context, attrs);
	}

	public LevelDisplay(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		__init(context, attrs);
		
	}
	
	private void __init(Context c, AttributeSet attrs) {
		setEnabled(true);
		 TypedArray a = c.obtainStyledAttributes(attrs,
	                R.styleable.LevelDisplay);
		 String orientation = a.getString(R.styleable.LevelDisplay_ld_orientation);
		
				
		if(orientation != null && 0 == orientation.compareTo("vertical")){
				m_is_vertical = true;				
		
		}
		
	}
	@Override
	protected void onDraw(Canvas c){
		if(m_is_vertical){
			c.save();
			c.translate(getHeight(), 0);
			c.rotate((float) (90.0));
			super.onDraw(c);
			c.restore();
		}
		else {
			super.onDraw(c);
		}
	} 
	@Override
	protected void onMeasure(int x, int y){
		if(m_is_vertical){
			super.onMeasure(y,x);
		}
		else {
			super.onMeasure(x,y);
		}
			
	}
	@Override
	public boolean onTouchEvent(MotionEvent Event){
	 // Do nothing
		return true;
	}	
	@Override
	public boolean onKeyDown (int keyCode, KeyEvent event)
	{
		// Do nothing
		return true;
	}

}
