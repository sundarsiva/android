package com.ti.lprf;


import android.content.Context;

import android.graphics.Canvas;

import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

/** View to visualize rotation */
public class RotationDisplay extends ImageView {
	/** Tag for logging */
	private static final String TAG = "RotationDisplay";
	/** The current angle */
	private float m_angle;
	/** Textview to display the angle */
	private TextView m_textview;
	public RotationDisplay(Context c) {
		super(c);
		m_textview = null;

	}
	public RotationDisplay(Context c, AttributeSet s) {
		super(c,s);
		m_textview = null;

	}
	public RotationDisplay(Context c, AttributeSet s, int d) {
		super(c,s,d);
		m_textview = null;
	}
	/** 
	 * Set rotation angle
	 * @param angle
	 */
	void setAngle(float angle) {
		m_angle = angle;
		if(m_textview != null)
			m_textview.setText(String.format("%.0f", angle));
		postInvalidate();
	}
	/**
	 * Set textview to write angle to
	 * @param t The textview
	 */
	void setTextview(TextView t){
		m_textview = t;
	}
	/**
	 * Get the current angle
	 * @return the current angle
	 */
	float getAngle()
	{
		return m_angle;
	}
	/**
	 * Called when widget should be drawed
	 */
	@Override
	protected void onDraw(Canvas c) {
		c.save();
		int x = getDrawable().getIntrinsicWidth()/2;
		int y = getDrawable().getIntrinsicHeight()/2;
		// Rotate the canvas
		c.rotate((float) (360.0-m_angle),x,y);
		// Draw on it
		super.onDraw(c);
		// And restore it
		c.restore();
	}
	
	
}
