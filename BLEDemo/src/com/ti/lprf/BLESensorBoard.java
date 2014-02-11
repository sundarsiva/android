package com.ti.lprf;

import java.util.ArrayList;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import com.motorola.bluetoothle.*;

public class BLESensorBoard {

	/** Temperature sensor ID */
	public final static int SENSOR_TEMPERATURE = 0;
	/** Accelerometer sensor ID */
	public final static int SENSOR_ACCELEROMETER = 1;
	/** Gyro sensor ID */
	public final static int SENSOR_GYRO = 2;
	/** Humidity sensor ID */
	public final static int SENSOR_HUMIDITY = 3;
	/** Pressure sensor ID */
	public final static int SENSOR_PRESSURE = 4;
	/** Magnetometer sensor ID */
	public final static int SENSOR_MAGNET = 5;

	/** Highest sensor ID */
	public final int SENSORID_MAX = 5;

	/** Board disconnected */
	protected static final int STATUS_DISCONNECTED = 0;
	/** Board connecting */
	protected static final int STATUS_CONNECTING = 1;
	/** Connected, but not running */
	protected static final int STATUS_CONNECTED = 2;
	/** Board running (you should now expect notifications */
	protected static final int STATUS_RUNNING = 3;
	/** Some error */
	protected static final int STATUS_ERROR = 4;

	/** Our current status */
	protected int m_status;

	/** Messages for the status codes */
	protected static final String[] STATUS_MSGS = { "disconnected",
			"connecting", "connected", "running", "error" };

	/**
	 * SensorListener interface. Interface to facilitate listening to a
	 * BLESensorBoard.
	 * 
	 * @author x0138327
	 * 
	 */
	public interface SensorListener {
		/**
		 * Called when accelerometer data arrives
		 * 
		 * @param x
		 *            X acceleration in (m/s)^2
		 * @param y
		 *            Y acceleration in (m/s)^2
		 * @param z
		 *            Z acceleration in (m/s)^2
		 */
		void onAccelData(float x, float y, float z);

		/**
		 * Called when infrared temperature sensor data arrives
		 * 
		 * @param ambient_temp
		 *            Ambient temperature in celcius
		 * @param object_temp
		 *            Object temperature in celcius
		 */
		void onTemperatureData(float ambient_temp, float object_temp);

		/**
		 * Called when humidity sensor data arrives
		 * 
		 * @param temp
		 *            Ambient temperature in celcius
		 * @param humidity
		 *            Humidity (0.0f-1.0f)
		 */
		void onHumidityData(float temp, float humidity);

		/**
		 * Called when gyro data arrives
		 * 
		 * @param data
		 *            No idea.
		 */
		void onGyroData(float data);

		/**
		 * Called when pressure data arrives
		 * 
		 * @param temperature
		 * 			  Temperature in degrees celcius
		 * @param pressure
		 *            Pressure in hPa
		 */
		void onPressureData(float temperature, float pressure);

		/**
		 * Called when magnetometer data arrives
		 * 
		 * @param x
		 *            field strength in x direction
		 * @param y
		 *            field strength in y direction
		 * @param z
		 *            field strength in z direction
		 */
		void onMagnetData(float x, float y, float z);

		/**
		 * Called when status changes for some sensor
		 * 
		 * @param sensorid
		 *            The sensor for which the status changed
		 * @param oldstatus
		 *            The old status
		 * @param newstatus
		 *            The new status
		 */
		void onStatusChanged(int sensorid, int oldstatus, int newstatus);
		
		/**
		 * Called during calibration
		 * 
		 * @param sensorid
		 *            The sensor being calibrated
		 * @param step
		 *            the step we're at
		 * @param total
		 *            total number of steps
		 */
		void onCalibrationData(int sensorid, int step, int total);

		/**
		 * Called when data is written
		 * @param what Message to present to user
		 */
		void onWrite(String what);
		
	}

	/** Whether or not we're connected */
	private boolean m_isconnected = false;

	/** The UUID we're connecting to */
	protected String m_uuid = "0000f0b0-0000-1000-8000-00805f9b34fb";

	/** The handles of the UUID we're connecting to */
	protected String[] m_handles;

	/** Whether or not the phone supports BLE */
	private boolean m_supportsble;

	/**
	 * Array telling what sensors are connected (i.e. which sensors are we
	 * reading data from). Indexed by sensorid
	 */
	private boolean[] m_sensorconnected = { false, false, false, false, false,
			false, };

	/** Bluetooth adapter */
	private BluetoothAdapter m_btadapter = null;
	/** Bluetooth address of sensor board */
	private String m_btaddr;

	/** Array of sensorlisteners */
	private ArrayList<SensorListener> m_listeners = null;

	/** Name of BluetoothGattService class, used to check if BLE is supported */
	private static final String className = "android.server.BluetoothGattService";

	/** BluetoothDevice representing the SensorBoard */
	private BluetoothDevice m_bledevice;

	/** BluetoothGatt instance to do BLE stuff */
	private BluetoothGatt m_btgatt;

	/** Sensor values */
	private float m_accelx = 0;
	private float m_accely = 0;
	private float m_accelz = 0;
	private float m_gyroval = 0;
	private float m_ambient_temperature = 0;
	private float m_object_temperature = 0;
	private float m_humidityval = 0;
	private float m_hum_tempval = 0;
	private float m_pressureval = 0;
	private float m_pressure_temp = 0;
	private float m_magnetx = 0;
	private float m_magnety = 0;
	private float m_magnetz = 0;

	/** The callback to use for connection. See documentation below */
	private callback m_callback = new callback();

	/** Temperature sensor */
	private BLETemperatureSensor m_tempsensor;
	/** Accelerometer */
	private BLEAccelerometer m_accelerometer;
	/** Magnetometer */
	private BLEMagnetometer m_magnetometer;
	/** Humidity sensor */
	private BLEHumiditySensor m_humiditysensor;
	/** Pressure sensor */
	private BLEPressureSensor m_pressuresensor;

	/** Tag for logging */
	private static final String TAG = "BLESensorBoard";

	/** Context, needed for BLE and progress dialogs */
	private Context m_context;
	/** BroadcastReceiver to take care of intents that BluetoothGatt sends. */
	protected final BroadcastReceiver m_broadcastreceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context ctx, Intent intent) {
			String action = intent.getAction();
			Bundle extras = intent.getExtras();

			Log.d(TAG, "Got (pre-filter) intent " + action);

			if (!(extras.get(BluetoothDevice.EXTRA_DEVICE).equals(m_bledevice) && extras
					.get(BluetoothGatt.EXTRA_PRIMARY_UUID).equals(m_uuid)))
				return;
			Log.d(TAG, "Got Intent " + action);

			if (BluetoothGatt.ACTION_GATT_CONNECTED.equals(action)) {
				if (extras.getBoolean(BluetoothGatt.EXTRA_CONNECT_RESULT)) {
					// We're happily connected. Now read characteristics
					setStatus(STATUS_CONNECTED);
					onWrite("Connected");
					synchronized(BLESensorBoard.this){
						m_isconnected = true;
					}
					m_btgatt.readGattCharacteristics(m_bledevice, m_uuid);
				} else {
					setStatus(STATUS_ERROR);
				}
			}

			if (BluetoothGatt.ACTION_GATT_DISCONNECTED.equals(action)) {
				if (extras.getBoolean(BluetoothGatt.EXTRA_CONNECT_RESULT)) {
					setStatus(STATUS_DISCONNECTED);
					// We're disconnected
					synchronized(BLESensorBoard.this){
						m_isconnected = false;
						BLESensor.m_writequeue.clear();
					}
				} else {
					setStatus(STATUS_ERROR);
				}
			}

			if (BluetoothGatt.ACTION_GATT_CHARACTERISTICS_GET.equals(action)) {
				Log.d(TAG, "Got characteristics");
				String[] handles;
				handles = extras
						.getStringArray(BluetoothGatt.EXTRA_CHARCATERISTICS_HANDLE);

				setHandles(handles);
				// Now start any sensors that were previously enableSensor()-ed
				BLESensor s;
				for (int i = 0; i <= SENSORID_MAX; i++) {
					if (m_sensorconnected[i]) {
						s = getSensor(i);
						if(s != null) s.startSensor();
					}
				}

			}
			
			  if(BluetoothGatt.ACTION_GATT_CHARACTERISTICS_WRITE.equals(action))
			  { 
				  // We never get these :(
				  Log.d(TAG, "Write complete");
			  
			  }
			 
			if (BluetoothGatt.ACTION_GATT_CHARACTERISTICS_READ.equals(action)) {
				// Not really in use, but here for debugging purposes
				String c = extras.getString(BluetoothGatt.EXTRA_CHAR_HANDLE);
				String d[] = extras
						.getStringArray(BluetoothGatt.EXTRA_CHARCATERISTIC_READ_VALUE);
				Log.d(TAG, "Read characteristic " + c + ", got ");
				for (String data : d) {
					Log.d(TAG, data);
				}
			}
		}
	};
	
	/**
	 * Constructor
	 * @param ctx Context (to listen for intents)
	 * @param btadapter System bluetooth adapter
	 */
	public BLESensorBoard(Context ctx, BluetoothAdapter btadapter) {
		m_btadapter = btadapter;
		m_listeners = new ArrayList<SensorListener>();
		m_context = ctx;
		// Check for BLE support
		try {
			Class<?> object = Class.forName(className);
			m_supportsble = true;

		} catch (Exception e) {
			m_supportsble = false;
		} 
		
		if (m_supportsble) {
			m_btgatt = new BluetoothGatt(ctx);
			
		
			
			// Start BLE
			Intent startBLEintent = new Intent(BluetoothGatt.ACTION_START_LE);
			startBLEintent.putExtra(BluetoothGatt.EXTRA_PRIMARY_UUID, m_uuid);
			ctx.sendBroadcast(startBLEintent);

			// Start listening to BluetoothGatt Intents
			IntentFilter filter = new IntentFilter();
			filter.addAction(BluetoothGatt.ACTION_GATT_CHARACTERISTICS_GET);
			filter.addAction(BluetoothGatt.ACTION_GATT_CONNECTED);
			filter.addAction(BluetoothGatt.ACTION_GATT_DISCONNECTED);
			filter.addAction(BluetoothGatt.ACTION_GATT_CHARACTERISTICS_READ);
			filter.addAction(BluetoothGatt.ACTION_GATT_CHARACTERISTICS_WRITE);


			ctx.registerReceiver(m_broadcastreceiver, filter);
		}
	}

	protected void finalize() throws Throwable {
		unregisterReceivers();
		super.finalize();
	}
	public void unregisterReceivers(){
		m_context.unregisterReceiver(m_broadcastreceiver);
	}
	
	/**
	 * Set handles (as returned from readGattCharacteristics
	 * @param handles New handles
	 */
	protected void setHandles(String[] handles) {
		m_handles = handles;
		m_tempsensor.setHandles(handles);
		m_accelerometer.setHandles(handles);
		m_pressuresensor.setHandles(handles);
		m_magnetometer.setHandles(handles);
		m_humiditysensor.setHandles(handles);
	}
	
	/**
	 * Set status
	 * @param status new status
	 */
	protected void setStatus(int status) {
		Log.d(TAG, "Status: " + STATUS_MSGS[status]);
		int oldstatus = m_status;
		m_status = status;
		onStatusChanged(-1, oldstatus, m_status);
	}

	/**
	 * Connect to BLE device
	 * @param hwaddr Bluetooth address to connect to
	 */
	public void connect(String hwaddr) {
		Log.d(TAG, "connect(" + hwaddr + ")");
		
		m_btaddr = hwaddr;
		m_bledevice = m_btadapter.getRemoteDevice(hwaddr);
		
		// Now that we know the BLEdevice, instantiate sensors
		// Instantiate sensors
		m_tempsensor = new BLETemperatureSensor(this, m_btgatt, m_bledevice,
				m_uuid, m_context);
		m_accelerometer = new BLEAccelerometer(this, m_btgatt, m_bledevice,
				m_uuid, m_context);
		m_pressuresensor = new BLEPressureSensor(this, m_btgatt, m_bledevice,
				m_uuid, m_context);
		m_magnetometer = new BLEMagnetometer(this, m_btgatt, m_bledevice,
				m_uuid, m_context);
		m_humiditysensor = new BLEHumiditySensor(this, m_btgatt, m_bledevice,
				m_uuid, m_context);
		
		// Retrieve primary services
		String s[] = m_btgatt.getGattPrimaryServices(m_bledevice);
		Log.d(TAG, "Got primary services");
		for (String svc : s) {
			Log.d(TAG, svc);
		}
		// Use UUID from getGattPrimaryServices
		 m_uuid = s[3];
		// Connect to GATT server
		m_btgatt.connectGatt(m_bledevice, m_uuid, m_callback);

		if (m_bledevice == null)
			return;

	}
	
	/** 
	 * Set the timing parameter for a sensor. This is only supported for accelerometer and 
	 * magnetometer
	 * @param sensorId Sensor ID
	 * @param timing Notifications will be received at (50ms + 10*timing ms)
	 */
	public void setTiming(int sensorId, int timing){
		BLESensor s = getSensor(sensorId);
		if(s == null) return;
		s.setTiming(timing);
	}
	
	/** 
	 * Helper function to retrieve sensor based on sensorId
	 * @param sensorId
	 * @return the BLESensor
	 */
	protected BLESensor getSensor(int sensorId){
		switch(sensorId){
		case SENSOR_TEMPERATURE:
			return m_tempsensor;
			
		case SENSOR_ACCELEROMETER:
			return m_accelerometer;
		case SENSOR_MAGNET:
			return m_magnetometer;
		case SENSOR_HUMIDITY:
			return m_humiditysensor;
		case SENSOR_PRESSURE:
			return m_pressuresensor;
			
		}
		return null;
	}

	/**
	 * Enable sensor
	 * @param sensorId Sensor to enable
	 * @return true if successful, false otherwise
	 */
	public synchronized boolean enableSensor(int sensorId) {
		if (sensorId < 0 || sensorId > SENSORID_MAX || !m_supportsble)
			return false;
	
		Log.d(TAG, "EnableSensor " + sensorId);

		// Get sensor
		BLESensor s = getSensor(sensorId);
		if(s == null) return false;
		// If we're not connected, don't start it. Then it will be started by the 
		// BroadcastReceiver when receiving characteristics.
		if(m_isconnected) {
			s.startSensor();
		}
		m_sensorconnected[sensorId] = true;
		return true;
	}

	/**
	 * Disable sensor
	 * @param sensorId Sensor to disable
	 * @return true if successful, false otherwise
	 */
	public boolean disableSensor(int sensorId) {
		if (sensorId < 0 || sensorId > SENSORID_MAX || !m_supportsble)
			return false;
		// Don't disconnect if it ain't connected
		if (!m_sensorconnected[sensorId])
			return false;
		
		BLESensor s = getSensor(sensorId);
		if(s == null) return false;
		
		s.stopSensor();

		m_sensorconnected[sensorId] = false;
		return true;

	}

	/**
	 * Disconnect
	 */
	public void disconnect() {
		m_btaddr = null;
		for (int i = 0; i <= SENSORID_MAX; i++) {
			disableSensor(i);

		}
		
		// Add disconnect request to write queue
		BLESensor.disconnectGatt(m_btgatt, m_bledevice, m_uuid);
		
	}

	

	/** Get sensor by the handle - useful for determining which sensor a notification came from */
	public int getSensorByHandle(String handle) {
		if (handle.equals(m_accelerometer.getReadHandle()))
			return SENSOR_ACCELEROMETER;
		if (handle.equals(m_pressuresensor.getReadHandle()) || handle.equals(m_pressuresensor.getCaliHandle()))
			return SENSOR_PRESSURE;
		if (handle.equals(m_tempsensor.getReadHandle()))
			return SENSOR_TEMPERATURE;
		if(handle.equals(m_magnetometer.getReadHandle()))
			return SENSOR_MAGNET;
		if(handle.equals(m_humiditysensor.getReadHandle()))
			return SENSOR_HUMIDITY;
		return -1;

	}
	
	/**
	 * Add SensorListener. Will receive all events specified in
	 * SensorListener interface.
	 * @param l SensorListener to add
	 */
	public void addListener(SensorListener l) {
		m_listeners.add(l);
	}

	/**
	 * Delete sensorlistener
	 * @param l SensorListener to delete
	 */
	public void deleteListener(SensorListener l) {
		m_listeners.remove(l);
	}


	/**
	 * Called by accelerometer sensor class when it receives data.
	 * @param x 
	 * @param y
	 * @param z
	 */
	protected void onAccelData(float x, float y, float z) {
		// Store it and pass it on
		m_accelx = x;
		m_accely = y;
		m_accelz = z;
		for (SensorListener l : m_listeners) {
			l.onAccelData(x, y, z);
		}
	}


	/** 
	 * Called by IR thermometer class when it receives data
	 * @param ambient_temp Ambient temperature
	 * @param object_temp Object temperature
	 */
	protected void onTemperatureData(float ambient_temp, float object_temp) {
		// Store it and pass it on to listeners
		m_ambient_temperature = ambient_temp;
		m_object_temperature = object_temp;
		for (SensorListener l : m_listeners) {
			l.onTemperatureData(ambient_temp, object_temp);
		}
	}

	/**
	 * Called by humidity sensor class when it receives data
	 * @param temp
	 * @param humidity
	 */
	protected void onHumidityData(float temp, float humidity) {
		// Store it and pass it on
		m_humidityval = humidity;
		m_hum_tempval = temp;
		for (SensorListener l : m_listeners) {
			l.onHumidityData(temp, humidity);
		}
	}
	/**
	 * Called by gyro sensor class when it receives data
	 * @param data
	 */
	protected void onGyroData(float data) {
		// Store it and pass it on
		m_gyroval = data;
		for (SensorListener l : m_listeners) {
			l.onGyroData(data);
		}
	}
	/**
	 * Called by pressure sensor class when it receives data
	 * @param data
	 */
	protected void onPressureData(float temp, float pressure) {
		// Store it and pass it on
		m_pressureval = pressure;
		for (SensorListener l : m_listeners) {
			l.onPressureData(temp,pressure);
		}
	}
	/**
	 * Called by magnetometer class when it receives data
	 * @param x
	 * @param y
	 * @param z
	 */
	protected void onMagnetData(float x, float y, float z) {
		// Store it and pass it on
		m_magnetx = x;
		m_magnety = y;
		m_magnetz = z;
		for (SensorListener l : m_listeners) {
			l.onMagnetData(x, y, z);
		}
	}
	/** Status changed. Pass it on to any sensorlisteners */
	public void onStatusChanged(int sensorid, int oldstatus, int newstatus) {
		for (SensorListener l : m_listeners) {
			l.onStatusChanged(sensorid, oldstatus, newstatus);
		}
	}
	/** Calibration step. Pass it on to any sensorlisteners */
	public void onCalibrationData(int sensorid, int step, int total) {
		for (SensorListener l : m_listeners) {
			l.onCalibrationData(sensorid, step, total);
		}
	}
	
	/** Called when a write is executed */
	public void onWrite(String what) {
		for (SensorListener l : m_listeners) {
			l.onWrite(what);
		}
	}
	
	// Various getters
	
	public String getBtAddr() {
		return m_btaddr;
	}
	
	public float getAccelX() {
		return m_accelx;
	}

	public float getAccelY() {
		return m_accely;
	}

	public float getAccelZ() {
		return m_accelz;
	}

	public float getObjectTemperature() {
		return m_object_temperature;
	}

	public float getAmbientTemperature() {
		return m_ambient_temperature;
	}

	public float getHumidityTemperature() {
		return m_hum_tempval;
	}

	public float getHumidity() {
		return m_humidityval;
	}

	public float getPressure() {
		return m_pressureval;
	}

	public float getMagnetX() {
		return m_magnetx;
	}

	public float getMagnetY() {
		return m_magnety;
	}

	public float getMagnetZ() {
		return m_magnetz;
	}

	public float getGyro() {
		return m_gyroval;
	}

	public synchronized boolean isConnected() {
		return m_isconnected;
	}

	

	/** Callback class */
	private class callback extends IBluetoothGattCallback.Stub {

		public void notificationGattCb(BluetoothDevice dev, String uuid,
				String char_handle, byte[] data) {
			Log.d(TAG, "Got notification");

			// Find the sensor that sent this notification
			int sensorid = getSensorByHandle(char_handle);
			BLESensor b = getSensor(sensorid);
			if(b == null) return;
			
			// Pass the notification on to the appropriate BLESensor.
			try {
				b.m_callback.notificationGattCb(dev, uuid,
								char_handle, data);
			} catch (RemoteException e) {
				e.printStackTrace();
			}

		
		}

		public void indicationGattCb(BluetoothDevice device, String uuid,
				String char_handle, String[] data) throws RemoteException {

		}
	}


	/** 
	 * Calibrate a sensor. This is only valid for the magnetometer
	 * @param sensorid
 	*/
	public void calibrate(int sensorid) {
		if(sensorid != SENSOR_MAGNET) return;
		m_magnetometer.calibrate();
		
	};
	
}
