package com.ti.lprf;

import com.motorola.bluetoothle.BluetoothGatt;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

/** This class represents a Write request for the write queue in BLESensor.
 */
public class BLEWriteRequest {
	/** The BluetoothDevice to write to */
	public BluetoothDevice bledevice;
	/** The handle to write to */
	public String handle;
	/** The UUID of the Primary Service to write to */
	public String uuid;
	/** The data to write */
	public byte[] data;
	/** The length of the data to write */
	public int length;
	/** The BluetoothGatt instance to use for writing */
	public BluetoothGatt btgatt;
	/** True if this is a handle configuration write request */
	public boolean isConf;
	/** True if this is a disconnect request */
	public boolean isDisconnect = false;
	/** Textual description of what this request does (to be shown to the user) */
	public String what;
	/** The parent BLESensorBoard */
	public BLESensorBoard sb;
	/**
	 * Constructor
	 * @param btg The BluetoothGatt
	 * @param bld BluetoothDevice
	 * @param u UUID of Primary Service
	 * @param h Handle to write to
	 * @param d Data to write
	 * @param l Length of data to write
	 * @param b true if this is a configuration request, false otherwise 
	 * @param w Textual description of this request (user-readable)
	 * @param _sb Parent sensor board
	 */
	public BLEWriteRequest(BluetoothGatt btg, BluetoothDevice bld, String u, String h, byte[] d, int l, boolean b, String w, BLESensorBoard _sb){
		bledevice = bld;
		btgatt = btg;
		uuid = u;
		handle = h;
		data = d;
		length = l;
		isConf = b;
		what = w;
		sb = _sb;
	}
	/** Empty constructor */
	public BLEWriteRequest() {
	
	}
	/** Execute the write request */
	public void execute() {
		Log.d("BLEWriteRequest", "Executing write: " + length + " bytes to " + handle + " configuration: " + isConf);
		if(what != null) sb.onWrite(what);
			
		if(isDisconnect){
			btgatt.disconnectGatt(bledevice, uuid);
		}
		else if(isConf){
			btgatt.writeGattConfigurationDesc(bledevice, uuid, handle, data, length);
		}
		else {
			btgatt.writeGattCharacteristicValue(bledevice, uuid, handle, data, length);
			
		}
		
	}
}
