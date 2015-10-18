package com.betomaluje.miband.bluetooth;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.betomaluje.miband.ActionCallback;
import com.betomaluje.miband.NotifyListener;
import com.betomaluje.miband.model.Profile;
import com.betomaluje.miband.model.Protocol;
import com.betomaluje.miband.models.ActivityData;
import com.betomaluje.miband.sqlite.ActivitySQLite;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class BTCommandManager {

    private static final String TAG = BTCommandManager.class.getSimpleName();

    private ActionCallback currentCallback;
    private QueueConsumer mQueueConsumer;

    public HashMap<UUID, NotifyListener> notifyListeners = new HashMap<UUID, NotifyListener>();

    private Context context;
    private BluetoothGatt gatt;

    public BTCommandManager(Context context, BluetoothGatt gatt) {
        this.context = context;
        this.gatt = gatt;

        mQueueConsumer = new QueueConsumer(context, this);

        Thread t = new Thread(mQueueConsumer);
        t.start();
    }

    public void queueTask(final BLETask task) {
        mQueueConsumer.add(task);
    }

    public QueueConsumer getmQueueConsumer() {
        return mQueueConsumer;
    }

    public void writeAndRead(final UUID uuid, byte[] valueToWrite, final ActionCallback callback) {
        ActionCallback readCallback = new ActionCallback() {

            @Override
            public void onSuccess(Object characteristic) {
                BTCommandManager.this.readCharacteristic(uuid, callback);
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
            } else {
                onSuccess(chara);
            }
        } catch (Throwable tr) {
            Log.e(TAG, "writeCharacteristic", tr);
            this.onFail(-1, tr.getMessage());
        }
    }

    public boolean writeCharacteristicWithResponse(UUID uuid, byte[] value, ActionCallback callback) {
        try {
            this.currentCallback = callback;
            BluetoothGattCharacteristic chara = gatt.getService(Profile.UUID_SERVICE_MILI).getCharacteristic(uuid);
            if (null == chara) {
                return false;
            }
            chara.setValue(value);
            return this.gatt.writeCharacteristic(chara);
        } catch (Throwable tr) {
            return false;
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

    public boolean readCharacteristicWithResponse(UUID uuid, ActionCallback callback) {
        try {
            this.currentCallback = callback;
            BluetoothGattCharacteristic chara = gatt.getService(Profile.UUID_SERVICE_MILI).getCharacteristic(uuid);
            if (null == chara) {
                return false;
            }
            return this.gatt.readCharacteristic(chara);
        } catch (Throwable tr) {
            Log.e(TAG, "readCharacteristic", tr);
            return false;
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

    public void setHighLatency() {
        writeCharacteristic(Profile.UUID_CHAR_LE_PARAMS, Protocol.HIGH_LATENCY_LEPARAMS, null);
    }

    public void onSuccess(Object data) {
        if (this.currentCallback != null) {
            ActionCallback callback = this.currentCallback;
            this.currentCallback = null;
            callback.onSuccess(data);
        }
    }

    public void onFail(int errorCode, String msg) {
        if (this.currentCallback != null) {
            ActionCallback callback = this.currentCallback;
            this.currentCallback = null;
            callback.onFail(errorCode, msg);
        }
    }

    public void handleControlPointResult(byte[] value) {
        if (value != null) {
            for (byte b : value) {
                Log.i(TAG, "handleControlPoint GOT DATA:" + String.format("0x%8x", b));
            }
        } else {
            Log.e(TAG, "handleControlPoint GOT null");
        }
    }

    //ACTIVITY DATA
    //temporary buffer, size is a multiple of 60 because we want to store complete minutes (1 minute = 3 bytes)
    private static final int activityDataHolderSize = 3 * 60 * 4; // 8h

    private static class ActivityStruct {
        public byte[] activityDataHolder = new byte[activityDataHolderSize];
        //index of the buffer above
        public int activityDataHolderProgress = 0;
        //number of bytes we will get in a single data transfer, used as counter
        public int activityDataRemainingBytes = 0;
        //same as above, but remains untouched for the ack message
        public int activityDataUntilNextHeader = 0;
        //timestamp of the single data transfer, incremented to store each minute's data
        public GregorianCalendar activityDataTimestampProgress = null;
        //same as above, but remains untouched for the ack message
        public GregorianCalendar activityDataTimestampToAck = null;
    }

    private ActivityStruct activityStruct;

    public void handleActivityNotif(byte[] value) {
        boolean firstChunk = activityStruct == null;
        if (firstChunk) {
            activityStruct = new ActivityStruct();
        }

        if (value.length == 11) {
            // byte 0 is the data type: 1 means that each minute is represented by a triplet of bytes
            int dataType = value[0];
            // byte 1 to 6 represent a timestamp
            GregorianCalendar timestamp = parseTimestamp(value, 1);

            // counter of all data held by the band
            int totalDataToRead = (value[7] & 0xff) | ((value[8] & 0xff) << 8);
            totalDataToRead *= (dataType == 1) ? 3 : 1;


            // counter of this data block
            int dataUntilNextHeader = (value[9] & 0xff) | ((value[10] & 0xff) << 8);
            dataUntilNextHeader *= (dataType == 1) ? 3 : 1;

            // there is a total of totalDataToRead that will come in chunks (3 bytes per minute if dataType == 1),
            // these chunks are usually 20 bytes long and grouped in blocks
            // after dataUntilNextHeader bytes we will get a new packet of 11 bytes that should be parsed
            // as we just did

            if (firstChunk && dataUntilNextHeader != 0) {
                String message = String.format("About to transfer %1$s of data starting from %2$s",
                        (totalDataToRead / 3),
                        DateFormat.getDateTimeInstance().format(timestamp.getTime()));
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }

            Log.i(TAG, "total data to read: " + totalDataToRead + " len: " + (totalDataToRead / 3) + " minute(s)");
            Log.i(TAG, "data to read until next header: " + dataUntilNextHeader + " len: " + (dataUntilNextHeader / 3) + " minute(s)");
            Log.i(TAG, "TIMESTAMP: " + DateFormat.getDateTimeInstance().format(timestamp.getTime()) + " magic byte: " + dataUntilNextHeader);

            activityStruct.activityDataRemainingBytes = activityStruct.activityDataUntilNextHeader = dataUntilNextHeader;
            activityStruct.activityDataTimestampToAck = (GregorianCalendar) timestamp.clone();
            activityStruct.activityDataTimestampProgress = timestamp;

        } else {
            bufferActivityData(value);
        }

        Log.e(TAG, "activity data: length: " + value.length + ", remaining bytes: " + activityStruct.activityDataRemainingBytes);

        if (activityStruct.activityDataRemainingBytes == 0) {
            sendAckDataTransfer(activityStruct.activityDataTimestampToAck, activityStruct.activityDataUntilNextHeader);
        }
    }

    private GregorianCalendar parseTimestamp(byte[] value, int offset) {
        GregorianCalendar timestamp = new GregorianCalendar(
                value[offset] + 2000,
                value[offset + 1],
                value[offset + 2],
                value[offset + 3],
                value[offset + 4],
                value[offset + 5]);
        return timestamp;
    }

    private void bufferActivityData(byte[] value) {
        if (activityStruct.activityDataRemainingBytes >= value.length) {
            //I don't like this clause, but until we figure out why we get different data sometimes this should work
            if (value.length == 20 || value.length == activityStruct.activityDataRemainingBytes) {
                System.arraycopy(value, 0, activityStruct.activityDataHolder, activityStruct.activityDataHolderProgress, value.length);
                activityStruct.activityDataHolderProgress += value.length;
                activityStruct.activityDataRemainingBytes -= value.length;

                if (this.activityDataHolderSize == activityStruct.activityDataHolderProgress) {
                    flushActivityDataHolder();
                }
            } else {
                // the length of the chunk is not what we expect. We need to make sense of this data
                Log.w(TAG, "GOT UNEXPECTED ACTIVITY DATA WITH LENGTH: " + value.length + ", EXPECTED LENGTH: " + activityStruct.activityDataRemainingBytes);
                for (byte b : value) {
                    Log.w(TAG, "DATA: " + String.format("0x%8x", b));
                }
            }
        } else {
            Log.e(TAG, "error buffering activity data: remaining bytes: " + activityStruct.activityDataRemainingBytes + ", received: " + value.length);
        }
    }

    private void flushActivityDataHolder() {
        if (activityStruct == null) {
            Log.d(TAG, "nothing to flush, struct is already null");
            return;
        }

        byte category, intensity, steps;

        ActivitySQLite dbHandler = ActivitySQLite.getInstance(context);

        for (int i = 0; i < activityStruct.activityDataHolderProgress; i += 3) { //TODO: check if multiple of 3, if not something is wrong
            category = activityStruct.activityDataHolder[i];
            intensity = activityStruct.activityDataHolder[i + 1];
            steps = activityStruct.activityDataHolder[i + 2];

            dbHandler.saveActivity((int) (activityStruct.activityDataTimestampProgress.getTimeInMillis() / 1000),
                    ActivityData.PROVIDER_MIBAND,
                    intensity,
                    steps,
                    category);

            activityStruct.activityDataTimestampProgress.add(Calendar.MINUTE, 1);
        }

        activityStruct.activityDataHolderProgress = 0;
    }

    private void sendAckDataTransfer(Calendar time, int bytesTransferred) {
        byte[] ack = new byte[]{
                Protocol.COMMAND_CONFIRM_ACTIVITY_DATA_TRANSFER_COMPLETE,
                (byte) (time.get(Calendar.YEAR) - 2000),
                (byte) time.get(Calendar.MONTH),
                (byte) time.get(Calendar.DATE),
                (byte) time.get(Calendar.HOUR_OF_DAY),
                (byte) time.get(Calendar.MINUTE),
                (byte) time.get(Calendar.SECOND),
                (byte) (bytesTransferred & 0xff),
                (byte) (0xff & (bytesTransferred >> 8))
        };

        final List<BLEAction> list = new ArrayList<>();

        list.add(new WriteAction(Profile.UUID_CHAR_CONTROL_POINT, ack));

        BLETask task = new BLETask(list);

        try {
            queueTask(task);
        } catch (NullPointerException e) {

        } finally {
            // flush to the DB after sending the ACK
            flushActivityDataHolder();

            //The last data chunk sent by the miband has always length 0.
            //When we ack this chunk, the transfer is done.
            if (bytesTransferred == 0) {
                activityStruct = null;
                onSuccess("sync complete");
            }
        }
    }
}
