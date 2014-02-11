package com.ti.lprf;

import java.util.ArrayList;
import java.util.LinkedList;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import com.motorola.bluetoothle.BluetoothGatt;
import com.motorola.bluetoothle.IBluetoothGattCallback;


/** Abstract superclass for all the sensors to take care of everything that is common to them */
public abstract class BLESensor {

	// Status codes

	/** Sensor stopped */
	protected static final int STATUS_STOPPED = 0;
	/** Sensor running (you should now expect notifications */
	protected static final int STATUS_RUNNING = 1;
	/** Some error */
	protected static final int STATUS_ERROR = 2;

	/** Our current status */
	protected int m_status;

	/** Messages for the status codes */
	protected static final String[] STATUS_MSGS = { "stopped", "running", "error" };

	/** Our connection to the system BluetoothGattService */
	protected BluetoothGatt m_btgatt;
	/** The BLE device we're talking to */
	protected BluetoothDevice m_bledevice;
	/** The UUID of the service */
	protected String m_uuid;
	/** The handles of that service */
	protected String m_handles[];

	/** Index in m_handles of the data handle for this sensor */
	protected int READ_HANDLE_INDEX;
	/** Index in m_handles of the configuration handle for this sensor */
	protected int CONF_HANDLE_INDEX;
	
	/** Index in m_handles of the handle for configuring timings of the sensor. -1 if no such handle exists */
	protected int TIMING_CONF_HANDLE_INDEX = -1;

	/** Our parent sensor board */
	protected BLESensorBoard m_sb;

	/** The callback of this sensor */
	protected IBluetoothGattCallback m_callback;

	/** Tag for logging - to be set in subclass */
	protected String TAG;
	/**
	 * ID of this sensor - to be set by subclass. See BLESensorBoard's sensor
	 * IDs
	 */
	protected int SENSOR_ID;
	/**
	 * Time to wait between writes, as we don't seem to receive write completed
	 * notifications
	 */
	protected static final int WAIT_PERIOD = 15000;

	/** Textual name of the sensor */
	protected String m_sensorname;
	
	/** Write queue. Static, as it needs to be global for all sensors */
	protected static LinkedList<BLEWriteRequest> m_writequeue = new LinkedList<BLEWriteRequest>();
	
	/** Write thread. Note that this one is static - we want this thread to organize writes to all
	 * the sensors.
	 * For some reason, writing too often (i.e. more than once every 15 sec
	 * causes writes to get lost, or something of the sort (buggy BLE stack). Strangeness happens. 
	 * So we work around by making sure we don't write too often */
	private static Thread m_writethread = new Thread() {
		@Override
		public void run() {
			Log.d("BLEWriteThread", "Starting..");
			BLEWriteRequest b;
			while(true) {
				doSleep(WAIT_PERIOD);
				synchronized(m_writequeue){
					while(m_writequeue.isEmpty()) {
					try {
						m_writequeue.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					}
					if(!m_writequeue.isEmpty()) {
						b = m_writequeue.poll();
						b.execute();
						if(b.isDisconnect){
							m_writequeue.clear();
						}
						
					}
				}
				
			}
		}
	};
	
	
	
	/**
	 * Constructor
	 * 
	 * @param sb
	 *            The parent sensorboard
	 * @param btgatt
	 *            The bluetoothgatt instance
	 * @param bledevice
	 *            The bluetooth device to connect to
	 * @param uuid
	 *            The UUID of our service
	 * @param ctx
	 * 			  The context of the service/application (not really needed)
	 */
		protected BLESensor(BLESensorBoard sb, BluetoothGatt btgatt,
			BluetoothDevice bledevice, String uuid, Context ctx) {
		m_uuid = uuid;
		m_btgatt = btgatt;
		m_bledevice = bledevice;
		m_sb = sb;
		if(!m_writethread.isAlive()) m_writethread.start();
		setStatus(STATUS_STOPPED);
	}

	/**
	 * Destructor
	 */
	protected void finalize() throws Throwable {
		if (m_status == STATUS_RUNNING) {
			stopSensor();
		}
		m_writethread.stop();
		super.finalize();
	}

	/** Return the data handle of this sensor */
	public String getReadHandle() {
		return m_handles[READ_HANDLE_INDEX];
	}

	/** Start sensor function */
	public void startSensor() {
		if(m_status == STATUS_RUNNING) return;
		for (String h : m_handles) {
			Log.d(TAG, "Handle: " + h);
		}

		Log.d(TAG, "Starting sensor");

		byte data[] = { 0x01 };
		byte data2[] = { 0x01, 0x00 };

		// Start sensor
		write(m_handles[CONF_HANDLE_INDEX], data, false, "Starting " + m_sensorname);

		// Enable notifications
		write(m_handles[READ_HANDLE_INDEX], data2, true, "Enabling notifications for " + m_sensorname);
		
	

		setStatus(STATUS_RUNNING);
	}

	/** Stop sensor function */
	public void stopSensor() {
		if(m_status == STATUS_STOPPED) return;
		byte data2[] = { 0x00, 0x00 };
		Log.d(TAG, "StopSensor");

		write(m_handles[READ_HANDLE_INDEX], data2, true, "Disabling notifications for " + m_sensorname);
		byte data[] = { 0x00 };

		write(m_handles[CONF_HANDLE_INDEX], data, false, "Stopping " + m_sensorname);

		setStatus(STATUS_STOPPED);
	}

	/**
	 * Perform a write
	 * 
	 * @param handle
	 *            The handle to write to
	 * @param data
	 *            The data to write
	 * @param isDesc
	 *            True if configuring the handle, false if writing the handle
	 * @param what
	 * 			  String describing the operation (will be shown to user)
	 */
	public void write(String handle, byte[] data, boolean isDesc, String what) {
	
		Log.d(TAG,"Queuing write of " + data.length + " bytes to " + handle + " configuration: " + isDesc);
		BLEWriteRequest b = new BLEWriteRequest(m_btgatt,m_bledevice, m_uuid, handle, data,data.length, isDesc, what, m_sb);
		synchronized(m_writequeue){
			m_writequeue.add(b);
			m_writequeue.notifyAll();
		}
	}

	
	/** 
	 * Set timing of sensor
	 * @param timing Notifications will be received at (50ms + 10*timing ms)
	 */
	protected void setTiming(int timing)
	{
		if(TIMING_CONF_HANDLE_INDEX == -1) return; 
		byte[] data = {(byte)timing};
		write(m_handles[TIMING_CONF_HANDLE_INDEX],data,false, "Setting update rate to " + (50+10*timing) + "ms for " + m_sensorname);
		
	}
	/**
	 * Set the status of the sensor. Passes it on to sensorboard.
	 * 
	 * @param status
	 *            The current status
	 */
	protected void setStatus(int status) {
		Log.d(TAG, "Status: " + STATUS_MSGS[status]);
		int oldstatus = m_status;

		m_status = status;
		m_sb.onStatusChanged(SENSOR_ID, oldstatus, m_status);

	}

	/**
	 * Set handles of sensor
	 * 
	 * @param handles
	 *            Handles as returned by readGattCharacteristics()
	 */
	public void setHandles(String handles[]) {
		for (String h : handles) {
			Log.d(TAG, "Handle: " + h);
		}
		m_handles = handles;
	}

	/**
	 * Helper function to do sleeping without having to try()/catch() all the
	 * time
	 * 
	 * @param t
	 *            Time to sleep
	 */
	protected static void doSleep(int t) {
		try {
			Thread.sleep(t);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Disconnect function
	 * This adds a disconnect request to the write queue.
	 * @param btgatt The BluetoothGatt instance
	 * @param bledevice The device to disconnect from
	 * @param uuid The UUID of the Primary Service to disconnect from
	 */
	public static void disconnectGatt(BluetoothGatt btgatt, BluetoothDevice bledevice, String uuid){
		BLEWriteRequest b = new BLEWriteRequest();
		b.uuid = uuid;
		b.bledevice = bledevice;
		b.isDisconnect = true;
		b.btgatt = btgatt;
		synchronized(m_writequeue){
			m_writequeue.add(b);
			m_writequeue.notifyAll();
		}
	}

}
