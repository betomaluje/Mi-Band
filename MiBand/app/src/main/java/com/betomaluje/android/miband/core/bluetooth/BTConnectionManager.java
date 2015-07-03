package com.betomaluje.android.miband.core.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.betomaluje.android.miband.MiBandApplication;
import com.betomaluje.android.miband.core.ActionCallback;
import com.betomaluje.android.miband.core.model.UserInfo;

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
    private static final long SCAN_PERIOD = 45000;

    private static BTConnectionManager instance;

    public synchronized static BTConnectionManager getInstance(Context context, ActionCallback actionCallback, BluetoothGattCallback classCallback) {
        if (instance == null) {
            instance = new BTConnectionManager(context, actionCallback, classCallback);
        }

        return instance;
    }

    public BTConnectionManager(Context context, ActionCallback actionCallback, BluetoothGattCallback classCallback) {
        this.context = context;

        Log.i(TAG, "new BTConnectionManager");

        adapter = BluetoothAdapter.getDefaultAdapter();

        this.actionCallback = actionCallback;
        this.classCallback = classCallback;
    }

    public void connect() {
        Log.i(TAG, "trying to connect");
        mFound = false;

        if (adapter == null || !adapter.isEnabled()) {
            actionCallback.onFail(NotificationConstants.BLUETOOTH_OFF, "Bluetooth disabled or not supported");
        } else {
            if (!adapter.isDiscovering()) {

                Log.i(TAG, "connecting...");

                if (!tryPairedDevices()) {

                    Log.i(TAG, "not already paired");
                    mScanning = true;

                    if (MiBandApplication.supportsBluetoothLE()) {
                        //Log.i(TAG, "is BTLE");
                        adapter.stopLeScan(mLeScanCallback);
                        startBTLEDiscovery();
                    } else {
                        //Log.i(TAG, "is BT");
                        adapter.cancelDiscovery();
                        startBTDiscovery();
                    }
                }
            }
        }
    }

    private boolean tryPairedDevices() {
        String mDeviceAddress = "";

        SharedPreferences sharedPreferences = context.getSharedPreferences(UserInfo.KEY_PREFERENCES, Context.MODE_PRIVATE);
        String btAddress = sharedPreferences.getString(UserInfo.KEY_BT_ADDRESS, "");

        if (btAddress != null) {
            if (!btAddress.equals("")) {
                //we use our previously paired device
                mFound = true;
                mDeviceAddress = btAddress;
            } else {
                //we search for paired devices
                final Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();

                for (BluetoothDevice pairedDevice : pairedDevices) {
                    if (pairedDevice.getName().equals("MI") && pairedDevice.getAddress().startsWith("88:0F:10")) {
                        mDeviceAddress = pairedDevice.getAddress();
                        mFound = true;
                        break;
                    }
                }
            }

            if (mFound && !mDeviceAddress.equals("")) {
                mDeviceAddress = btAddress;
            } else {
                mFound = false;
            }
        } else {
            //we search only for paired devices
            final Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();

            for (BluetoothDevice pairedDevice : pairedDevices) {
                if (pairedDevice.getName().equals("MI") && pairedDevice.getAddress().startsWith("88:0F:10")) {
                    mDeviceAddress = pairedDevice.getAddress();
                    mFound = true;
                    break;
                }
            }
        }

        if (mFound) {
            Log.i(TAG, "already paired!");
            BluetoothDevice mBluetoothMi = adapter.getRemoteDevice(mDeviceAddress);
            mBluetoothMi.connectGatt(context, false, classCallback);
            //mGatt.connect();
        }

        return mFound;
    }

    public boolean isAlreadyPaired() {
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
                    "onLeScan: name: " + device.getName() + ", uuid: "
                            + device.getUuids() + ", add: "
                            + device.getAddress() + ", type: "
                            + device.getType() + ", bondState: "
                            + device.getBondState() + ", rssi: " + rssi);

            if (device.getName() != null && device.getAddress() != null && device.getName().equals("MI") && device.getAddress().startsWith("88:0F:10")) {
                mFound = true;

                stopDiscovery();

                device.connectGatt(context, false, classCallback);
            }
        }
    };
}
