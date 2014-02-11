package com.ti.lprf;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.RemoteException;
import android.util.Log;
import com.motorola.bluetoothle.BluetoothGatt;
import com.motorola.bluetoothle.IBluetoothGattCallback;

public class BLEHumiditySensor extends BLESensor {

	/** Temperature in celcius */
	private float m_temperature;
	/** Humidity in percent */
	private float m_humidity;
	
	/** Time to wait between each poll */
	private int m_poll_period = 15000;
	/** Write request for polling thread */
	private BLEWriteRequest m_writerequest;
	
	
	

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
	public BLEHumiditySensor(BLESensorBoard sb, BluetoothGatt btgatt,
			BluetoothDevice bledevice, String uuid, Context ctx) {
		super(sb, btgatt, bledevice, uuid,ctx);
		// Set default handles
		m_handles = new String[7];
		READ_HANDLE_INDEX = 5;
		CONF_HANDLE_INDEX = 6;
		
		m_handles[READ_HANDLE_INDEX] = "0035";
		m_handles[CONF_HANDLE_INDEX] = "0039";
		m_temperature = 0.0f;
		m_humidity = 0.0f;
		m_callback = new callback();
		SENSOR_ID = BLESensorBoard.SENSOR_HUMIDITY;
		TAG = "BLEHumiditySensor";
		m_sensorname = "humidity sensor";
		byte[] d = {0x01};
		m_writerequest = new BLEWriteRequest(btgatt,bledevice, uuid,m_handles[CONF_HANDLE_INDEX],d,1,false, null, null);
		m_pollingthread.start();
		
	}


	
	@Override
	public void startSensor() {
		super.startSensor();
		m_dopolling = true;
		
	}
	
	@Override
	public void stopSensor(){
		m_dopolling = false;
		super.stopSensor();
	}
	
	@Override
	protected void setTiming(int timing) {
		//m_poll_period = timing;
	}
	
	
	
	/** Process data */
	private void onData(byte[] data){
		int b0 = data[0];
		int b1 = data[1];
		int b2 = data[2];
		int b3 = data[3];
		
		// Both fields are unsigned.
		int tempdata = (((b1 & 0x000000FF)<<8) & 0xFF00) | b0 & 0x000000FF;
		tempdata = tempdata & 0x0000FFFF;
		int humiddata = (((b3 & 0x000000FF)<<8) & 0xFF00) | b2 & 0x000000FF;
		humiddata = humiddata & 0x0000FFFF;
		
		m_temperature = 175.72f*((float)tempdata/65536.0f)-46.85f;
		m_humidity = 125.0f*(float)humiddata/65536.0f-6.0f;
		
		m_sb.onHumidityData(m_temperature, m_humidity);
		
		Log.d(TAG, "Temp: " + m_temperature + " Humidity: " + m_humidity);

	}
	
	
	/** Callback class to receive notifications.
	 * We won't be receiving notification for this type of sensor, but if m_callback is null
	 * strange things might happen */
	private class callback extends IBluetoothGattCallback.Stub {

		public void notificationGattCb(BluetoothDevice dev, String uuid,
				String char_handle, byte[] data) {
			onData(data);
		
		}

		public void indicationGattCb(BluetoothDevice device, String uuid,
				String char_handle, String[] data) throws RemoteException {

		}
	};

}
