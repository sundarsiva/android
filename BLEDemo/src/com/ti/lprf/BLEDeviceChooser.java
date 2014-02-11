package com.ti.lprf;

import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

/** A dialog for choosing a paired BLE device */
public class BLEDeviceChooser {
	/** The parent activity */
	private Activity m_activity;
	/** The listener that will be called when a device has been selected */
	private BLEDeviceChooserListener m_listener;
	/** Our dialog builder */
	private AlertDialog.Builder m_dialog_builder;
	/** Our dialog */
	private AlertDialog m_dialog;
	/** The system bluetoothadapter */
	private BluetoothAdapter m_btadapter;
	/** Array Adapter for our paired devices */
	private ArrayAdapter<String> m_paired_devices;
	/** Wether or not we have any devices */
	private boolean m_have_devices;
	/**
	 * Construct BLEDeviceChooser dialog
	 * @param parent The parent activity
	 * @param adapter The system BluetoothAdapter
	 * @param listener The listener that will receive events
	 */
	public BLEDeviceChooser(Activity parent, 
							BluetoothAdapter adapter, 
							BLEDeviceChooserListener listener)
	{
			m_activity = parent;
			m_listener = listener;
			m_btadapter = adapter;
			m_paired_devices = new ArrayAdapter<String>(parent, R.layout.device_name);
			m_have_devices = false;
	}
	
	/**
	 * Show the dialog
	 */
	public void showDialog() 
	{
		// Make OnItemClickListener for the list
		OnItemClickListener clicklistener = new OnItemClickListener() 
		{
            public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) 
            {
            	if(m_have_devices) 
            	{
            		String info = ((TextView) v).getText().toString();
            		String addr = info.substring(info.length() - 17);
            		if(m_listener != null)
            		{
            			// User clicked on element with this address
            			m_listener.onBLEDeviceSelected(addr);
            		}
            	}
            	// Cancel dialog
            	((DialogInterface)m_dialog).cancel();
            	
            }
		};
		
		// Build UI of Dialog
		LayoutInflater inflater = 
				(LayoutInflater) m_activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View layout_listview = inflater.inflate(R.layout.device_chooser, (ViewGroup) m_activity.findViewById(R.id.root_device_list));
		m_dialog_builder = new AlertDialog.Builder(m_activity);
		m_dialog_builder.setView(layout_listview);
		m_dialog_builder.setTitle(R.string.title_device_chooser);
		
		// Hook up ListView with the ArrayAdapter containing the data
		ListView pairedLV = (ListView)layout_listview.findViewById(R.id.paired_devices);
		pairedLV.setAdapter(m_paired_devices);
		// Set click listener
		pairedLV.setOnItemClickListener(clicklistener);
		
		m_paired_devices.clear();
		
		// Get the paired devices
		if(m_btadapter != null) {
			Set<BluetoothDevice> paired_devices = m_btadapter.getBondedDevices();
			BluetoothClass btClass;
			if (paired_devices.size() > 0) {
				// We have devices
				for (BluetoothDevice device : paired_devices) {
					btClass = device.getBluetoothClass();
					// Check if its BLE (hope for a more robust API for this later on)
					if(btClass == null) {
						m_paired_devices.add(device.getName() + "\n"
							+ device.getAddress());
					}
				}
			}
		}
		if(m_paired_devices.isEmpty())
		{
			// We didn't get any devices
			String nodev = m_activity.getResources().getString(R.string.no_device_found);
			m_paired_devices.add(nodev);
			m_have_devices = false;
			
		}
		else {
			m_have_devices = true;
		}
		m_dialog = m_dialog_builder.show();

	}
	
	/** The BLEDeviceChooserListener interface */
	public interface BLEDeviceChooserListener {
		/**
		 * Called when the user selected a Bluetooth Low Energy device
		 * @param addr The MAC address of the selected BLE device
		 */
		public void onBLEDeviceSelected(String addr);
	}
}
