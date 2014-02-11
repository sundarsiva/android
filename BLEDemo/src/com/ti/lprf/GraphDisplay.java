package com.ti.lprf;


import java.util.Date;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;




public class GraphDisplay extends View implements GraphListener {

	/** Our graph data */
	private GraphModel m_data;
	
	/** Scaling factor in the X direction */
	private float m_xscale = 1.0f;
	/** Scaling factor in the Y direction */
	private float m_yscale = 10.0f;
	
	/** Lowest X value */
	private Date m_minx = new Date(System.currentTimeMillis()-1);
	/** Highest X value */
	private Date m_maxx = new Date();

	/** Lowest y value */
	private float m_miny = 0.0f;
	/** Highest y value */
	private float m_maxy = 30.0f;
	
	/** Store the initial extreme y values (setMaxY/minY) for when data is cleared */
	private float m_initialminy = 0.0f;
	/** Store the initial extreme y values (setMaxY/minY) for when data is cleared */
	private float m_initialmaxy = 30.0f;
	
	/** Front color (color of the graph) */
	private int m_frontcolor = Color.RED;
	/** Background color */
	private int m_bgcolor = Color.BLACK;
	/** Grid color */
	private int m_gridcolor = Color.GRAY;
	
	/** Height of the graph */
	private int m_height = 0;
	/** Width of the graph */
	private int m_width = 540;
	
	private int m_initialwidth = 540;
	
	/** Whether or not to draw grid */
	private boolean m_showgrid = true;
	
	/** Distance between every y grid line */
	private float m_gridyspacing = 10.0f;
	/** Distance between x grid lines (in minutes) */
	private long m_gridxspacing = 2; // 2 minutes  

	/** Bitmap to draw to for caching - this bitmap is drawn to canvas on onDraw() */
	private Bitmap m_bitmap;
	/** Whether or not m_bitmap is valid */
	private boolean m_isvalid = false;
	
	/** Constructor */
	public GraphDisplay(Context context) {
		super(context);
	}
	/** Constructor */
	public GraphDisplay(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	/** Constructor */
	public GraphDisplay(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	
	
	/** getModel()
	 * Get the data model
	 * @return The data model behind this graph
	 */
	public GraphModel getModel()
	{
		return m_data;
	}
	
	/**
	 * Set show grid
	 * @param b true to display grid, false if not
	 */
	public void setShowGrid(boolean b){
		m_showgrid = b;
		postInvalidate();
	}
	
	/** 
	 * Set X grid spacing
	 * Set number of minutes to separate two X grid lines
	 * @param spacing Number of minutes
	 */
	public void setGridXSpacing(long spacing) {
		m_gridxspacing = spacing;
		postInvalidate();
	}
	/**
	 * Set Y grid spacing
	 * @param spacing Distance between two Y grid lines 
	 */
	public void setGridYSpacing(float spacing) {
		m_gridyspacing = spacing;
		postInvalidate();
	}
	
	/**
	 * Set graph model
	 * @param g the new graph model
	 */
	public void setModel(GraphModel g)
	{
		// Don't listen to previous model
		if(m_data != null){
			m_data.removeListener(this);
		}
		
		// Set new model
		m_data = g;
		m_data.addListener(this);
		

		m_isvalid = false;

		

		Date maxx = m_data.getMaxx();
		Date minx = m_data.getMinx();
		// Get new max/min values, if they're valid
		if(minx.getTime() == 0) return;
		if(m_minx.after(minx)) m_minx = minx;
		if(m_maxx.before(maxx)) m_maxx = maxx;
		
		// Same for Y
		float maxy = m_data.getMaxy();
		float miny = m_data.getMiny();
		if(maxy > m_maxy) m_maxy = maxy;
		if(miny < m_miny) m_miny = miny;
		
		postInvalidate();
	}
	
	/**
	 * Set background color (default black)
	 * @param c The new bacground color
	 */
	public void setBgColor(int c)
	{
		m_bgcolor = c;
	}
	/** 
	 * Set foreground color (default red)
	 * @param c New foreground color
	 */
	public void setFrontColor(int c) 
	{
		m_frontcolor = c;
	}
	
	/**
	 * Explicitly set minimum X value
	 * If a new x value comes in and is smaller than the supplied x, that new value will be used
	 * @param x new minimium value
	 */
	public void setMinX(Date x) {
		m_minx = x;
		updateScales();
	}
	/**
	 * Explicitly set minimum Y value
	 * If a new Y value comes in and is smaller than the supplied y, that new value will be used
	 * @param y new minimium value
	 */
	public void setMinY(float y) {
		m_miny = y;
		m_initialminy = y;
		updateScales();
	}
	/**
	 * Explicitly set maximum X value
	 * If a new X value comes in and is bigger than the supplied x, that new value will be used
	 * @param x new maximum value
	 */
	public void setMaxX(Date x) {
		m_maxx = x;
		updateScales();
	}
	/**
	 * Explicitly set maximum Y value
	 * If a new Y value comes in and is bigger than the supplied y, that new value will be used
	 * @param y new maximum value
	 */
	public void setMaxY(float y){
		m_maxy = y;
		m_initialmaxy = y;
		updateScales();
	}
	
	/**
	 * Set width of the graph
	 * @param width
	 */
	public void setWidth(int width) {
		m_width = width;
		m_initialwidth = width;
		requestLayout();
	}
	/** Update scaling factors
	 */
	private void updateScales() {
		// Two pixels/second
		m_xscale = (float)1/(500);
		// Fill view
		m_yscale = (float)m_height/(float)(m_maxy - m_miny);
	}
	
	
	/** 
	 * Draw grid to canvas
	 * @param c Canvas to draw grid on
	 */
	protected void drawGrid(Canvas c){
		// Find a good Y value to start on, aligned with zero
		float cury = 0.0f;
		if(cury > m_miny) {
			while(cury > m_miny) cury -= m_gridyspacing;
		} else {
			while(cury < m_miny) cury += m_gridyspacing;

		}
		
		// Get a paint
		Paint fg = new Paint();
		// Set correct color
		fg.setColor(m_gridcolor);
		
		// Current string representation of y value
		String curstring;
		
		// Iterate over the lines (until we're sufficiently close to the top, 
		while(m_height - (cury-m_miny)*m_yscale > 12 ){
			curstring = String.format("%.0f", cury);
			// Draw it to the far left
			c.drawText(curstring, 0, (m_height - (cury-m_miny)*m_yscale)-1, fg);
			// and the far right
			c.drawText(curstring, m_width-15, (m_height - (cury-m_miny)*m_yscale)-1, fg);
			// Draw the line
			c.drawLine(0,(m_height - (cury-m_miny)*m_yscale),m_width, (m_height - (cury-m_miny)*m_yscale),fg);
			cury += m_gridyspacing;
			
		}
		
		// Draw X grid based on coordinates. But start on the closest whole gridspacing
		final long msecspacing = m_gridxspacing*60*1000; // spacing in in msecs
		
		// Find first whole timestamp that is a multiple of msecspacing and is inside the graph
		long closest = (m_minx.getTime() - (m_minx.getTime() % msecspacing) + msecspacing);

		// Find initial position
		int curx = (int)((closest - m_minx.getTime())*m_xscale);
		
		// Get hours and minutes for that timestamp
		Date d = new Date(closest);
		int mins = d.getMinutes();
		int hours = d.getHours();
		// Do until end of graph
		while(curx < m_width - 15) {
			if(curx < 15) {
				curx += msecspacing*m_xscale;
				continue;
			}
			// Draw text
			c.drawText(getHourMinuteString(hours,mins),curx-16,12,fg);
			// Draw line
			c.drawLine(curx,12, curx,m_height,fg);
			
			// Update curx
			curx += msecspacing*m_xscale;
			
			// Update minutes/hours (would use GregorianCalendar, but it is slooow)
			mins += m_gridxspacing;
			if(mins >= 60)
			{
				hours++;
				mins -=60;
				if(hours >= 24) hours = 0;
			}
		}
	}
	/** Helper function to build a HH:MM string */
	private String getHourMinuteString(int hours, int mins){
		String ret = "";
		if(hours < 10) {
			ret += "0";
		}
		ret += hours + ":";
		
		if(mins < 10)
			ret += "0";
		ret += mins;
		return ret;
	}
	
	/** 
	 * Draw the graph in its entirety
	 * 
	 * @param c Canvas to draw on
	 */
	protected void drawGraph(Canvas c) {
		// Get foreground paint
		Paint fg = new Paint();
		fg.setColor(m_frontcolor);
		
		// Fill canvas with background color
		c.drawColor(m_bgcolor);
		
		// Draw grid if we want it
		if(m_showgrid){
			drawGrid(c);
		}
		
		// If we have some data, draw graph!
		if(m_data != null && m_data.size() > 1) {
			// The current points we draw lines between
			DataPoint data, nextdata;
			// Data is the first one
			data = m_data.get(0);
			// The timestamps - minx (t2 = next)
			int t, t2;
			// Hate typing m_minx.getTime() all the time
			long minx = m_minx.getTime();
			// Make sure scales is updated
			updateScales();
			
			// We start with prev = 0, so we stop at prev = lastindex-1
			int size = m_data.size() - 1;
			// Get data point array
			Object data_array[] = m_data.getArray();
			// Get first data point
			data = (DataPoint) data_array[0];
			// Get initial x position ( this is moved to t in first iteration
			t2 = (int) (data.first.getTime() - minx);
			
			// Iterate over all the points
			for (int i = 0; i < size; i++) {
				// Get next data point
				nextdata = (DataPoint) data_array[i + 1];
				// Shift the t values
				t = t2;
				// Get new t2
				t2 = (int) (nextdata.first.getTime() - minx);
				// Skip duplicates
				if(t == t2) continue;

				if(t2 > 0){
				// Draw line
				c.drawLine((int) (t) * m_xscale, 
						m_height - (int) ((data.second - m_miny)* m_yscale),
						(int) (t2)* m_xscale, 
						m_height - (int) ((nextdata.second - m_miny)* m_yscale), 
						fg);
				
				
				}
				// Shift data
				data = nextdata;
			}
		}
	

	}
	
	
	/**
	 * Called when new data arrives
	 */
	public void onDataAdded(int index, Date timestamp, float data) {
		// Check if we need to update minx and maxx
		if(timestamp.getTime() == 0) return;
		if(timestamp.before(m_minx)){
			m_minx = timestamp;
			
		}
		if(timestamp.after(m_maxx)){
			
			m_maxx = timestamp;
			
		}
		// Same for miny and maxy
		if(data < m_miny){
			m_miny = data;
			m_isvalid = false;
			updateScales();
		}
		if(data > m_maxy){
			m_maxy = data;
			m_isvalid = false;
			updateScales();
		}
		
		// Is the graph becoming too big?
		if(m_xscale*(m_maxx.getTime()-m_minx.getTime())+100 > m_width) {
			if(m_width > 5000) {
				// Chop off minx
				m_minx = new Date((long)(m_minx.getTime()+2000/m_xscale));
			}
			
			// Grow in X direction by 500 pixels
			m_width = (int)(m_xscale*(float)(m_maxx.getTime()-m_minx.getTime()))+500;
			// Tell parents we want to be measured again
			requestLayout();
			// We will need to redraw everything on next onDraw
			m_isvalid = false;
		}
		
		// If we're valid and have more than one data point, draw line between previous datapoint
		// and the new one
		if(m_isvalid && index > 0) {
			// Get previous data point
			DataPoint prev = m_data.get(index-1);
			if(prev.first.getTime() == timestamp.getTime()) return;
			// Get painting kit (draw to m_bitmap)
			Canvas c = new Canvas(m_bitmap);
			Paint p = new Paint();
			p.setColor(m_frontcolor);
			
			// Draw line
			c.drawLine((prev.first.getTime()-m_minx.getTime())*m_xscale,
					m_height-(int)((prev.second-m_miny)*m_yscale),
					(timestamp.getTime()-m_minx.getTime())*m_xscale,
					m_height-(int)((data-m_miny)*m_yscale),
					p);
			
		}
		// Tell parents we want to be drawn again
		postInvalidate();
	}
	
	
	
	@Override
	protected void onDraw(Canvas c){
		// Do we need to redraw the whole thing?
		if(!m_isvalid) {
			Canvas c2 = new Canvas(m_bitmap);
			drawGraph(c2);
			// We now know that the bitmap is valid
			m_isvalid = true;

		}
		// Draw the bitmap
		c.drawBitmap(m_bitmap,new Matrix(),null);
		
	}
	
	@Override
	protected void onMeasure(int xspec, int yspec){
		super.onMeasure(xspec, yspec);

		m_height = View.MeasureSpec.getSize(yspec);
		setMeasuredDimension(m_width, m_height);
		if(!m_isvalid) {
			m_bitmap = Bitmap.createBitmap(m_width, m_height, Bitmap.Config.RGB_565);
		}
		updateScales();
	} 
	public void onDataRemoved(int index) {
		m_isvalid = false;
		if(index == -1) {
			// Everything was cleared. Let's also reset the minx/maxx-es
			m_minx = new Date(System.currentTimeMillis()-1);
			m_maxx = new Date();
			m_miny = m_initialminy;
			m_maxy = m_initialmaxy;
			m_width = m_initialwidth;
			updateScales();
		}
		postInvalidate();
	}
	
	

}
