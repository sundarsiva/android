package com.ti.lprf;

import java.util.Date;

/**
 * Interface to capture graph data events
 */
public interface GraphListener {
	/** Called when data is added
	 * 
	 * @param index Index in model of data
	 * @param timestamp Timestamp of data
	 * @param data Data point
	 */
	void onDataAdded(int index, Date timestamp, float data);
	/**
	 * Called when data is removed
	 * @param index Index of data (before removal), -1 if everything was cleared
	 */
	void onDataRemoved(int index);
}
