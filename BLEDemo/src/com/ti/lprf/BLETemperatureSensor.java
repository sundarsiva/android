package com.ti.lprf;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.RemoteException;
import android.util.Log;
import com.motorola.bluetoothle.BluetoothGatt;
import com.motorola.bluetoothle.IBluetoothGattCallback;

public class BLETemperatureSensor extends BLESensor {

	/** Ambient temperature */
	private float m_ambient_temperature;
	/** Object temperature */
	private float m_object_temperature;

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
	 */
	public BLETemperatureSensor(BLESensorBoard sb, BluetoothGatt btgatt,
			BluetoothDevice bledevice, String uuid, Context ctx) {

		super(sb, btgatt, bledevice, uuid,ctx);
		m_handles = new String[3];
		READ_HANDLE_INDEX = 0;
		CONF_HANDLE_INDEX = 1;
		m_handles[READ_HANDLE_INDEX] = "0022";
		m_handles[CONF_HANDLE_INDEX] = "0026";
		m_ambient_temperature = 0.0f;
		m_callback = new callback();
		SENSOR_ID = BLESensorBoard.SENSOR_TEMPERATURE;
		m_sensorname = "temperature sensor";
		TAG = "BLETemperatureSensor";

	}

	private class callback extends IBluetoothGattCallback.Stub {

		public void notificationGattCb(BluetoothDevice dev, String uuid,
				String char_handle, byte[] data) {
			Log.d(TAG, "Got notification");
			// This calculation is a little bit nasty - see TMP006 documentation

			int amb_data = (data[3] << 8) & 0xFF00;
			amb_data |= (data[2] & 0xFF);
			amb_data = amb_data >> 2;

			m_ambient_temperature = (float) amb_data / 32;
			double t_die = m_ambient_temperature + 273.15f;
			double t_ref = 298.15f;
			double b_0 = -0.0000294f;
			double b_1 = -0.00000057f;
			double b_2 = 0.00000000463f;
			double c_2 = 13.4f;
			double v_obj = (data[1] << 8 | data[0]) * 0.00000015625;
			double s_0 = 0.000000000000064f;
			double a_1 = 0.00175f;
			double a_2 = -0.00001678f;
			double s = s_0
					* (1 + a_1 * (t_die - t_ref) + a_2
							* Math.pow((t_die - t_ref), 2));
			double v_os = b_0 + b_1 * (t_die - t_ref) + b_2
					* Math.pow((t_die - t_ref), 2);
			double f_obj = (v_obj - v_os) + c_2 * Math.pow(v_obj - v_os, 2);
			double tmp1 = Math.pow(t_die, 4);
			double tmp2 = (f_obj / s);
			double tmp3 = Math.sqrt(tmp1 + tmp2);

			m_object_temperature = (float) (Math.sqrt(tmp3) - 273.15f);
			m_sb.onTemperatureData(m_ambient_temperature, m_object_temperature);

		}

		public void indicationGattCb(BluetoothDevice device, String uuid,
				String char_handle, String[] data) throws RemoteException {
			// TODO Auto-generated method stub

		}
	};

}
