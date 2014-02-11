package com.ti.lprf;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.RemoteException;
import android.util.Log;
import com.motorola.bluetoothle.BluetoothGatt;
import com.motorola.bluetoothle.IBluetoothGattCallback;

public class BLEPressureSensor extends BLESensor {

	/** Pressure */
	private float m_pressure;
	private float m_temperature;
	protected int CALIBRATION_HANDLE_INDEX = 11;
	
	/** Time to wait between each poll */
	private int m_poll_period = 15000;
	/** Write request for polling thread */

	

	/** Whether or not to do the actual polling (i.e. sensor enabled or not) */
	private boolean m_dopolling = false;
	
	
	/** Thread that periodically writes config handle to get notification */
	private Thread m_pollingthread = new Thread(){
		public void run(){
			while(true){
				if(m_dopolling){
					byte d[] = {0x01};
					if(m_dopolling){
						Log.d(TAG, "Polling..");
						write(m_handles[CONF_HANDLE_INDEX],d,false, "Polling " + m_sensorname);
					}
				}
				try {
					sleep(m_poll_period);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}
	};
	
	
	private int m_caliValues[] = null;
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
	public BLEPressureSensor(BLESensorBoard sb, BluetoothGatt btgatt,
			BluetoothDevice bledevice, String uuid, Context ctx) {
		super(sb, btgatt, bledevice, uuid,ctx);
		// Set default handles
		m_handles = new String[13];
		READ_HANDLE_INDEX = 10;
		CONF_HANDLE_INDEX = 12;
		m_handles[READ_HANDLE_INDEX] = "0048";
		m_handles[CONF_HANDLE_INDEX] = "0050";
		m_handles[CALIBRATION_HANDLE_INDEX] = "004C";

		m_pressure = 0.0f;
		m_temperature = 0.0f;
		m_callback = new callback();
		SENSOR_ID = BLESensorBoard.SENSOR_PRESSURE;
		TAG = "BLEPressureSensor";
		m_sensorname = "pressure sensor";
		m_pollingthread.start();

	}

	public void startSensor() {
		// Read out calibration values
		byte caliNoti[]  = {0x01, 0x00 };
		write(m_handles[CALIBRATION_HANDLE_INDEX],caliNoti,true, "Enabling notifications for calibration for " + m_sensorname);
		byte caliData[] = {0x02};
		write(m_handles[CONF_HANDLE_INDEX],caliData,false, "Reading calibration data for " + m_sensorname);
		
		// Enable notification for read handle
		byte data2[] = {0x01, 0x00};
		write(m_handles[READ_HANDLE_INDEX], data2, true, "Enabling notifications for " + m_sensorname);
		
		m_dopolling = true;
	}
	
	public void stopSensor(){
		m_dopolling = false;
	}
	/**
	 * This sensor has a separate calibration data handle that has to be read out before operation
	 * @return The calibration handle
	 */
	public String getCaliHandle() {
		return m_handles[CALIBRATION_HANDLE_INDEX];
	};

	/** Callback class to receive notifications */
	private class callback extends IBluetoothGattCallback.Stub {

		
		public void notificationGattCb(BluetoothDevice dev, String uuid,
				String char_handle, byte[] data) {
			
			// A lot of blood has been spilled trying to get this right.
			
			Log.d(TAG, "Got notification on handle " + char_handle);
			Log.d(TAG, "Comparing to calibration handle, which is " + m_handles[CALIBRATION_HANDLE_INDEX]);
			if(char_handle.toLowerCase().equals(m_handles[CALIBRATION_HANDLE_INDEX].toLowerCase())){
				// Got calibration data
				Log.d(TAG, "Calibration raw data");
				int t[] = new int[16];
				for(int i = 0; i < 16; i++) {
					Log.d(TAG,"Value " + i + " " + String.format("%x",data[i]));
					t[i] = ((int)data[i]) & 0x000000FF;
				}
				m_caliValues = new int[8];
				// These are unsigned
				m_caliValues[0] = ((t[1] << 8) | t[0]) & 0xFFFF;
				m_caliValues[1] = ((t[3] << 8) | t[2]) & 0xFFFF;
				m_caliValues[2] = ((t[5] << 8) | t[4]) & 0xFFFF;
				m_caliValues[3] = ((t[7] << 8) | t[6]) & 0xFFFF;
				// These three are signed!
				m_caliValues[4] = ((data[9] << 8) | t[8]);
				m_caliValues[5] = ((data[11] << 8) | t[10]);
				m_caliValues[6] = ((data[13] << 8) | t[12]);
				// And unsigned.
				m_caliValues[7] = ((t[15] << 8) | t[14]) & 0xFFFF;

				Log.d(TAG,"Calibrated!");
				
				Log.d(TAG, "Values are");
				for(int i = 0; i < 6; i++) {
					Log.d(TAG, String.format("%x %x %x %x %x %x %x %x",
							m_caliValues[0],
							m_caliValues[1],
							m_caliValues[2],
							m_caliValues[3],
							m_caliValues[4],
							m_caliValues[5],
							m_caliValues[6],
							m_caliValues[7]
							));
				}
				return;
			}
			if(m_caliValues == null) {
				Log.d(TAG, "Got data, but don't have calibration values");
				return;
			}
			
			// Normal data
			Log.d(TAG, "Got raw data: ");
			for(int i = 0; i < 4; i++) {
				Log.d(TAG,String.format("%x",data[i]));
			}
			
			
			// T_r is signed
			int temp_data = (data[1] << 8) + (((int)data[0]) & 0x000000FF);
			
			// Pressure is unsigned
			int pressure_data = (((((int)data[3]) & 0x000000FF) << 8) | ((int)data[2]) & 0x000000FF) & 0xFFFF;
			Log.d(TAG, "Got data: t_r = " + String.format("%x",temp_data) + ", p_r = " + String.format("%x", pressure_data));

			// Cut'n'paste from data sheet (c953h_v1 0 a1.pdf, dated 20 Dec 2011, Version 1.0.A1)
			double t_a = ((double)m_caliValues[0]*temp_data)/(double)16777216
					+	((double)m_caliValues[1])/(double)1024;
			
			double S = m_caliValues[2] 
				+	(double)m_caliValues[3]*(double)temp_data/131072.0
				+ (double)temp_data*temp_data*m_caliValues[4]/17179869184.0;
	
			double O = m_caliValues[5]*16384 
					+ m_caliValues[6] * (double)temp_data / 8.0
					+ m_caliValues[7] * (double)temp_data*temp_data /524288.0;

					
			
			double p_a = ((S*pressure_data) + O)/16384.0;
			
			m_pressure = (float) p_a;
			m_temperature = (float) (t_a);
			
			
			Log.d(TAG,"S: " + S);
			Log.d(TAG,"O: " + O);
			Log.d(TAG, "Temperature: " + m_temperature + " Pressure: " + m_pressure);
			
			m_sb.onPressureData(m_temperature, m_pressure);
		}

		public void indicationGattCb(BluetoothDevice device, String uuid,
				String char_handle, String[] data) throws RemoteException {

		}
	}
	
}
