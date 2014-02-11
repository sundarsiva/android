package com.ti.lprf;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.RemoteException;
import android.util.Log;
import com.motorola.bluetoothle.BluetoothGatt;
import com.motorola.bluetoothle.IBluetoothGattCallback;

public class BLEMagnetometer extends BLESensor {

	/** X coordinate */
	private float m_x;
	/** Y coordinate */
	private float m_y;
	/** Z coordinate */
	private float m_z;
	
	/** Calibration values */
	private float m_calx = 4262.55f;
	private float m_caly = -4764.55f;
	private float m_calz = -1811.9f;
	
	private static final int NUM_CALI_VALUES = 50;
	private float m_calivaluesx[] = new float[NUM_CALI_VALUES];
	private float m_calivaluesy[] = new float[NUM_CALI_VALUES];
	private float m_calivaluesz[] = new float[NUM_CALI_VALUES];
	private int m_caliindex = 0;
	
	private boolean m_isCalibrating = false;

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
	 * 			  The context of the service/application
	 */
	public BLEMagnetometer(BLESensorBoard sb, BluetoothGatt btgatt,
			BluetoothDevice bledevice, String uuid, Context ctx) {
		super(sb, btgatt, bledevice, uuid,ctx);
		// Set default handles
		m_handles = new String[10];
		READ_HANDLE_INDEX = 7;
		CONF_HANDLE_INDEX = 8;
		TIMING_CONF_HANDLE_INDEX = 9;
		m_handles[READ_HANDLE_INDEX] = "003D";
		m_handles[CONF_HANDLE_INDEX] = "0041";
		m_x = 0.0f;
		m_y = 0.0f;
		m_z = 0.0f;
		m_callback = new callback();
		SENSOR_ID = BLESensorBoard.SENSOR_MAGNET;
		TAG = "BLEMagnetometer";
		m_sensorname = "magnetometer";
	}
	
	public void calibrate() {
		m_isCalibrating = true;
		m_caliindex = 0;
	}

	/** 
	 * Calibration function
	 * The idea here is to make the user spin the magnetometer around its axis, 
	 * and average the measured values. Then we get the component of the vector that is static 
	 * with respect to rotation, and we can subtract it on subsequent measurements.
	 * @param x The just read X value
	 * @param y The just read Y value
	 * @param z The just read Z value
	 */
	private void do_calibrate(float x, float y, float z) {
		
		if(m_caliindex < NUM_CALI_VALUES) {
			// Tell client activities about our progress
			m_sb.onCalibrationData(SENSOR_ID, m_caliindex, NUM_CALI_VALUES);
			// Store the calibration values
			m_calivaluesx[m_caliindex] = x;
			m_calivaluesy[m_caliindex] = y;
			m_calivaluesz[m_caliindex] = z;
			m_caliindex++;
		}
		else {
			// We're done. Average the values
			m_calx = 0;
			m_caly = 0;
			m_calz = 0;
			for(int i = 0; i < NUM_CALI_VALUES; i++) {
				m_calx += m_calivaluesx[i];
				m_caly += m_calivaluesy[i];
				m_calz += m_calivaluesz[i];
			}
			m_calx /= NUM_CALI_VALUES;
			m_caly /= NUM_CALI_VALUES;
			m_calz /= NUM_CALI_VALUES;
			// Stop calibrating
			m_isCalibrating = false;
			Log.d(TAG, "Calibrated to " +m_calx + " " + m_caly + " " + m_calz);
			m_sb.onCalibrationData(SENSOR_ID, m_caliindex, NUM_CALI_VALUES);

		}
	}
	
	/** Callback class to receive notifications */
	private class callback extends IBluetoothGattCallback.Stub {

		public void notificationGattCb(BluetoothDevice dev, String uuid,
				String char_handle, byte[] data) {
		//	Log.d(TAG, "Got notification");
			int d[] = new int[6];
			Log.d(TAG, "Got raw data: " + String.format("%d %d %d %d %d %d", 
					data[0],
					data[1], 
					data[2], 
					data[3], 
					data[4], 
					data[5] ));
			// Java doesn't have unsigned types, so we have to extract the bytes in a strange way.
			for(int i = 0; i < 6; i++) {
				d[i] = ((int)data[i]) & 0x000000FF;
			}
			// The data is signed, so the most significant byte should be sign-expanded, but the 
			// LSB shouldn't.
			int x = data[0] << 8 | d[1];
			int y = data[2] << 8 | d[3] ;
			int z = data[4] << 8 | d[5];
			if(m_isCalibrating) {
				do_calibrate(x,y,z);
			} else {
				Log.d(TAG,"x: " + x + " y:" + y + " z:"
					+ z);
				m_x = ((float) x) - m_calx;
				m_y = ((float) y) - m_caly;
				m_z = ((float) z) - m_calz;

				m_sb.onMagnetData(m_x, m_y, m_z);
			}
		}

		public void indicationGattCb(BluetoothDevice device, String uuid,
				String char_handle, String[] data) throws RemoteException {

		}
	};

}
