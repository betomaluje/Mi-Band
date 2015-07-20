package com.betomaluje.miband.bluetooth;

import android.util.Log;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Lewis on 10/01/15.
 */
public class QueueConsumer implements Runnable {
    private String TAG = this.getClass().getSimpleName();

    private BTCommandManager bleCommandManager;
    private final LinkedBlockingQueue<BLETask> queue;

    public QueueConsumer(final BTCommandManager bleCommandManager) {
        this.bleCommandManager = bleCommandManager;
        this.queue = new LinkedBlockingQueue<>();
    }

    public void add(final BLETask task) {
        queue.add(task);
    }

    @Override
    public void run() {
        while (true) {
            try {
                final BLETask task = queue.take();

                final List<BLEAction> actions = task.getActions();

                for (BLEAction action : actions) {
                    if (action instanceof WaitAction) {
                        action.run();
                    } else if (action instanceof WriteAction) {

                        WriteAction writeAction = (WriteAction) action;
                        bleCommandManager.writeCharacteristic(writeAction.getCharacteristic(), writeAction.getPayload(), writeAction.getCallback());
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, e.toString());
            } finally {
                if (queue.isEmpty()) {
                    bleCommandManager.setHighLatency();
                }
            }
        }
    }
}
