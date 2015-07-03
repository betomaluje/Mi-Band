package com.betomaluje.android.miband.core.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
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

    //the scanning timeout period
    private static final long SCAN_PERIOD = 45000;
    private static BTConnectionManager instance;
    private final String TAG = getClass().getSimpleName();
    private Context context;
    private boolean mScanning = false;
    private boolean mFound = false;
    private boolean isConnected = false;

    private Handler mHandler = new Handler();
    private BluetoothAdapter adapter;
    private ActionCallback connectionCallback;

    private BTCommandManager io;
    private BluetoothGatt gatt;

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

                device.connectGatt(context, false, btleGattCallback);
            }
        }
    };

    private Runnable stopRunnable = new Runnable() {
        @Override
        public void run() {
            stopDiscovery();
        }
    };

    public BTConnectionManager(Context context, ActionCallback connectionCallback) {
        this.context = context;

        Log.i(TAG, "new BTConnectionManager");

        this.connectionCallback = connectionCallback;
    }

    public synchronized static BTConnectionManager getInstance(Context context, ActionCallback connectionCallback) {
        if (instance == null) {
            instance = new BTConnectionManager(context, connectionCallback);
        }

        return instance;
    }

    public void connect() {
        Log.i(TAG, "trying to connect");
        mFound = false;

        adapter = BluetoothAdapter.getDefaultAdapter();

        if (adapter == null || !adapter.isEnabled()) {
            connectionCallback.onFail(NotificationConstants.BLUETOOTH_OFF, "Bluetooth disabled or not supported");
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

    public void disconnect() {
        if (gatt != null) {
            gatt.close();
            gatt = null;
        }

        isConnected = false;

        connectionCallback.onFail(-1, "disconnected");
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
            mBluetoothMi.connectGatt(context, false, btleGattCallback);
            //mGatt.connect();
        }

        return mFound;
    }

    public boolean isAlreadyPaired() {
        return mFound;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public BluetoothDevice getDevice() {
        return gatt.getDevice();
    }

    public BluetoothGatt getGatt() {
        return gatt;
    }

    public void setIo(BTCommandManager io) {
        this.io = io;
    }

    private final BluetoothGattCallback btleGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            //Log.e(TAG, "onConnectionStateChange (2): " + newState);

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.discoverServices();
            } else {
                Log.e(TAG, "onConnectionStateChange disconnect: " + newState);
                //disconnect();
                isConnected = false;
                //connect(context, this.connectionCallback);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);

            Log.e(TAG, "onServicesDiscovered (0): " + status + " paired: " + isAlreadyPaired());

            if (status == BluetoothGatt.GATT_SUCCESS) {

                //we set the Gatt instance
                BTConnectionManager.this.gatt = gatt;

                //we update current band bluetooth MAC address
                SharedPreferences sharedPrefs = context.getSharedPreferences(UserInfo.KEY_PREFERENCES, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putString(UserInfo.KEY_BT_ADDRESS, gatt.getDevice().getAddress());
                editor.commit();

                isConnected = true;
                connectionCallback.onSuccess(isAlreadyPaired());
            } else {
                disconnect();
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            if (BluetoothGatt.GATT_SUCCESS == status) {
                if (io != null)
                    io.onSuccess(characteristic);
            } else {
                io.onFail(status, "onCharacteristicRead fail");
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            if (BluetoothGatt.GATT_SUCCESS == status) {
                io.onSuccess(characteristic);
            } else {
                io.onFail(status, "onCharacteristicWrite fail");
            }
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            if (BluetoothGatt.GATT_SUCCESS == status) {
                io.onSuccess(rssi);
            } else {
                io.onFail(status, "onCharacteristicRead fail");
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            if (io.notifyListeners.containsKey(characteristic.getUuid())) {
                io.notifyListeners.get(characteristic.getUuid()).onNotify(characteristic.getValue());
            }
        }
    };

    /*
     *
     *
     * DISCOVERY REGION
     *
     *
     */

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
                connectionCallback.onFail(-1, "No bluetooth devices");
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
}
