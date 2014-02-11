package com.ti.lprf;

import java.util.ArrayList;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;


/** Class to do interfacing with BLESensorBoardService */
public class BLESensorBoardClient {

	
	/** IncomingHandler to handle messages from the service */
	private class IncomingHandler extends Handler {
		/** 
		 * Handle incoming message.
		 * This functions handles an incoming message from the service and passes it on to the 
		 * BLESensorBoardListeners.
		 */
		@Override
		public void handleMessage(Message msg) {
			Bundle b = msg.getData();
			switch(msg.what) {
			case BLESensorBoardService.MSG_STATUS:
				for(BLESensorBoard.SensorListener l: m_listeners) {
					l.onStatusChanged(b.getInt(BLESensorBoardService.MSG_ID_SENSOR),
							b.getInt(BLESensorBoardService.MSG_ID_OLDSTATUS),
							b.getInt(BLESensorBoardService.MSG_ID_NEWSTATUS));
					if(b.getInt(BLESensorBoardService.MSG_ID_NEWSTATUS) == BLESensorBoard.STATUS_CONNECTED) 
						m_isconnected = true;
					if(b.getInt(BLESensorBoardService.MSG_ID_NEWSTATUS) == BLESensorBoard.STATUS_DISCONNECTED) 
						m_isconnected = false;
				}
			break;
			case BLESensorBoardService.MSG_SENSOR_ACCELEROMETER:
				for(BLESensorBoard.SensorListener l: m_listeners) {
					l.onAccelData(b.getFloat(BLESensorBoardService.MSG_ID_XCOORD),
							b.getFloat(BLESensorBoardService.MSG_ID_YCOORD),
							b.getFloat(BLESensorBoardService.MSG_ID_ZCOORD));
				}
			break;
			case BLESensorBoardService.MSG_SENSOR_MAGNETOMETER:
				for(BLESensorBoard.SensorListener l: m_listeners) {
					l.onMagnetData(b.getFloat(BLESensorBoardService.MSG_ID_XCOORD),
							b.getFloat(BLESensorBoardService.MSG_ID_YCOORD),
							b.getFloat(BLESensorBoardService.MSG_ID_ZCOORD));
				}
			break;
			case BLESensorBoardService.MSG_SENSOR_GYRO:
				for(BLESensorBoard.SensorListener l: m_listeners) {
					l.onGyroData(b.getFloat(BLESensorBoardService.MSG_ID_GYRO));
				}
			break;
			case BLESensorBoardService.MSG_SENSOR_HUMIDITY:
				for(BLESensorBoard.SensorListener l: m_listeners) {
					l.onHumidityData(b.getFloat(BLESensorBoardService.MSG_ID_HUMIDITY_TEMP),
							b.getFloat(BLESensorBoardService.MSG_ID_HUMIDITY));
				}
			break;
			case BLESensorBoardService.MSG_SENSOR_PRESSURE:
				for(BLESensorBoard.SensorListener l: m_listeners) {
					l.onPressureData(b.getFloat(BLESensorBoardService.MSG_ID_PRESSURE_TEMP),b.getFloat(BLESensorBoardService.MSG_ID_PRESSURE));


				}
			break;
			case BLESensorBoardService.MSG_SENSOR_TEMPERATURE:
				for(BLESensorBoard.SensorListener l: m_listeners) {
					l.onTemperatureData(b.getFloat(BLESensorBoardService.MSG_ID_TEMP_AMBIENT),
							b.getFloat(BLESensorBoardService.MSG_ID_TEMP_OBJECT));
				}
			break;
			case BLESensorBoardService.MSG_CALIBRATION:
				for(BLESensorBoard.SensorListener l: m_listeners) {
					l.onCalibrationData(b.getInt(BLESensorBoardService.MSG_ID_SENSOR),
							b.getInt(BLESensorBoardService.MSG_ID_STEP), 
							b.getInt(BLESensorBoardService.MSG_ID_TOTAL));
				}
			break;
			case BLESensorBoardService.MSG_WRITE:
				for(BLESensorBoard.SensorListener l: m_listeners) {
					l.onWrite(b.getString(BLESensorBoardService.MSG_ID_WHAT));
							
				}
			break;
			}
		}
	}
	/** Our serviceconnection */
	private ServiceConnection m_serviceconnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName arg0, IBinder arg1) {
			m_service = new Messenger(arg1);
			// Tell service we're bound
			Message msg = Message.obtain(null, BLESensorBoardService.MSG_REGISTER_CLIENT);
			msg.replyTo = m_messenger;
			try {
				m_service.send(msg);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			
		}

		public void onServiceDisconnected(ComponentName arg0) {
			m_service = null;
		}
		
	};
	/** The listeners */
	private ArrayList<BLESensorBoard.SensorListener> m_listeners = new ArrayList<BLESensorBoard.SensorListener>();
	/** Our service messenger, (the service is listening on the other end) */
	private Messenger m_service = null;
	/** The messenger that we receive data on */
	final Messenger m_messenger = new Messenger(new IncomingHandler());
	/** Application context */
	private Context m_context;
	/** Whether or not we're connected */
	private boolean m_isconnected = false;

	/** 
	 * Constructor
	 * @param ctx the parent application context
	 */
	public BLESensorBoardClient(Context ctx) {
		m_context = ctx;
	}
	
	/** 
	 * Bind to BLESensorBoardService 
	 */
	public void bindService() {
		Intent i = new Intent(m_context,BLESensorBoardService.class);
		m_context.bindService(i, m_serviceconnection, Context.BIND_AUTO_CREATE);
	}
	/** Unbind from service */
	public void unbindService() {
		if(m_service != null) {
			// Tell the service we don't want updates anymore 
			Message msg = Message.obtain(null, BLESensorBoardService.MSG_UNREGISTER_CLIENT);
			msg.replyTo = m_messenger;

			try {
				m_service.send(msg);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			// and then unbind
			m_context.unbindService(m_serviceconnection);
			m_service = null;

		}
	}
	/**
	 * Tell service to connect to a sensor board with given HW addr 
	 * @param hwaddr The bluetooth MAC address to connect to
	 */
	public void connect(String hwaddr) {
		Message msg = Message.obtain(null, BLESensorBoardService.MSG_CONNECT_SENSOR_BOARD);
		Bundle b = new Bundle();
		b.putString(BLESensorBoardService.MSG_ID_HWADDR, hwaddr);
		msg.setData(b);
		try {
			m_service.send(msg);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Tell service to disconnect from sensor board
	 */
	public void disconnect() {
		Message msg = Message.obtain(null, BLESensorBoardService.MSG_DISCONNECT_SENSOR_BOARD);
		if(m_service != null) {
			try {
				m_service.send(msg);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * Tell service to calibrate a sensor (Magnetometer only)
	 * 
	 * @param sensorid The ID of the sensor to calibrate (but only magnetometer is valid)
	 */
	public void calibrate(int sensorid){
		Message msg = Message.obtain(null, BLESensorBoardService.MSG_CALIBRATION);
		msg.arg1 = sensorid;
		if(m_service != null) {
			try {
				m_service.send(msg);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * Tell service to enable a sensor
	 * @param sensorid ID of sensor to enable (see BLESensorBoard)
	 */
	public void enableSensor(int sensorid) {
		Message msg = Message.obtain(null, BLESensorBoardService.MSG_ENABLE_SENSOR);
		msg.arg1 = sensorid;
		try {
			m_service.send(msg);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	/** 
	 * Tell service to disable a sensor.
	 * @param sensorid ID of sensor to disable (see BLESensorBoard)
	 */
	public void disableSensor(int sensorid) {
		Message msg = Message.obtain(null, BLESensorBoardService.MSG_DISABLE_SENSOR);
		msg.arg1 = sensorid;
		try {
			m_service.send(msg);
		} catch (RemoteException e) {
			e.printStackTrace();
		}

	}
	/**
	 * Add SensorListener
	 * @param l The listener to receive updates on
	 */
	public void addListener(BLESensorBoard.SensorListener l){
		m_listeners.add(l);
	}
	
	/**
	 * Tell service to set timing for a sensor
	 * @param sensorid The sensor to set timing for (see BLESensorBoard)
	 * @param timing The timing parameter. 
	 */
	public void setTiming(int sensorid, byte timing)
	{
		Message msg = Message.obtain(null, BLESensorBoardService.MSG_SET_TIMING);
		msg.arg1 = sensorid;
		msg.arg2 = timing;
		try {
			m_service.send(msg);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	/** Return whether or not the service is connected to a sensor board */
	public boolean isConnected() {
		return m_isconnected;
	}
	
}
