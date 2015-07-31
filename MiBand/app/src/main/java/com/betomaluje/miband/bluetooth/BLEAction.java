package com.betomaluje.miband.bluetooth;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

/**
 * Created by Lewis on 10/01/15.
 */
public abstract interface BLEAction {

    /**
     * Returns true if this actions expects an (async) result which must
     * be waited for, before continuing with other actions.
     * <p/>
     * This is needed because the current Bluedroid stack can only deal
     * with one single bluetooth operation at a time.
     */
    public abstract boolean expectsResult();

    /**
     * Executes this action, e.g. reads or write a GATT characteristic.
     *
     * @return true if the action was successful, false otherwise
     */
    public abstract boolean run(BTCommandManager btCommandManager);
}
