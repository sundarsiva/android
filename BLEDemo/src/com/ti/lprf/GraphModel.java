package com.ti.lprf;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

import android.util.Pair;

public class GraphModel implements Serializable {

	/** Serial version UID */
	private static final long serialVersionUID = -2034367220603788788L;
	/** GraphListeners */
	private ArrayList<GraphListener> m_listeners = new ArrayList<GraphListener>();
	/** Our data points */
	public ArrayList<DataPoint> m_data = new ArrayList<DataPoint>();
	
	/** Minimum y value */
	private float m_miny = 0.0f;
	/** Maximum y value */
	private float m_maxy = 0.0f;
	/** Minimum x value */
	private Date m_minx = new Date(0);
	/** Maximum X value */
	private Date m_maxx = new Date(0);
	
	/** Constructor
	 */
	 public GraphModel() {
		 super();
		// add(0.0f);
		// add(0.0f);
	}
	/**
	 * Get number of elements in model
	 * @return number of elements in model
	 */
	 public synchronized int size() {
		 	return m_data.size();
	 }
	
	 /** 
	  * Add a data point
	  * @param index Index to add to
	  * @param d Timestamp
	  * @param f Value
	  */
	 public synchronized void add(int index, Date d, float f){
		 updateMinMax(d,f);
		 m_data.add(index,new DataPoint(d,f));
		 for(GraphListener l: m_listeners)
		 {
			 l.onDataAdded(index,d,f);
		 }
	 }
	 /** 
	  * Add a data point to end
	  * @param d Timestamp
	  * @param data value
	  * @return
	  */
	 public synchronized boolean add(Date d,float data){
		 boolean ret;
		 updateMinMax(d,data);
		 ret = m_data.add(new DataPoint(d,data));
		 for(GraphListener l: m_listeners)
		 {
			 l.onDataAdded(m_data.size()-1,d,data);
		 }
		 return ret;
	 }
	 /**
	  * Clear data
	  */
	 public synchronized void clear(){
		 m_data.clear();
		 for(GraphListener l: m_listeners)
		 {
			 l.onDataRemoved(-1);
		 }
		 
	 }
	 /**
	  * Remove data point
	  * @param idx Index of data point to remove
	  * @return The removed data point
	  */
	 public synchronized DataPoint remove(int idx){
	 	DataPoint ret = m_data.remove(idx);
	 	updateMinMax();
		 for(GraphListener l: m_listeners)
		 {
			 l.onDataRemoved(idx);
		 }
		 
		 return ret;
	 }
	 /**
	  * Set a data point
	  * @param idx Index to set
	  * @param d Timestamp
	  * @param f Data
	  * @return The data point that was replaced
	  */
	 public synchronized DataPoint set(int idx, Date d,Float f){
		 updateMinMax(d,f);
		 DataPoint ret = m_data.set(idx,new DataPoint(d,f));
		 for(GraphListener l: m_listeners)
		 {
			 l.onDataAdded(idx,d,f);
		 }
		 return ret;
	 }
	 /**
	  * Get data point
	  * @param idx Index of point to get
	  * @return
	  */
	 public synchronized DataPoint get(int idx) {
		 return m_data.get(idx);
	 }
	 /**
	  * Get minimum x value
	  * @return smallest x value in data set
	  */
	 public Date getMinx() {
		 return m_minx;
	 }
	 /**
	  * Get maximum x value
	  * @return maximum x value in data set
	  */
	 public Date getMaxx(){
		 return m_maxx;
	 }

	 /**
	  * Get minimum y value
	  * @return smallest y value in data set
	  */
	 public float getMiny() {
		 return m_miny;
	 }
	 /**
	  * Get maximum y value
	  * @return biggest y value in data set
	  */
	 public float getMaxy(){
		 return m_maxy;
	 }

	 /**
	  * Get array of all the elements in data set
	  * @return array of all the elements in the data set
	  */
	 public synchronized Object[] getArray(){
		 return  m_data.toArray();
	 }

	 /**
	  * Helper method to keep max and min values updated
	  * Checks if the supplied parameters are the new extrema
	  * 
	  * @param d Newly added timestamp
	  * @param f Newly added value
	  */
	 private void updateMinMax(Date d, float f)
	 {
		 if(m_data.size() == 0){
			 m_miny = m_maxy = f;
			 m_minx = m_maxx = d;
		 }
		 else {
			 if(m_miny > f) m_miny = f;
			 if(m_maxy < f) m_maxy = f;
			 if(m_minx.after(d)) m_minx = d;
			 if(m_maxx.before(d)) m_maxx = d;
		 }
	 }
	 /**
	  * Find min and max for entire data set
	  */
	 private void updateMinMax(){
		 
		 for(DataPoint d: m_data){
			 updateMinMax(d.first,d.second);
		 }
	 }
	 /**
	  * Add listener
	  * @param l new listener
	  */
	 public void addListener(GraphListener l){
		 m_listeners.add(l);
	 }
	 /**
	  * Remove listener
	  * @param l listener to remove
	  */
	 public void removeListener(GraphListener l){
		 m_listeners.remove(l);
	 }
	}
