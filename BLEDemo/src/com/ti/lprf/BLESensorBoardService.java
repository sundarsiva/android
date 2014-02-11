package com.ti.lprf;

import java.util.ArrayList;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class BLESensorBoardService extends Service implements BLESensorBoard.SensorListener {
	
	
	// Message codes
	/** Register client to service. Supply the Messenger to receive messages in msg.replyTo */
	public static final int MSG_REGISTER_CLIENT = 1;
	/** Unregister client from service. Supply the Messenger to unregister in msg.replyTo */
	public static final int MSG_UNREGISTER_CLIENT = 2;
	/** Enable sensor. Supply sensorid in msg.arg1 */
	public static final int MSG_ENABLE_SENSOR = 3;
	/** Disable sensor. Supply sensorid in msg.arg1 */
	public static final int MSG_DISABLE_SENSOR = 4;
	/** Connect to sensor board. Supply HW addr in bundle with ID MSG_ID_HWADDR */
	public static final int MSG_CONNECT_SENSOR_BOARD = 5;
	/** Disconnect from sensor board */
	public static final int MSG_DISCONNECT_SENSOR_BOARD = 6;
	
	
	/** Message has data from temperature sensor.
	 * ID: MSG_ID_TEMP_OBJECT - Object temperature (float)
	 * ID: MSG_ID_TEMP_AMBIENT - Ambient temperature (float)
	 */
	public static final int MSG_SENSOR_TEMPERATURE = 10;
	
	/** Message has data from humidity sensor.
	 * ID: MSG_ID_HUMIDITY_TEMP - Ambient temperature (float)
	 * ID: MSG_ID_HUMIDITY - Relative humidity (float)
	 */
	public static final int MSG_SENSOR_HUMIDITY = 11;
	
	/** Message has data from accelerometer.
	 * ID: MSG_ID_XCOORD - X acceleration (float)
	 * ID: MSG_ID_YCOORD - Y acceleration (float)
	 * ID: MSG_ID_ZCOORD - Z acceleration (float)
	 */
	public static final int MSG_SENSOR_ACCELEROMETER = 12;
	
	/** Message has data from gyro
	 * ID: MSG_ID_GYRO - Gyro value (float)
	 */
	public static final int MSG_SENSOR_GYRO = 13;
	
	/** Message has data from pressure sensor
	 * ID: MSG_ID_PRESSURE - Pressure value (float)
	 */
	public static final int MSG_SENSOR_PRESSURE = 14;
	
	/** Message has data from magnetometer.
	 * ID: MSG_ID_XCOORD - X acceleration (float)
	 * ID: MSG_ID_YCOORD - Y acceleration (float)
	 * ID: MSG_ID_ZCOORD - Z acceleration (float)
	 */
	public static final int MSG_SENSOR_MAGNETOMETER = 15;

	/** Message is a status update message.
	 * ID: MSG_ID_OLDSTATUS - Old status (int)
	 * ID: MSG_ID_NEWSTATUS - New status (int)
	 * ID: MSG_ID_SENSOR - Sensor ID (int)
	 */
	public static final int MSG_STATUS = 20;
	
	/** Set timing for a sensor
	 * msg.arg1 == Sensor ID
	 * msg.arg2 == timing parameter (see BLESensorBoard.setTiming())
	 */
	public static final int MSG_SET_TIMING = 21;
	
	/** Calibrate a sensor
	 * When sending, arg1 == sensor. When receiving
	 * ID: MSG_ID_SENSOR: sensor
	 * ID: MSG_ID_STEP: step 
	 * ID: MSG_ID_TOTAL: Number of steps in total
	 * 
	 */
	public static final int MSG_CALIBRATION = 22;
	/**
	 * Write message
	 * A sensor is being written
	 * ID: MSG_ID_WHAT gives a user string about what.
	 */
	public static final int MSG_WRITE = 23;
	
	
	// String constants for fields in Bundle 
	/** String constant for bluetooth address in Bundle */
	public static final String MSG_ID_HWADDR = "ID_HWADDR";
	
	/** String constant for X coordinate in Bundle */
	public static final String MSG_ID_XCOORD = "ID_XCOORD";
	/** String constant for Y coordinatein Bundle */
	public static final String MSG_ID_YCOORD = "ID_YCOORD";
	/** String constant for Z coordinatein Bundle */
	public static final String MSG_ID_ZCOORD = "ID_ZCOORD";
	
	/** String constant for object temperature in Bundle */
	public static final String MSG_ID_TEMP_OBJECT = "ID_TEMPOBJ";
	/** String constant for ambient temperature in Bundle */
	public static final String MSG_ID_TEMP_AMBIENT = "ID_TEMPAMB";
	
	/** String constant for ambient temperature from humidity sensor in Bundle */
	public static final String MSG_ID_HUMIDITY_TEMP = "ID_HUMTEMP";
	/** String constant for humidity in Bundle */
	public static final String MSG_ID_HUMIDITY = "ID_HUMIDITY";
	
	/** String constant for gyro data in Bundle */
	public static final String MSG_ID_GYRO = "ID_GYRO";
	
	/** String constant for pressure data in Bundle */
	public static final String MSG_ID_PRESSURE = "ID_PRESSURE";
	/** String constant for accessing pressure sensor temperature data in Bundle */
	public static final String MSG_ID_PRESSURE_TEMP = "ID_PRESSURE_TEMP";

	
	/** String constant for old status in Bundle */
	public static final String MSG_ID_OLDSTATUS = "ID_OLDSTATUS";
	/** String constant for new status in Bundle */
	public static final String MSG_ID_NEWSTATUS = "ID_NEWTATUS";
	/** String constant for sensor ID in Bundle */
	public static final String MSG_ID_SENSOR = "ID_SENSOR";
	/** String constant for step(calibration) in Bundle */
	public static final String MSG_ID_STEP = "ID_STEP";
	/** String constant for total (calibration) in Bundle */
	public static final String MSG_ID_TOTAL = "ID_TOTAL";
	/** String constant for user message when writing */
	public static final String MSG_ID_WHAT = "ID_WHAT";
	
	/** Tag for logging */
	private static final String TAG = "BLESensorBoardService";
	
	
	/** Handler to handle incoming messages */
	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			Log.d(TAG,"Got message " + msg.what);

			switch(msg.what){
			// Register new client
			case MSG_REGISTER_CLIENT:
				m_clients.add(msg.replyTo);
			break;
			// Unregister client
			case MSG_UNREGISTER_CLIENT:
				m_clients.remove(msg.replyTo);
			break;
			// Enable sensor
			case MSG_ENABLE_SENSOR:
				Log.d(TAG, "Enable sensor " + msg.arg1);
				m_sensorboard.enableSensor(msg.arg1);
			break;
			// Disable sensor
			case MSG_DISABLE_SENSOR:
				m_sensorboard.disableSensor(msg.arg1);
				break;
			// Connect to sensorboard
			case MSG_CONNECT_SENSOR_BOARD:
				if(!m_sensorboard.isConnected()) { 
					m_sensorboard.connect(msg.getData().getString(MSG_ID_HWADDR));
				}
			break;
			// Disconnect from sensorboard
			case MSG_DISCONNECT_SENSOR_BOARD:
				m_sensorboard.disconnect();
			break;
			// Set timing
			case MSG_SET_TIMING:
				m_sensorboard.setTiming(msg.arg1, msg.arg2);
				break;
			case MSG_CALIBRATION:
				// Initiate calibration
				m_sensorboard.calibrate(msg.arg1);
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}
	
	/** The messenger we listen on */
	private final Messenger m_messenger = new Messenger(new IncomingHandler());
	/** The sensor board */
	private BLESensorBoard m_sensorboard;
	/** Our clients */
	ArrayList<Messenger> m_clients = new ArrayList<Messenger>();
	
	
	@Override
	public void onCreate() {
		Log.d(TAG,"Creating service");
		m_sensorboard = new BLESensorBoard(this,BluetoothAdapter.getDefaultAdapter());
		m_sensorboard.addListener(this);
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		Log.d(TAG,"Binding service");
		return m_messenger.getBinder();
	}
	@Override
	public void onDestroy() {
		Log.d(TAG,"Destroying service");
		m_sensorboard.disconnect();
		m_sensorboard.unregisterReceivers();
		super.onDestroy();
	}
	/**
	 * Send message to all clients
	 * @param id Message code 
	 * @param data Data to send
	 */
	private void broadcastMessage(int id, Bundle data) {
		
		for(Messenger m: m_clients) {
			Message msg = Message.obtain(null,id);
			msg.setData(data);
			try {
				m.send(msg);
			} catch (RemoteException e) {
				// Dead client, remove
				m_clients.remove(m);
				e.printStackTrace();
			}
		}
	}
	
	public void onAccelData(float x, float y, float z) {
		Bundle b = new Bundle();
		b.putFloat(MSG_ID_XCOORD, x);
		b.putFloat(MSG_ID_YCOORD, y);
		b.putFloat(MSG_ID_ZCOORD, z);
		broadcastMessage(MSG_SENSOR_ACCELEROMETER, b);
	}
	
	public void onTemperatureData(float ambient_temp, float object_temp) {
		Bundle b = new Bundle();
		b.putFloat(MSG_ID_TEMP_AMBIENT, ambient_temp);
		b.putFloat(MSG_ID_TEMP_OBJECT, object_temp);
		
		broadcastMessage(MSG_SENSOR_TEMPERATURE, b);
		
	}
	public void onHumidityData(float temp, float humidity) {
		Bundle b = new Bundle();
		b.putFloat(MSG_ID_HUMIDITY_TEMP, temp);
		b.putFloat(MSG_ID_HUMIDITY, humidity);
		broadcastMessage(MSG_SENSOR_HUMIDITY, b);
	}
	
	public void onGyroData(float data) {
		Bundle b = new Bundle();
		b.putFloat(MSG_ID_GYRO, data);
		broadcastMessage(MSG_SENSOR_GYRO,b);
		
	}
	
	public void onPressureData(float temperature, float pressure) {
		Bundle b = new Bundle();
		b.putFloat(MSG_ID_PRESSURE, pressure);
		b.putFloat(MSG_ID_PRESSURE_TEMP, temperature);
		broadcastMessage(MSG_SENSOR_PRESSURE, b);
		
	}
	
	public void onMagnetData(float x, float y, float z) {
		Bundle b = new Bundle();
		b.putFloat(MSG_ID_XCOORD, x);
		b.putFloat(MSG_ID_YCOORD, y);
		b.putFloat(MSG_ID_ZCOORD, z);
		broadcastMessage(MSG_SENSOR_MAGNETOMETER, b);
	}

	public void onStatusChanged(int sensorid, int oldstatus, int newstatus) {
		Bundle b = new Bundle();
		b.putInt(MSG_ID_SENSOR, sensorid);
		b.putInt(MSG_ID_OLDSTATUS, oldstatus);
		b.putInt(MSG_ID_NEWSTATUS,newstatus);
		broadcastMessage(MSG_STATUS,b);
		
	}

	public void onCalibrationData(int sensorid, int step, int total) {
		Bundle b = new Bundle();
		b.putInt(MSG_ID_SENSOR, sensorid);
		b.putInt(MSG_ID_STEP, step);
		b.putInt(MSG_ID_TOTAL, total);
		broadcastMessage(MSG_CALIBRATION,b);
	}
	public void onWrite(String what){
		Bundle b = new Bundle();
		b.putString(MSG_ID_WHAT, what);
		broadcastMessage(MSG_WRITE, b);
	}
}
