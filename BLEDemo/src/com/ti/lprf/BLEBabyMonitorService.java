package com.ti.lprf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class BLEBabyMonitorService extends Service implements BLESensorBoard.SensorListener {
	
	
	/** Connect to SensorBoard.
	 * Needs parameter with id MSG_ID_HWADDR
	 */
	public static final int MSG_CONNECT = 1;
	
	/** Disconnect from SensorBoard
	 * No parameters needed
	 */
	public static final int MSG_DISCONNECT = 2;
	/** Get old models
	 * Will send back message with the same message ID and
	 * the GraphModels under the IDs
	 * MSG_ID_TEMPERATURE - temperature model
	 * MSG_ID_ACTIVITY - activity model
	 * MSG_ID_MOISTURE - moisture model
	 */
	public static final int MSG_GET_DATA = 3;
	
	/** Register client
	 * Register to receive messages when data is updated
	 * to the messenger supplied in msg.replyTo
	 * If msg.arg1 = 1, a MSG_GET_DATA message with the models included will be sent
	 */
	public static final int MSG_REGISTER_CLIENT = 4;
	/** Unregister client
	 * Unregister messenger supplied in msg.replyTo
	 */
	public static final int MSG_UNREGISTER_CLIENT = 5;
	/**
	 * Clear data
	 */
	public static final int MSG_CLEAR_DATA = 6;
	/** Write info
	 * 
	 */
	public static final int MSG_WRITE = 7;
	
	/** Message ID for temperature update */
	public static final int MSG_TEMPERATURE = 10;
	/** Message ID for moisture update */
	public static final int MSG_MOISTURE = 11;
	/** Message ID for activity update */
	public static final int MSG_ACTIVITY = 12;
	/** Message ID for pressure update */
	public static final int MSG_PRESSURE = 13;

	/** Message ID to select sensor */
	public static final int MSG_SELECT_SENSOR = 14;
	
	
	/** Paramter ID for Bluetooth address */
	public static final String MSG_ID_HWADDR = "ID_HWADDR";
	/** Parameter ID for temperature data */
	public static final String MSG_ID_TEMPERATURE = "ID_TEMPERATURE";
	/** Parameter ID for moisture data */
	public static final String MSG_ID_MOISTURE = "ID_MOISTURE";
	/** Parameter ID for activity data */
	public static final String MSG_ID_ACTIVITY = "ID_ACTIVITY";
	/** Parameter ID for pressure data */
	public static final String MSG_ID_PRESSURE = "ID_PRESSURE";
	/** Parameter ID for timestamp */
	public static final String MSG_ID_TIMESTAMP = "ID_TIMESTAMP";
	
	/** Parameter ID for write string */
	public static final String MSG_ID_WHAT = "ID_WHAT";
	
	
	
	private GraphModel m_activitymodel; 
	private GraphModel m_temperaturemodel; 
	private GraphModel m_moisturemodel;
	private GraphModel m_pressuremodel;

	private NotificationManager m_nm;
	/** Our connection to the BLESensorBoardService */
	private BLESensorBoardClient m_sbclient;
	/** Our clients */
	private ArrayList<Messenger> m_clients;
	
	private boolean m_isconnected = false;
	/** Previous accelerometer values - needed to compute gradient */
	private float m_acc_prevy;
	private float m_acc_prevx;
	private float m_acc_prevz; 
	private Date m_acc_prevdate;
	/** Activity */
	private float m_act;
	/** Temperature */
	private float m_temp;
	private float m_prev_temps[] = { 0.0f, -99.0f, 99.0f};
	/** Moisture */
	private float m_moisture;
	/** Pressure */
	private float m_pressure;
	
	/** Incoming messenger */
	private Messenger m_messenger = new Messenger(new IncomingHandler());
	/** Notification to display data in */
	private Notification m_notification;
	/** Intent for the BLEBabyMonitor application */
	private Intent m_babymonitorintent;
	
	
	/** Tag for Log.d */
	private static final String TAG = "BLEBabyMonitorService";
	
	/**
	 * Current sensor enabled
	 */
	private int m_current_sensor = BLESensorBoard.SENSOR_TEMPERATURE;
	
	private int m_notification_id = 1230919;
	/** Service entrypoint */
	@Override
	public void onCreate()
	{
		// Get system notification manager
		m_nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		
		// Get sensorboard connection
		m_sbclient = new BLESensorBoardClient(this);
		m_sbclient.bindService();
		// And listen to it
		m_sbclient.addListener(this);
		
		// Initialize data fields
		m_acc_prevx = 0.0f;
		m_acc_prevy = 0.0f;
		m_acc_prevz = 0.0f;
		m_temp = 0.0f;
		m_act = 0.0f;
		m_moisture = 0.0f;
		m_acc_prevdate = new Date();
		// Initialize clients
		m_clients = new ArrayList<Messenger>();
		
		// Initialize models
		m_activitymodel = new GraphModel();
		m_temperaturemodel = new GraphModel();
		m_moisturemodel = new GraphModel();
		m_pressuremodel = new GraphModel();
		
		// Setup the notification
		m_babymonitorintent =  new Intent(this,BLEBabyMonitor.class);
		m_notification = new Notification(R.drawable.tilogo,getResources().getText(R.string.baby_monitor),System.currentTimeMillis());
		PendingIntent pendIntent = PendingIntent.getActivity(this,0,m_babymonitorintent,0);
		m_notification.setLatestEventInfo(this, getResources().getText(R.string.baby_monitor), "Baby Monitor Service", pendIntent);
		//m_nm.notify(0, m_notification);
		
		super.onCreate();
	}
	/** Called when service is started (i.e. startService()) */
	@Override
	public int onStartCommand(Intent in, int flags, int startid){
		/** We are a foreground service, the user is actively aware of us */
		if(m_notification == null) {
			m_notification = new Notification(R.drawable.tilogo,getResources().getText(R.string.baby_monitor),System.currentTimeMillis());
			PendingIntent pendIntent = PendingIntent.getActivity(this,0,m_babymonitorintent,0);
			m_notification.setLatestEventInfo(this, getResources().getText(R.string.baby_monitor), "Starting..", pendIntent);
		}
		startForeground(m_notification_id, m_notification);
		Log.d(TAG,"Started service");
		//m_nm.notify(0,m_notification);
		return START_STICKY;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return m_messenger.getBinder();
	}
	/** Called on destruction */
	@Override
	public void onDestroy(){
		// Cancel notification
		m_nm.cancel(0);
		// Unbind from sensorboard service
		m_sbclient.unbindService();
		super.onDestroy();
	}
	
	/** Send a message to all clients 
	 * @param id The message ID
	 * @param data The data to supply
	 */
	private void broadcastMessage(int id, Bundle data){
	
		for(Messenger m: m_clients)
		{
			Message msg = Message.obtain(null,id);
			msg.setData(data);
			try {
				m.send(msg);
			} catch (RemoteException e) {
				
				m_clients.remove(m);
				
			}
		}
		
	}
	
	/** Update notification with current data */
	private void updateNotification() {
		PendingIntent pendIntent = PendingIntent.getActivity(this,0,m_babymonitorintent,0);

	/*	m_notification.setLatestEventInfo(this, 
				getResources().getText(R.string.baby_monitor),  
				String.format("Temp: %.1f, Moisture: %.2f, Activity: %.2f",m_temp, m_moisture, m_act), 
				pendIntent); */
		//m_nm.notify(m_notification_id, m_notification);
	}
	
	/** Received accelerometer data from SB
	 * @param x X magnitude
	 * @param y Y magnitude
	 * @param z Z magnitude
	 */
	public void onAccelData(float x, float y, float z) {
		// Compute gradient
		Date d = new Date();
		int dt = (int) (d.getTime()-m_acc_prevdate.getTime());
		//	Discard data coming in too fast
		if(dt < 100) return;
		final float act = (float)Math.sqrt(Math.pow((x - m_acc_prevx),2)
								+   Math.pow((y - m_acc_prevy),2)
								+ Math.pow((z - m_acc_prevz),2));

		m_acc_prevx = x;
		m_acc_prevy = y;
		m_acc_prevz = z;
		m_acc_prevdate = d;
		Log.d(TAG,"Got accel data, " + x + " " + y + " " + z);
		// Get timestamp
		// Add to model
		m_activitymodel.add(d, act);
		// Send to clients
		Bundle b = new Bundle();
		b.putFloat(MSG_ID_ACTIVITY,act);
		b.putSerializable(MSG_ID_TIMESTAMP, d);
		broadcastMessage(MSG_ACTIVITY,b);
		
		// Update data field
		m_act = act;
		// Update notification
		updateNotification();
		
	}
	/**
	 * Received IR thermometer data
	 * @param ambient_temp Ambient temperature
	 * @param object_temp Object temperature
	 */
	public void onTemperatureData(float ambient_temp, float object_temp) {
		
		
		// Get timestamp
		Date d = new Date();
		// Median filter the data
		m_prev_temps[0] = m_prev_temps[1];
		m_prev_temps[1] = m_prev_temps[2];
		m_prev_temps[2] = object_temp;
		
		float sorted[] = new float[3];
		System.arraycopy(m_prev_temps, 0, sorted, 0, 3);
		Arrays.sort(sorted);
		
		float temp = sorted[1];
		
		
		// Add to model
		m_temperaturemodel.add(d,temp);
		
		// Send to clients
		Bundle b = new Bundle();
		b.putFloat(MSG_ID_TEMPERATURE,temp);
		b.putSerializable(MSG_ID_TIMESTAMP, d);
		broadcastMessage(MSG_TEMPERATURE,b);
		
		// Update notification
		m_temp = temp;
		updateNotification();

		
	}
	/** Received humidity data
	 * @param temp Temperature
	 * @param humidity Humidity 
	 * */
	public void onHumidityData(float temp, float humidity) {
		// Get timestamp
		Date d = new Date();
		// Add to model
		m_moisturemodel.add(d,humidity);
		
		// Send to clients
		Bundle b = new Bundle();
		b.putFloat(MSG_ID_MOISTURE,humidity);
		b.putSerializable(MSG_ID_TIMESTAMP, d);
		broadcastMessage(MSG_MOISTURE,b);
		// Update notification
		m_moisture = humidity;
		updateNotification();
		
	}
	public void onPressureData(float temperature, float pressure) {
		// Get timestamp
		Date d = new Date();
		// Add to model
		m_pressuremodel.add(d,pressure/100);
		
		// Send to clients
		Bundle b = new Bundle();
		b.putFloat(MSG_ID_PRESSURE,pressure/100);
		b.putSerializable(MSG_ID_TIMESTAMP, d);
		broadcastMessage(MSG_PRESSURE,b);
		// Update notification
		m_moisture = pressure/100;
		updateNotification();
	}
	
	// We don't care about these
	public void onGyroData(float data) {
		
	}
	
	public void onMagnetData(float x, float y, float z) {
		
	}
	public void onStatusChanged(int sensorid, int oldstatus, int newstatus) {
		if(sensorid == -1){
			if(newstatus == BLESensorBoard.STATUS_CONNECTED) m_isconnected = true;
			if(newstatus == BLESensorBoard.STATUS_DISCONNECTED) m_isconnected = false;
		}
	}


	
	/** Class for handling incoming messages */
	private class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg){
			Bundle b = msg.getData();
			switch(msg.what){
			case MSG_CONNECT:
				
				m_sbclient.connect(b.getString(MSG_ID_HWADDR));
			//	m_sbclient.enableSensor(BLESensorBoard.SENSOR_TEMPERATURE);
			 //   m_sbclient.enableSensor(BLESensorBoard.SENSOR_HUMIDITY);
				//m_sbclient.enableSensor(BLESensorBoard.SENSOR_ACCELEROMETER); 
				m_current_sensor = msg.arg1;
				m_sbclient.enableSensor(m_current_sensor);
				if(m_current_sensor == BLESensorBoard.SENSOR_ACCELEROMETER) {
					m_sbclient.setTiming(m_current_sensor,(byte)45);
				}


			break;
			case MSG_SELECT_SENSOR:
				if(m_isconnected) {
				m_sbclient.enableSensor(msg.arg1);
				m_sbclient.disableSensor(m_current_sensor);
				if(msg.arg1 == BLESensorBoard.SENSOR_ACCELEROMETER) {
					m_sbclient.setTiming(msg.arg1,(byte)45);
				}
				}

				m_current_sensor = msg.arg1;
				// Clear all the models
			/*	m_temperaturemodel.clear();
				m_moisturemodel.clear();
				m_activitymodel.clear();
				m_pressuremodel.clear();
				
				Bundle b3 = new Bundle();
				Message tosend2 = Message.obtain(null, MSG_GET_DATA);
				b3.putSerializable(MSG_ID_TEMPERATURE, m_temperaturemodel);
				b3.putSerializable(MSG_ID_MOISTURE, m_moisturemodel);
				b3.putSerializable(MSG_ID_ACTIVITY, m_activitymodel);
				b3.putSerializable(MSG_ID_PRESSURE, m_pressuremodel);
				tosend2.setData(b3);
				try {
					msg.replyTo.send(tosend2);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				*/
				break;
			case MSG_DISCONNECT:
				m_sbclient.disconnect();
			break;
			case MSG_REGISTER_CLIENT:
				m_clients.add(msg.replyTo);
				if(msg.arg1 == 1){
					// msg.arg1 = 1 means client wants the old models.
					Bundle b2 = new Bundle();
					Message tosend = Message.obtain(null, MSG_GET_DATA);
					b2.putSerializable(MSG_ID_TEMPERATURE, m_temperaturemodel);
					b2.putSerializable(MSG_ID_MOISTURE, m_moisturemodel);
					b2.putSerializable(MSG_ID_ACTIVITY, m_activitymodel);
					b2.putSerializable(MSG_ID_PRESSURE, m_pressuremodel);
					tosend.setData(b2);
					try {
						msg.replyTo.send(tosend);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
				
			break;
			case MSG_UNREGISTER_CLIENT:
				m_clients.remove(msg.replyTo);
				break;
			case MSG_GET_DATA:
				Bundle b2 = new Bundle();
				Message tosend = Message.obtain(null, MSG_GET_DATA);
				b2.putSerializable(MSG_ID_TEMPERATURE, m_temperaturemodel);
				b2.putSerializable(MSG_ID_MOISTURE, m_moisturemodel);
				b2.putSerializable(MSG_ID_ACTIVITY, m_activitymodel);
				b2.putSerializable(MSG_ID_PRESSURE, m_pressuremodel);
				tosend.setData(b2);
				try {
					msg.replyTo.send(tosend);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				break;
			case MSG_CLEAR_DATA:
				m_moisturemodel.clear();
				m_temperaturemodel.clear();
				m_pressuremodel.clear();
				m_activitymodel.clear();
				break;
			
			default:
				super.handleMessage(msg);
				break;
			}
		}
	}



	public void onCalibrationData(int sensorid, int step, int total) {
		// TODO Auto-generated method stub
		
	}
	public void onWrite(String what) {
		Bundle b = new Bundle();
		b.putString(MSG_ID_WHAT, what);
		
		broadcastMessage(MSG_WRITE, b);
	}



	
}
