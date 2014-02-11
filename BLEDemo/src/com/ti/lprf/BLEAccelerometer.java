package com.ti.lprf;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.RemoteException;
import com.motorola.bluetoothle.BluetoothGatt;
import com.motorola.bluetoothle.IBluetoothGattCallback;

public class BLEAccelerometer extends BLESensor {

	/** X coordinate */
	private float m_x;
	/** Y coordinate */
	private float m_y;
	/** Z coordinate */
	private float m_z;

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
	public BLEAccelerometer(BLESensorBoard sb, BluetoothGatt btgatt,
			BluetoothDevice bledevice, String uuid, Context ctx) {
		super(sb, btgatt, bledevice, uuid,ctx);
		// Set default handles
		m_handles = new String[5];
		READ_HANDLE_INDEX = 2;
		CONF_HANDLE_INDEX = 3;
		TIMING_CONF_HANDLE_INDEX = 4;
		m_handles[READ_HANDLE_INDEX] = "002a";
		m_handles[CONF_HANDLE_INDEX] = "002e";
		m_handles[TIMING_CONF_HANDLE_INDEX] = "0031";

		m_x = 0.0f;
		m_y = 0.0f;
		m_z = 0.0f;
		m_callback = new callback();
		SENSOR_ID = BLESensorBoard.SENSOR_ACCELEROMETER;
		TAG = "BLEAccelerometer";
		m_sensorname = "accelerometer";
	}

	/** Callback class to receive notifications */
	private class callback extends IBluetoothGattCallback.Stub {

		public void notificationGattCb(BluetoothDevice dev, String uuid,
				String char_handle, byte[] data) {

			byte x = data[0];
			byte y = data[1];
			byte z = data[2];

			// Convert into m/s^2
			m_x = ((float) x / 64.0f) * 9.81f;
			m_y = ((float) y / 64.0f) * 9.81f;
			m_z = ((float) z / 64.0f) * 9.81f;

			m_sb.onAccelData(m_x, m_y, m_z);
		}

		public void indicationGattCb(BluetoothDevice device, String uuid,
				String char_handle, String[] data) throws RemoteException {

		}
	};

}
