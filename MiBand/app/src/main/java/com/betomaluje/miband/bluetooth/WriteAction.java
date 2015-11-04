package com.betomaluje.miband.bluetooth;

import com.betomaluje.miband.ActionCallback;

import java.util.UUID;

/**
 * Created by Lewis on 10/01/15.
 */
public class WriteAction implements BLEAction {
    private UUID service;

    private final UUID characteristic;

    private final byte[] payload;

    private ActionCallback callback;

    public WriteAction(final UUID service, final UUID characteristic, final byte[] payload) {
        this.service = service;
        this.characteristic = characteristic;
        this.payload = payload;
    }

    public WriteAction(final UUID service, final UUID characteristic, final byte[] payload, ActionCallback callback) {
        this.service = service;
        this.characteristic = characteristic;
        this.payload = payload;
        this.callback = callback;
    }

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

    public UUID getService() {
        return service;
    }

    @Override
    public boolean expectsResult() {
        return true;
    }

    @Override
    public boolean run(BTCommandManager btCommandManager) {
        if (service == null)
            return btCommandManager.writeCharacteristicWithResponse(getCharacteristic(), getPayload(), getCallback());
        else
            return btCommandManager.writeCharacteristicWithResponse(getService(), getCharacteristic(), getPayload(), getCallback());
    }
}
