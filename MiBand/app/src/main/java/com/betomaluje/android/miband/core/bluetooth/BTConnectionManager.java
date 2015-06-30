package com.betomaluje.android.miband.core.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.betomaluje.android.miband.MiBandApplication;
import com.betomaluje.android.miband.core.ActionCallback;

import java.util.Set;

/**
 * Created by betomaluje on 6/26/15.
 */
public class BTConnectionManager {

    private final String TAG = getClass().getSimpleName();

    private Context context;
    private boolean mScanning = false;
    private boolean mFound = false;
    private Handler mHandler = new Handler();
    private BluetoothAdapter adapter;
    private ActionCallback actionCallback;
    private BluetoothGattCallback classCallback;

    //the scanning timeout period
    private static final long SCAN_PERIOD = 30000;

    public BTConnectionManager(Context context, ActionCallback actionCallback, BluetoothGattCallback classCallback) {
        this.context = context;

        adapter = ((BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();

        this.actionCallback = actionCallback;
        this.classCallback = classCallback;

        if (adapter == null || !adapter.isEnabled()) {
            actionCallback.onFail(NotificationConstants.BLUETOOTH_OFF, "Bluetooth disabled or not supported");
        } else {
            this.context = context;

            if (!adapter.isDiscovering()) {

                mScanning = true;
                mFound = false;

                if (!tryPairedDevices()) {
                    if (MiBandApplication.supportsBluetoothLE()) {
                        //Log.i(TAG, "is BTLE");
                        startBTLEDiscovery();
                    } else {
                        //Log.i(TAG, "is BT");
                        startBTDiscovery();
                    }
                }
            }
        }
    }

    private boolean tryPairedDevices() {
        final Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();

        String mDeviceAddress = "";

        for (BluetoothDevice pairedDevice : pairedDevices) {
            if (pairedDevice.getName().equals("MI") && pairedDevice.getAddress().startsWith("88:0F:10")) {
                mDeviceAddress = pairedDevice.getAddress();

                mFound = true;
            }
        }

        if (mFound) {
            BluetoothDevice mBluetoothMi = adapter.getRemoteDevice(mDeviceAddress);

            BluetoothGatt mGatt = mBluetoothMi.connectGatt(context, true, classCallback);
            mGatt.connect();
        }

        return mFound;
    }

    private Runnable stopRunnable = new Runnable() {
        @Override
        public void run() {
            stopDiscovery();
        }
    };

    private void stopDiscovery() {
        Log.i(TAG, "Stopping discovery");
        if (mScanning) {
            if (MiBandApplication.supportsBluetoothLE()) {
                stopBTLEDiscovery();
            } else {
                stopBTDiscovery();
            }

            mHandler.removeMessages(0, stopRunnable);
            mScanning = false;

            if (!mFound)
                actionCallback.onFail(-1, "No bluetooth devices");
        }
    }

    private void startBTDiscovery() {
        Log.i(TAG, "Starting BT Discovery");
        mHandler.removeMessages(0, stopRunnable);
        mHandler.sendMessageDelayed(getPostMessage(stopRunnable), SCAN_PERIOD);
        stopBTDiscovery();
        if (adapter.startDiscovery())
            Log.v(TAG, "starting scan");
    }

    private void startBTLEDiscovery() {
        Log.i(TAG, "Starting BTLE Discovery");
        mHandler.removeMessages(0, stopRunnable);
        mHandler.sendMessageDelayed(getPostMessage(stopRunnable), SCAN_PERIOD);
        stopBTLEDiscovery();
        if (adapter.startLeScan(mLeScanCallback))
            Log.v(TAG, "starting scan");
    }

    private void stopBTLEDiscovery() {
        if (adapter.isDiscovering())
            adapter.stopLeScan(mLeScanCallback);
    }

    private void stopBTDiscovery() {
        if (adapter.isDiscovering())
            adapter.cancelDiscovery();
    }

    private Message getPostMessage(Runnable runnable) {
        Message m = Message.obtain(mHandler, runnable);
        m.obj = runnable;
        return m;
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {

            Log.d(TAG,
                    "onLeScan: name:" + device.getName() + ",uuid:"
                            + device.getUuids() + ",add:"
                            + device.getAddress() + ",type:"
                            + device.getType() + ",bondState:"
                            + device.getBondState() + ",rssi:" + rssi);

            if (device.getName() == null || device.getAddress() == null) {
                Log.e(TAG, "name or address null");
            } else {
                if (device.getName().equals("MI") && device.getAddress().startsWith("88:0F:10")) {

                    mFound = true;

                    stopDiscovery();

                    device.connectGatt(context, false, classCallback);
                }
            }
        }
    };
}
