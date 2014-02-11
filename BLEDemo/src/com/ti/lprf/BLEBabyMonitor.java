package com.ti.lprf;

import java.util.Date;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

public class BLEBabyMonitor extends Activity {
	
	/** Model for the activity graph */
	GraphModel m_activity_model;
	/** Model for the moisture graph */
	GraphModel m_moisture_model;
	/** Model for the temperature graph */
	GraphModel m_temperature_model;
	/** Model for the pressure graph */
	GraphModel m_pressure_model;
	
	/** Display for the temperature graph */
	GraphDisplay m_gd_temperature;
	/** Display for the moisture graph */
	GraphDisplay m_gd_moisture;
	/** Display for the activity graph */
	GraphDisplay m_gd_activity;
	/** Display for the pressure graph */
	GraphDisplay m_gd_pressure;

	/** TextView for the current temperature */
	TextView m_tw_temperature;

	/** TextView for the current moisture */
	TextView m_tw_moisture;

	/** TextView for the current activity level */
	TextView m_tw_activity;
	
	/** TextView for the current pressure */
	TextView m_tw_pressure;
	

	/** Scroll View for the temperature graph */
	HorizontalScrollView m_sw_temperature;
	/** Scroll View for the moisture graph */
	HorizontalScrollView m_sw_moisture;
	/** Scroll View for the activity graph */
	HorizontalScrollView m_sw_activity;
	/** Scroll View for the pressure graph */
	HorizontalScrollView m_sw_pressure;
	
	
	/** Do we need to obtain old data from the BLEBabyMonitorService? */
	public boolean m_needdata = false;
	
	/** Do we want the graph to scroll automatically? (Controlled with CheckBox) */
	boolean m_autoscroll;
	
	/** Our service messenger - used to send messages to the service */
	private Messenger m_service;
	/** Our incoming messenger - used to receive messages from the service */
	final private Messenger m_messenger = new Messenger(new IncomingHandler());
	
	/** The system BluetoothAdapter */
	private BluetoothAdapter m_btadapter = null;
	
	/** The currently selected sensor */
	private int m_selected_sensor = BLESensorBoard.SENSOR_TEMPERATURE;
	/** Whether or not we are connected to the service */
	private boolean m_isconnected = false;
	
	/** Our ServiceConnection */
	private ServiceConnection m_serviceconnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName name, IBinder service) {
			// Obtain the service messenger from the IBinder
			m_service = new Messenger(service);
			m_isconnected = true;
			
			// Tell the service that we're connected to it and where to send updates
			Message msg = Message.obtain(null,BLEBabyMonitorService.MSG_REGISTER_CLIENT);
			// We want incoming messages coming to m_messenger
			msg.replyTo = m_messenger;
			// If we need the models, set arg1 to 1 
			msg.arg1 = m_needdata ? 1 : 0;
			// Aaaand send!
			try {
				m_service.send(msg);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public void onServiceDisconnected(ComponentName name) {
			// Make sure we don't try to use the service when disconnected
			m_service = null;
			m_isconnected = false;
		}
		
	};
	
	/**
	 * Start BLEBabyMonitorService
	 */
	private void doStartService() {
		Intent i = new Intent(this,BLEBabyMonitorService.class);
		startService(i);
	}
	/** 
	 * Bind to BLEBabyMonitorService using m_serviceconnection
	 */
	private void doBindService() {
		Intent i = new Intent(this,BLEBabyMonitorService.class);
		bindService(i, m_serviceconnection, Context.BIND_AUTO_CREATE);
	}
	/**
	 * Unbind from BLEBabyMonitorService
	 */
	private void doUnbindService() {
		// If we're already unbound, do nothing
		if(m_service != null) {
			// If we are bound, tell service that we do not want updates anymore
			Message msg = Message.obtain(null, BLEBabyMonitorService.MSG_UNREGISTER_CLIENT);
			msg.replyTo = m_messenger;

			try {
				m_service.send(msg);
			} catch (RemoteException e) {
				// If we get this exception, it's not really a problem because we're disconnecting anyways.
				e.printStackTrace();
			}
			// And then unbind
			unbindService(m_serviceconnection);
			m_service = null;

		}
	}
/**  
 * Stop BLEBabyMonitorService
 */
	protected void doStopService(){
		Intent i = new Intent(this,BLEBabyMonitorService.class);
		stopService(i);
	}
	
	/**
	 * Tell service to connect to a SensorBoard
	 * @param hwaddr The bluetooth address of the SensorBoard
	 */
	private void connect(String hwaddr){
		// Build the message
		Message msg = Message.obtain(null, BLEBabyMonitorService.MSG_CONNECT);
		msg.arg1 = m_selected_sensor;
		Bundle b = new Bundle();
		b.putString(BLEBabyMonitorService.MSG_ID_HWADDR,hwaddr);
		msg.setData(b);
		// Send
		try {
			m_service.send(msg);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Tell service to disconnect from the SensorBoard
	 */
	private void disconnect(){
		Message msg = Message.obtain(null, BLEBabyMonitorService.MSG_DISCONNECT);
		try {
			m_service.send(msg);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	

	/**
	 * Application entry point
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.baby_monitor);

		// Obtain temperature GraphDisplay
		m_gd_temperature = (GraphDisplay) findViewById(R.id.graph_temp);
		// Obtain activity GraphDisplay
		m_gd_activity = (GraphDisplay) findViewById(R.id.graph_activity);
		// Obtain moisture GraphDisplay
		m_gd_moisture = (GraphDisplay) findViewById(R.id.graph_moisture);
		// Obtain pressure GraphDisplay
		m_gd_pressure = (GraphDisplay) findViewById(R.id.graph_pressure);

		
		// Obtain temperature TextView
		m_tw_temperature = (TextView) findViewById(R.id.tw_temp);
		// Obtain moisture TextView
		m_tw_moisture = (TextView) findViewById(R.id.tw_moist);
		// Obtain activity TextView
		m_tw_activity = (TextView) findViewById(R.id.tw_activity);
		// Obtain pressure TextView
		m_tw_pressure = (TextView) findViewById(R.id.tw_pressure);

		// Obtain temperature ScrollView
		m_sw_temperature = (HorizontalScrollView) findViewById(R.id.horizontalScrollView1);
		// Obtain moisture ScrollView
		m_sw_moisture = (HorizontalScrollView) findViewById(R.id.horizontalScrollView2);
		// Obtain activity ScrollView
		m_sw_activity = (HorizontalScrollView) findViewById(R.id.horizontalScrollView3);
		// Obtain activity ScrollView
		m_sw_pressure = (HorizontalScrollView) findViewById(R.id.horizontalScrollView4);
				
		
		// Disable scrollbar fading - we want the user to see where they are at all times
	/*	m_sw_temperature.setScrollbarFadingEnabled(false);
		m_sw_moisture.setScrollbarFadingEnabled(false);
		m_sw_activity.setScrollbarFadingEnabled(false);
		m_sw_pressure.setScrollbarFadingEnabled(false); */

		// Disable smooth scrolling, it looks kind of awkward with autoscroll
	/*	m_sw_temperature.setSmoothScrollingEnabled(false);
		m_sw_moisture.setSmoothScrollingEnabled(false);
		m_sw_activity.setSmoothScrollingEnabled(false);
		m_sw_pressure.setSmoothScrollingEnabled(false);

*/
		// Make models for the graphs
		m_activity_model = new GraphModel();
		m_moisture_model = new GraphModel();
		m_temperature_model = new GraphModel();
		m_pressure_model = new GraphModel();


		// And make the GraphDisplays use them
		m_gd_temperature.setModel(m_temperature_model);
		m_gd_moisture.setModel(m_moisture_model);
		m_gd_activity.setModel(m_activity_model);
		m_gd_pressure.setModel(m_pressure_model);


		// Set width of GraphDisplay (FIXME: Should be able to pull this from the layout somehow)
		m_gd_temperature.setWidth(540);
		m_gd_moisture.setWidth(540);
		m_gd_activity.setWidth(540);
		m_gd_pressure.setWidth(540);
		
		// Set reasonable initial max/min Y values
		m_gd_temperature.setMaxY(36.0f);
		m_gd_temperature.setMinY(19.0f);
		m_gd_temperature.setGridYSpacing(5.0f);
		
		m_gd_moisture.setMaxY(81.0f);
		m_gd_moisture.setMinY(49.0f);
		
		m_gd_activity.setMaxY(11.0f);
		m_gd_activity.setMinY(-1.0f);
		m_gd_activity.setGridYSpacing(2.0f);
		
		m_gd_pressure.setMaxY(1050.0f);
		m_gd_pressure.setMinY(900.0f);
		m_gd_pressure.setGridYSpacing(50.0f);
		
		// Get the autoscroll checkbox
		CheckBox scrollcb = (CheckBox) findViewById(R.id.cb_autoscroll);
		// and make it change our m_autoscroll
		scrollcb.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				m_autoscroll = isChecked;
			}

		});
		
		// Get the system bluetooth adapter
		m_btadapter = BluetoothAdapter.getDefaultAdapter();
		// Autoscroll defaults to on
		m_autoscroll = true;
		
		// Start BLEBabyMonitorService
		doStartService();
		// And bind to it
		doBindService();
	}
	
	/** Application exit point
	 * Unbinds service, but does not stop it (we want it running in the background)
	 */
	@Override
	protected void onDestroy() {
		doUnbindService();
		super.onDestroy();

	}

	/**
	 * Create options menu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.option_menu_baby_monitor, menu);
		return true;
	}

	/** Function to clear the graph models, both in the app and in the service 
	 * 
	 */
	private void clearData(){
		m_moisture_model.clear();
		m_temperature_model.clear();
		m_pressure_model.clear();
		m_activity_model.clear();
		
		// Tell service to clear data
		Message msg = Message.obtain(null,BLEBabyMonitorService.MSG_CLEAR_DATA);
		try {
			m_service.send(msg);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
	}
	/** 
	 * onOptionsItemSelected (called when options menu item is selected)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menuitem_connect:
			// User hit connect, show connect dialog
			showConnectDialog();
			return true;
		case R.id.disconnect:
			// User hit disconnect, disconnect!
			disconnect();
			return true;
		case R.id.exit:
			// User hit exit, stop the service and exit application
			doStopService();
			disconnect();
			finish();
		case R.id.clear:
			// User hit clear
			clearData();
			return true;
		case R.id.menu_temperature:
			// User selected temperature sensor
			m_selected_sensor = BLESensorBoard.SENSOR_TEMPERATURE;
			item.setChecked(true);
			selectSensor(m_selected_sensor);
			return true;
		case R.id.menu_activity:
			// User selected activity (accelerometer)
			m_selected_sensor = BLESensorBoard.SENSOR_ACCELEROMETER;
			item.setChecked(true);
			selectSensor(m_selected_sensor);
			return true;
		case R.id.menu_pressure:
			// User selected pressure
			m_selected_sensor = BLESensorBoard.SENSOR_PRESSURE;
			item.setChecked(true);
			selectSensor(m_selected_sensor);
			return true;
		case R.id.menu_moisture:
			// User selected moisture
			m_selected_sensor = BLESensorBoard.SENSOR_HUMIDITY;
			item.setChecked(true);
			selectSensor(m_selected_sensor);
			return true;
		default:
			return super.onOptionsItemSelected(item);

		}
	}
	
	/**
	 * Select sensor to get data from
	 * @param sensor The sensor ID we want, see BLESensorBoard
	 */
	private void selectSensor(int sensor) {
		Message msg = Message.obtain(null, BLEBabyMonitorService.MSG_SELECT_SENSOR);
		msg.arg1 = sensor;
		
		try {
			m_service.send(msg);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	/**
	 * Called when application is resumed
	 */
	@Override
	protected void onResume(){
		// If we lost service, rebind
		if(m_service == null) doBindService();
		// We need the old data
		m_needdata = true;
		
		super.onResume();
	}
	
	/**
	 * Displays BLEDeviceChooser
	 */
	protected void showConnectDialog() {
		BLEDeviceChooser chooser = new BLEDeviceChooser(this, m_btadapter,
				new BLEDeviceChooser.BLEDeviceChooserListener() {

					public void onBLEDeviceSelected(String addr) {
						// Tell service to connect to this address
						connect(addr);
					}

				});
		chooser.showDialog();

	}

	/**
	 * Scroll all scrollviews to the far right
	 */
	private void updateScrolls() {
		if (m_autoscroll) {

			m_sw_temperature.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
			m_sw_moisture.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
			m_sw_activity.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
			m_sw_pressure.fullScroll(HorizontalScrollView.FOCUS_RIGHT);


		}

	}

	/** 
	 * We received activity data
	 * @param d Timestamp of activity
	 * @param act Activity (magnitude of gradient of accelerometer data)
	 */
	public void onActivity(Date d, float act) {
		m_tw_activity.setText(String.format("%.2f", act));
		m_activity_model.add(d, act);
		updateScrolls();

	}

	/**
	 * We received temperature data
	 * @param d Timestamp
	 * @param object_temp Object temperature in celcius
	 */
	public void onTemperature(Date d, float object_temp) {
		m_temperature_model.add(d, object_temp);
		m_tw_temperature.setText(String.format("%.1f°C", + object_temp));
		updateScrolls();

	}
	/**
	 * We received moisture data
	 * @param d Timestamp
	 * @param humidity 
	 */
	public void onMoisture(Date d, float humidity) {
		m_moisture_model.add(d, humidity);
		m_tw_moisture.setText(String.format("%.1f %%",humidity));
		updateScrolls();
	}
	/**
	 * We received pressure
	 * @param d Timestamp
	 * @param pressure pressure
	 */
	public void onPressure(Date d, float pressure) {
		m_pressure_model.add(d, pressure);
		m_tw_pressure.setText(String.format("%.0f " + "hPa",pressure));
		updateScrolls();
	}
	
	/**
	 * We received old models
	 * @param actmodel Activity model
	 * @param tempmodel Temperature models
	 * @param moistmodel Moisture models
	 */
	public void onGetData(GraphModel actmodel, GraphModel tempmodel, GraphModel moistmodel, GraphModel pressuremodel) {
		// Set models from the BLEBabyMonitorService
		
		// Note that some strangeness occurs here. The models are actually not properly serialized from the BLEBabyMonitorService,
		// so the instance in this class will BE THE SAME as the one in BLEBabyMonitorService. Yeah, that isn't desirable 
		// (since both this program and the service updates the model), but I haven't had time to fix this. For now, we just check for duplicates
		// when drawing the graphs, but this should be fixed.
		
		m_gd_activity.setModel(actmodel);
		m_gd_temperature.setModel(tempmodel);
		m_gd_moisture.setModel(moistmodel);
		m_gd_pressure.setModel(pressuremodel);
		m_moisture_model = moistmodel;
		m_temperature_model = tempmodel;
		m_activity_model = actmodel;
		m_pressure_model = pressuremodel;
		m_needdata = false;
	}

	/** Class for handling incoming messages from BLEBabyMonitorService */
	private class IncomingHandler extends Handler {
		public void handleMessage(Message msg) {
			final Bundle b = msg.getData();
			switch (msg.what) {
			case BLEBabyMonitorService.MSG_TEMPERATURE:
				// We got temperature data. Post it back to the GUI thread
				post(new Runnable() {
					public void run() {
						onTemperature(
								(Date)b.getSerializable(BLEBabyMonitorService.MSG_ID_TIMESTAMP),
								b.getFloat(BLEBabyMonitorService.MSG_ID_TEMPERATURE));
					}
				});
				break;
			case BLEBabyMonitorService.MSG_ACTIVITY:
				// We got activity data. Post it back to the GUI thread
				post(new Runnable() {
					public void run() {
						onActivity(
								(Date)b.getSerializable(BLEBabyMonitorService.MSG_ID_TIMESTAMP),
								b.getFloat(BLEBabyMonitorService.MSG_ID_ACTIVITY));
					}
				});
				break;
			case BLEBabyMonitorService.MSG_MOISTURE:
				// We got moisture data. Post it back to the GUI thread
				post(new Runnable() {
					public void run() {
						onMoisture(
								(Date)b.getSerializable(BLEBabyMonitorService.MSG_ID_TIMESTAMP),
								b.getFloat(BLEBabyMonitorService.MSG_ID_MOISTURE));
					}
				});
				break;
			case BLEBabyMonitorService.MSG_PRESSURE:
				// We got moisture data. Post it back to the GUI thread
				post(new Runnable() {
					public void run() {
						onPressure(
								(Date)b.getSerializable(BLEBabyMonitorService.MSG_ID_TIMESTAMP),
								b.getFloat(BLEBabyMonitorService.MSG_ID_PRESSURE));
					}
				});
				break;
			case BLEBabyMonitorService.MSG_GET_DATA:
				// We got previous data (i.e. models full of data) from the service.
				// Post that data back to the GUI thread.
				post(new Runnable() {
					public void run() {
						onGetData(
								(GraphModel)b.getSerializable(BLEBabyMonitorService.MSG_ID_ACTIVITY),
								(GraphModel)b.getSerializable(BLEBabyMonitorService.MSG_ID_TEMPERATURE),
								(GraphModel)b.getSerializable(BLEBabyMonitorService.MSG_ID_MOISTURE),
								(GraphModel)b.getSerializable(BLEBabyMonitorService.MSG_ID_PRESSURE));
					}
				});
			break;
			case BLEBabyMonitorService.MSG_WRITE:
				// Something was written to us
				post(new Runnable() {
					public void run() {
						Toast.makeText(BLEBabyMonitor.this, 
								b.getString(BLEBabyMonitorService.MSG_ID_WHAT), 
								Toast.LENGTH_SHORT).show();
					}
				});
			}
		}
	}


}
