package com.ti.lprf;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

/** This is the Compass activity */
public class BLEDemoActivity extends Activity {
	
	/** Custom widget to display the rotation */
	private RotationDisplay m_rotation_display;

	/** The system Bluetooth Adapter */
	private BluetoothAdapter m_btadapter = null;
	
	/** Our connection to BLESensorBoardService */
	private BLESensorBoardClient m_sensorboard;

	/** A handler to do stuff in the GUI thread for us */
	private Handler m_handler = new Handler();
	
	/** Whether or not we have set the timing for the magnetometer data retrieval */
	private boolean m_timingisset = false;
	
	/** Our ProgressDialog for visualizing calibration progress */
	private ProgressDialog m_caliprogress = null;
	
	/** Our SensorListener - react on data from the sensor board */
	private BLESensorBoard.SensorListener m_sensorlistener = new BLESensorBoard.SensorListener() {

		/** Currently unused */
		public void onAccelData(float x, float y, float z) {
			
			// Make the accelerometer go as fast as we can if we haven't already
			if(!m_timingisset) {
				m_sensorboard.setTiming(BLESensorBoard.SENSOR_ACCELEROMETER, (byte)200);
				m_timingisset = true;
			}
			
			// Use the greatest component as reference axis
			// 0 == x, 1 == y, 2 == z
			
			float val;
			if (x*x > y*y && x*x > z*z) {
				val = y;
			} else if (y*y > x*x && y*y > z*z) {
				val = x;
			} else { // Default to Z axis
				val = x;
			}
/*			int max =  m_level_display_1.getMax();
			m_level_display_1.setProgress((int) ((val / 9.81)*max) + max/2 );
	*/		

		}

		/** Currently unused */
		public void onTemperatureData(final float ambient_temp, final float object_temp) {
			m_handler.post(new Runnable() {
				public void run() {
			//		m_tw_temp.setText(String.format("%.1f°C", object_temp));
				}
			});
		}

		/** Currently unused */
		public void onGyroData(final float data) {
			m_handler.post(new Runnable() {
				public void run() {
					m_rotation_display.setAngle(data);
				}
			});
		}

		/** Currently unused */
		public void onHumidityData(float temp, final float humidity) {
			m_handler.post(new Runnable() {
				public void run() {
			//		m_tw_moist.setText(String.format("%.1f%%",humidity));
				}
			});
		}

		public void onMagnetData(float x, float y, float z) {
			// Update timing if we haven't already
			if(!m_timingisset) {
				// Timing for magnetometer is (50 + 10*(second parameter))ms
				m_sensorboard.setTiming(BLESensorBoard.SENSOR_MAGNET, (byte)45);
				m_timingisset = true;
			}
			Log.d("BLEDemo", "Got magnetometer data: " + x + " " + y + " " + z);
			
			// So, the magnetometer gives us a vector describing the direction of the magnetic field.
			// To get the angle between that vector and one of our axes, we can use the dot product identity:
			// A dot B = |A||B|cos(alpha).
			// So, we normalize our vector, and dot it with a unit axis, (so |A| == |B| == 1), 
			// and we get cos(alpha).
			float length = (float)Math.sqrt(x*x+y*y+z*z);
			float n_x = x/length;
			float n_y = y/length;
			float n_z = z/length;
			
			// Dotting with the unit X axis == the X component.
			double angle = Math.acos(n_x)*360.0f/(2*3.14159265);
			
			// Okay, so now we have the angle. But we don't know what quadrant it is in yet.
			// We check the sign of the y axis and adjust accordingly.
			
			if(n_y > 0)
				angle = 360.0f-angle;
			
			// And display it
			m_rotation_display.setAngle((float)angle);
			
			
		}
		
		public void onStatusChanged(int sensorid, int oldstatus, int newstatus) {
			if(newstatus == BLESensorBoard.STATUS_CONNECTED) {
				
			}
		}

		public void onPressureData(float temperature, float pressure) {
		
		}
		
		/** Give some user feedback on calibration progress */
		public void onCalibrationData(int sensorid, int step, int total) {
			m_caliprogress.setMax(total);
			m_caliprogress.setProgress(step);
			
			if(step == total) m_caliprogress.dismiss();
		}

		/** Display a toast when writing something */
		public void onWrite(String what) {
			Toast.makeText(BLEDemoActivity.this, what, Toast.LENGTH_SHORT).show();;
		}

	};

	/** Entry point */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		m_rotation_display = (RotationDisplay) findViewById(R.id.rotation_display);
		m_rotation_display.setTextview((TextView) findViewById(R.id.tw_angle));
		
		//m_tw_hw_addr = (TextView) findViewById(R.id.tw_hw_addr);
	    // m_level_display_1 = (LevelDisplay) findViewById(R.id.level_display_1);
		// m_level_display_2 = (LevelDisplay)findViewById(R.id.level_display_2);

		m_btadapter = BluetoothAdapter.getDefaultAdapter();
		//m_tw_moist = (TextView) findViewById(R.id.tw_moisture);
		//m_tw_temp = (TextView) findViewById(R.id.tw_temperature);

		/*
		 * if (m_btadapter == null) { Toast.makeText(this,
		 * "Bluetooth is not available", Toast.LENGTH_LONG).show(); finish();
		 * return; }
		 */
		m_sensorboard = new BLESensorBoardClient(this);
		m_sensorboard.bindService();
		m_sensorboard.addListener(m_sensorlistener);

	}

	@Override
	protected void onStart() {
		super.onStart();
		/*
		 * if (!m_btadapter.isEnabled()) { Intent enableIntent = new
		 * Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		 * startActivityForResult(enableIntent, REQUEST_ENABLE_BT); }
		 */
	}

	/** Display options menu */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.option_menu, menu);
		return true;
	}
	@Override
	protected void onDestroy(){
		// Unbind from service when destroying
		m_sensorboard.unbindService();
		super.onDestroy();
	}
	/** User selected something in the options menu */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menuitem_connect:
			// Connect
			showConnectDialog();
			return true;
		case R.id.exit:
			// Exit
			this.finish();
		case R.id.disconnect:
			// Disconnect
			m_sensorboard.disconnect();
			return true;
		case R.id.menu_calibrate:
			// Calibration
			m_sensorboard.calibrate(BLESensorBoard.SENSOR_MAGNET);
			
			// Display progress
			m_caliprogress = new ProgressDialog(this);
			m_caliprogress.setCancelable(false);
			m_caliprogress.setTitle("Calibrating..");
			m_caliprogress.setMessage("Please rotate sensor tag slowly");
			m_caliprogress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			m_caliprogress.show();
			return true;
			
			
		default:
			return super.onOptionsItemSelected(item);

		}
	}
	

	protected void showConnectDialog() {
		BLEDeviceChooser chooser = new BLEDeviceChooser(this, m_btadapter,
				new BLEDeviceChooser.BLEDeviceChooserListener() {

					public void onBLEDeviceSelected(String addr) {
							// Connect to the selected device
							m_sensorboard.connect(addr);
				/*		m_sensorboard
								.enableSensor(BLESensorBoard.SENSOR_TEMPERATURE);
						m_sensorboard
						.enableSensor(BLESensorBoard.SENSOR_ACCELEROMETER); 
						m_sensorboard
								.enableSensor(BLESensorBoard.SENSOR_HUMIDITY);
				*/
						
							m_sensorboard.enableSensor(BLESensorBoard.SENSOR_MAGNET);
						
						
						
					}

				});
		chooser.showDialog();

	}

}