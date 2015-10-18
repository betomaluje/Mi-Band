package com.betomaluje.miband.bluetooth;

import com.betomaluje.miband.ActionCallback;

import java.util.UUID;

/**
 * Created by betomaluje on 8/3/15.
 */
public class ReadAction implements BLEAction {

    private final UUID characteristic;

    private ActionCallback callback;

    public ReadAction(final UUID characteristic) {
        this.characteristic = characteristic;
    }

    public ReadAction(final UUID characteristic, ActionCallback callback) {
        this.characteristic = characteristic;
        this.callback = callback;
    }

    public UUID getCharacteristic() {
        return characteristic;
    }

    public ActionCallback getCallback() {
        return callback;
    }

    @Override
    public boolean expectsResult() {
        return true;
    }

    @Override
    public boolean run(BTCommandManager btCommandManager) {
        return btCommandManager.readCharacteristicWithResponse(getCharacteristic(), getCallback());
    }
}
