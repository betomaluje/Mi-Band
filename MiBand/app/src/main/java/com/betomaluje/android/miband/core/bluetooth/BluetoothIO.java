package com.betomaluje.android.miband.core.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import com.betomaluje.android.miband.core.ActionCallback;
import com.betomaluje.android.miband.core.NotifyListener;
import com.betomaluje.android.miband.core.model.Profile;

import java.util.HashMap;
import java.util.UUID;

public class BluetoothIO extends BluetoothGattCallback {

    private static final String TAG = BluetoothIO.class.getSimpleName();
    private BluetoothGatt gatt;
    private ActionCallback currentCallback;
    private ActionCallback connectionCallback;

    private HashMap<UUID, NotifyListener> notifyListeners = new HashMap<UUID, NotifyListener>();

    private boolean isConnected = false;

    public void connect(final Context context, final ActionCallback callback) {
        if (!isConnected()) {
            this.connectionCallback = callback;
            new BTConnectionManager(context, callback, BluetoothIO.this);
        }
    }

    public void disconnect() {
        if (gatt != null) {
            //gatt.disconnect();
            gatt.close();
            gatt = null;
        }

        isConnected = false;

        connectionCallback.onFail(-1, "disconnected");
    }

    public BluetoothDevice getDevice() {
        return gatt.getDevice();
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void writeAndRead(final UUID uuid, byte[] valueToWrite, final ActionCallback callback) {
        ActionCallback readCallback = new ActionCallback() {

            @Override
            public void onSuccess(Object characteristic) {
                BluetoothIO.this.readCharacteristic(uuid, callback);
            }

            @Override
            public void onFail(int errorCode, String msg) {
                callback.onFail(errorCode, msg);
            }
        };
        this.writeCharacteristic(uuid, valueToWrite, readCallback);
    }

    /**
     * Sends a command to the Mi Band
     *
     * @param uuid     the {@link Profile} used
     * @param value    the values to send
     * @param callback
     */
    public void writeCharacteristic(UUID uuid, byte[] value, ActionCallback callback) {
        try {
            this.currentCallback = callback;
            BluetoothGattCharacteristic chara = gatt.getService(Profile.UUID_SERVICE_MILI).getCharacteristic(uuid);
            if (null == chara) {
                this.onFail(-1, "BluetoothGattCharacteristic " + uuid + " doesn't exist");
                return;
            }
            chara.setValue(value);
            if (!this.gatt.writeCharacteristic(chara)) {
                this.onFail(-1, "gatt.writeCharacteristic() return false");
            }
        } catch (Throwable tr) {
            Log.e(TAG, "writeCharacteristic", tr);
            this.onFail(-1, tr.getMessage());
        }
    }

    /**
     * Reads a command from the Mi Band
     *
     * @param uuid     the {@link Profile} used
     * @param callback
     */
    public void readCharacteristic(UUID uuid, ActionCallback callback) {
        try {
            this.currentCallback = callback;
            BluetoothGattCharacteristic chara = gatt.getService(Profile.UUID_SERVICE_MILI).getCharacteristic(uuid);
            if (null == chara) {
                this.onFail(-1, "BluetoothGattCharacteristic " + uuid + " doesn't exist");
                return;
            }
            if (!this.gatt.readCharacteristic(chara)) {
                this.onFail(-1, "gatt.readCharacteristic() return false");
            }
        } catch (Throwable tr) {
            Log.e(TAG, "readCharacteristic", tr);
            this.onFail(-1, tr.getMessage());
        }
    }

    /**
     * Reads the bluetooth's received signal strength indication
     *
     * @param callback
     */
    public void readRssi(ActionCallback callback) {
        try {
            this.currentCallback = callback;
            this.gatt.readRemoteRssi();
        } catch (Throwable tr) {
            Log.e(TAG, "readRssi", tr);
            this.onFail(-1, tr.getMessage());
        }
    }

    public void setNotifyListener(UUID characteristicId, NotifyListener listener) {
        if (this.notifyListeners.containsKey(characteristicId))
            return;

        BluetoothGattCharacteristic chara = gatt.getService(Profile.UUID_SERVICE_MILI).getCharacteristic(characteristicId);
        if (chara == null)
            return;

        this.gatt.setCharacteristicNotification(chara, true);
        BluetoothGattDescriptor descriptor = chara.getDescriptor(Profile.UUID_DESCRIPTOR_UPDATE_NOTIFICATION);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        this.gatt.writeDescriptor(descriptor);
        this.notifyListeners.put(characteristicId, listener);
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        super.onConnectionStateChange(gatt, status, newState);

        //Log.e(TAG, "onConnectionStateChange (2): " + newState);

        if (newState == BluetoothProfile.STATE_CONNECTED) {
            this.gatt = gatt;
            gatt.discoverServices();
        } else {
            disconnect();
        }
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicRead(gatt, characteristic, status);
        if (BluetoothGatt.GATT_SUCCESS == status) {
            this.onSuccess(characteristic);
        } else {
            this.onFail(status, "onCharacteristicRead fail");
        }
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicWrite(gatt, characteristic, status);
        if (BluetoothGatt.GATT_SUCCESS == status) {
            this.onSuccess(characteristic);
        } else {
            this.onFail(status, "onCharacteristicWrite fail");
        }
    }

    @Override
    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
        super.onReadRemoteRssi(gatt, rssi, status);
        if (BluetoothGatt.GATT_SUCCESS == status) {
            this.onSuccess(rssi);
        } else {
            this.onFail(status, "onCharacteristicRead fail");
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        super.onServicesDiscovered(gatt, status);

        //Log.e(TAG, "onServicesDiscovered (0): " + status);

        if (status == BluetoothGatt.GATT_SUCCESS) {

            isConnected = true;
            connectionCallback.onSuccess(null);
        } else {
            disconnect();
        }
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicChanged(gatt, characteristic);
        if (this.notifyListeners.containsKey(characteristic.getUuid())) {
            this.notifyListeners.get(characteristic.getUuid()).onNotify(characteristic.getValue());
        }
    }

    private void onSuccess(Object data) {
        if (this.currentCallback != null) {
            ActionCallback callback = this.currentCallback;
            this.currentCallback = null;
            callback.onSuccess(data);
        }
    }

    private void onFail(int errorCode, String msg) {
        if (this.currentCallback != null) {
            ActionCallback callback = this.currentCallback;
            this.currentCallback = null;
            callback.onFail(errorCode, msg);
        }
    }
}
