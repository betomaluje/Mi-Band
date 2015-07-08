package com.betomaluje.android.miband.core.bluetooth;

import com.betomaluje.android.miband.core.ActionCallback;

import java.util.UUID;

/**
 * Created by Lewis on 10/01/15.
 */
public class WriteAction implements BLEAction {
    private final UUID characteristic;

    private final byte[] payload;

    private ActionCallback callback;

    public WriteAction(final UUID characteristic, final byte[] payload) {
        this.characteristic = characteristic;
        this.payload = payload;
    }

    public WriteAction(final UUID characteristic, final byte[] payload, ActionCallback callback) {
        this.characteristic = characteristic;
        this.payload = payload;
        this.callback = callback;
    }

    public UUID getCharacteristic() {
        return characteristic;
    }

    public byte[] getPayload() {
        return payload;
    }

    public ActionCallback getCallback() {
        return callback;
    }

    @Override
    public void run() {
        //Do nothing.
    }
}
